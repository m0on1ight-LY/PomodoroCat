package com.example.pomodorocat.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 各主题配色方案组合
private val PinkColorScheme = lightColorScheme(
    primary = PinkPrimary,
    secondary = PinkSecondary,
    tertiary = PinkTertiary,
    background = PinkBackground,
    surface = PinkSurface,
    onPrimary = TextDark,
    onSecondary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark
)

private val GreenColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    tertiary = GreenTertiary,
    background = GreenBackground,
    surface = GreenSurface,
    onPrimary = TextDark,
    onSecondary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark
)

private val YellowColorScheme = lightColorScheme(
    primary = YellowPrimary,
    secondary = YellowSecondary,
    tertiary = YellowTertiary,
    background = YellowBackground,
    surface = YellowSurface,
    onPrimary = TextDark,
    onSecondary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark
)

// 补充：这里将占位符值替换为真正的颜色
private val border_color_placeholder_to_argb_text = androidx.compose.ui.graphics.Color(0xFFECEFF1)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onBackground = border_color_placeholder_to_argb_text, // 会映射到淡色文本
    onSurface = border_color_placeholder_to_argb_text
)

@Composable
fun PomodoroCatTheme(
    themeIndex: Int = 0, // 0: 粉萌, 1: 森绿, 2: 向日黄
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        val primaryColor = when (themeIndex) {
            1 -> GreenSecondary
            2 -> YellowSecondary
            else -> PinkSecondary
        }
        darkColorScheme(
            primary = primaryColor,
            secondary = primaryColor.copy(alpha = 0.7f),
            background = DarkBackground,
            surface = DarkSurface,
            onPrimary = DarkBackground,
            onSecondary = DarkBackground,
            onBackground = border_color_placeholder_to_argb_text,
            onSurface = border_color_placeholder_to_argb_text
        )
    } else {
        when (themeIndex) {
            1 -> GreenColorScheme
            2 -> YellowColorScheme
            else -> PinkColorScheme
        }
    }

    LocalViewContextForStatusbarOrHeader(colorScheme, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
private fun LocalViewContextForStatusbarOrHeader(colorScheme: ColorScheme, darkTheme: Boolean): android.view.View {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    return view
}
