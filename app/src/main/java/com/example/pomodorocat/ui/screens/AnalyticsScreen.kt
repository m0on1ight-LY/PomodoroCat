package com.example.pomodorocat.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodorocat.data.db.FocusSessionEntity
import com.example.pomodorocat.ui.components.TagIconHelper
import java.text.SimpleDateFormat
import java.util.*

enum class TimePeriod(val label: String) {
    DAY("今日"),
    WEEK("本周"),
    MONTH("本月"),
    ALL("总览")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    sessions: List<FocusSessionEntity>,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(TimePeriod.DAY) }

    // 过滤当前周期内的数据
    val filteredSessions = remember(sessions, selectedPeriod) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        when (selectedPeriod) {
            TimePeriod.DAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                sessions.filter { it.startTime >= startOfDay }
            }
            TimePeriod.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfWeek = calendar.timeInMillis
                sessions.filter { it.startTime >= startOfWeek }
            }
            TimePeriod.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfMonth = calendar.timeInMillis
                sessions.filter { it.startTime >= startOfMonth }
            }
            TimePeriod.ALL -> sessions
        }
    }

    val totalMinutes = remember(filteredSessions) {
        filteredSessions.filter { it.isCompleted }.sumOf { it.durationMinutes }
    }
    val totalCount = remember(filteredSessions) {
        filteredSessions.count { it.isCompleted }
    }
    val totalFish = remember(filteredSessions) {
        filteredSessions.sumOf { it.earnedFish }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "📊 数据复盘",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. 周期切换 Tab 胶囊
            item {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )
            }

            // 2. 核心指标统计卡片网格
            item {
                MetricsGrid(
                    totalMinutes = totalMinutes,
                    totalCount = totalCount,
                    totalFish = totalFish
                )
            }

            // 3. 任务标签时间分布环形图
            item {
                TagDistributionCard(sessions = filteredSessions)
            }

            // 4. 最近 7 天打卡热力条
            item {
                WeeklyActivityCard(allSessions = sessions)
            }

            // 5. 历史专注日记时间流标题
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📖 专注心得日记流",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "共 ${filteredSessions.size} 条记录",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            // 6. 日记列表或空态
            if (filteredSessions.isEmpty()) {
                item {
                    EmptyDiaryPlaceholder()
                }
            } else {
                items(filteredSessions) { session ->
                    DiarySessionCard(session = session)
                }
            }
        }
    }
}

/**
 * 周期切换器
 */
@Composable
private fun PeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TimePeriod.values().forEach { period ->
            val isSelected = selectedPeriod == period
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 核心指标卡片
 */
@Composable
private fun MetricsGrid(
    totalMinutes: Int,
    totalCount: Int,
    totalFish: Int
) {
    val hours = totalMinutes / 60
    val remainingMins = totalMinutes % 60
    val timeText = if (hours > 0) "${hours}h ${remainingMins}m" else "${totalMinutes}m"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricCard(
            title = "专注时长",
            value = timeText,
            icon = "⏱️",
            containerColor = Color(0xFF64B5F6).copy(alpha = 0.15f),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "完成番茄",
            value = "$totalCount 个",
            icon = "🍅",
            containerColor = Color(0xFFFF8A65).copy(alpha = 0.15f),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "获得鱼干",
            value = "+$totalFish",
            icon = "🐟",
            containerColor = Color(0xFFFFD54F).copy(alpha = 0.2f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 标签时长占比环形图
 */
@Composable
private fun TagDistributionCard(sessions: List<FocusSessionEntity>) {
    val completedSessions = sessions.filter { it.isCompleted }
    val totalTime = completedSessions.sumOf { it.durationMinutes }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 任务标签占比",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (totalTime == 0) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无专注时长数据 🐾",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                val tagGroup = completedSessions.groupBy { it.tagName }
                val tagStats = tagGroup.map { (name, list) ->
                    val sum = list.sumOf { it.durationMinutes }
                    val percent = (sum.toFloat() / totalTime) * 100f
                    TagStat(name = name, minutes = sum, percent = percent)
                }.sortedByDescending { it.minutes }

                val colors = listOf(
                    Color(0xFF64B5F6), Color(0xFFFFB74D), Color(0xFF81C784),
                    Color(0xFFBA68C8), Color(0xFFFF8A65), Color(0xFF4DB6AC)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 绘制环形饼图
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            var startAngle = -90f
                            tagStats.forEachIndexed { index, stat ->
                                val sweepAngle = (stat.percent / 100f) * 360f
                                val color = colors[index % colors.size]
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = 24f, cap = StrokeCap.Butt)
                                )
                                startAngle += sweepAngle
                            }
                        }
                        Text(
                            text = "${totalTime}m",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 图例列表
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tagStats.take(4).forEachIndexed { index, stat ->
                            val color = colors[index % colors.size]
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Text(
                                    text = stat.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${stat.minutes}m (${stat.percent.toInt()}%)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class TagStat(val name: String, val minutes: Int, val percent: Float)

/**
 * 最近 7 天打卡热力条
 */
@Composable
private fun WeeklyActivityCard(allSessions: List<FocusSessionEntity>) {
    val days = remember(allSessions) {
        val list = mutableListOf<DayActivity>()
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("E", Locale.CHINA)
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

        for (i in 6 downTo 0) {
            val targetCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = targetCal.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000L

            val count = allSessions.count { it.startTime in startOfDay until endOfDay && it.isCompleted }
            val dayName = if (i == 0) "今" else dayFormat.format(targetCal.time).replace("周", "").replace("星期", "")

            list.add(DayActivity(dayName = dayName, dateStr = dateFormat.format(targetCal.time), pomodoroCount = count))
        }
        list
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔥 最近 7 天打卡热力",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 热力方块 (根据番茄数渐变饱和度)
                        val alpha = when {
                            day.pomodoroCount >= 4 -> 1.0f
                            day.pomodoroCount >= 2 -> 0.65f
                            day.pomodoroCount >= 1 -> 0.35f
                            else -> 0.1f
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                                .border(
                                    width = 1.dp,
                                    color = if (day.pomodoroCount > 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.pomodoroCount > 0) {
                                Text(
                                    text = "${day.pomodoroCount}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (alpha > 0.5f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = day.dayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

private data class DayActivity(val dayName: String, val dateStr: String, val pomodoroCount: Int)

/**
 * 历史日记卡片
 */
@Composable
private fun DiarySessionCard(session: FocusSessionEntity) {
    val timeFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val formattedTime = remember(session.startTime) { timeFormat.format(Date(session.startTime)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 标签 Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🏷️ ${session.tagName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 时间戳
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "专注 ${session.durationMinutes} 分钟",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "• 获得 ${session.earnedFish} 🐟",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD35400)
                )

                Spacer(modifier = Modifier.weight(1f))

                // 评分星级
                Row {
                    for (i in 1..5) {
                        Text(
                            text = if (i <= session.rating) "⭐" else "☆",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 心得笔记
            if (session.diaryNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "💬 \"${session.diaryNote}\"",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDiaryPlaceholder() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🐱", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "还没有专注记录喵~",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "完成一个番茄钟，猫咪就会为你记录日记并奉上小鱼干！",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
