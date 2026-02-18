package com.tenebralis.dreamos.presentation.screens.notes

import com.tenebralis.dreamos.domain.model.UserNote

/**
 * 备忘管理 UI 状态
 */
data class NoteUiState(
    val notes: List<UserNote> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val editingNote: UserNote? = null,
    val isCreating: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    // 编辑表单临时字段
    val editTitle: String = "",
    val editContent: String = "",
    val editTags: String = "",
    val editAiVisibility: com.tenebralis.dreamos.domain.model.enums.AiVisibility =
        com.tenebralis.dreamos.domain.model.enums.AiVisibility.PRIVATE
) {
    /** 是否正在显示编辑弹窗 */
    val isEditSheetVisible: Boolean get() = editingNote != null || isCreating

    /** 经过搜索过滤后的备忘列表 */
    val filteredNotes: List<UserNote>
        get() = notes.filter { note ->
            searchQuery.isBlank() ||
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true) ||
                note.tags.any { it.contains(searchQuery, ignoreCase = true) }
        }
}
