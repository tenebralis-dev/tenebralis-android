package com.tenebralis.dreamos.presentation.screens.notes

import com.tenebralis.dreamos.domain.model.UserNote
import com.tenebralis.dreamos.domain.model.enums.AiVisibility

/**
 * 备忘管理 UI 事件
 */
sealed interface NoteEvent {
    data object Refresh : NoteEvent
    data class SearchChanged(val query: String) : NoteEvent

    // 编辑
    data object StartCreate : NoteEvent
    data class StartEdit(val note: UserNote) : NoteEvent
    data object DismissEdit : NoteEvent
    data object SaveNote : NoteEvent

    // 编辑表单字段
    data class TitleChanged(val value: String) : NoteEvent
    data class ContentChanged(val value: String) : NoteEvent
    data class TagsChanged(val value: String) : NoteEvent
    data class AiVisibilityChanged(val value: AiVisibility) : NoteEvent

    // 操作
    data class Delete(val noteId: String) : NoteEvent

    data object ClearError : NoteEvent
    data object ClearInfo : NoteEvent
}
