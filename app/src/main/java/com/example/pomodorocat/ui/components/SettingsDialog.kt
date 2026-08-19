package com.example.pomodorocat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pomodorocat.data.PreferenceManager
import com.example.pomodorocat.ui.theme.GreenPrimary
import com.example.pomodorocat.ui.theme.PinkPrimary
import com.example.pomodorocat.ui.theme.YellowPrimary

@Composable
fun SettingsDialog(
    prefManager: PreferenceManager,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    // 临时状态存储
    var workMin by remember { mutableStateOf(prefManager.workDurationMin) }
    var shortBreakMin by remember { mutableStateOf(prefManager.shortBreakMin) }
    var longBreakMin by remember { mutableStateOf(prefManager.longBreakMin) }
    var targetPomodoros by remember { mutableStateOf(prefManager.targetPomodoros) }
    var autoStartBreak by remember { mutableStateOf(prefManager.autoStartBreak) }
    var autoStartWork by remember { mutableStateOf(prefManager.autoStartWork) }
    var themeIndex by remember { mutableStateOf(prefManager.selectedTheme) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "⚙️ 个性化设置",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 1. 专注时长
                SettingSlider(
                    title = "工作时长",
                    value = workMin,
                    onValueChange = { workMin = it },
                    valueRange = 5f..60f,
                    unit = "分钟"
                )

                // 2. 短休时长
                SettingSlider(
                    title = "短休时长",
                    value = shortBreakMin,
                    onValueChange = { shortBreakMin = it },
                    valueRange = 1f..20f,
                    unit = "分钟"
                )

                // 3. 长休时长
                SettingSlider(
                    title = "长休时长",
                    value = longBreakMin,
                    onValueChange = { longBreakMin = it },
                    valueRange = 5f..45f,
                    unit = "分钟"
                )

                // 4. 大循环番茄数
                SettingSlider(
                    title = "目标大循环番茄数",
                    value = targetPomodoros,
                    onValueChange = { targetPomodoros = it },
                    valueRange = 2f..10f,
                    unit = "个"
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // 5. 自动开关
                SettingSwitch(
                    title = "专注结束后自动休息",
                    checked = autoStartBreak,
                    onCheckedChange = { autoStartBreak = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingSwitch(
                    title = "休息结束后自动专注",
                    checked = autoStartWork,
                    onCheckedChange = { autoStartWork = it }
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // 6. 昼夜模式切换
                Text(
                    text = "🌓 显示模式 (深色/浅色)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                var darkModeSetting by remember { mutableStateOf(prefManager.darkMode) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modeOptions = listOf(0 to "📱 跟随系统", 1 to "☀️ 浅色", 2 to "🌙 深色")
                    modeOptions.forEach { (mode, label) ->
                        val isSelected = darkModeSetting == mode
                        OutlinedButton(
                            onClick = { darkModeSetting = mode },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonBorder.let {
                                if (isSelected) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                                else ButtonDefaults.outlinedButtonColors()
                            },
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = if (isSelected) 2.dp else 1.dp
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 7. 主题切换
                Text(
                    text = "🎨 猫咪外套配色 (主题色)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ThemeColorDot(color = PinkPrimary, selected = themeIndex == 0) { themeIndex = 0 }
                    ThemeColorDot(color = GreenPrimary, selected = themeIndex == 1) { themeIndex = 1 }
                    ThemeColorDot(color = YellowPrimary, selected = themeIndex == 2) { themeIndex = 2 }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 保存 & 取消按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            prefManager.workDurationMin = workMin
                            prefManager.shortBreakMin = shortBreakMin
                            prefManager.longBreakMin = longBreakMin
                            prefManager.targetPomodoros = targetPomodoros
                            prefManager.autoStartBreak = autoStartBreak
                            prefManager.autoStartWork = autoStartWork
                            prefManager.darkMode = darkModeSetting
                            prefManager.selectedTheme = themeIndex
                            onSaved()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("保存设置")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 版本号脚标
                Text(
                    text = "🐾 番茄猫 v2.0.0 (Cat Sanctuary Edition)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingSlider(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text("$value $unit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange,
            steps = (valueRange.endInclusive - valueRange.start).toInt() - 1,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun ThemeColorDot(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}
