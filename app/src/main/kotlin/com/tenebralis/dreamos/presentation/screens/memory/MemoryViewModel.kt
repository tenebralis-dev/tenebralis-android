package com.tenebralis.dreamos.presentation.screens.memory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.GlobalMemory
import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.MemorySourceType
import com.tenebralis.dreamos.domain.model.enums.MemoryType
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.GlobalMemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val globalMemoryRepository: GlobalMemoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    init {
        loadMemories()
    }

    fun onEvent(event: MemoryEvent) {
        when (event) {
            MemoryEvent.Refresh -> loadMemories()
            is MemoryEvent.SearchChanged ->
                _uiState.update { it.copy(searchQuery = event.query) }
            is MemoryEvent.FilterChanged ->
                _uiState.update { it.copy(filter = event.filter) }

            MemoryEvent.StartCreate -> {
                _uiState.update {
                    it.copy(
                        isCreating = true,
                        editingMemory = null,
                        editContent = "",
                        editImportance = 3,
                        editTags = ""
                    )
                }
            }
            is MemoryEvent.StartEdit -> {
                val memory = event.memory
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        editingMemory = memory,
                        editContent = memory.content,
                        editImportance = importanceFromDouble(memory.importanceScore),
                        editTags = extractTagsString(memory.tagsJson)
                    )
                }
            }
            MemoryEvent.DismissEdit ->
                _uiState.update { it.copy(isCreating = false, editingMemory = null) }
            MemoryEvent.SaveMemory -> saveMemory()

            is MemoryEvent.ContentChanged ->
                _uiState.update { it.copy(editContent = event.value) }
            is MemoryEvent.ImportanceChanged ->
                _uiState.update { it.copy(editImportance = event.value) }
            is MemoryEvent.TagsChanged ->
                _uiState.update { it.copy(editTags = event.value) }

            is MemoryEvent.TogglePin -> togglePin(event.memoryId, event.isPinned)
            is MemoryEvent.ToggleArchive -> toggleArchive(event.memoryId, event.isArchived)
            is MemoryEvent.Delete -> deleteMemory(event.memoryId)

            MemoryEvent.ClearError ->
                _uiState.update { it.copy(errorMessage = null) }
            MemoryEvent.ClearInfo ->
                _uiState.update { it.copy(infoMessage = null) }
        }
    }

    // ─── 数据加载 ───────────────────────────────────────────

    private fun loadMemories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            globalMemoryRepository.getAll().collect { result ->
                result.fold(
                    onSuccess = { memories ->
                        _uiState.update {
                            it.copy(isLoading = false, memories = memories)
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to load memories", error)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "加载记忆失败：${error.message ?: "未知错误"}"
                            )
                        }
                    }
                )
            }
        }
    }

    // ─── 保存 ───────────────────────────────────────────────

    private fun saveMemory() {
        val state = _uiState.value
        val content = state.editContent.trim()
        if (content.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "记忆内容不能为空") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val tagsJson = buildTagsJson(state.editTags)
            val importanceScore = importanceToDouble(state.editImportance)

            val result = if (state.isCreating) {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "当前未登录") }
                    return@launch
                }
                val newMemory = GlobalMemory(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    memoryKey = null,
                    content = content,
                    summary = null,
                    memoryType = MemoryType.FACT,
                    aiVisibility = AiVisibility.ASSISTANT,
                    importanceScore = importanceScore,
                    confidenceScore = 70.0,
                    sourceType = MemorySourceType.MANUAL,
                    sourceRefJson = JsonObject(emptyMap()),
                    tagsJson = tagsJson,
                    metadataJson = JsonObject(emptyMap()),
                    isPinned = false,
                    isArchived = false,
                    recalledCount = 0,
                    lastRecalledAt = null,
                    expiresAt = null,
                    createdAt = null,
                    updatedAt = null,
                    deletedAt = null
                )
                globalMemoryRepository.create(newMemory)
            } else {
                val editing = state.editingMemory ?: run {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "编辑状态异常") }
                    return@launch
                }
                val updated = editing.copy(
                    content = content,
                    importanceScore = importanceScore,
                    tagsJson = tagsJson
                )
                globalMemoryRepository.update(updated)
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isCreating = false,
                            editingMemory = null,
                            infoMessage = if (state.isCreating) "记忆已创建" else "记忆已更新"
                        )
                    }
                    loadMemories()
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to save memory", error)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "保存失败：${error.message ?: "未知错误"}"
                        )
                    }
                }
            )
        }
    }

    // ─── 快捷操作 ─────────────────────────────────────────

    private fun togglePin(memoryId: String, isPinned: Boolean) {
        viewModelScope.launch {
            globalMemoryRepository.togglePin(memoryId, !isPinned).fold(
                onSuccess = { loadMemories() },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = "操作失败：${error.message ?: "未知错误"}")
                    }
                }
            )
        }
    }

    private fun toggleArchive(memoryId: String, isArchived: Boolean) {
        viewModelScope.launch {
            globalMemoryRepository.toggleArchive(memoryId, !isArchived).fold(
                onSuccess = { loadMemories() },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = "操作失败：${error.message ?: "未知错误"}")
                    }
                }
            )
        }
    }

    private fun deleteMemory(memoryId: String) {
        viewModelScope.launch {
            globalMemoryRepository.softDelete(memoryId).fold(
                onSuccess = {
                    _uiState.update { it.copy(infoMessage = "记忆已删除") }
                    loadMemories()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = "删除失败：${error.message ?: "未知错误"}")
                    }
                }
            )
        }
    }

    // ─── 工具方法 ─────────────────────────────────────────

    /** 1-5 星 → Double(20-100) */
    private fun importanceToDouble(stars: Int): Double = (stars.coerceIn(1, 5) * 20).toDouble()

    /** Double(0-100) → 1-5 星 */
    private fun importanceFromDouble(score: Double): Int = (score / 20.0).toInt().coerceIn(1, 5)

    /** 逗号分隔标签 → JsonObject */
    private fun buildTagsJson(tagsInput: String): JsonObject {
        val tags = tagsInput.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        return if (tags.isEmpty()) {
            JsonObject(emptyMap())
        } else {
            JsonObject(mapOf("tags" to JsonArray(tags.map { JsonPrimitive(it) })))
        }
    }

    /** JsonObject → 逗号分隔字符串 */
    private fun extractTagsString(tagsJson: JsonObject): String {
        val tagsArray = tagsJson["tags"] as? JsonArray ?: return ""
        return tagsArray.mapNotNull { element ->
            (element as? JsonPrimitive)?.content
        }.joinToString(", ")
    }

    companion object {
        private const val TAG = "MemoryViewModel"
    }
}
