package com.example.pomodorocat.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.pomodorocat.MainActivity
import com.example.pomodorocat.R
import com.example.pomodorocat.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class TimerService : Service() {

    private val binder = TimerBinder()
    private val tag = "TimerService"

    // 协程作用域，用于控制后台倒计时
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    // 目标结束时间戳 (基于 SystemClock.elapsedRealtime)，彻底杜绝协程休眠漂移
    private var targetEndTimeMillis: Long = 0L

    // 状态流供 UI 订阅
    private val _timerState = MutableStateFlow(TimerData())
    val timerState: StateFlow<TimerData> = _timerState.asStateFlow()

    private val _mixerSettings = MutableStateFlow(MixerSettings())
    val mixerSettings: StateFlow<MixerSettings> = _mixerSettings.asStateFlow()

    // 结算事件
    private val _settlementEvent = kotlinx.coroutines.flow.MutableSharedFlow<SettlementData>(extraBufferCapacity = 1)
    val settlementEvent: kotlinx.coroutines.flow.SharedFlow<SettlementData> = _settlementEvent.asSharedFlow()

    // 白噪音播放器映射表
    private val mediaPlayers = mutableMapOf<String, MediaPlayer>()
    
    // 音频焦点管理
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                hasAudioFocus = false
                pauseWhiteNoise()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                pauseWhiteNoise()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
                if (_timerState.value.phase == TimerPhase.RUNNING) {
                    resumeWhiteNoise()
                }
            }
        }
    }

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
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        // 载入保存的任务标签
        val savedTagId = prefManager.selectedTagId
        _timerState.value = _timerState.value.copy(
            activeTagId = savedTagId,
            activeTagName = getTagNameById(savedTagId)
        )

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

    fun setActiveTag(tagId: String, tagName: String) {
        _timerState.value = _timerState.value.copy(activeTagId = tagId, activeTagName = tagName)
        prefManager.selectedTagId = tagId
        updateNotification()
    }

    private fun getTagNameById(tagId: String): String {
        return when (tagId) {
            "work" -> "工作"
            "study" -> "学习"
            "reading" -> "阅读"
            "code" -> "编程"
            "exercise" -> "运动"
            "meditation" -> "冥想"
            else -> "专注"
        }
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

    // --- 计时器核心逻辑 (基于绝对时间戳) ---

    fun startTimer() {
        timerJob?.cancel()

        val current = _timerState.value
        val durationMillis = when (current.sessionType) {
            SessionType.WORK -> prefManager.workDurationMin * 60 * 1000L
            SessionType.SHORT_BREAK -> prefManager.shortBreakMin * 60 * 1000L
            SessionType.LONG_BREAK -> prefManager.longBreakMin * 60 * 1000L
        }

        targetEndTimeMillis = SystemClock.elapsedRealtime() + durationMillis
        safeAcquireWakeLock(durationMillis + 60_000L) // 充足持有锁直至会话结束

        _timerState.value = current.copy(
            phase = TimerPhase.RUNNING,
            remainingMillis = durationMillis,
            totalMillis = durationMillis
        )

        startForegroundWithCompat()
        startWhiteNoise()

        runTimerLoop()
    }

    fun pauseTimer() {
        timerJob?.cancel()
        val now = SystemClock.elapsedRealtime()
        val remaining = (targetEndTimeMillis - now).coerceAtLeast(0L)
        _timerState.value = _timerState.value.copy(
            phase = TimerPhase.PAUSED,
            remainingMillis = remaining
        )
        updateNotification()
        pauseWhiteNoise()
        safeReleaseWakeLock()
    }

    fun resumeTimer() {
        timerJob?.cancel()
        val remaining = _timerState.value.remainingMillis
        targetEndTimeMillis = SystemClock.elapsedRealtime() + remaining
        safeAcquireWakeLock(remaining + 60_000L)

        _timerState.value = _timerState.value.copy(phase = TimerPhase.RUNNING)
        updateNotification()
        resumeWhiteNoise()

        runTimerLoop()
    }

    private fun runTimerLoop() {
        timerJob = serviceScope.launch {
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                val remaining = targetEndTimeMillis - now
                if (remaining <= 0L) {
                    _timerState.value = _timerState.value.copy(remainingMillis = 0L)
                    onTimerFinished()
                    break
                } else {
                    _timerState.value = _timerState.value.copy(remainingMillis = remaining)
                    updateNotification()
                    
                    // 计算距离下一个整秒的对齐延时，保证秒级刷新的流畅与省电
                    val delayToNextSec = (remaining % 1000L).let { if (it <= 50L) 1000L else it }
                    delay(delayToNextSec.coerceIn(100L, 1000L))
                }
            }
        }
    }

    fun skipCurrentPhase() {
        timerJob?.cancel()
        pauseWhiteNoise()
        val current = _timerState.value
        val nextSessionType = determineNextSession(current.sessionType, current.completedPomodoros)
        
        val completed = current.completedPomodoros
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
        safeReleaseWakeLock()
    }

    fun switchSessionType(type: SessionType) {
        if (_timerState.value.phase == TimerPhase.RUNNING) return
        timerJob?.cancel()
        pauseWhiteNoise()
        val duration = getDurationForSession(type)
        val current = _timerState.value
        _timerState.value = current.copy(
            phase = TimerPhase.IDLE,
            sessionType = type,
            remainingMillis = duration,
            totalMillis = duration
        )
        updateNotification()
        safeReleaseWakeLock()
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
        safeReleaseWakeLock()
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

        // 如果是专注工作完成，记录数据库并派发小鱼干结算
        if (current.sessionType == SessionType.WORK) {
            val durationMin = prefManager.workDurationMin
            val earnedFish = durationMin // 1分钟 = 1小鱼干
            val now = System.currentTimeMillis()

            serviceScope.launch(Dispatchers.IO) {
                val repository = com.example.pomodorocat.data.repository.PomodoroRepository.getInstance(applicationContext)
                val sessionId = repository.recordSession(
                    startTime = now - durationMin * 60 * 1000L,
                    endTime = now,
                    durationMinutes = durationMin,
                    tagId = current.activeTagId,
                    tagName = current.activeTagName,
                    sessionType = "WORK",
                    isCompleted = true,
                    rating = 5,
                    diaryNote = "",
                    earnedFish = earnedFish
                )
                _settlementEvent.emit(
                    SettlementData(
                        sessionId = sessionId,
                        durationMinutes = durationMin,
                        earnedFish = earnedFish,
                        tagName = current.activeTagName
                    )
                )
            }
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

        safeReleaseWakeLock()
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

    // --- 白噪音混音管理 (带音频焦点与异常保护) ---

    private fun initMediaPlayers() {
        val soundMap = mapOf(
            "rain" to R.raw.rain,
            "campfire" to R.raw.campfire,
            "ocean" to R.raw.ocean,
            "forest" to R.raw.forest
        )
        soundMap.forEach { (key, resId) ->
            try {
                val mp = MediaPlayer.create(this, resId)?.apply {
                    isLooping = true
                    setVolume(0f, 0f)
                    setOnErrorListener { _, what, extra ->
                        Log.e(tag, "MediaPlayer error on $key: what=$what, extra=$extra")
                        true
                    }
                }
                if (mp != null) {
                    mediaPlayers[key] = mp
                    Log.d(tag, "Successfully loaded white noise: $key")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading white noise resource: $key", e)
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
            if (_timerState.value.phase == TimerPhase.RUNNING && volume > 0f) {
                if (requestAudioFocus() && !mp.isPlaying) {
                    try {
                        mp.start()
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to start player $key", e)
                    }
                }
            } else if (volume == 0f && mp.isPlaying) {
                mp.pause()
                checkAndReleaseAudioFocusIfSilent()
            }
        }
    }

    private fun startWhiteNoise() {
        val settings = _mixerSettings.value
        val hasAnyVolume = settings.rainVolume > 0f || settings.campfireVolume > 0f ||
                settings.oceanVolume > 0f || settings.forestVolume > 0f

        if (hasAnyVolume && requestAudioFocus()) {
            adjustAndPlay("rain", settings.rainVolume)
            adjustAndPlay("campfire", settings.campfireVolume)
            adjustAndPlay("ocean", settings.oceanVolume)
            adjustAndPlay("forest", settings.forestVolume)
        }
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
        abandonAudioFocus()
    }

    private fun resumeWhiteNoise() {
        if (_timerState.value.phase == TimerPhase.RUNNING) {
            startWhiteNoise()
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        val am = audioManager ?: return false
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioFocusRequest = req
            am.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
        hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(audioFocusChangeListener)
        }
        hasAudioFocus = false
    }

    private fun checkAndReleaseAudioFocusIfSilent() {
        val settings = _mixerSettings.value
        val isAllSilent = settings.rainVolume == 0f && settings.campfireVolume == 0f &&
                settings.oceanVolume == 0f && settings.forestVolume == 0f
        if (isAllSilent) {
            abandonAudioFocus()
        }
    }

    private fun releasePlayers() {
        abandonAudioFocus()
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

    private fun safeAcquireWakeLock(duration: Long) {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock?.acquire(duration)
        } catch (e: Exception) {
            Log.e(tag, "Failed to acquire wake lock", e)
        }
    }

    private fun safeReleaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to release wake lock", e)
        }
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

    private fun startForegroundWithCompat() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            type
        )
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
            .setSmallIcon(android.R.drawable.ic_media_play)
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
        val totalSecs = (millis + 999L) / 1000L // 向上取整，给用户自然的倒计时视觉体验 (如 25:00 开始)
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }

    private fun stopForegroundAndExit() {
        timerJob?.cancel()
        pauseWhiteNoise()
        releasePlayers()
        safeReleaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "Service Destroyed")
        serviceScope.cancel()
        releasePlayers()
        safeReleaseWakeLock()
    }
}
