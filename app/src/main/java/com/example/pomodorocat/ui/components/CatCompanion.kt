package com.example.pomodorocat.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodorocat.data.SessionType
import com.example.pomodorocat.data.TimerPhase

/**
 * 旗舰超萌全身猫咪组件：
 * 包含萌系毛茸茸脸颊、额头小花纹、高光水汪汪大眼、粉嫩小肉垫、悠闲坐垫、
 * 以及眨眼、抖耳、摆尾、呼吸、拨弄毛线球等全套灵动骨骼动画！
 */
@Composable
fun CatCompanion(
    sessionType: SessionType,
    phase: TimerPhase,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "super_cat_pet")

    // 1. 身体呼吸起伏动效
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // 2. 尾巴灵动摇摆
    val tailSway by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tail_sway"
    )

    // 3. 耳朵间歇性抖动 (更加灵敏自然)
    val earTwitch by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3600
                0f at 0
                0f at 3000
                9f at 3150
                -4f at 3300
                5f at 3450
                0f at 3600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "ear_twitch"
    )

    // 4. 眨眼动效
    val eyeBlink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                1f at 0
                1f at 2750
                0.05f at 2850
                1f at 2950
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "eye_blink"
    )

    // 5. 扑腾毛线球爪子摆动
    val pawPlayOffset by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "paw_play"
    )

    // 6. 浮动爱心/星星微动效（休息或完成时）
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    // 对话文本状态机
    val bubbleText = when (phase) {
        TimerPhase.IDLE -> "喵呜！我是你的专属番茄猫，准备好一起专注了吗？"
        TimerPhase.RUNNING -> {
            when (sessionType) {
                SessionType.WORK -> "嘘... 小猫咪正陪你认真看书，不许分心哦！"
                SessionType.SHORT_BREAK -> "棒！休息时间到啦，来陪我玩粉色毛线球吧喵~"
                SessionType.LONG_BREAK -> "长假休息喵~ 猫咪要抱着小鱼干去小垫子上打呼噜了..."
            }
        }
        TimerPhase.PAUSED -> "唔？计时暂停了... 猫咪会缩成小圆球乖乖等你回来的！"
        TimerPhase.FINISHED -> "太厉害啦！又完成了一个番茄钟，奖励一条大鱼干！"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 对话气泡
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(18.dp)
                )
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(
                text = bubbleText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 全身精致猫咪 Canvas
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary

        Canvas(
            modifier = Modifier
                .size(205.dp)
                .padding(4.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f + 14f

            val scale = if (phase == TimerPhase.RUNNING) breathScale else 1.0f
            val adjustedHeight = height * scale
            val shiftY = (height - adjustedHeight) / 2f

            // ================= 0. 底部舒适小坐垫 (使猫咪不再悬空) =================
            val matColor = secondaryColor.copy(alpha = 0.45f)
            drawOval(
                color = matColor,
                topLeft = Offset(centerX - 75f, centerY + 52f),
                size = Size(150f, 32f)
            )
            drawOval(
                color = primaryColor.copy(alpha = 0.25f),
                topLeft = Offset(centerX - 65f, centerY + 56f),
                size = Size(130f, 22f)
            )

            // ================= 1. 蓬松可爱的蓬蓬大尾巴 =================
            val tailBaseX = centerX + 48f
            val tailBaseY = centerY + 50f

            withTransform({
                rotate(degrees = tailSway, pivot = Offset(tailBaseX, tailBaseY))
            }) {
                // 主尾巴路径 (更圆润饱满)
                val tailPath = Path().apply {
                    moveTo(tailBaseX, tailBaseY)
                    quadraticBezierTo(centerX + 90f, centerY + 15f, centerX + 80f, centerY - 30f)
                    quadraticBezierTo(centerX + 98f, centerY - 28f, centerX + 102f, centerY + 32f)
                    quadraticBezierTo(centerX + 80f, centerY + 68f, tailBaseX, tailBaseY)
                    close()
                }
                drawPath(tailPath, color = primaryColor)

                // 尾巴尖端白色萌点
                val tailTip = Path().apply {
                    moveTo(centerX + 80f, centerY - 30f)
                    quadraticBezierTo(centerX + 86f, centerY - 18f, centerX + 92f, centerY - 28f)
                    quadraticBezierTo(centerX + 98f, centerY - 28f, centerX + 102f, centerY - 22f)
                    quadraticBezierTo(centerX + 92f, centerY - 38f, centerX + 80f, centerY - 30f)
                    close()
                }
                drawPath(tailTip, color = Color.White)
            }

            // ================= 2. 圆滚滚猫咪身体 & 白软肚皮 =================
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(centerX - 58f, centerY + 8f + shiftY),
                size = Size(116f, 68f * scale),
                cornerRadius = CornerRadius(42f, 32f)
            )
            // 爱心形/大椭圆白肚皮
            drawRoundRect(
                color = Color.White.copy(alpha = 0.92f),
                topLeft = Offset(centerX - 32f, centerY + 22f + shiftY),
                size = Size(64f, 48f * scale),
                cornerRadius = CornerRadius(28f, 22f)
            )

            // ================= 3. 道具 & 萌萌猫爪 (带粉嫩肉垫 🐾) =================
            when {
                // 专注工作：小课本 + 小爪爪
                phase == TimerPhase.RUNNING && sessionType == SessionType.WORK -> {
                    // 精美书本
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(centerX - 38f, centerY + 32f),
                        size = Size(76f, 28f),
                        cornerRadius = CornerRadius(7f, 7f)
                    )
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.3f),
                        topLeft = Offset(centerX - 38f, centerY + 32f),
                        size = Size(76f, 28f),
                        cornerRadius = CornerRadius(7f, 7f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // 书中缝与页码装饰
                    drawLine(Color.Gray.copy(alpha = 0.6f), Offset(centerX, centerY + 32f), Offset(centerX, centerY + 60f), strokeWidth = 3f)
                    drawLine(Color.LightGray, Offset(centerX - 30f, centerY + 40f), Offset(centerX - 8f, centerY + 40f), strokeWidth = 2.5f)
                    drawLine(Color.LightGray, Offset(centerX - 30f, centerY + 48f), Offset(centerX - 14f, centerY + 48f), strokeWidth = 2.5f)
                    drawLine(Color.LightGray, Offset(centerX + 8f, centerY + 40f), Offset(centerX + 30f, centerY + 40f), strokeWidth = 2.5f)
                    drawLine(Color.LightGray, Offset(centerX + 8f, centerY + 48f), Offset(centerX + 24f, centerY + 48f), strokeWidth = 2.5f)

                    // 左右爪爪搭在书边 (粉色小肉垫 🐾)
                    drawCircle(color = primaryColor, radius = 10f, center = Offset(centerX - 34f, centerY + 34f))
                    drawCircle(color = Color(0xFFFFB7B2), radius = 5.5f, center = Offset(centerX - 34f, centerY + 34f))
                    drawCircle(color = primaryColor, radius = 10f, center = Offset(centerX + 34f, centerY + 34f))
                    drawCircle(color = Color(0xFFFFB7B2), radius = 5.5f, center = Offset(centerX + 34f, centerY + 34f))
                }

                // 短休：毛线球 + 扑腾小爪
                phase == TimerPhase.RUNNING && sessionType == SessionType.SHORT_BREAK -> {
                    val ballX = centerX
                    val ballY = centerY + 46f
                    // 渐变球体
                    drawCircle(color = Color(0xFFFFB7B2), radius = 17f, center = Offset(ballX, ballY))
                    drawCircle(color = Color(0xFFFF8B94), radius = 17f, center = Offset(ballX, ballY), style = Stroke(width = 3f))
                    // 纹理线
                    drawLine(color = Color(0xFFFF8B94), start = Offset(ballX - 12f, ballY - 10f), end = Offset(ballX + 12f, ballY + 10f), strokeWidth = 2.5f)
                    drawLine(color = Color(0xFFFF8B94), start = Offset(ballX - 10f, ballY + 11f), end = Offset(ballX + 11f, ballY - 10f), strokeWidth = 2.5f)

                    // 扑腾爪爪
                    drawCircle(color = primaryColor, radius = 10f, center = Offset(centerX - 26f, centerY + 30f + pawPlayOffset))
                    drawCircle(color = Color(0xFFFFB7B2), radius = 5.5f, center = Offset(centerX - 26f, centerY + 30f + pawPlayOffset))
                    drawCircle(color = primaryColor, radius = 10f, center = Offset(centerX + 26f, centerY + 30f - pawPlayOffset))
                    drawCircle(color = Color(0xFFFFB7B2), radius = 5.5f, center = Offset(centerX + 26f, centerY + 30f - pawPlayOffset))
                }

                // 其它模式：抱着金灿灿香香小鱼干
                else -> {
                    val fishPath = Path().apply {
                        moveTo(centerX - 22f, centerY + 32f + shiftY)
                        quadraticBezierTo(centerX, centerY + 17f + shiftY, centerX + 20f, centerY + 32f + shiftY)
                        quadraticBezierTo(centerX + 30f, centerY + 32f + shiftY, centerX + 35f, centerY + 23f + shiftY)
                        lineTo(centerX + 35f, centerY + 41f + shiftY)
                        quadraticBezierTo(centerX + 30f, centerY + 32f + shiftY, centerX + 20f, centerY + 32f + shiftY)
                        quadraticBezierTo(centerX, centerY + 46f + shiftY, centerX - 22f, centerY + 32f + shiftY)
                        close()
                    }
                    drawPath(fishPath, color = Color(0xFFFFD54F)) // 金黄色烤鱼干
                    drawCircle(color = Color(0xFF5D4037), radius = 2.5f, center = Offset(centerX - 13f, centerY + 31f + shiftY))

                    // 抱紧小鱼干的小黑粉爪
                    drawCircle(color = primaryColor, radius = 10f, center = Offset(centerX - 20f, centerY + 36f + shiftY))
                    drawCircle(color = Color(0xFFFFB7B2), radius = 5.5f, center = Offset(centerX - 20f, centerY + 36f + shiftY))
                    drawCircle(color = primaryColor, radius = 10f, center = Offset(centerX + 20f, centerY + 36f + shiftY))
                    drawCircle(color = Color(0xFFFFB7B2), radius = 5.5f, center = Offset(centerX + 20f, centerY + 36f + shiftY))
                }
            }

            // ================= 4. 猫咪大脑袋 & 蓬松脸颊毛 & 额头花纹 =================
            val headCenterY = centerY - 30f + shiftY

            // A. 左大耳朵 (带抖动 & 双层毛发)
            withTransform({
                rotate(degrees = earTwitch, pivot = Offset(centerX - 48f, headCenterY - 22f))
            }) {
                val earLeft = Path().apply {
                    moveTo(centerX - 44f, headCenterY - 14f)
                    lineTo(centerX - 72f, headCenterY - 82f)
                    lineTo(centerX - 12f, headCenterY - 40f)
                    close()
                }
                drawPath(earLeft, color = primaryColor)
                val innerEarLeft = Path().apply {
                    moveTo(centerX - 40f, headCenterY - 18f)
                    lineTo(centerX - 63f, headCenterY - 70f)
                    lineTo(centerX - 16f, headCenterY - 36f)
                    close()
                }
                drawPath(innerEarLeft, color = Color(0xFFFFB7B2))
            }

            // B. 右大耳朵 (带抖动 & 双层毛发)
            withTransform({
                rotate(degrees = -earTwitch, pivot = Offset(centerX + 48f, headCenterY - 22f))
            }) {
                val earRight = Path().apply {
                    moveTo(centerX + 44f, headCenterY - 14f)
                    lineTo(centerX + 72f, headCenterY - 82f)
                    lineTo(centerX + 12f, headCenterY - 40f)
                    close()
                }
                drawPath(earRight, color = primaryColor)
                val innerEarRight = Path().apply {
                    moveTo(centerX + 38f, headCenterY - 18f)
                    lineTo(centerX + 63f, headCenterY - 70f)
                    lineTo(centerX + 16f, headCenterY - 36f)
                    close()
                }
                drawPath(innerEarRight, color = Color(0xFFFFB7B2))
            }

            // C. 蓬松主脸蛋
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(centerX - 68f, headCenterY - 48f),
                size = Size(136f, 96f),
                cornerRadius = CornerRadius(46f, 40f)
            )

            // D. 左右两侧蓬松的腮毛 (脸颊两侧的毛茸茸小弧线，超级加分！)
            val cheekTuftLeft = Path().apply {
                moveTo(centerX - 64f, headCenterY - 10f)
                quadraticBezierTo(centerX - 78f, headCenterY - 2f, centerX - 70f, headCenterY + 12f)
                quadraticBezierTo(centerX - 76f, headCenterY + 20f, centerX - 60f, headCenterY + 25f)
                close()
            }
            val cheekTuftRight = Path().apply {
                moveTo(centerX + 64f, headCenterY - 10f)
                quadraticBezierTo(centerX + 78f, headCenterY - 2f, centerX + 70f, headCenterY + 12f)
                quadraticBezierTo(centerX + 76f, headCenterY + 20f, centerX + 60f, headCenterY + 25f)
                close()
            }
            drawPath(cheekTuftLeft, color = primaryColor)
            drawPath(cheekTuftRight, color = primaryColor)

            // E. 额头可爱斑纹 (小虎斑纹理，增加细节)
            val stripeColor = primaryColor.copy(alpha = 0.55f)
            drawRoundRect(color = stripeColor, topLeft = Offset(centerX - 4f, headCenterY - 44f), size = Size(8f, 16f), cornerRadius = CornerRadius(4f, 4f))
            drawRoundRect(color = stripeColor, topLeft = Offset(centerX - 18f, headCenterY - 42f), size = Size(6f, 13f), cornerRadius = CornerRadius(3f, 3f))
            drawRoundRect(color = stripeColor, topLeft = Offset(centerX + 12f, headCenterY - 42f), size = Size(6f, 13f), cornerRadius = CornerRadius(3f, 3f))

            // F. 软萌大腮红 (带微光)
            drawCircle(
                color = Color(0xFFFFB7B2).copy(alpha = 0.85f),
                radius = 12.5f,
                center = Offset(centerX - 46f, headCenterY + 10f)
            )
            drawCircle(
                color = Color(0xFFFFB7B2).copy(alpha = 0.85f),
                radius = 12.5f,
                center = Offset(centerX + 46f, headCenterY + 10f)
            )

            // ================= 6. 面部表情 (高光大水眼/眯眼笑/鼻子/吐舌头) =================
            when {
                // A. 专注工作：小憩沉静专注眼 (弯曲向下细弧线 + 睫毛)
                phase == TimerPhase.RUNNING && sessionType == SessionType.WORK -> {
                    val eyeLeft = Path().apply {
                        moveTo(centerX - 40f, headCenterY - 10f)
                        quadraticBezierTo(centerX - 29f, headCenterY - 1f, centerX - 18f, headCenterY - 10f)
                    }
                    val eyeRight = Path().apply {
                        moveTo(centerX + 18f, headCenterY - 10f)
                        quadraticBezierTo(centerX + 29f, headCenterY - 1f, centerX + 38f, headCenterY - 10f)
                    }
                    drawPath(eyeLeft, color = Color.White, style = Stroke(width = 4.5f))
                    drawPath(eyeRight, color = Color.White, style = Stroke(width = 4.5f))
                }

                // B. 暂停：打哈欠眯眼
                phase == TimerPhase.PAUSED -> {
                    drawLine(Color.White, Offset(centerX - 40f, headCenterY - 6f), Offset(centerX - 20f, headCenterY - 6f), strokeWidth = 5f)
                    val eyeRight = Path().apply {
                        moveTo(centerX + 18f, headCenterY - 8f)
                        quadraticBezierTo(centerX + 29f, headCenterY - 1f, centerX + 40f, headCenterY - 8f)
                    }
                    drawPath(eyeRight, color = Color.White, style = Stroke(width = 4.5f))
                }

                // C. 休息/完成：开心弯月笑眼
                (phase == TimerPhase.RUNNING && (sessionType == SessionType.SHORT_BREAK || sessionType == SessionType.LONG_BREAK))
                        || phase == TimerPhase.FINISHED -> {
                    val eyeLeft = Path().apply {
                        moveTo(centerX - 40f, headCenterY - 6f)
                        quadraticBezierTo(centerX - 29f, headCenterY - 18f, centerX - 18f, headCenterY - 6f)
                    }
                    val eyeRight = Path().apply {
                        moveTo(centerX + 18f, headCenterY - 6f)
                        quadraticBezierTo(centerX + 29f, headCenterY - 18f, centerX + 38f, headCenterY - 6f)
                    }
                    drawPath(eyeLeft, color = Color.White, style = Stroke(width = 4.8f))
                    drawPath(eyeRight, color = Color.White, style = Stroke(width = 4.8f))
                }

                // D. 空闲模式：超级水汪汪灵动大眼睛 (包含高光与瞳孔星星！)
                else -> {
                    val blink = eyeBlink
                    // 左眼大眼白
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(centerX - 41f, headCenterY - (14f + 9f * blink)),
                        size = Size(20f, 20f * blink)
                    )
                    // 左眼黑瞳孔
                    drawCircle(
                        color = Color(0xFF2C3E50),
                        radius = 6.5f * blink,
                        center = Offset(centerX - 31f, headCenterY - 4f)
                    )
                    // 左眼高光星点 (让眼睛会说话的水灵关键点！)
                    drawCircle(
                        color = Color.White,
                        radius = 2.5f * blink,
                        center = Offset(centerX - 33.5f, headCenterY - 6.5f)
                    )

                    // 右眼大眼白
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(centerX + 21f, headCenterY - (14f + 9f * blink)),
                        size = Size(20f, 20f * blink)
                    )
                    // 右眼黑瞳孔
                    drawCircle(
                        color = Color(0xFF2C3E50),
                        radius = 6.5f * blink,
                        center = Offset(centerX + 31f, headCenterY - 4f)
                    )
                    // 右眼高光星点
                    drawCircle(
                        color = Color.White,
                        radius = 2.5f * blink,
                        center = Offset(centerX + 28.5f, headCenterY - 6.5f)
                    )
                }
            }

            // 倒三角粉萌小鼻子
            val nose = Path().apply {
                moveTo(centerX, headCenterY + 5f)
                lineTo(centerX - 5.5f, headCenterY)
                lineTo(centerX + 5.5f, headCenterY)
                close()
            }
            drawPath(nose, color = Color(0xFFFFB7B2))

            // 倒 3 嘴巴 (与露出的小粉舌头 👅)
            if (phase == TimerPhase.PAUSED) {
                // 打哈欠的大嘴
                drawCircle(color = Color(0xFFE67E22), radius = 8f, center = Offset(centerX, headCenterY + 13f))
                drawCircle(color = Color(0xFFFF8B94), radius = 4f, center = Offset(centerX, headCenterY + 15f))
            } else {
                val mouth = Path().apply {
                    moveTo(centerX - 9.5f, headCenterY + 8.5f)
                    quadraticBezierTo(centerX - 4.8f, headCenterY + 13.5f, centerX, headCenterY + 8.5f)
                    quadraticBezierTo(centerX + 4.8f, headCenterY + 13.5f, centerX + 9.5f, headCenterY + 8.5f)
                }
                drawPath(mouth, color = Color.White, style = Stroke(width = 3.6f))
                
                // 完成或休息时，露出半颗小粉舌头！
                if (phase == TimerPhase.FINISHED || sessionType != SessionType.WORK) {
                    drawCircle(color = Color(0xFFFF8B94), radius = 3.5f, center = Offset(centerX, headCenterY + 12.5f))
                }
            }

            // 胡须 (白色柔和长须)
            drawLine(Color.White, Offset(centerX - 64f, headCenterY + 7f), Offset(centerX - 88f, headCenterY + 4f), strokeWidth = 3f)
            drawLine(Color.White, Offset(centerX - 64f, headCenterY + 15f), Offset(centerX - 91f, headCenterY + 17f), strokeWidth = 3f)
            drawLine(Color.White, Offset(centerX + 64f, headCenterY + 7f), Offset(centerX + 88f, headCenterY + 4f), strokeWidth = 3f)
            drawLine(Color.White, Offset(centerX + 64f, headCenterY + 15f), Offset(centerX + 91f, headCenterY + 17f), strokeWidth = 3f)

            // ================= 7. 顶部悬浮萌趣元素 (休息/完成时浮动爱心与小星星) =================
            if (phase == TimerPhase.FINISHED || phase == TimerPhase.RUNNING && sessionType == SessionType.SHORT_BREAK) {
                val heartY = headCenterY - 70f + floatY
                // 小爱心
                val heartPath = Path().apply {
                    moveTo(centerX - 35f, heartY)
                    cubicTo(centerX - 45f, heartY - 12f, centerX - 55f, heartY + 4f, centerX - 35f, heartY + 16f)
                    cubicTo(centerX - 15f, heartY + 4f, centerX - 25f, heartY - 12f, centerX - 35f, heartY)
                    close()
                }
                drawPath(heartPath, color = Color(0xFFFF8B94))

                // 小金星
                drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(centerX + 40f, heartY + 5f))
                drawCircle(color = Color(0xFFFFD54F), radius = 2.5f, center = Offset(centerX + 50f, heartY - 6f))
            }
        }
    }
}
