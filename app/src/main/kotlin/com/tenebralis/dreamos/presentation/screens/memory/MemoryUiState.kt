package com.tenebralis.dreamos.presentation.screens.memory

import com.tenebralis.dreamos.domain.model.GlobalMemory

/**
 * 记忆管理 UI 状态
 */
data class MemoryUiState(
    val memories: List<GlobalMemory> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filter: MemoryFilter = MemoryFilter.ALL,
    val editingMemory: GlobalMemory? = null,
    val isCreating: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    // 编辑表单临时字段
    val editContent: String = "",
    val editImportance: Int = 3,       // 1-5 星
    val editTags: String = ""          // 逗号分隔
) {
    /** 是否正在显示编辑弹窗 */
    val isEditSheetVisible: Boolean get() = editingMemory != null || isCreating

    /** 经过搜索和筛选后的记忆列表 */
    val filteredMemories: List<GlobalMemory>
        get() = memories
            .filter { memory ->
                when (filter) {
                    MemoryFilter.ALL      -> true
                    MemoryFilter.PINNED   -> memory.isPinned
                    MemoryFilter.ARCHIVED -> memory.isArchived
                }
            }
            .filter { memory ->
                searchQuery.isBlank() ||
                    memory.content.contains(searchQuery, ignoreCase = true)
            }
}

enum class MemoryFilter { ALL, PINNED, ARCHIVED }
