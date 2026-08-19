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
import androidx.compose.ui.draw.clip
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
    tags: List<com.example.pomodorocat.data.db.TaskTagEntity> = emptyList(),
    activeCat: com.example.pomodorocat.data.db.CatProfileEntity? = null,
    fishBalance: Int = 0,
    settlementData: com.example.pomodorocat.data.SettlementData? = null,
    onDismissSettlement: () -> Unit = {},
    onSaveDiary: (rating: Int, diaryNote: String) -> Unit = { _, _ -> },
    onTagSelected: (com.example.pomodorocat.data.db.TaskTagEntity) -> Unit = {},
    onAddNewTag: (name: String, iconKey: String, colorHex: Long) -> Unit = { _, _, _ -> },
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onReset: () -> Unit,
    onSwitchSessionType: (com.example.pomodorocat.data.SessionType) -> Unit = {},
    onMixerVolumeChange: (MixerSettings) -> Unit,
    onThemeChanged: () -> Unit,
    onToggleDarkMode: () -> Unit = {},
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    val skinSpec = remember(activeCat?.id) {
        com.example.pomodorocat.ui.components.CatSkinSpec.getById(activeCat?.id ?: "orange_tabby")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🐾 番茄猫",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // 小鱼干余额胶囊 (与标题自然排布在左侧，永不与右侧操作按钮重叠)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(androidx.compose.ui.graphics.Color(0xFFFFD54F).copy(alpha = 0.22f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text("🐟", fontSize = 11.sp)
                                Text(
                                    text = "$fishBalance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = androidx.compose.ui.graphics.Color(0xFFD35400)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // 昼夜模式快速切换按钮
                    IconButton(onClick = onToggleDarkMode) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "切换昼夜模式",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "设置",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
            
            // 0. 任务标签横向选择栏
            if (tags.isNotEmpty()) {
                com.example.pomodorocat.ui.components.TagSelectorRow(
                    tags = tags,
                    activeTagId = timerData.activeTagId,
                    onTagSelected = onTagSelected,
                    onAddNewTag = { showAddTagDialog = true },
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            // 1. 猫咪陪伴 (应用选中的皮肤与亲密度装扮)
            CatCompanion(
                sessionType = timerData.sessionType,
                phase = timerData.phase,
                skinSpec = skinSpec,
                bondLevel = activeCat?.bondLevel ?: 1,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 模式切换芯片 (空闲时展示)
            if (timerData.phase == com.example.pomodorocat.data.TimerPhase.IDLE || timerData.phase == com.example.pomodorocat.data.TimerPhase.FINISHED) {
                SessionModeSelector(
                    activeType = timerData.sessionType,
                    workMin = prefManager.workDurationMin,
                    shortBreakMin = prefManager.shortBreakMin,
                    longBreakMin = prefManager.longBreakMin,
                    onSelect = onSwitchSessionType,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            // 2. 倒计时圆环
            TimerDisplay(
                remainingMillis = timerData.remainingMillis,
                totalMillis = timerData.totalMillis,
                sessionType = timerData.sessionType,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 3. 今日番茄鱼干奖励栏
            RewardPanel(
                completed = timerData.completedPomodoros,
                target = timerData.targetPomodoros
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. 控制操作栏 (状态自适应)
            ControlBar(
                phase = timerData.phase,
                sessionType = timerData.sessionType,
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

        // 专注完成结算日记弹窗
        settlementData?.let { settlement ->
            com.example.pomodorocat.ui.components.SettlementDialog(
                data = settlement,
                onDismiss = onDismissSettlement,
                onSaveDiary = { rating, diaryNote ->
                    onSaveDiary(rating, diaryNote)
                    onDismissSettlement()
                }
            )
        }

        // 新建标签弹窗
        if (showAddTagDialog) {
            com.example.pomodorocat.ui.components.AddTagDialog(
                onDismiss = { showAddTagDialog = false },
                onConfirm = { name, iconKey, colorHex ->
                    onAddNewTag(name, iconKey, colorHex)
                    showAddTagDialog = false
                }
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
 * 模式选择芯片组 (专注 / 短休 / 长休)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionModeSelector(
    activeType: com.example.pomodorocat.data.SessionType,
    workMin: Int,
    shortBreakMin: Int,
    longBreakMin: Int,
    onSelect: (com.example.pomodorocat.data.SessionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        val modes = listOf(
            com.example.pomodorocat.data.SessionType.WORK to "🍅 专注 ${workMin}m",
            com.example.pomodorocat.data.SessionType.SHORT_BREAK to "☕ 短休 ${shortBreakMin}m",
            com.example.pomodorocat.data.SessionType.LONG_BREAK to "🌴 长休 ${longBreakMin}m"
        )
        modes.forEach { (type, label) ->
            val isSelected = activeType == type
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(type) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
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
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..target) {
                    if (i <= completed) {
                        Text("🐟", fontSize = 16.sp)
                    } else {
                        Text("🦴", fontSize = 16.sp)
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
 * 控制动作栏 (状态严密感应)
 */
@Composable
private fun ControlBar(
    phase: com.example.pomodorocat.data.TimerPhase,
    sessionType: com.example.pomodorocat.data.SessionType,
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
            com.example.pomodorocat.data.TimerPhase.IDLE, com.example.pomodorocat.data.TimerPhase.FINISHED -> {
                val actionTitle = if (sessionType == com.example.pomodorocat.data.SessionType.WORK) "开始专注喵 🐾" else "开始休息喵 ☕"
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(0.85f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "开始")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(actionTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            com.example.pomodorocat.data.TimerPhase.RUNNING -> {
                // 暂停按钮 (主操作)
                Button(
                    onClick = onPause,
                    modifier = Modifier.weight(1.2f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Pause, contentDescription = "暂停")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("暂停", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 放弃/重置按钮
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1.2f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "放弃")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("放弃", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 跳过按钮 (小图标按钮)
                FilledTonalIconButton(
                    onClick = onSkip,
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.SkipNext, contentDescription = "跳过本轮")
                }
            }

            com.example.pomodorocat.data.TimerPhase.PAUSED -> {
                val resumeTitle = if (sessionType == com.example.pomodorocat.data.SessionType.WORK) "继续专注" else "继续休息"
                // 继续按钮
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1.2f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "继续")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(resumeTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 重置按钮
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1.2f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "重置")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("重置", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}
