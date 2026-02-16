package com.tenebralis.dreamos.presentation.screens.world

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.World
import com.tenebralis.dreamos.domain.usecase.common.UseCaseErrorMapper
import com.tenebralis.dreamos.domain.usecase.world.CreateWorldUseCase
import com.tenebralis.dreamos.domain.usecase.world.GetWorldsUseCase
import com.tenebralis.dreamos.domain.usecase.world.SelectWorldUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorldUiState(
    val worlds: List<World> = emptyList(),
    val selectedWorldId: String? = null,
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isSelecting: Boolean = false,
    val isCreateDialogVisible: Boolean = false,
    val createName: String = "",
    val createDescription: String = "",
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val navigateToWorldId: String? = null
) {
    val emptyState: Boolean get() = !isLoading && worlds.isEmpty()
}

@HiltViewModel
class WorldViewModel @Inject constructor(
    private val getWorldsUseCase: GetWorldsUseCase,
    private val createWorldUseCase: CreateWorldUseCase,
    private val selectWorldUseCase: SelectWorldUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorldUiState())
    val uiState: StateFlow<WorldUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getWorldsUseCase().fold(
                onSuccess = { worlds ->
                    _uiState.update { state ->
                        val selectedId = state.selectedWorldId?.takeIf { selected ->
                            worlds.any { it.id == selected }
                        } ?: worlds.firstOrNull()?.id
                        state.copy(
                            isLoading = false,
                            worlds = worlds,
                            selectedWorldId = selectedId
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
        _uiState.update {
            it.copy(
                isCreateDialogVisible = true,
                errorMessage = null
            )
        }
    }

    fun dismissCreateDialog() {
        if (_uiState.value.isCreating) return
        _uiState.update {
            it.copy(
                isCreateDialogVisible = false,
                createName = "",
                createDescription = ""
            )
        }
    }

    fun updateCreateName(value: String) {
        _uiState.update { it.copy(createName = value, errorMessage = null) }
    }

    fun updateCreateDescription(value: String) {
        _uiState.update { it.copy(createDescription = value, errorMessage = null) }
    }

    fun createWorld() {
        val state = _uiState.value
        if (state.isCreating) return

        val name = state.createName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请输入世界名称") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null, infoMessage = null) }
            createWorldUseCase(
                name = state.createName,
                description = state.createDescription
            ).fold(
                onSuccess = { created ->
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            isCreateDialogVisible = false,
                            createName = "",
                            createDescription = "",
                            selectedWorldId = created.id,
                            infoMessage = "世界已创建"
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

    fun selectWorld(worldId: String) {
        if (_uiState.value.isSelecting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSelecting = true, errorMessage = null, infoMessage = null) }
            selectWorldUseCase(worldId).fold(
                onSuccess = { world ->
                    _uiState.update {
                        it.copy(
                            isSelecting = false,
                            selectedWorldId = world.id,
                            navigateToWorldId = world.id
                        )
                    }
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
        _uiState.update { it.copy(navigateToWorldId = null) }
    }

    fun consumeInfo() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
