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

// Tenebralis 浅色配色方案（IM 风格，浅色优先）
private val TenebralisLightColorScheme = lightColorScheme(
    primary = TenebralisPrimary,
    onPrimary = TenebralisOnPrimary,
    primaryContainer = TenebralisPrimaryLight,
    onPrimaryContainer = TenebralisOnSurface,
    surface = TenebralisSurface,
    onSurface = TenebralisOnSurface,
    background = TenebralisBackground,
    onBackground = TenebralisOnSurface,
    outline = TenebralisOutline,
    error = TenebralisError,
)

// Tenebralis 深色配色方案
private val TenebralisDarkColorScheme = darkColorScheme(
    primary = TenebralisPrimaryDark,
    onPrimary = TenebralisOnPrimaryDark,
    primaryContainer = TenebralisContainerDark,
    surface = TenebralisSurfaceDark,
    background = TenebralisBackgroundDark,
    error = TenebralisError,
)

/**
 * Tenebralis 主题
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
fun TenebralisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontManager: FontManager? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TenebralisDarkColorScheme else TenebralisLightColorScheme

    val displayFont by fontManager?.currentDisplayFontFamily?.collectAsState()
        ?: androidx.compose.runtime.mutableStateOf(FontFamily.Default)
    val codeFont by fontManager?.currentCodeFontFamily?.collectAsState()
        ?: androidx.compose.runtime.mutableStateOf(FontFamily.Monospace)

    val typography = createTenebralisTypography(
        displayFont = displayFont,
        codeFont = codeFont
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = TenebralisShapes,
        content = content
    )
}
