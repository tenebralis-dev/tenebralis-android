package com.tenebralis.dreamos.presentation.screens.save

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.WorldSaveState
import com.tenebralis.dreamos.domain.usecase.common.UseCaseErrorMapper
import com.tenebralis.dreamos.domain.usecase.save.CreateSaveStateUseCase
import com.tenebralis.dreamos.domain.usecase.save.GetSaveStatesUseCase
import com.tenebralis.dreamos.domain.usecase.save.SelectSaveStateUseCase
import com.tenebralis.dreamos.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SaveSelectUiState(
    val worldId: String = "",
    val identityId: String = "",
    val saveStates: List<WorldSaveState> = emptyList(),
    val selectedSaveId: String? = null,
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isSelecting: Boolean = false,
    val isCreateDialogVisible: Boolean = false,
    val createSlotInput: String = "",
    val createTitleInput: String = "",
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val navigateToChatListSaveId: String? = null
) {
    val emptyState: Boolean get() = !isLoading && saveStates.isEmpty()
}

@HiltViewModel
class SaveSelectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSaveStatesUseCase: GetSaveStatesUseCase,
    private val createSaveStateUseCase: CreateSaveStateUseCase,
    private val selectSaveStateUseCase: SelectSaveStateUseCase
) : ViewModel() {

    private val worldId: String = checkNotNull(savedStateHandle[Screen.SaveSelect.ARG_WORLD_ID])
    private val identityId: String = checkNotNull(savedStateHandle[Screen.SaveSelect.ARG_IDENTITY_ID])

    private val _uiState = MutableStateFlow(
        SaveSelectUiState(
            worldId = worldId,
            identityId = identityId
        )
    )
    val uiState: StateFlow<SaveSelectUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getSaveStatesUseCase(identityId).fold(
                onSuccess = { saveStates ->
                    _uiState.update { state ->
                        val selectedId = state.selectedSaveId?.takeIf { id ->
                            saveStates.any { it.id == id }
                        } ?: saveStates.firstOrNull()?.id

                        state.copy(
                            isLoading = false,
                            saveStates = saveStates,
                            selectedSaveId = selectedId
                        )
                    }
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

    fun showCreateDialog() {
        val nextSlot = ((_uiState.value.saveStates.maxOfOrNull { it.slot } ?: 0) + 1).coerceAtLeast(1)
        _uiState.update {
            it.copy(
                isCreateDialogVisible = true,
                createSlotInput = nextSlot.toString(),
                createTitleInput = "",
                errorMessage = null
            )
        }
    }

    fun dismissCreateDialog() {
        if (_uiState.value.isCreating) return
        _uiState.update {
            it.copy(
                isCreateDialogVisible = false,
                createSlotInput = "",
                createTitleInput = ""
            )
        }
    }

    fun updateCreateSlotInput(value: String) {
        _uiState.update { it.copy(createSlotInput = value, errorMessage = null) }
    }

    fun updateCreateTitleInput(value: String) {
        _uiState.update { it.copy(createTitleInput = value, errorMessage = null) }
    }

    fun createSaveState() {
        val state = _uiState.value
        if (state.isCreating) return

        val slot = state.createSlotInput.trim().toIntOrNull()
        if (slot == null || slot <= 0) {
            _uiState.update { it.copy(errorMessage = "存档槽位必须是大于 0 的整数") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCreating = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            createSaveStateUseCase(
                worldId = state.worldId,
                identityId = state.identityId,
                slot = slot,
                title = state.createTitleInput
            ).fold(
                onSuccess = { created ->
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            isCreateDialogVisible = false,
                            createSlotInput = "",
                            createTitleInput = "",
                            selectedSaveId = created.id,
                            infoMessage = "存档已创建"
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

    fun selectSaveState(saveState: WorldSaveState) {
        if (_uiState.value.isSelecting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSelecting = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            selectSaveStateUseCase(saveState).fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(
                            isSelecting = false,
                            selectedSaveId = updated.id,
                            navigateToChatListSaveId = updated.id
                        )
                    }
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSelecting = false,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigateToChatListSaveId = null) }
    }

    fun consumeInfo() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
