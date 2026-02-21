package com.tenebralis.dreamos.data.repository

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.tenebralis.dreamos.domain.model.enums.FontCategory
import com.tenebralis.dreamos.domain.repository.FontRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FontManager — 全局字体管理单例
 *
 * 根据用户选中的字体 ID 加载对应 Typeface，暴露 StateFlow<FontFamily>
 * 供 DreamOsTheme 消费，实现字体热切换。
 */
@Singleton
class FontManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fontRepository: FontRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentDisplayFontFamily = MutableStateFlow<FontFamily>(FontFamily.Default)
    val currentDisplayFontFamily: StateFlow<FontFamily> = _currentDisplayFontFamily.asStateFlow()

    private val _currentCodeFontFamily = MutableStateFlow<FontFamily>(FontFamily.Monospace)
    val currentCodeFontFamily: StateFlow<FontFamily> = _currentCodeFontFamily.asStateFlow()

    init {
        observeFontChanges()
    }

    private fun observeFontChanges() {
        scope.launch {
            fontRepository.getSelectedFontId(FontCategory.DISPLAY).collectLatest { fontId ->
                _currentDisplayFontFamily.value = loadFontFamily(fontId, FontCategory.DISPLAY)
            }
        }
        scope.launch {
            fontRepository.getSelectedFontId(FontCategory.CODE).collectLatest { fontId ->
                _currentCodeFontFamily.value = loadFontFamily(fontId, FontCategory.CODE)
            }
        }
    }

    private suspend fun loadFontFamily(fontId: String?, category: FontCategory): FontFamily {
        if (fontId == null || fontId == FontRepositoryImpl.BUILT_IN_FONT_ID) {
            return if (category == FontCategory.CODE) FontFamily.Monospace
            else loadBuiltInFont()
        }

        val fontsDir = File(context.filesDir, "fonts")

        // 尝试从远端字体列表找到 fileName
        val remoteFonts = fontRepository.fetchRemoteFonts().getOrNull()
        val remoteItem = remoteFonts?.find { it.id == fontId }
        if (remoteItem != null) {
            val file = File(fontsDir, remoteItem.fileName)
            if (file.exists()) {
                return loadFontFamilyFromFile(file)
            }
        }

        // 尝试从导入字体列表找
        val importedFonts = fontRepository.getImportedFonts().first()
        val importedItem = importedFonts.find { it.id == fontId }
        if (importedItem != null) {
            val file = File(fontsDir, importedItem.fileName)
            if (file.exists()) {
                return loadFontFamilyFromFile(file)
            }
        }

        // 都找不到则降级
        return if (category == FontCategory.CODE) FontFamily.Monospace else loadBuiltInFont()
    }

    private fun loadBuiltInFont(): FontFamily {
        return try {
            // 将 assets 字体复制到缓存再通过 Font(File) 加载
            val cacheFile = File(context.cacheDir, "builtin_font/${FontRepositoryImpl.BUILT_IN_FILE_NAME}")
            if (!cacheFile.exists()) {
                cacheFile.parentFile?.mkdirs()
                context.assets.open("fonts/${FontRepositoryImpl.BUILT_IN_FILE_NAME}").use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            FontFamily(Font(cacheFile))
        } catch (_: Exception) {
            FontFamily.Default
        }
    }

    private fun loadFontFamilyFromFile(file: File): FontFamily {
        return try {
            FontFamily(Font(file))
        } catch (_: Exception) {
            FontFamily.Default
        }
    }
}
