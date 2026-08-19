package com.example.pomodorocat.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodorocat.data.SessionType
import com.example.pomodorocat.data.TimerPhase
import com.example.pomodorocat.data.db.BadgeEntity
import com.example.pomodorocat.data.db.CatProfileEntity
import com.example.pomodorocat.ui.components.CatCompanion
import com.example.pomodorocat.ui.components.CatSkinSpec

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanctuaryScreen(
    cats: List<CatProfileEntity>,
    badges: List<BadgeEntity>,
    fishBalance: Int,
    onSelectCat: (String) -> Unit,
    onUnlockCat: (String, Int) -> Unit,
    onFeedCat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeCat = remember(cats) { cats.find { it.isActive } ?: cats.firstOrNull() }
    val activeSkinSpec = remember(activeCat) {
        CatSkinSpec.getById(activeCat?.id ?: "orange_tabby")
    }

    var petDialogue by remember { mutableStateOf<String?>(null) }
    var petCount by remember { mutableStateOf(0) }

    val petQuotes = listOf(
        "呼噜呼噜... 摸摸好舒服喵~ 🐾",
        "最喜欢主人了！今天也要元气满满！✨",
        "喵呜~ (用小脑袋蹭了蹭你的手心) 💖",
        "有你一直陪着，本喵觉得特别安心~ 🌿",
        "抓到更多小鱼干了吗？本喵随时准备好专注了！🐟"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "🐱 治愈猫舍",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    // 鱼干余额胶囊
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFD54F).copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🐟", fontSize = 14.sp)
                            Text(
                                text = "$fishBalance",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD35400)
                            )
                        }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. 当前猫咪舞台与互动
            item {
                activeCat?.let { cat ->
                    CatStageCard(
                        cat = cat,
                        skinSpec = activeSkinSpec,
                        customQuote = petDialogue,
                        fishBalance = fishBalance,
                        onPet = {
                            petCount++
                            petDialogue = petQuotes[petCount % petQuotes.size]
                        },
                        onFeed = onFeedCat
                    )
                }
            }

            // 2. 猫咪图鉴选择栏
            item {
                Text(
                    text = "🐾 猫咪图鉴 (${cats.count { it.isUnlocked }} / ${cats.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cats) { cat ->
                        CatProfileCard(
                            cat = cat,
                            fishBalance = fishBalance,
                            onSelect = { onSelectCat(cat.id) },
                            onUnlock = { onUnlockCat(cat.id, cat.unlockCostFish) }
                        )
                    }
                }
            }

            // 3. 喵喵荣誉勋章墙
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "👑 喵喵荣誉墙",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "已点亮 ${badges.count { it.isUnlocked }} / ${badges.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            item {
                BadgeGrid(badges = badges)
            }
        }
    }
}

/**
 * 核心猫咪互动主舞台卡片
 */
