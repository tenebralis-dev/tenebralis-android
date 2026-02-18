package com.tenebralis.dreamos.presentation.screens.memory

import com.tenebralis.dreamos.domain.model.GlobalMemory

/**
 * 记忆管理 UI 事件
 */
sealed interface MemoryEvent {
    data object Refresh : MemoryEvent
    data class SearchChanged(val query: String) : MemoryEvent
    data class FilterChanged(val filter: MemoryFilter) : MemoryEvent

    // 编辑
    data object StartCreate : MemoryEvent
    data class StartEdit(val memory: GlobalMemory) : MemoryEvent
    data object DismissEdit : MemoryEvent
    data object SaveMemory : MemoryEvent

    // 编辑表单字段
    data class ContentChanged(val value: String) : MemoryEvent
    data class ImportanceChanged(val value: Int) : MemoryEvent
    data class TagsChanged(val value: String) : MemoryEvent

    // 快捷操作
    data class TogglePin(val memoryId: String, val isPinned: Boolean) : MemoryEvent
    data class ToggleArchive(val memoryId: String, val isArchived: Boolean) : MemoryEvent
    data class Delete(val memoryId: String) : MemoryEvent

    data object ClearError : MemoryEvent
    data object ClearInfo : MemoryEvent
}
