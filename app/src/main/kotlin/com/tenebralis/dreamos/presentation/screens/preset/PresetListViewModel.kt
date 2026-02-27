package com.tenebralis.dreamos.presentation.screens.preset

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.AiPreset
import com.tenebralis.dreamos.domain.usecase.preset.CreatePresetUseCase
import com.tenebralis.dreamos.domain.usecase.preset.DeletePresetUseCase
import com.tenebralis.dreamos.domain.usecase.preset.GetPresetsUseCase
import com.tenebralis.dreamos.domain.usecase.preset.ImportPresetUseCase
import com.tenebralis.dreamos.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.util.UUID

data class PresetListUiState(
    val presets: List<AiPreset> = emptyList(),
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val isCreating: Boolean = false,
    val isDeleting: Boolean = false,
    val emptyState: Boolean = false,

    // 创建弹窗
    val isCreateDialogVisible: Boolean = false,
    val createName: String = "",

    // 删除弹窗
    val isDeleteDialogVisible: Boolean = false,
    val deleteTarget: AiPreset? = null,

    // 导入冲突弹窗
    val isConflictDialogVisible: Boolean = false,
    val conflictPresetName: String = "",
    val conflictExistingPreset: AiPreset? = null,
    val conflictParsedPresetJson: JsonObject? = null,
    val conflictParsedRegexJson: JsonArray? = null,

    // 消息
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

@HiltViewModel
class PresetListViewModel @Inject constructor(
    private val getPresetsUseCase: GetPresetsUseCase,
    private val createPresetUseCase: CreatePresetUseCase,
    private val deletePresetUseCase: DeletePresetUseCase,
    private val importPresetUseCase: ImportPresetUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PresetListUiState())
    val uiState: StateFlow<PresetListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPresetsUseCase().collect { result ->
                result.fold(
                    onSuccess = { list ->
                        _uiState.update {
                            it.copy(
                                presets = list,
                                isLoading = false,
                                emptyState = list.isEmpty()
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "加载失败：${e.message}"
                            )
                        }
                    }
                )
            }
        }
    }

    // ─── 创建弹窗 ────────────────────────────────────────

    fun showCreateDialog() {
        _uiState.update { it.copy(isCreateDialogVisible = true, createName = "") }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(isCreateDialogVisible = false, createName = "") }
    }

    fun updateCreateName(value: String) {
        _uiState.update { it.copy(createName = value) }
    }

    fun createPreset() {
        val name = _uiState.value.createName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "名称不能为空") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update {
                    it.copy(isCreating = false, errorMessage = "当前未登录")
                }
                return@launch
            }

            val preset = AiPreset(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                presetJson = com.tenebralis.dreamos.domain.model.DefaultPreset.toJsonObject(),
                regexScriptsJson = JsonArray(emptyList()),
                source = "manual",
                createdAt = null,
                updatedAt = null
            )

            createPresetUseCase(preset).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            isCreateDialogVisible = false,
                            createName = "",
                            infoMessage = "预设「$name」创建成功"
                        )
                    }
                    refresh()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isCreating = false, errorMessage = "创建失败：${e.message}")
                    }
                }
            )
        }
    }

    // ─── 删除弹窗 ────────────────────────────────────────

    fun showDeleteDialog(preset: AiPreset) {
        _uiState.update { it.copy(isDeleteDialogVisible = true, deleteTarget = preset) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogVisible = false, deleteTarget = null) }
    }

    fun confirmDelete() {
        val target = _uiState.value.deleteTarget ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            deletePresetUseCase(target.id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            isDeleteDialogVisible = false,
                            deleteTarget = null,
                            infoMessage = "已删除「${target.name}」"
                        )
                    }
                    refresh()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isDeleting = false, errorMessage = "删除失败：${e.message}")
                    }
                }
            )
        }
    }

    // ─── 导入 ────────────────────────────────────────────

    fun importFile(uri: Uri, fileName: String, resolver: android.content.ContentResolver) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                val jsonString = resolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: throw IllegalStateException("无法读取文件")

                // 用文件名（去掉扩展名）作为默认名称
                val defaultName = fileName
                    .substringBeforeLast(".")
                    .trim()
                    .ifEmpty { null }

                importPresetUseCase(jsonString, defaultName).fold(
                    onSuccess = { result ->
                        when (result) {
                            is ImportPresetUseCase.ImportResult.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isImporting = false,
                                        infoMessage = "导入成功：${result.preset.name}"
                                    )
                                }
                                refresh()
                            }
                            is ImportPresetUseCase.ImportResult.Conflict -> {
                                _uiState.update {
                                    it.copy(
                                        isImporting = false,
                                        isConflictDialogVisible = true,
                                        conflictPresetName = result.newPresetName,
                                        conflictExistingPreset = result.existingPreset,
                                        conflictParsedPresetJson = result.parsedPresetJson,
                                        conflictParsedRegexJson = result.parsedRegexScriptsJson
                                    )
                                }
                            }
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isImporting = false, errorMessage = "导入失败：${e.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isImporting = false, errorMessage = "读取文件失败：${e.message}")
                }
            }
        }
    }

    fun dismissConflictDialog() {
        _uiState.update {
            it.copy(
                isConflictDialogVisible = false,
                conflictPresetName = "",
                conflictExistingPreset = null,
                conflictParsedPresetJson = null,
                conflictParsedRegexJson = null
            )
        }
    }

    fun conflictOverwrite() {
        val existing = _uiState.value.conflictExistingPreset ?: return
        val presetJson = _uiState.value.conflictParsedPresetJson ?: return
        val regexJson = _uiState.value.conflictParsedRegexJson ?: return
        val name = _uiState.value.conflictPresetName

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            dismissConflictDialog()

            importPresetUseCase.overwrite(
                existingPresetId = existing.id,
                presetJson = presetJson,
                regexScriptsJson = regexJson,
                name = name
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isImporting = false, infoMessage = "已覆盖更新「$name」")
                    }
                    refresh()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isImporting = false, errorMessage = "覆盖失败：${e.message}")
                    }
                }
            )
        }
    }

    fun conflictRename() {
        val presetJson = _uiState.value.conflictParsedPresetJson ?: return
        val regexJson = _uiState.value.conflictParsedRegexJson ?: return
        val name = _uiState.value.conflictPresetName

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            dismissConflictDialog()

            val newName = "${name}_imported"
            importPresetUseCase.importWithRename(
                newName = newName,
                presetJson = presetJson,
                regexScriptsJson = regexJson
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isImporting = false, infoMessage = "已导入为「$newName」")
                    }
                    refresh()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isImporting = false, errorMessage = "重命名导入失败：${e.message}")
                    }
                }
            )
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeInfo() {
        _uiState.update { it.copy(infoMessage = null) }
    }
}
