package com.tenebralis.dreamos.presentation.screens.font

import com.tenebralis.dreamos.domain.model.FontItem
import com.tenebralis.dreamos.domain.model.enums.FontCategory

/**
 * 字体选择页 UI 状态
 */
data class FontPickerUiState(
    val fonts: List<FontItem> = emptyList(),
    val selectedTab: FontTab = FontTab.DISPLAY,
    val selectedFontId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    /** 正在下载的字体 ID → 进度 (0..1) */
    val downloadingFonts: Map<String, Float> = emptyMap(),
    /** 显示删除确认对话框的字体 */
    val showDeleteDialog: FontItem? = null,
    /** 显示 URL 导入对话框 */
    val showUrlImportDialog: Boolean = false
)

enum class FontTab(val displayName: String, val category: FontCategory) {
    DISPLAY("显示字体", FontCategory.DISPLAY),
    CODE("代码字体", FontCategory.CODE)
}

sealed interface FontPickerEvent {
    data class SwitchTab(val tab: FontTab) : FontPickerEvent
    data object Refresh : FontPickerEvent

    // 下载
    data class DownloadFont(val item: FontItem) : FontPickerEvent

    // 选中
    data class SelectFont(val fontId: String) : FontPickerEvent

    // 删除
    data class ShowDeleteDialog(val item: FontItem) : FontPickerEvent
    data object DismissDeleteDialog : FontPickerEvent
    data class ConfirmDelete(val item: FontItem) : FontPickerEvent

    // 本地导入
    data class ImportLocalFont(
        val uri: android.net.Uri,
        val displayName: String
    ) : FontPickerEvent

    // URL 导入
    data object ShowUrlImportDialog : FontPickerEvent
    data object DismissUrlImportDialog : FontPickerEvent
    data class ConfirmUrlImport(
        val url: String,
        val displayName: String
    ) : FontPickerEvent

    // 消息清除
    data object DismissError : FontPickerEvent
    data object DismissSuccess : FontPickerEvent
}
