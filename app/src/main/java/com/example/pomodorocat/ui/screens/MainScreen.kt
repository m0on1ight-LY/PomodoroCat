package com.example.pomodorocat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodorocat.data.MixerSettings
import com.example.pomodorocat.data.PreferenceManager
import com.example.pomodorocat.data.TimerData
import com.example.pomodorocat.data.TimerPhase
import com.example.pomodorocat.ui.components.CatCompanion
import com.example.pomodorocat.ui.components.MixerPanel
import com.example.pomodorocat.ui.components.SettingsDialog
import com.example.pomodorocat.ui.components.TimerDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    timerData: TimerData,
    mixerSettings: MixerSettings,
    prefManager: PreferenceManager,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onReset: () -> Unit,
    onMixerVolumeChange: (MixerSettings) -> Unit,
    onThemeChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "🐾 番茄猫",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "设置",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // 1. 猫咪陪伴
            CatCompanion(
                sessionType = timerData.sessionType,
                phase = timerData.phase,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 2. 倒计时圆环
            TimerDisplay(
                remainingMillis = timerData.remainingMillis,
                totalMillis = timerData.totalMillis,
                sessionType = timerData.sessionType,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // 3. 今日番茄鱼干奖励栏
            RewardPanel(
                completed = timerData.completedPomodoros,
                target = timerData.targetPomodoros
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. 控制操作栏
            ControlBar(
                phase = timerData.phase,
                onStart = onStart,
                onPause = onPause,
                onResume = onResume,
                onSkip = onSkip,
                onReset = onReset
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. 白噪音混音滑块面板
            MixerPanel(
                settings = mixerSettings,
                onSettingsChanged = onMixerVolumeChange,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // 个性化设置弹窗
        if (showSettings) {
            SettingsDialog(
                prefManager = prefManager,
                onDismiss = { showSettings = false },
                onSaved = {
                    showSettings = false
                    onThemeChanged() // 回调通知 Activity 重新加载主题
                    onReset()        // 修改时长设置后，重置计时器状态
                }
            )
        }
    }
}

/**
 * 奖励栏：今日小鱼干成就显示
 */
@Composable
private fun RewardPanel(completed: Int, target: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "今日奖励: ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            
            // 循环绘制小鱼干。已完成的用 🐟，未完成的用 🦴
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..target) {
                    if (i <= completed) {
                        Text("🐟", fontSize = 16.sp) // 饱满的烤鱼
                    } else {
                        Text("🦴", fontSize = 16.sp) // 鱼骨头
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "$completed / $target",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 控制动作栏
 */
@Composable
private fun ControlBar(
    phase: TimerPhase,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (phase) {
            TimerPhase.IDLE, TimerPhase.FINISHED -> {
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(0.8f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "开始")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始专注喵", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            TimerPhase.RUNNING -> {
                // 暂停按钮
                Button(
                    onClick = onPause,
                    modifier = Modifier.weight(1.2f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Pause, contentDescription = "暂停")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("稍微歇下", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 跳过按钮
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(0.8f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 2.dp
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.SkipNext, contentDescription = "跳过")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("跳过", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            TimerPhase.PAUSED -> {
                // 继续按钮
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1.2f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "继续")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("继续努力", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 放弃/重置按钮
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(0.8f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 2.dp
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "重新开始")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("重置", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
