package com.example.pomodorocat.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

object TagIconHelper {
    val availableIcons = mapOf(
        "Computer" to Icons.Rounded.Computer,
        "School" to Icons.Rounded.School,
        "MenuBook" to Icons.Rounded.MenuBook,
        "Terminal" to Icons.Rounded.Terminal,
        "FitnessCenter" to Icons.Rounded.FitnessCenter,
        "SelfImprovement" to Icons.Rounded.SelfImprovement,
        "Brush" to Icons.Rounded.Brush,
        "MusicNote" to Icons.Rounded.MusicNote,
        "Lightbulb" to Icons.Rounded.Lightbulb,
        "VideogameAsset" to Icons.Rounded.VideogameAsset
    )

    fun getIcon(key: String): ImageVector {
        return availableIcons[key] ?: Icons.Rounded.Tag
    }
}
