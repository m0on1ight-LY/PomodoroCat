package com.example.pomodorocat.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pomodorocat.MainActivity
import com.example.pomodorocat.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerService : Service() {

    private val binder = TimerBinder()
    private val tag = "TimerService"

    // 协程作用域，用于控制后台倒计时
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    // 状态流供 UI 订阅
    private val _timerState = MutableStateFlow(TimerData())
    val timerState: StateFlow<TimerData> = _timerState.asStateFlow()

    private val _mixerSettings = MutableStateFlow(MixerSettings())
    val mixerSettings: StateFlow<MixerSettings> = _mixerSettings.asStateFlow()

    // 白噪音播放器映射表
    private val mediaPlayers = mutableMapOf<String, MediaPlayer>()
    
    // 振动器和 WakeLock
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var prefManager: PreferenceManager

    companion object {
        const val CHANNEL_ID = "pomodoro_cat_channel"
        const val NOTIFICATION_ID = 1001

        // Intent Actions
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_SKIP = "ACTION_SKIP"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(tag, "Service Bound")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "Service Created")
        prefManager = PreferenceManager(this)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // 初始化 WakeLock 防止系统深度休眠挂起计时器
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PomodoroCat::TimerWakeLock")

        createNotificationChannel()
        // 初始装载白噪音播放器
        initMediaPlayers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            Log.d(tag, "Received Action: $action")
            when (action) {
                ACTION_START -> startTimer()
                ACTION_PAUSE -> pauseTimer()
                ACTION_RESUME -> resumeTimer()
                ACTION_SKIP -> skipCurrentPhase()
                ACTION_STOP_SERVICE -> stopForegroundAndExit()
            }
        }
        return START_NOT_STICKY
    }

    // --- 计时器核心逻辑 ---

    fun startTimer() {
        wakeLock?.acquire(10 * 60 * 1000L /* 10 mins */)
        timerJob?.cancel()

        val current = _timerState.value
        val durationMillis = when (current.sessionType) {
            SessionType.WORK -> prefManager.workDurationMin * 60 * 1000L
            SessionType.SHORT_BREAK -> prefManager.shortBreakMin * 60 * 1000L
            SessionType.LONG_BREAK -> prefManager.longBreakMin * 60 * 1000L
        }

        _timerState.value = current.copy(
            phase = TimerPhase.RUNNING,
            remainingMillis = durationMillis,
            totalMillis = durationMillis
        )

        startForeground(NOTIFICATION_ID, buildNotification())
        startWhiteNoise()

        timerJob = serviceScope.launch {
            while (_timerState.value.remainingMillis > 0) {
                delay(1000L)
                val updatedRemaining = _timerState.value.remainingMillis - 1000L
                if (updatedRemaining <= 0) {
                    _timerState.value = _timerState.value.copy(remainingMillis = 0)
                    onTimerFinished()
                } else {
                    _timerState.value = _timerState.value.copy(remainingMillis = updatedRemaining)
                    updateNotification()
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(phase = TimerPhase.PAUSED)
        updateNotification()
        pauseWhiteNoise()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    fun resumeTimer() {
        wakeLock?.acquire(10 * 60 * 1000L)
        _timerState.value = _timerState.value.copy(phase = TimerPhase.RUNNING)
        updateNotification()
        resumeWhiteNoise()

        timerJob = serviceScope.launch {
            while (_timerState.value.remainingMillis > 0) {
                delay(1000L)
                val updatedRemaining = _timerState.value.remainingMillis - 1000L
                if (updatedRemaining <= 0) {
                    _timerState.value = _timerState.value.copy(remainingMillis = 0)
                    onTimerFinished()
                } else {
                    _timerState.value = _timerState.value.copy(remainingMillis = updatedRemaining)
                    updateNotification()
                }
            }
        }
    }

    fun skipCurrentPhase() {
        timerJob?.cancel()
        pauseWhiteNoise()
        val current = _timerState.value
        val nextSessionType = determineNextSession(current.sessionType, current.completedPomodoros)
        
        var completed = current.completedPomodoros
        if (current.sessionType == SessionType.WORK && current.phase != TimerPhase.IDLE) {
            // 如果是在工作状态下跳过，可以选择是否计入已完成 (这里为鼓励机制，跳过不计入)
        }

        val nextDuration = getDurationForSession(nextSessionType)

        _timerState.value = TimerData(
            phase = TimerPhase.IDLE,
            sessionType = nextSessionType,
            remainingMillis = nextDuration,
            totalMillis = nextDuration,
            completedPomodoros = completed,
            targetPomodoros = prefManager.targetPomodoros
        )

        updateNotification()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        pauseWhiteNoise()
        val nextDuration = getDurationForSession(SessionType.WORK)
        _timerState.value = TimerData(
            phase = TimerPhase.IDLE,
            sessionType = SessionType.WORK,
            remainingMillis = nextDuration,
            totalMillis = nextDuration,
            completedPomodoros = _timerState.value.completedPomodoros,
            targetPomodoros = prefManager.targetPomodoros
        )
        updateNotification()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    private fun onTimerFinished() {
        timerJob?.cancel()
        pauseWhiteNoise()
        triggerAlertEffect()

        val current = _timerState.value
        val completed = if (current.sessionType == SessionType.WORK) {
            current.completedPomodoros + 1
        } else {
            current.completedPomodoros
        }

        val nextSession = determineNextSession(current.sessionType, completed)
        val nextDuration = getDurationForSession(nextSession)

        _timerState.value = current.copy(
            phase = TimerPhase.FINISHED,
            completedPomodoros = completed,
            remainingMillis = 0
        )

        updateNotification()

        val shouldAutoStart = if (current.sessionType == SessionType.WORK) {
            prefManager.autoStartBreak
        } else {
            prefManager.autoStartWork
        }

        if (shouldAutoStart) {
            serviceScope.launch {
                delay(2000L) // 延迟2秒后开始下一阶段，让用户有个缓冲
                _timerState.value = TimerData(
                    phase = TimerPhase.RUNNING,
                    sessionType = nextSession,
                    remainingMillis = nextDuration,
                    totalMillis = nextDuration,
                    completedPomodoros = completed,
                    targetPomodoros = prefManager.targetPomodoros
                )
                startTimer()
            }
        } else {
            // 不自动开始，置为 IDLE 并切换到下一阶段
            serviceScope.launch {
                delay(2000L)
                _timerState.value = TimerData(
                    phase = TimerPhase.IDLE,
                    sessionType = nextSession,
                    remainingMillis = nextDuration,
                    totalMillis = nextDuration,
                    completedPomodoros = completed,
                    targetPomodoros = prefManager.targetPomodoros
                )
                updateNotification()
            }
        }

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    private fun determineNextSession(current: SessionType, completedCount: Int): SessionType {
        return if (current == SessionType.WORK) {
            if (completedCount > 0 && completedCount % prefManager.targetPomodoros == 0) {
                SessionType.LONG_BREAK
            } else {
                SessionType.SHORT_BREAK
            }
        } else {
            SessionType.WORK
        }
    }

    private fun getDurationForSession(sessionType: SessionType): Long {
        return when (sessionType) {
            SessionType.WORK -> prefManager.workDurationMin * 60 * 1000L
            SessionType.SHORT_BREAK -> prefManager.shortBreakMin * 60 * 1000L
            SessionType.LONG_BREAK -> prefManager.longBreakMin * 60 * 1000L
        }
    }

    // --- 白噪音混音管理 ---

    private fun initMediaPlayers() {
        val soundKeys = listOf("rain", "campfire", "ocean", "forest")
        soundKeys.forEach { key ->
            val resId = resources.getIdentifier(key, "raw", packageName)
            if (resId != 0) {
                try {
                    val mp = MediaPlayer.create(this, resId).apply {
                        isLooping = true
                        setVolume(0f, 0f)
                    }
                    mediaPlayers[key] = mp
                    Log.d(tag, "Successfully loaded white noise: $key")
                } catch (e: Exception) {
                    Log.e(tag, "Error loading white noise resource: $key", e)
                }
            } else {
                Log.w(tag, "White noise resource raw/$key.mp3 not found. Mixer will skip playing this channel.")
            }
        }
    }

    fun updateMixerVolume(settings: MixerSettings) {
        _mixerSettings.value = settings
        // 动态调音量
        setPlayerVolume("rain", settings.rainVolume)
        setPlayerVolume("campfire", settings.campfireVolume)
        setPlayerVolume("ocean", settings.oceanVolume)
        setPlayerVolume("forest", settings.forestVolume)
    }

    private fun setPlayerVolume(key: String, volume: Float) {
        val mp = mediaPlayers[key]
        if (mp != null) {
            mp.setVolume(volume, volume)
            // 如果计时器在运行，且音量大于 0，播放器处于暂停状态，则开始播放
            if (_timerState.value.phase == TimerPhase.RUNNING && volume > 0f && !mp.isPlaying) {
                try {
                    mp.start()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to start player $key", e)
                }
            } else if (volume == 0f && mp.isPlaying) {
                mp.pause()
            }
        }
    }

    private fun startWhiteNoise() {
        val settings = _mixerSettings.value
        adjustAndPlay("rain", settings.rainVolume)
        adjustAndPlay("campfire", settings.campfireVolume)
        adjustAndPlay("ocean", settings.oceanVolume)
        adjustAndPlay("forest", settings.forestVolume)
    }

    private fun adjustAndPlay(key: String, volume: Float) {
        val mp = mediaPlayers[key]
        if (mp != null && volume > 0f) {
            mp.setVolume(volume, volume)
            if (!mp.isPlaying) {
                try { mp.start() } catch (e: Exception) { Log.e(tag, "Start fail for $key", e) }
            }
        }
    }

    private fun pauseWhiteNoise() {
        mediaPlayers.values.forEach { mp ->
            if (mp.isPlaying) {
                mp.pause()
            }
        }
    }

    private fun resumeWhiteNoise() {
        if (_timerState.value.phase == TimerPhase.RUNNING) {
            startWhiteNoise()
        }
    }

    private fun releasePlayers() {
        mediaPlayers.values.forEach { mp ->
            try {
                if (mp.isPlaying) mp.stop()
                mp.release()
            } catch (e: Exception) {
                Log.e(tag, "Release player error", e)
            }
        }
        mediaPlayers.clear()
    }

    // --- 倒计时结束振动与铃声效果 ---

    private fun triggerAlertEffect() {
        // 振动：长振动或者双击震动
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 400, 200, 400)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 400, 200, 400), -1)
        }

        // 响铃：使用系统默认通知声
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, notificationUri)
            ringtone.play()
        } catch (e: Exception) {
            Log.e(tag, "Play alert ringtone failed", e)
        }
    }

    // --- 前台服务与通知栏逻辑 ---

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "番茄猫后台计时服务",
                NotificationManager.IMPORTANCE_LOW // 低优先级，避免频繁嘟嘟声
            ).apply {
                description = "在后台精确执行番茄钟计时并进行白噪音混音播放"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val state = _timerState.value
        val timeString = formatTime(state.remainingMillis)
        
        // 点击通知返回主界面
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 按钮动作
        val pauseIntent = Intent(this, TimerService::class.java).apply { action = ACTION_PAUSE }
        val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val resumeIntent = Intent(this, TimerService::class.java).apply { action = ACTION_RESUME }
        val resumePendingIntent = PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE)

        val skipIntent = Intent(this, TimerService::class.java).apply { action = ACTION_SKIP }
        val skipPendingIntent = PendingIntent.getService(this, 3, skipIntent, PendingIntent.FLAG_IMMUTABLE)

        // 拼装通知栏样式
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play) // 暂时使用系统默认图标代替，用户可自定义
            .setContentTitle("猫咪陪你中: ${state.sessionType.label}")
            .setContentText("剩余时间: $timeString | 今日完成: ${state.completedPomodoros}/${state.targetPomodoros}")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // 根据计时器状态添加操作按钮
        if (state.phase == TimerPhase.RUNNING) {
            builder.addAction(android.R.drawable.ic_media_pause, "暂停", pausePendingIntent)
            builder.addAction(android.R.drawable.ic_media_next, "跳过", skipPendingIntent)
        } else if (state.phase == TimerPhase.PAUSED) {
            builder.addAction(android.R.drawable.ic_media_play, "继续", resumePendingIntent)
            builder.addAction(android.R.drawable.ic_media_next, "跳过", skipPendingIntent)
        }

        return builder.build()
    }

    private fun updateNotification() {
        if (_timerState.value.phase != TimerPhase.IDLE) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun formatTime(millis: Long): String {
        val totalSecs = millis / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }

    private fun stopForegroundAndExit() {
        timerJob?.cancel()
        pauseWhiteNoise()
        releasePlayers()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "Service Destroyed")
        serviceScope.cancel()
        releasePlayers()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}
