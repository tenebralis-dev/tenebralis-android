package com.tenebralis.dreamos.presentation.screens.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.UserNote
import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    init {
        loadNotes()
    }

    fun onEvent(event: NoteEvent) {
        when (event) {
            NoteEvent.Refresh -> loadNotes()
            is NoteEvent.SearchChanged ->
                _uiState.update { it.copy(searchQuery = event.query) }

            NoteEvent.StartCreate -> {
                _uiState.update {
                    it.copy(
                        isCreating = true,
                        editingNote = null,
                        editTitle = "",
                        editContent = "",
                        editTags = "",
                        editAiVisibility = AiVisibility.PRIVATE
                    )
                }
            }
            is NoteEvent.StartEdit -> {
                val note = event.note
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        editingNote = note,
                        editTitle = note.title,
                        editContent = note.content,
                        editTags = note.tags.joinToString(", "),
                        editAiVisibility = note.aiVisibility
                    )
                }
            }
            NoteEvent.DismissEdit ->
                _uiState.update { it.copy(isCreating = false, editingNote = null) }
            NoteEvent.SaveNote -> saveNote()

            is NoteEvent.TitleChanged ->
                _uiState.update { it.copy(editTitle = event.value) }
            is NoteEvent.ContentChanged ->
                _uiState.update { it.copy(editContent = event.value) }
            is NoteEvent.TagsChanged ->
                _uiState.update { it.copy(editTags = event.value) }
            is NoteEvent.AiVisibilityChanged ->
                _uiState.update { it.copy(editAiVisibility = event.value) }

            is NoteEvent.Delete -> deleteNote(event.noteId)

            NoteEvent.ClearError ->
                _uiState.update { it.copy(errorMessage = null) }
            NoteEvent.ClearInfo ->
                _uiState.update { it.copy(infoMessage = null) }
        }
    }

    // ─── 数据加载 ───────────────────────────────────────────

    private fun loadNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            noteRepository.getAll().collect { result ->
                result.fold(
                    onSuccess = { notes ->
                        _uiState.update {
                            it.copy(isLoading = false, notes = notes)
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to load notes", error)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "加载备忘失败：${error.message ?: "未知错误"}"
                            )
                        }
                    }
                )
            }
        }
    }

    // ─── 保存 ───────────────────────────────────────────────

    private fun saveNote() {
        val state = _uiState.value
        val title = state.editTitle.trim()
        val content = state.editContent.trim()
        if (title.isEmpty() && content.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "标题和内容不能同时为空") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val tags = state.editTags.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val result = if (state.isCreating) {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "当前未登录") }
                    return@launch
                }
                val newNote = UserNote(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = title,
                    content = content,
                    tags = tags,
                    aiVisibility = state.editAiVisibility
                )
                noteRepository.create(newNote)
            } else {
                val editing = state.editingNote ?: run {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "编辑状态异常") }
                    return@launch
                }
                val updated = editing.copy(
                    title = title,
                    content = content,
                    tags = tags,
                    aiVisibility = state.editAiVisibility
                )
                noteRepository.update(updated)
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isCreating = false,
                            editingNote = null,
                            infoMessage = if (state.isCreating) "备忘已创建" else "备忘已更新"
                        )
                    }
                    loadNotes()
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to save note", error)
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

    // ─── 删除 ───────────────────────────────────────────────

    private fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.delete(noteId).fold(
                onSuccess = {
                    _uiState.update { it.copy(infoMessage = "备忘已删除") }
                    loadNotes()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = "删除失败：${error.message ?: "未知错误"}")
                    }
                }
            )
        }
    }

    companion object {
        private const val TAG = "NoteViewModel"
    }
}
