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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.pomodorocat.data.MixerSettings
import com.example.pomodorocat.data.PreferenceManager
import com.example.pomodorocat.data.SettlementData
import com.example.pomodorocat.data.TimerData
import com.example.pomodorocat.data.repository.PomodoroRepository
import com.example.pomodorocat.service.TimerService
import com.example.pomodorocat.ui.screens.AnalyticsScreen
import com.example.pomodorocat.ui.screens.MainScreen
import com.example.pomodorocat.ui.screens.SanctuaryScreen
import com.example.pomodorocat.ui.theme.PomodoroCatTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val icon: ImageVector) {
    FOCUS("专注", Icons.Rounded.Timer),
    ANALYTICS("复盘", Icons.Rounded.BarChart),
    SANCTUARY("猫舍", Icons.Rounded.Pets)
}

class MainActivity : ComponentActivity() {

    private var timerService by mutableStateOf<TimerService?>(null)
    private var isBound by mutableStateOf(false)

    private lateinit var prefManager: PreferenceManager

    // 用于触发重绘的主题与昼夜变动 State
    private var themeState by mutableStateOf(0)
    private var darkModeState by mutableStateOf(0)

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
        darkModeState = prefManager.darkMode

        // 申请通知权限
        checkNotificationPermission()

        // 启动并绑定前台计时服务
        val intent = Intent(this, TimerService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        val repository = PomodoroRepository.getInstance(this)

        setContent {
            val coroutineScope = rememberCoroutineScope()
            var currentTab by remember { mutableStateOf(AppTab.FOCUS) }

            val tags by repository.allTags.collectAsState(initial = emptyList())
            val allSessions by repository.allSessions.collectAsState(initial = emptyList())
            val cats by repository.allCats.collectAsState(initial = emptyList())
            val activeCat by repository.activeCat.collectAsState(initial = null)
            val badges by repository.allBadges.collectAsState(initial = emptyList())

            val timerData by (timerService?.timerState ?: remember { MutableStateFlow(TimerData()) }).collectAsState()
            val mixerSettings by (timerService?.mixerSettings ?: remember { MutableStateFlow(MixerSettings()) }).collectAsState()

            var activeSettlement by remember { mutableStateOf<SettlementData?>(null) }

            LaunchedEffect(timerService) {
                timerService?.settlementEvent?.collect { data ->
                    activeSettlement = data
                }
            }

            PomodoroCatTheme(themeIndex = themeState, darkModeSetting = darkModeState) {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            AppTab.values().forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (currentTab) {
                            AppTab.FOCUS -> {
                                val isDarkEffective = darkModeState == 2 || (darkModeState == 0 && androidx.compose.foundation.isSystemInDarkTheme())
                                MainScreen(
                                    timerData = timerData,
                                    mixerSettings = mixerSettings,
                                    prefManager = prefManager,
                                    tags = tags,
                                    activeCat = activeCat,
                                    fishBalance = prefManager.totalDriedFish,
                                    settlementData = activeSettlement,
                                    isDarkMode = isDarkEffective,
                                    onToggleDarkMode = {
                                        val newMode = if (isDarkEffective) 1 else 2
                                        prefManager.darkMode = newMode
                                        darkModeState = newMode
                                    },
                                    onDismissSettlement = { activeSettlement = null },
                                    onSaveDiary = { rating, note ->
                                        activeSettlement?.let { settlement ->
                                            coroutineScope.launch {
                                                repository.updateSessionRatingAndNote(settlement.sessionId, rating, note)
                                            }
                                        }
                                    },
                                    onTagSelected = { tag ->
                                        timerService?.setActiveTag(tag.id, tag.name)
                                    },
                                    onAddNewTag = { name, iconKey, colorHex ->
                                        coroutineScope.launch {
                                            repository.createCustomTag(name, iconKey, colorHex)
                                        }
                                    },
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
                                    onSwitchSessionType = { type ->
                                        timerService?.switchSessionType(type)
                                    },
                                    onMixerVolumeChange = { updatedSettings ->
                                        timerService?.updateMixerVolume(updatedSettings)
                                    },
                                    onThemeChanged = {
                                        themeState = prefManager.selectedTheme
                                        darkModeState = prefManager.darkMode
                                    }
                                )
                            }

                            AppTab.ANALYTICS -> {
                                AnalyticsScreen(
                                    sessions = allSessions
                                )
                            }

                            AppTab.SANCTUARY -> {
                                SanctuaryScreen(
                                    cats = cats,
                                    badges = badges,
                                    fishBalance = prefManager.totalDriedFish,
                                    onSelectCat = { catId ->
                                        coroutineScope.launch {
                                            repository.setActiveCat(catId)
                                        }
                                    },
                                    onUnlockCat = { catId, cost ->
                                        coroutineScope.launch {
                                            val success = repository.unlockCat(catId, cost)
                                            if (success) {
                                                Toast.makeText(this@MainActivity, "🎉 成功解锁新猫咪伙伴！", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this@MainActivity, "小鱼干不足喵~ 快去专注赚鱼干吧！", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onFeedCat = {
                                        coroutineScope.launch {
                                            val success = repository.feedActiveCat()
                                            if (success) {
                                                Toast.makeText(this@MainActivity, "🐟 投喂成功！亲密度 +10 EXP ✨", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this@MainActivity, "小鱼干不足 10 条喵，快去专注吧！", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
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
