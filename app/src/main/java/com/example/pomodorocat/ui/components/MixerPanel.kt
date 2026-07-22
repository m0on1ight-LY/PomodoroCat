package com.example.pomodorocat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Nature
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodorocat.data.MixerSettings

@Composable
fun MixerPanel(
    settings: MixerSettings,
    onSettingsChanged: (MixerSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "🏕️ 白噪音混音大师",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 音道 1: 雨声
            MixerRow(
                icon = Icons.Rounded.WaterDrop,
                title = "窗外雨声",
                volume = settings.rainVolume,
                onVolumeChange = { onSettingsChanged(settings.copy(rainVolume = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 音道 2: 篝火
            MixerRow(
                icon = Icons.Rounded.Whatshot,
                title = "红泥篝火",
                volume = settings.campfireVolume,
                onVolumeChange = { onSettingsChanged(settings.copy(campfireVolume = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 音道 3: 海浪
            MixerRow(
                icon = Icons.Rounded.Waves,
                title = "沙滩海浪",
                volume = settings.oceanVolume,
                onVolumeChange = { onSettingsChanged(settings.copy(oceanVolume = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 音道 4: 森林鸟鸣
            MixerRow(
                icon = Icons.Rounded.Nature,
                title = "清晨森林",
                volume = settings.forestVolume,
                onVolumeChange = { onSettingsChanged(settings.copy(forestVolume = it)) }
            )
        }
    }
}

@Composable
private fun MixerRow(
    icon: ImageVector,
    title: String,
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (volume > 0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(volume * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
                modifier = Modifier.height(28.dp)
            )
        }
    }
}
