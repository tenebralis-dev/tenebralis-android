package com.tenebralis.dreamos.presentation.screens.npc

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.CharacterCardData
import com.tenebralis.dreamos.domain.model.Npc
import com.tenebralis.dreamos.domain.usecase.chat.GetNpcsUseCase
import com.tenebralis.dreamos.domain.usecase.common.UseCaseErrorMapper
import com.tenebralis.dreamos.domain.usecase.npc.CreateNpcUseCase
import com.tenebralis.dreamos.domain.usecase.npc.DeleteNpcUseCase
import com.tenebralis.dreamos.domain.usecase.npc.ImportCharacterCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NpcListUiState(
    val npcs: List<Npc> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isDeleting: Boolean = false,
    val isImporting: Boolean = false,

    // 创建弹窗
    val isCreateDialogVisible: Boolean = false,
    val createName: String = "",
    val createDescription: String = "",

    // 删除确认弹窗
    val isDeleteDialogVisible: Boolean = false,
    val deleteTargetNpc: Npc? = null,

    // 导入冲突弹窗
    val isConflictDialogVisible: Boolean = false,
    val conflictExistingNpc: Npc? = null,
    val conflictCardData: CharacterCardData? = null,
    val conflictPngBytes: ByteArray? = null,

    // 消息
    val errorMessage: String? = null,
    val infoMessage: String? = null
) {
    val emptyState: Boolean get() = !isLoading && npcs.isEmpty()
}

@HiltViewModel
class NpcListViewModel @Inject constructor(
    private val getNpcsUseCase: GetNpcsUseCase,
    private val createNpcUseCase: CreateNpcUseCase,
    private val deleteNpcUseCase: DeleteNpcUseCase,
    private val importCharacterCardUseCase: ImportCharacterCardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NpcListUiState())
    val uiState: StateFlow<NpcListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getNpcsUseCase().fold(
                onSuccess = { npcs ->
                    _uiState.update { it.copy(isLoading = false, npcs = npcs) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }

    // ─── 创建 ────────────────────────────────────────────

    fun showCreateDialog() {
        _uiState.update {
            it.copy(isCreateDialogVisible = true, createName = "", createDescription = "")
        }
    }

    fun dismissCreateDialog() {
        if (_uiState.value.isCreating) return
        _uiState.update { it.copy(isCreateDialogVisible = false) }
    }

    fun updateCreateName(value: String) {
        _uiState.update { it.copy(createName = value) }
    }

    fun updateCreateDescription(value: String) {
        _uiState.update { it.copy(createDescription = value) }
    }

    fun createNpc() {
        val state = _uiState.value
        if (state.isCreating) return
        val name = state.createName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请输入 NPC 名称") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }
            createNpcUseCase(
                name = name,
                description = state.createDescription.trim().takeIf { it.isNotEmpty() }
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            isCreateDialogVisible = false,
                            infoMessage = "NPC 已创建"
                        )
                    }
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }

    // ─── 删除 ────────────────────────────────────────────

    fun showDeleteDialog(npc: Npc) {
        _uiState.update { it.copy(isDeleteDialogVisible = true, deleteTargetNpc = npc) }
    }

    fun dismissDeleteDialog() {
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(isDeleteDialogVisible = false, deleteTargetNpc = null) }
    }

    fun confirmDelete() {
        val npc = _uiState.value.deleteTargetNpc ?: return
        if (_uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            deleteNpcUseCase(npc.id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            isDeleteDialogVisible = false,
                            deleteTargetNpc = null,
                            infoMessage = "已删除 ${npc.name}"
                        )
                    }
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }

    // ─── 导入 ────────────────────────────────────────────

    fun importFile(uri: Uri, fileName: String, resolver: android.content.ContentResolver) {
        if (_uiState.value.isImporting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, errorMessage = null) }
            try {
                val inputStream = resolver.openInputStream(uri)
                    ?: throw IllegalStateException("无法读取文件")
                inputStream.use { stream ->
                    importCharacterCardUseCase(stream, fileName).fold(
                        onSuccess = { result ->
                            when (result) {
                                is ImportCharacterCardUseCase.ImportResult.Success -> {
                                    _uiState.update {
                                        it.copy(
                                            isImporting = false,
                                            infoMessage = "已导入角色 ${result.npc.name}"
                                        )
                                    }
                                    refresh()
                                }
                                is ImportCharacterCardUseCase.ImportResult.Conflict -> {
                                    _uiState.update {
                                        it.copy(
                                            isImporting = false,
                                            isConflictDialogVisible = true,
                                            conflictExistingNpc = result.existingNpc,
                                            conflictCardData = result.cardData,
                                            conflictPngBytes = result.pngBytes
                                        )
                                    }
                                }
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(
                                    isImporting = false,
                                    errorMessage = UseCaseErrorMapper.toMessage(error)
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        errorMessage = e.message ?: "导入失败"
                    )
                }
            }
        }
    }

    fun dismissConflictDialog() {
        _uiState.update {
            it.copy(
                isConflictDialogVisible = false,
                conflictExistingNpc = null,
                conflictCardData = null,
                conflictPngBytes = null
            )
        }
    }

    fun conflictOverwrite() {
        val existing = _uiState.value.conflictExistingNpc ?: return
        val card = _uiState.value.conflictCardData ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            importCharacterCardUseCase.overwrite(existing.id, card).fold(
                onSuccess = { npc ->
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            isConflictDialogVisible = false,
                            conflictExistingNpc = null,
                            conflictCardData = null,
                            conflictPngBytes = null,
                            infoMessage = "已覆盖更新 ${npc.name}"
                        )
                    }
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }

    fun conflictRename() {
        val card = _uiState.value.conflictCardData ?: return
        val newName = "${card.name} (2)"

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            importCharacterCardUseCase.importWithRename(card, newName).fold(
                onSuccess = { npc ->
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            isConflictDialogVisible = false,
                            conflictExistingNpc = null,
                            conflictCardData = null,
                            conflictPngBytes = null,
                            infoMessage = "已导入为 ${npc.name}"
                        )
                    }
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }

    // ─── 消息消费 ────────────────────────────────────────

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeInfo() {
        _uiState.update { it.copy(infoMessage = null) }
    }
}