@Composable
private fun CatStageCard(
    cat: CatProfileEntity,
    skinSpec: CatSkinSpec,
    customQuote: String?,
    fishBalance: Int,
    onPet: () -> Unit,
    onFeed: () -> Unit
) {
    val levelTitles = mapOf(
        1 to "Lv.1 怯生生",
        2 to "Lv.2 渐熟络",
        3 to "Lv.3 粘人精 (佩戴领结)",
        4 to "Lv.4 撒娇怪 (微光环绕)",
        5 to "Lv.5 挚友之契 (金色守护皇冠)"
    )

    val expThresholds = listOf(30, 90, 180, 300)
    val nextLevelExp = when (cat.bondLevel) {
        1 -> 30
        2 -> 90
        3 -> 180
        4 -> 300
        else -> 300
    }
    val progress = if (cat.bondLevel >= 5) 1.0f else (cat.affectionExp.toFloat() / nextLevelExp).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 猫咪 Canvas 与对话气泡
            CatCompanion(
                sessionType = SessionType.WORK,
                phase = TimerPhase.IDLE,
                skinSpec = skinSpec,
                bondLevel = cat.bondLevel,
                customBubbleText = customQuote ?: cat.quote,
                onPet = onPet,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 亲密度等级与进度条
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = levelTitles[cat.bondLevel] ?: "Lv.${cat.bondLevel}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (cat.bondLevel >= 5) "已达最高亲密度 💖" else "${cat.affectionExp} / ${nextLevelExp} EXP",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 投喂按钮
            Button(
                onClick = onFeed,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🐟", fontSize = 18.sp)
                    Text(
                        text = "投喂 10 鱼干 (+10 亲密度)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * 过程化微缩猫咪头像 (真实渲染各品种的毛色、耳朵、花斑、暹罗面具和眼瞳)
 */
@Composable
private fun MiniCatAvatar(
    skinSpec: CatSkinSpec,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(2.dp, skinSpec.furColor.copy(alpha = 0.6f), CircleShape)
            .padding(4.dp)
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f + 4f

        val furColor = skinSpec.furColor
        val stripeColor = skinSpec.stripeColor
        val eyeColor = skinSpec.eyeColor
        val earInnerColor = skinSpec.earInnerColor

        // 1. 左耳
        val earLeft = Path().apply {
            moveTo(centerX - 18f, centerY - 8f)
            lineTo(centerX - 28f, centerY - 28f)
            lineTo(centerX - 6f, centerY - 18f)
            close()
        }
        drawPath(earLeft, color = if (skinSpec.isCalicoPatches) skinSpec.calicoPatchColor else furColor)
        val innerEarLeft = Path().apply {
            moveTo(centerX - 16f, centerY - 10f)
            lineTo(centerX - 24f, centerY - 24f)
            lineTo(centerX - 8f, centerY - 17f)
            close()
        }
        drawPath(innerEarLeft, color = earInnerColor)

        // 2. 右耳
        val earRight = Path().apply {
            moveTo(centerX + 18f, centerY - 8f)
            lineTo(centerX + 28f, centerY - 28f)
            lineTo(centerX + 6f, centerY - 18f)
            close()
        }
        drawPath(earRight, color = if (skinSpec.isCalicoPatches) stripeColor else furColor)
        val innerEarRight = Path().apply {
            moveTo(centerX + 16f, centerY - 10f)
            lineTo(centerX + 24f, centerY - 24f)
            lineTo(centerX + 8f, centerY - 17f)
            close()
        }
        drawPath(innerEarRight, color = earInnerColor)

        // 3. 头部主轮廓
        drawRoundRect(
            color = furColor,
            topLeft = Offset(centerX - 26f, centerY - 20f),
            size = Size(52f, 40f),
            cornerRadius = CornerRadius(18f, 16f)
        )

        // 额头高光
        drawOval(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(centerX - 14f, centerY - 18f),
            size = Size(28f, 8f)
        )

        // 4. 品种面部特征
        if (skinSpec.isSiameseMask) {
            drawOval(
                color = stripeColor,
                topLeft = Offset(centerX - 14f, centerY - 10f),
                size = Size(28f, 22f)
            )
        } else if (skinSpec.id == "orange_tabby") {
            val stCol = stripeColor.copy(alpha = 0.8f)
            drawRoundRect(color = stCol, topLeft = Offset(centerX - 2f, centerY - 18f), size = Size(4f, 8f), cornerRadius = CornerRadius(2f, 2f))
            drawRoundRect(color = stCol, topLeft = Offset(centerX - 8f, centerY - 17f), size = Size(3f, 6f), cornerRadius = CornerRadius(1.5f, 1.5f))
            drawRoundRect(color = stCol, topLeft = Offset(centerX + 5f, centerY - 17f), size = Size(3f, 6f), cornerRadius = CornerRadius(1.5f, 1.5f))
        } else if (skinSpec.id == "tuxedo") {
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(centerX - 7f, centerY - 6f),
                size = Size(14f, 18f),
                cornerRadius = CornerRadius(7f, 7f)
            )
        }

        // 5. 腮红
        drawCircle(color = Color(0xFFFFB7B2).copy(alpha = 0.75f), radius = 4.5f, center = Offset(centerX - 18f, centerY + 3f))
        drawCircle(color = Color(0xFFFFB7B2).copy(alpha = 0.75f), radius = 4.5f, center = Offset(centerX + 18f, centerY + 3f))

        // 6. 晶莹大眼
        drawOval(color = eyeColor, topLeft = Offset(centerX - 17f, centerY - 7f), size = Size(10f, 11f))
        drawOval(color = eyeColor, topLeft = Offset(centerX + 7f, centerY - 7f), size = Size(10f, 11f))

        drawOval(color = Color(0xFF1A1A1A), topLeft = Offset(centerX - 15f, centerY - 6f), size = Size(6f, 8f))
        drawOval(color = Color(0xFF1A1A1A), topLeft = Offset(centerX + 9f, centerY - 6f), size = Size(6f, 8f))

        // 灵动高光
        drawCircle(Color.White, radius = 2f, center = Offset(centerX - 13f, centerY - 4f))
        drawCircle(Color.White, radius = 2f, center = Offset(centerX + 11f, centerY - 4f))

        // 7. 小鼻子与嘴
        drawCircle(skinSpec.noseColor, radius = 2f, center = Offset(centerX, centerY + 2f))
    }
}

/**
 * 猫咪图鉴卡片
 */
@Composable
private fun CatProfileCard(
    cat: CatProfileEntity,
    fishBalance: Int,
    onSelect: () -> Unit,
    onUnlock: () -> Unit
) {
    val skinSpec = remember(cat.id) { CatSkinSpec.getById(cat.id) }

    val traitTag = when (cat.id) {
        "orange_tabby" -> "🍊 元气干饭王"
        "calico" -> "🎨 软萌小锦鲤"
        "tuxedo" -> "🎩 优雅小警长"
        "siamese" -> "☕ 学霸糊脸包"
        "british_shorthair" -> "💎 贵族暖心宝"
        else -> "🐾 治愈小甜心"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (cat.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .width(150.dp)
            .border(
                width = if (cat.isActive) 2.dp else 1.dp,
                color = if (cat.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 真实过程化微缩猫咪头像
            MiniCatAvatar(skinSpec = skinSpec)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = cat.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 品种个性标签胶囊
            Box(
                modifier = Modifier
                    .padding(top = 3.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = traitTag,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            when {
                cat.isActive -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "陪伴中 ✨",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                cat.isUnlocked -> {
                    OutlinedButton(
                        onClick = onSelect,
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("切换陪伴", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = onUnlock,
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "${cat.unlockCostFish} 🐟 解锁",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 勋章成就墙
 */
@Composable
private fun BadgeGrid(badges: List<BadgeEntity>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            badges.chunked(2).forEach { rowBadges ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowBadges.forEach { badge ->
                        BadgeItemCard(badge = badge, modifier = Modifier.weight(1f))
                    }
                    if (rowBadges.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeItemCard(badge: BadgeEntity, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) Color(0xFFFFD54F).copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badge.isUnlocked) badge.iconEmoji else "🔒",
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    text = badge.description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    lineHeight = 12.sp
                )
            }
        }
    }
}
