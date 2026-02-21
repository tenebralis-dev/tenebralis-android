package com.tenebralis.dreamos.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontFamily
import com.tenebralis.dreamos.data.repository.FontManager

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
 * 接入 FontManager 实现字体热切换：
 * - displayFont：全局显示字体
 * - codeFont：代码专用字体
 *
 * @param darkTheme 是否使用深色主题，默认跟随系统
 * @param fontManager 字体管理器，可为 null（兼容无 DI 场景如 Preview）
 * @param content 主题内的 Composable 内容
 */
@Composable
fun DreamOsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontManager: FontManager? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DreamDarkColorScheme else DreamLightColorScheme

    val displayFont by fontManager?.currentDisplayFontFamily?.collectAsState()
        ?: androidx.compose.runtime.mutableStateOf(FontFamily.Default)
    val codeFont by fontManager?.currentCodeFontFamily?.collectAsState()
        ?: androidx.compose.runtime.mutableStateOf(FontFamily.Monospace)

    val typography = createDreamTypography(
        displayFont = displayFont,
        codeFont = codeFont
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = DreamShapes,
        content = content
    )
}
