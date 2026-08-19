package com.example.pomodorocat.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodorocat.data.SessionType

@Composable
fun TimerDisplay(
    remainingMillis: Long,
    totalMillis: Long,
    sessionType: SessionType,
    modifier: Modifier = Modifier
) {
    // 进度比例 (0.0 到 1.0)
    val progress = if (totalMillis > 0) remainingMillis.toFloat() / totalMillis.toFloat() else 0f
    
    // 平滑进度过渡
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "timer_progress"
    )

    // 格式化时间字符串 (MM:SS)
    val mins = (remainingMillis / 1000) / 60
    val secs = (remainingMillis / 1000) % 60
    val timeString = String.format("%02d:%02d", mins, secs)

    // 主题色配置
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val neutralColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)

    // 缓存渐变 Brush，避免重绘时每帧分配对象
    val gradientBrush = remember(primaryColor, secondaryColor) {
        Brush.sweepGradient(
            colors = listOf(
                secondaryColor,
                primaryColor,
                secondaryColor
            )
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .padding(12.dp)
    ) {
        // 使用 Canvas 绘制精美的环形进度条
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val innerSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // 1. 绘制背景圆环
            drawArc(
                color = neutralColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = innerSize,
                style = Stroke(width = strokeWidth)
            )

            // 2. 绘制前景带颜色圆环 (使用缓存的渐变色)
            drawArc(
                brush = gradientBrush,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = innerSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // 中间倒计时文本与阶段标签
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = sessionType.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = timeString,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
