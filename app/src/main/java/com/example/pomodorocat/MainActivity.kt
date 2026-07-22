package com.example.pomodorocat

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.pomodorocat.data.MixerSettings
import com.example.pomodorocat.data.PreferenceManager
import com.example.pomodorocat.data.TimerData
import com.example.pomodorocat.service.TimerService
import com.example.pomodorocat.ui.screens.MainScreen
import com.example.pomodorocat.ui.theme.PomodoroCatTheme

class MainActivity : ComponentActivity() {

    private var timerService by mutableStateOf<TimerService?>(null)
    private var isBound by mutableStateOf(false)

    private lateinit var prefManager: PreferenceManager

    // 用于触发重绘的主题变动 State
    private var themeState by mutableStateOf(0)

    // 连接前台计时服务
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            timerService = null
            isBound = false
        }
    }

    // 动态请求通知权限 (Android 13+)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(
                this,
                "猫咪悄悄话：没有通知权限，后台计时可能在退到桌面时失效哦喵~",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = PreferenceManager(this)
        themeState = prefManager.selectedTheme

        // 申请通知权限
        checkNotificationPermission()

        // 启动并绑定前台计时服务，保证其生命周期与组件隔离，能在后台常驻
        val intent = Intent(this, TimerService::class.java)
        startService(intent) // 保证服务在 Activity 销毁后仍在后台驻守
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        setContent {
            PomodoroCatTheme(themeIndex = themeState) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 如果已经连接上 Service，则订阅其 Flow 并渲染 UI；否则显示一个临时空态
                    if (isBound && timerService != null) {
                        val timerData by timerService!!.timerState.collectAsState()
                        val mixerSettings by timerService!!.mixerSettings.collectAsState()

                        MainScreen(
                            timerData = timerData,
                            mixerSettings = mixerSettings,
                            prefManager = prefManager,
                            onStart = { 
                                triggerServiceAction(TimerService.ACTION_START)
                            },
                            onPause = { 
                                triggerServiceAction(TimerService.ACTION_PAUSE)
                            },
                            onResume = { 
                                triggerServiceAction(TimerService.ACTION_RESUME)
                            },
                            onSkip = { 
                                triggerServiceAction(TimerService.ACTION_SKIP)
                            },
                            onReset = {
                                timerService?.resetTimer()
                            },
                            onMixerVolumeChange = { updatedSettings ->
                                timerService?.updateMixerVolume(updatedSettings)
                            },
                            onThemeChanged = {
                                // 用户在设置弹窗里修改了主题，触发 Activity 刷新
                                themeState = prefManager.selectedTheme
                            }
                        )
                    } else {
                        // 正在连接服务时的占位图，由于绑定极其迅速，通常眨眼即逝
                        // 我们直接渲染一个静态的 Compose 空状态
                        var fakeData by remember { mutableStateOf(TimerData()) }
                        var fakeSettings by remember { mutableStateOf(MixerSettings()) }
                        MainScreen(
                            timerData = fakeData,
                            mixerSettings = fakeSettings,
                            prefManager = prefManager,
                            onStart = {},
                            onPause = {},
                            onResume = {},
                            onSkip = {},
                            onReset = {},
                            onMixerVolumeChange = {},
                            onThemeChanged = {}
                        )
                    }
                }
            }
        }
    }

    private fun triggerServiceAction(action: String) {
        val intent = Intent(this, TimerService::class.java).apply {
            this.action = action
        }
        startService(intent)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}
