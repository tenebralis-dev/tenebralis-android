package com.tenebralis.dreamos.presentation.screens.preset

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.AiPreset
import com.tenebralis.dreamos.domain.repository.AiPresetRepository
import com.tenebralis.dreamos.domain.usecase.preset.UpdatePresetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class PresetEditUiState(
    // 原始数据
    val preset: AiPreset? = null,
    val editedName: String = "",

    // 各分区的可编辑数据
    val samplingParams: SamplingParams = SamplingParams(),
    val advancedSettings: AdvancedSettings = AdvancedSettings(),
    val utilityPrompts: UtilityPrompts = UtilityPrompts(),
    val prompts: List<EditablePrompt> = emptyList(),
    val promptOrders: List<PromptOrderGroup> = emptyList(),

    // 弹窗
    val editingPromptIndex: Int = -1,
    val editingPrompt: EditablePrompt? = null,

    // 折叠状态
    val expandedSections: Set<String> = setOf("sampling", "prompts"),

    // 状态
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

@HiltViewModel
class PresetEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AiPresetRepository,
    private val updatePresetUseCase: UpdatePresetUseCase
) : ViewModel() {

    private val presetId: String = checkNotNull(savedStateHandle["presetId"])

    private val _uiState = MutableStateFlow(PresetEditUiState())
    val uiState: StateFlow<PresetEditUiState> = _uiState.asStateFlow()

    init {
        loadPreset()
    }

    private fun loadPreset() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getById(presetId).fold(
                onSuccess = { preset ->
                    val json = preset.presetJson
                    val prompts = PresetJsonParser.parsePrompts(json)
                    _uiState.update {
                        it.copy(
                            preset = preset,
                            editedName = preset.name,
                            samplingParams = PresetJsonParser.parseSamplingParams(json),
                            advancedSettings = PresetJsonParser.parseAdvancedSettings(json),
                            utilityPrompts = PresetJsonParser.parseUtilityPrompts(json),
                            prompts = prompts,
                            promptOrders = PresetJsonParser.parsePromptOrders(json, prompts),
                            isLoading = false
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "加载失败：${e.message}")
                    }
                }
            )
        }
    }

    // ─── 名称 ──────────────────────────────────────────────

    fun updateName(value: String) {
        _uiState.update { it.copy(editedName = value, hasUnsavedChanges = true) }
    }

    // ─── 采样参数 ──────────────────────────────────────────

    fun updateSamplingParams(params: SamplingParams) {
        _uiState.update { it.copy(samplingParams = params, hasUnsavedChanges = true) }
    }

    // ─── 高级设置 ──────────────────────────────────────────

    fun updateAdvancedSettings(settings: AdvancedSettings) {
        _uiState.update { it.copy(advancedSettings = settings, hasUnsavedChanges = true) }
    }

    // ─── 辅助提示词 ────────────────────────────────────────

    fun updateUtilityPrompts(prompts: UtilityPrompts) {
        _uiState.update { it.copy(utilityPrompts = prompts, hasUnsavedChanges = true) }
    }

    // ─── Prompt 列表操作 ──────────────────────────────────

    fun togglePromptEnabled(index: Int, enabled: Boolean) {
        _uiState.update {
            val list = it.prompts.toMutableList()
            if (index in list.indices) {
                list[index] = list[index].copy(enabled = enabled)
            }
            it.copy(prompts = list, hasUnsavedChanges = true)
        }
    }

    fun openPromptEditor(index: Int) {
        val prompt = _uiState.value.prompts.getOrNull(index) ?: return
        _uiState.update { it.copy(editingPromptIndex = index, editingPrompt = prompt) }
    }

    fun closePromptEditor() {
        _uiState.update { it.copy(editingPromptIndex = -1, editingPrompt = null) }
    }

    fun confirmPromptEdit(edited: EditablePrompt) {
        val index = _uiState.value.editingPromptIndex
        _uiState.update {
            val list = it.prompts.toMutableList()
            if (index in list.indices) {
                list[index] = edited
            }
            it.copy(
                prompts = list,
                editingPromptIndex = -1,
                editingPrompt = null,
                hasUnsavedChanges = true
            )
        }
    }

    fun deletePrompt(index: Int) {
        _uiState.update {
            val list = it.prompts.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            it.copy(
                prompts = list,
                editingPromptIndex = -1,
                editingPrompt = null,
                hasUnsavedChanges = true
            )
        }
    }

    fun addPrompt() {
        _uiState.update {
            val newPrompt = EditablePrompt(
                identifier = UUID.randomUUID().toString(),
                name = "新提示词",
                role = "system",
                content = "",
                enabled = false,
                systemPrompt = false,
                marker = false
            )
            it.copy(
                prompts = it.prompts + newPrompt,
                hasUnsavedChanges = true
            )
        }
    }

    fun insertPromptBelow(index: Int) {
        _uiState.update {
            val list = it.prompts.toMutableList()
            val newPrompt = EditablePrompt(
                identifier = UUID.randomUUID().toString(),
                name = "新提示词",
                role = "system",
                content = "",
                enabled = false,
                systemPrompt = false,
                marker = false
            )
            list.add((index + 1).coerceAtMost(list.size), newPrompt)
            it.copy(prompts = list, hasUnsavedChanges = true)
        }
    }

    // ─── Prompt 排列顺序操作 ──────────────────────────────

    fun movePromptOrderUp(groupIndex: Int, itemIndex: Int) {
        if (itemIndex <= 0) return
        _uiState.update {
            val groups = it.promptOrders.toMutableList()
            if (groupIndex in groups.indices) {
                val orders = groups[groupIndex].orders.toMutableList()
                if (itemIndex in orders.indices) {
                    val tmp = orders[itemIndex]
                    orders[itemIndex] = orders[itemIndex - 1]
                    orders[itemIndex - 1] = tmp
                    groups[groupIndex] = groups[groupIndex].copy(orders = orders)
                }
            }
            it.copy(promptOrders = groups, hasUnsavedChanges = true)
        }
    }

    fun movePromptOrderDown(groupIndex: Int, itemIndex: Int) {
        _uiState.update {
            val groups = it.promptOrders.toMutableList()
            if (groupIndex in groups.indices) {
                val orders = groups[groupIndex].orders.toMutableList()
                if (itemIndex in orders.indices && itemIndex < orders.lastIndex) {
                    val tmp = orders[itemIndex]
                    orders[itemIndex] = orders[itemIndex + 1]
                    orders[itemIndex + 1] = tmp
                    groups[groupIndex] = groups[groupIndex].copy(orders = orders)
                }
            }
            it.copy(promptOrders = groups, hasUnsavedChanges = true)
        }
    }

    fun togglePromptOrderEnabled(groupIndex: Int, itemIndex: Int, enabled: Boolean) {
        _uiState.update {
            val groups = it.promptOrders.toMutableList()
            if (groupIndex in groups.indices) {
                val orders = groups[groupIndex].orders.toMutableList()
                if (itemIndex in orders.indices) {
                    orders[itemIndex] = orders[itemIndex].copy(enabled = enabled)
                    groups[groupIndex] = groups[groupIndex].copy(orders = orders)
                }
            }
            it.copy(promptOrders = groups, hasUnsavedChanges = true)
        }
    }

    // ─── 折叠控制 ──────────────────────────────────────────

    fun toggleSection(section: String) {
        _uiState.update {
            val current = it.expandedSections.toMutableSet()
            if (section in current) current.remove(section)
            else current.add(section)
            it.copy(expandedSections = current)
        }
    }

    // ─── 保存 ──────────────────────────────────────────────

    fun save() {
        val current = _uiState.value
        val preset = current.preset ?: return
        val newName = current.editedName.trim()

        if (newName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "名称不能为空") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // 重建 presetJson
            val newJson = PresetJsonParser.buildJsonObject(
                original = preset.presetJson,
                sampling = current.samplingParams,
                advanced = current.advancedSettings,
                utility = current.utilityPrompts,
                prompts = current.prompts,
                promptOrders = current.promptOrders
            )

            val updated = preset.copy(
                name = newName,
                presetJson = newJson
            )

            updatePresetUseCase(updated).fold(
                onSuccess = { saved ->
                    _uiState.update {
                        it.copy(
                            preset = saved,
                            editedName = saved.name,
                            isSaving = false,
                            hasUnsavedChanges = false,
                            saveSuccess = true,
                            infoMessage = "保存成功"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = "保存失败：${e.message}")
                    }
                }
            )
        }
    }

    // ─── 消息消费 ──────────────────────────────────────────

    fun consumeError() { _uiState.update { it.copy(errorMessage = null) } }
    fun consumeInfo() { _uiState.update { it.copy(infoMessage = null) } }
    fun consumeSaveSuccess() { _uiState.update { it.copy(saveSuccess = false) } }
}
