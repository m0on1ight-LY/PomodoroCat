package com.example.pomodorocat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pomodorocat.data.SettlementData

@Composable
fun SettlementDialog(
    data: SettlementData,
    onDismiss: () -> Unit,
    onSaveDiary: (rating: Int, diaryNote: String) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var diaryNote by remember { mutableStateOf("") }

    val ratingLabels = mapOf(
        1 to "稍微分心了 💦",
        2 to "还算投入 🌿",
        3 to "状态良好 ✨",
        4 to "高度专注 🚀",
        5 to "心流巅峰 🌟"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 专注大丰收！",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 鱼干收获大胶囊
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFD54F).copy(alpha = 0.25f))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🐟", fontSize = 26.sp)
                        Text(
                            text = "+${data.earnedFish} 小鱼干",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD35400)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "本轮专注【${data.tagName}】${data.durationMinutes} 分钟，猫咪为你抓回了满满的鱼干奖励喵！",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 评星栏
                Text(
                    text = "本次专注体验：${ratingLabels[rating]}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (star in 1..5) {
                        IconButton(
                            onClick = { rating = star },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = "$star 星",
                                tint = if (star <= rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 心得输入框
                OutlinedTextField(
                    value = diaryNote,
                    onValueChange = { diaryNote = it },
                    label = { Text("写下专注心得或收获 (可选)...") },
                    placeholder = { Text("例如：完成了第3章代码，猫咪很乖~") },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 确认按钮
                Button(
                    onClick = {
                        onSaveDiary(rating, diaryNote)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "收下鱼干并存入日记 🐾",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
