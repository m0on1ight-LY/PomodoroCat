package com.example.pomodorocat.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodorocat.data.SessionType
import com.example.pomodorocat.data.TimerPhase

/**
 * 路径复用持有类，避免高频动画下在 DrawScope 中重复创建 Path 导致频繁 GC 掉帧
 */
private class CatPathHolder {
    val tailPath = Path()
    val tailTipPath = Path()
    val fishPath = Path()
    val earLeftPath = Path()
    val innerEarLeftPath = Path()
    val earRightPath = Path()
    val innerEarRightPath = Path()
    val cheekTuftLeftPath = Path()
    val cheekTuftRightPath = Path()
    val eyeLeftPath = Path()
    val eyeRightPath = Path()
    val nosePath = Path()
    val mouthPath = Path()
    val heartPath = Path()
    val bowtieLeftPath = Path()
    val bowtieRightPath = Path()
    val crownPath = Path()
}

/**
 * 旗舰超萌多品种过程化全身猫咪组件：
 * 支持 5 大特色猫咪皮肤（橘猫、三花、奶牛、暹罗、英短）、5 级亲密度装扮（领结、爱心、皇冠光环）、
 * 眨眼、抖耳、摆尾、呼吸起伏与零 GC 路径复用！
 */
@Composable
fun CatCompanion(
    sessionType: SessionType,
    phase: TimerPhase,
    skinSpec: CatSkinSpec = CatSkinSpec.ORANGE_TABBY,
    bondLevel: Int = 1,
    customBubbleText: String? = null,
    onPet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "super_cat_pet")
    val pathHolder = remember { CatPathHolder() }

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

    // 3. 耳朵间歇性抖动
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

    // 6. 浮动爱心/微光动画 (亲密度等级 >= 4)
    val sparkleFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_float"
    )

    // 对话文本状态机
    val defaultBubbleText = when (phase) {
        TimerPhase.IDLE -> when (bondLevel) {
            5 -> "💖 挚友之契：只要和你在一起，每一秒都闪闪发光喵！"
            4 -> "🌟 喵呜~ 翻肚皮求摸摸，摸完我们一起专心搞定任务！"
            3 -> "🎀 今天特意戴上了漂亮的领结，准备陪你大显身手喵~"
            2 -> "✨ 喵呜！我们越来越默契了呢，加油！"
            else -> "喵呜！我是【${skinSpec.name}】，准备好一起专注了吗？"
        }
        TimerPhase.RUNNING -> {
            when (sessionType) {
                SessionType.WORK -> "嘘... 小猫咪正陪你认真看书，不许分心哦！"
                SessionType.SHORT_BREAK -> "棒！休息时间到啦，来陪我玩粉色毛线球吧喵~"
                SessionType.LONG_BREAK -> "长假休息喵~ 猫咪要抱着小鱼干去小垫子上打呼噜了..."
            }
        }
        TimerPhase.PAUSED -> "唔？计时暂停了... 猫咪会缩成小圆球乖乖等你回来的！"
        TimerPhase.FINISHED -> "太厉害啦！又完成了一个番茄钟，奖励一顿丰盛小鱼干！"
    }

    val bubbleText = customBubbleText ?: defaultBubbleText

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 对话气泡 (无框点击交互)
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(18.dp)
                )
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures { onPet() }
                }
        ) {
            Text(
                text = bubbleText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 全身精致猫咪 Canvas (应用 CatSkinSpec 皮肤配色，无矩形波纹)
        val furColor = skinSpec.furColor
        val furShadowColor = skinSpec.furShadowColor
        val bellyColor = skinSpec.bellyColor
        val stripeColor = skinSpec.stripeColor
        val eyeColor = skinSpec.eyeColor
        val earInnerColor = skinSpec.earInnerColor
        val pawColor = skinSpec.pawColor
        val matColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        val matInnerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

        Canvas(
            modifier = Modifier
                .size(205.dp)
                .padding(4.dp)
                .pointerInput(Unit) {
                    detectTapGestures { onPet() }
                }
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f + 14f

            val scale = if (phase == TimerPhase.RUNNING) breathScale else 1.0f
            val adjustedHeight = height * scale
            val shiftY = (height - adjustedHeight) / 2f

            // ================= 0. 底部舒适小坐垫 & 实体投影 =================
            drawOval(
                color = Color.Black.copy(alpha = 0.10f),
                topLeft = Offset(centerX - 72f, centerY + 58f),
                size = Size(144f, 26f)
            )
            drawOval(
                color = matColor,
                topLeft = Offset(centerX - 75f, centerY + 52f),
                size = Size(150f, 32f)
            )
            drawOval(
                color = matInnerColor,
                topLeft = Offset(centerX - 65f, centerY + 56f),
                size = Size(130f, 22f)
            )

            // ================= 1. 蓬松可爱的尾巴 =================
            val tailBaseX = centerX + 48f
            val tailBaseY = centerY + 50f

            withTransform({
                rotate(degrees = tailSway, pivot = Offset(tailBaseX, tailBaseY))
            }) {
                val tailPath = pathHolder.tailPath.apply {
                    reset()
                    moveTo(tailBaseX, tailBaseY)
                    quadraticBezierTo(centerX + 90f, centerY + 15f, centerX + 80f, centerY - 30f)
                    quadraticBezierTo(centerX + 98f, centerY - 28f, centerX + 102f, centerY + 32f)
                    quadraticBezierTo(centerX + 80f, centerY + 68f, tailBaseX, tailBaseY)
                    close()
                }
                drawPath(tailPath, color = furColor)

                // 尾巴外沿光泽线
                drawPath(tailPath, color = Color.White.copy(alpha = 0.15f), style = Stroke(width = 3f))

                // 尾巴尖端白色萌点
                val tailTip = pathHolder.tailTipPath.apply {
                    reset()
                    moveTo(centerX + 80f, centerY - 30f)
                    quadraticBezierTo(centerX + 86f, centerY - 18f, centerX + 92f, centerY - 28f)
                    quadraticBezierTo(centerX + 98f, centerY - 28f, centerX + 102f, centerY - 22f)
                    quadraticBezierTo(centerX + 92f, centerY - 38f, centerX + 80f, centerY - 30f)
                    close()
                }
                drawPath(tailTip, color = if (skinSpec.isSiameseMask) stripeColor else Color.White)
            }

            // ================= 2. 圆滚滚身体 & 肚皮 =================
            drawRoundRect(
                color = furColor,
                topLeft = Offset(centerX - 58f, centerY + 8f + shiftY),
                size = Size(116f, 68f * scale),
                cornerRadius = CornerRadius(42f, 32f)
            )
            // 身体高光弧
            drawOval(
                color = Color.White.copy(alpha = 0.16f),
                topLeft = Offset(centerX - 42f, centerY + 12f + shiftY),
                size = Size(84f, 20f)
            )

            // 肚皮
            drawRoundRect(
                color = bellyColor.copy(alpha = 0.95f),
                topLeft = Offset(centerX - 32f, centerY + 22f + shiftY),
                size = Size(64f, 48f * scale),
                cornerRadius = CornerRadius(28f, 22f)
            )

            // 三花特有身体深色斑
            if (skinSpec.isCalicoPatches) {
                drawCircle(
                    color = skinSpec.calicoPatchColor,
                    radius = 14f,
                    center = Offset(centerX - 38f, centerY + 28f + shiftY)
                )
                drawCircle(
                    color = stripeColor,
                    radius = 11f,
                    center = Offset(centerX + 36f, centerY + 38f + shiftY)
                )
            }

            // ================= 3. 道具 & 萌萌肉垫小爪 =================
            when {
                // 专注工作：小书本 + 爪爪
                phase == TimerPhase.RUNNING && sessionType == SessionType.WORK -> {
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(centerX - 38f, centerY + 32f),
                        size = Size(76f, 28f),
                        cornerRadius = CornerRadius(7f, 7f)
                    )
                    drawRoundRect(
                        color = furShadowColor.copy(alpha = 0.3f),
                        topLeft = Offset(centerX - 38f, centerY + 32f),
                        size = Size(76f, 28f),
                        cornerRadius = CornerRadius(7f, 7f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawLine(Color.Gray.copy(alpha = 0.6f), Offset(centerX, centerY + 32f), Offset(centerX, centerY + 60f), strokeWidth = 3f)
                    drawLine(Color.LightGray, Offset(centerX - 30f, centerY + 40f), Offset(centerX - 8f, centerY + 40f), strokeWidth = 2.5f)
                    drawLine(Color.LightGray, Offset(centerX - 30f, centerY + 48f), Offset(centerX - 14f, centerY + 48f), strokeWidth = 2.5f)
                    drawLine(Color.LightGray, Offset(centerX + 8f, centerY + 40f), Offset(centerX + 30f, centerY + 40f), strokeWidth = 2.5f)
                    drawLine(Color.LightGray, Offset(centerX + 8f, centerY + 48f), Offset(centerX + 24f, centerY + 48f), strokeWidth = 2.5f)

                    // 左右爪搭在书边 (带 3 颗小肉垫肉球)
                    drawCircle(color = if (skinSpec.id == "tuxedo") Color.White else furColor, radius = 10.5f, center = Offset(centerX - 34f, centerY + 34f))
                    drawCircle(color = pawColor, radius = 5.5f, center = Offset(centerX - 34f, centerY + 34f))
                    drawCircle(color = pawColor, radius = 2.2f, center = Offset(centerX - 38f, centerY + 30f))
                    drawCircle(color = pawColor, radius = 2.2f, center = Offset(centerX - 34f, centerY + 28f))
                    drawCircle(color = pawColor, radius = 2.2f, center = Offset(centerX - 30f, centerY + 30f))

                    drawCircle(color = if (skinSpec.id == "tuxedo") Color.White else furColor, radius = 10.5f, center = Offset(centerX + 34f, centerY + 34f))
                    drawCircle(color = pawColor, radius = 5.5f, center = Offset(centerX + 34f, centerY + 34f))
                    drawCircle(color = pawColor, radius = 2.2f, center = Offset(centerX + 30f, centerY + 30f))
                    drawCircle(color = pawColor, radius = 2.2f, center = Offset(centerX + 34f, centerY + 28f))
                    drawCircle(color = pawColor, radius = 2.2f, center = Offset(centerX + 38f, centerY + 30f))
                }

                // 短休：毛线球 + 扑腾小爪
                phase == TimerPhase.RUNNING && sessionType == SessionType.SHORT_BREAK -> {
                    val ballX = centerX
                    val ballY = centerY + 46f
                    drawCircle(color = Color(0xFFFF8B94), radius = 18f, center = Offset(ballX, ballY))
                    drawCircle(color = Color(0xFFFFB7B2), radius = 15f, center = Offset(ballX - 2f, ballY - 2f))
                    drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 4f, center = Offset(ballX - 6f, ballY - 6f))
                    drawLine(color = Color(0xFFFF6F7D), start = Offset(ballX - 12f, ballY - 10f), end = Offset(ballX + 12f, ballY + 10f), strokeWidth = 2.5f)
                    drawLine(color = Color(0xFFFF6F7D), start = Offset(ballX - 10f, ballY + 11f), end = Offset(ballX + 11f, ballY - 10f), strokeWidth = 2.5f)

                    drawCircle(color = if (skinSpec.id == "tuxedo") Color.White else furColor, radius = 10f, center = Offset(centerX - 26f, centerY + 30f + pawPlayOffset))
                    drawCircle(color = pawColor, radius = 5.5f, center = Offset(centerX - 26f, centerY + 30f + pawPlayOffset))
                    drawCircle(color = if (skinSpec.id == "tuxedo") Color.White else furColor, radius = 10f, center = Offset(centerX + 26f, centerY + 30f - pawPlayOffset))
                    drawCircle(color = pawColor, radius = 5.5f, center = Offset(centerX + 26f, centerY + 30f - pawPlayOffset))
                }

                // 其它模式：抱着香香金黄色烤小鱼干
                else -> {
                    val fishPath = pathHolder.fishPath.apply {
                        reset()
                        moveTo(centerX - 22f, centerY + 32f + shiftY)
                        quadraticBezierTo(centerX, centerY + 17f + shiftY, centerX + 20f, centerY + 32f + shiftY)
                        quadraticBezierTo(centerX + 30f, centerY + 32f + shiftY, centerX + 35f, centerY + 23f + shiftY)
                        lineTo(centerX + 35f, centerY + 41f + shiftY)
                        quadraticBezierTo(centerX + 30f, centerY + 32f + shiftY, centerX + 20f, centerY + 32f + shiftY)
                        quadraticBezierTo(centerX, centerY + 46f + shiftY, centerX - 22f, centerY + 32f + shiftY)
                        close()
                    }
                    drawPath(fishPath, color = Color(0xFFFFD54F))
                    drawCircle(color = Color(0xFF5D4037), radius = 2.5f, center = Offset(centerX - 13f, centerY + 31f + shiftY))
                    // 鱼身条纹
                    drawLine(Color(0xFFFFA000), Offset(centerX - 8f, centerY + 28f + shiftY), Offset(centerX - 6f, centerY + 36f + shiftY), strokeWidth = 2f)
                    drawLine(Color(0xFFFFA000), Offset(centerX + 2f, centerY + 27f + shiftY), Offset(centerX + 4f, centerY + 37f + shiftY), strokeWidth = 2f)

                    drawCircle(color = if (skinSpec.id == "tuxedo") Color.White else furColor, radius = 10.5f, center = Offset(centerX - 20f, centerY + 36f + shiftY))
                    drawCircle(color = pawColor, radius = 5.5f, center = Offset(centerX - 20f, centerY + 36f + shiftY))
                    drawCircle(color = if (skinSpec.id == "tuxedo") Color.White else furColor, radius = 10.5f, center = Offset(centerX + 20f, centerY + 36f + shiftY))
                    drawCircle(color = pawColor, radius = 5.5f, center = Offset(centerX + 20f, centerY + 36f + shiftY))
                }
            }

            // ================= 4. 猫咪大脑袋 & 耳朵 =================
            val headCenterY = centerY - 30f + shiftY

            // A. 左大耳
            withTransform({
                rotate(degrees = earTwitch, pivot = Offset(centerX - 48f, headCenterY - 22f))
            }) {
                val earLeft = pathHolder.earLeftPath.apply {
                    reset()
                    moveTo(centerX - 44f, headCenterY - 14f)
                    lineTo(centerX - 72f, headCenterY - 82f)
                    lineTo(centerX - 12f, headCenterY - 40f)
                    close()
                }
                drawPath(earLeft, color = if (skinSpec.isCalicoPatches) skinSpec.calicoPatchColor else furColor)

                val innerEarLeft = pathHolder.innerEarLeftPath.apply {
                    reset()
                    moveTo(centerX - 40f, headCenterY - 18f)
                    lineTo(centerX - 63f, headCenterY - 70f)
                    lineTo(centerX - 16f, headCenterY - 36f)
                    close()
                }
                drawPath(innerEarLeft, color = earInnerColor)
                // 耳尖阴影
                drawCircle(color = Color.White.copy(alpha = 0.35f), radius = 6f, center = Offset(centerX - 66f, headCenterY - 74f))
            }

            // B. 右大耳
            withTransform({
                rotate(degrees = -earTwitch, pivot = Offset(centerX + 48f, headCenterY - 22f))
            }) {
                val earRight = pathHolder.earRightPath.apply {
                    reset()
                    moveTo(centerX + 44f, headCenterY - 14f)
                    lineTo(centerX + 72f, headCenterY - 82f)
                    lineTo(centerX + 12f, headCenterY - 40f)
                    close()
                }
                drawPath(earRight, color = if (skinSpec.isCalicoPatches) stripeColor else furColor)

                val innerEarRight = pathHolder.innerEarRightPath.apply {
                    reset()
                    moveTo(centerX + 38f, headCenterY - 18f)
                    lineTo(centerX + 63f, headCenterY - 70f)
                    lineTo(centerX + 16f, headCenterY - 36f)
                    close()
                }
                drawPath(innerEarRight, color = earInnerColor)
                drawCircle(color = Color.White.copy(alpha = 0.35f), radius = 6f, center = Offset(centerX + 66f, headCenterY - 74f))
            }

            // C. 蓬松主脸蛋 (带顶层立体光泽)
            drawRoundRect(
                color = furColor,
                topLeft = Offset(centerX - 68f, headCenterY - 48f),
                size = Size(136f, 96f),
                cornerRadius = CornerRadius(46f, 40f)
            )
            // 额头天使高光环 (3D 质感大幅提升！)
            drawOval(
                color = Color.White.copy(alpha = 0.22f),
                topLeft = Offset(centerX - 35f, headCenterY - 44f),
                size = Size(70f, 20f)
            )

            // D. 左右腮毛
            val cheekTuftLeft = pathHolder.cheekTuftLeftPath.apply {
                reset()
                moveTo(centerX - 64f, headCenterY - 10f)
                quadraticBezierTo(centerX - 78f, headCenterY - 2f, centerX - 70f, headCenterY + 12f)
                quadraticBezierTo(centerX - 76f, headCenterY + 20f, centerX - 60f, headCenterY + 25f)
                close()
            }
            val cheekTuftRight = pathHolder.cheekTuftRightPath.apply {
                reset()
                moveTo(centerX + 64f, headCenterY - 10f)
                quadraticBezierTo(centerX + 78f, headCenterY - 2f, centerX + 70f, headCenterY + 12f)
                quadraticBezierTo(centerX + 76f, headCenterY + 20f, centerX + 60f, headCenterY + 25f)
                close()
            }
            drawPath(cheekTuftLeft, color = furColor)
            drawPath(cheekTuftRight, color = furColor)

            // E. 品种专属面部纹样
            if (skinSpec.isSiameseMask) {
                drawOval(
                    color = stripeColor,
                    topLeft = Offset(centerX - 36f, headCenterY - 24f),
                    size = Size(72f, 52f)
                )
            } else if (skinSpec.id == "orange_tabby") {
                val stCol = stripeColor.copy(alpha = 0.85f)
                drawRoundRect(color = stCol, topLeft = Offset(centerX - 4f, headCenterY - 44f), size = Size(8f, 16f), cornerRadius = CornerRadius(4f, 4f))
                drawRoundRect(color = stCol, topLeft = Offset(centerX - 18f, headCenterY - 42f), size = Size(6f, 13f), cornerRadius = CornerRadius(3f, 3f))
                drawRoundRect(color = stCol, topLeft = Offset(centerX + 12f, headCenterY - 42f), size = Size(6f, 13f), cornerRadius = CornerRadius(3f, 3f))
            } else if (skinSpec.id == "tuxedo") {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(centerX - 16f, headCenterY - 14f),
                    size = Size(32f, 38f),
                    cornerRadius = CornerRadius(16f, 16f)
                )
            }

            // F. 软萌渐变大腮红 (带微光白心)
            drawCircle(color = Color(0xFFFFB7B2).copy(alpha = 0.85f), radius = 12.5f, center = Offset(centerX - 46f, headCenterY + 10f))
            drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 4f, center = Offset(centerX - 48f, headCenterY + 8f))
            drawCircle(color = Color(0xFFFFB7B2).copy(alpha = 0.85f), radius = 12.5f, center = Offset(centerX + 46f, headCenterY + 10f))
            drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 4f, center = Offset(centerX + 44f, headCenterY + 8f))

            // ================= 5. 面部五官 (晶莹水灵大眼、鼻子、嘴巴、胡须) =================
            when {
                // 专注工作：向下沉静专注眼
                phase == TimerPhase.RUNNING && sessionType == SessionType.WORK -> {
                    val eyeLeft = pathHolder.eyeLeftPath.apply {
                        reset()
                        moveTo(centerX - 38f, headCenterY - 8f)
                        quadraticBezierTo(centerX - 28f, headCenterY + 3f, centerX - 18f, headCenterY - 8f)
                    }
                    val eyeRight = pathHolder.eyeRightPath.apply {
                        reset()
                        moveTo(centerX + 18f, headCenterY - 8f)
                        quadraticBezierTo(centerX + 28f, headCenterY + 3f, centerX + 38f, headCenterY - 8f)
                    }
                    drawPath(eyeLeft, color = Color.White, style = Stroke(width = 4.5f))
                    drawPath(eyeRight, color = Color.White, style = Stroke(width = 4.5f))
                }

                // 暂停：眯眼
                phase == TimerPhase.PAUSED -> {
                    drawLine(Color.White, Offset(centerX - 38f, headCenterY - 6f), Offset(centerX - 18f, headCenterY - 6f), strokeWidth = 4.5f)
                    val eyeRight = pathHolder.eyeRightPath.apply {
                        reset()
                        moveTo(centerX + 18f, headCenterY - 8f)
                        quadraticBezierTo(centerX + 28f, headCenterY + 1f, centerX + 38f, headCenterY - 8f)
                    }
                    drawPath(eyeRight, color = Color.White, style = Stroke(width = 4.5f))
                }

                // 正常/休息：灵动水汪汪大眼睛 (眼瞳晶莹分层)
                else -> {
                    val eyeScaleY = eyeBlink
                    // 1. 虹膜底色
                    drawOval(color = eyeColor, topLeft = Offset(centerX - 42f, headCenterY - 18f * eyeScaleY), size = Size(24f, 26f * eyeScaleY))
                    drawOval(color = eyeColor, topLeft = Offset(centerX + 18f, headCenterY - 18f * eyeScaleY), size = Size(24f, 26f * eyeScaleY))

                    // 2. 虹膜下缘亮泽月牙 (水灵感核心)
                    drawOval(color = Color.White.copy(alpha = 0.35f), topLeft = Offset(centerX - 39f, headCenterY - 6f * eyeScaleY), size = Size(18f, 12f * eyeScaleY))
                    drawOval(color = Color.White.copy(alpha = 0.35f), topLeft = Offset(centerX + 21f, headCenterY - 6f * eyeScaleY), size = Size(18f, 12f * eyeScaleY))

                    // 3. 瞳孔深色内芯
                    drawOval(color = Color(0xFF151515), topLeft = Offset(centerX - 37f, headCenterY - 16f * eyeScaleY), size = Size(14f, 18f * eyeScaleY))
                    drawOval(color = Color(0xFF151515), topLeft = Offset(centerX + 23f, headCenterY - 16f * eyeScaleY), size = Size(14f, 18f * eyeScaleY))

                    // 4. 双重水灵高光白点 (一大一小)
                    if (eyeBlink > 0.5f) {
                        drawCircle(Color.White, radius = 4.5f, center = Offset(centerX - 34f, headCenterY - 11f))
                        drawCircle(Color.White, radius = 2.2f, center = Offset(centerX - 25f, headCenterY - 3f))
                        drawCircle(Color.White, radius = 4.5f, center = Offset(centerX + 26f, headCenterY - 11f))
                        drawCircle(Color.White, radius = 2.2f, center = Offset(centerX + 35f, headCenterY - 3f))
                    }
                }
            }

            // 粉嫩倒三角鼻子 (带小高光)
            val nose = pathHolder.nosePath.apply {
                reset()
                moveTo(centerX, headCenterY + 5f)
                lineTo(centerX - 5f, headCenterY - 1f)
                lineTo(centerX + 5f, headCenterY - 1f)
                close()
            }
            drawPath(nose, color = skinSpec.noseColor)
            drawCircle(Color.White.copy(alpha = 0.6f), radius = 1.5f, center = Offset(centerX - 1.5f, headCenterY))

            // 三瓣小萌嘴
            val mouth = pathHolder.mouthPath.apply {
                reset()
                moveTo(centerX - 10f, headCenterY + 9f)
                quadraticBezierTo(centerX - 5f, headCenterY + 15f, centerX, headCenterY + 8f)
                quadraticBezierTo(centerX + 5f, headCenterY + 15f, centerX + 10f, headCenterY + 9f)
            }
            drawPath(mouth, color = Color(0xFF5D4037), style = Stroke(width = 2.5f))

            // 灵动胡须
            val whiskerColor = Color(0xFF424242).copy(alpha = 0.50f)
            drawLine(whiskerColor, Offset(centerX - 46f, headCenterY + 2f), Offset(centerX - 74f, headCenterY - 4f), strokeWidth = 2f)
            drawLine(whiskerColor, Offset(centerX - 46f, headCenterY + 8f), Offset(centerX - 76f, headCenterY + 9f), strokeWidth = 2f)
            drawLine(whiskerColor, Offset(centerX + 46f, headCenterY + 2f), Offset(centerX + 74f, headCenterY - 4f), strokeWidth = 2f)
            drawLine(whiskerColor, Offset(centerX + 46f, headCenterY + 8f), Offset(centerX + 76f, headCenterY + 9f), strokeWidth = 2f)

            // ================= 6. 亲密度装扮 (Bowtie & Crown & Sparkles) =================
            if (bondLevel >= 3) {
                val bowtieY = headCenterY + 44f
                val bowtieLeft = pathHolder.bowtieLeftPath.apply {
                    reset()
                    moveTo(centerX, bowtieY)
                    lineTo(centerX - 16f, bowtieY - 8f)
                    lineTo(centerX - 16f, bowtieY + 8f)
                    close()
                }
                val bowtieRight = pathHolder.bowtieRightPath.apply {
                    reset()
                    moveTo(centerX, bowtieY)
                    lineTo(centerX + 16f, bowtieY - 8f)
                    lineTo(centerX + 16f, bowtieY + 8f)
                    close()
                }
                drawPath(bowtieLeft, color = Color(0xFFE53935))
                drawPath(bowtieRight, color = Color(0xFFE53935))
                drawCircle(color = Color(0xFFFFD54F), radius = 4.5f, center = Offset(centerX, bowtieY))
            }

            if (bondLevel >= 4) {
                drawCircle(
                    color = Color(0xFFFF4081).copy(alpha = 0.85f),
                    radius = 5.5f,
                    center = Offset(centerX + 48f, headCenterY - 40f + sparkleFloat)
                )
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                    radius = 4f,
                    center = Offset(centerX - 48f, headCenterY - 35f - sparkleFloat)
                )
            }

            if (bondLevel >= 5) {
                val crownBaseY = headCenterY - 50f
                val crownPath = pathHolder.crownPath.apply {
                    reset()
                    moveTo(centerX - 18f, crownBaseY)
                    lineTo(centerX - 24f, crownBaseY - 16f)
                    lineTo(centerX - 8f, crownBaseY - 8f)
                    lineTo(centerX, crownBaseY - 20f)
                    lineTo(centerX + 8f, crownBaseY - 8f)
                    lineTo(centerX + 24f, crownBaseY - 16f)
                    lineTo(centerX + 18f, crownBaseY)
                    close()
                }
                drawPath(crownPath, color = Color(0xFFFFD54F))
                drawCircle(color = Color(0xFFE53935), radius = 2.5f, center = Offset(centerX, crownBaseY - 18f))
            }
        }
    }
}
