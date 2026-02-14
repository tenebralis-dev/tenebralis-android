package com.tenebralis.dreamos.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// DreamOS 浅色配色方案（PRD §12.1：柔蓝 + 白色系背景）
private val DreamLightColorScheme = lightColorScheme(
    primary = DreamBlue,
    onPrimary = DreamOnPrimary,
    primaryContainer = DreamBlueLight,
    onPrimaryContainer = DreamOnPrimaryDark,
    surface = DreamSurface,
    onSurface = DreamOnPrimaryDark,
    background = DreamBackground,
    onBackground = DreamOnPrimaryDark,
    outline = DreamOutline,
    error = DreamError,
)

// DreamOS 深色配色方案
private val DreamDarkColorScheme = darkColorScheme(
    primary = DreamBlueDark,
    onPrimary = DreamOnPrimaryDark,
    primaryContainer = DreamContainerDark,
    surface = DreamSurfaceDark,
    background = DreamBackgroundDark,
    error = DreamError,
)

/**
 * DreamOS 主题
 *
 * 当前为最小占位版本。后续 B3 阶段将接入 [UiConfig]（来自 user_settings.ui_config），
 * 支持用户自选主色、字体、壁纸等动态配置。
 *
 * @param darkTheme 是否使用深色主题，默认跟随系统
 * @param content 主题内的 Composable 内容
 */
@Composable
fun DreamOsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DreamDarkColorScheme else DreamLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DreamTypography,
        shapes = DreamShapes,
        content = content
    )
}
