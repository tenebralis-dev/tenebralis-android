package com.tenebralis.dreamos.presentation.screens.identity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.UserWorldIdentity
import com.tenebralis.dreamos.domain.usecase.common.UseCaseErrorMapper
import com.tenebralis.dreamos.domain.usecase.identity.CreateIdentityUseCase
import com.tenebralis.dreamos.domain.usecase.identity.GetIdentitiesUseCase
import com.tenebralis.dreamos.domain.usecase.identity.SetActiveIdentityUseCase
import com.tenebralis.dreamos.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IdentityUiState(
    val worldId: String = "",
    val identities: List<UserWorldIdentity> = emptyList(),
    val activeIdentityId: String? = null,
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isSwitchingActive: Boolean = false,
    val isCreateDialogVisible: Boolean = false,
    val createIdentityName: String = "",
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val navigateToIdentityId: String? = null
) {
    val emptyState: Boolean get() = !isLoading && identities.isEmpty()
}

@HiltViewModel
class IdentityViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getIdentitiesUseCase: GetIdentitiesUseCase,
    private val createIdentityUseCase: CreateIdentityUseCase,
    private val setActiveIdentityUseCase: SetActiveIdentityUseCase
) : ViewModel() {

    private val worldId: String = checkNotNull(savedStateHandle[Screen.Identity.ARG_WORLD_ID])

    private val _uiState = MutableStateFlow(IdentityUiState(worldId = worldId))
    val uiState: StateFlow<IdentityUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getIdentitiesUseCase(worldId).fold(
                onSuccess = { identities ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            identities = identities,
                            activeIdentityId = identities.firstOrNull { item -> item.isActive }?.id
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
        _uiState.update { it.copy(isCreateDialogVisible = true, errorMessage = null) }
    }

    fun dismissCreateDialog() {
        if (_uiState.value.isCreating) return
        _uiState.update { it.copy(isCreateDialogVisible = false, createIdentityName = "") }
    }

    fun updateCreateIdentityName(value: String) {
        _uiState.update { it.copy(createIdentityName = value, errorMessage = null) }
    }

    fun createIdentity() {
        val state = _uiState.value
        if (state.isCreating) return

        val name = state.createIdentityName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请输入身份名称") }
            return
        }

        val shouldSetActive = state.identities.none { it.isActive }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCreating = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            createIdentityUseCase(
                worldId = state.worldId,
                identityName = name,
                setActive = shouldSetActive
            ).fold(
                onSuccess = { created ->
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            isCreateDialogVisible = false,
                            createIdentityName = "",
                            infoMessage = "身份已创建",
                            activeIdentityId = if (created.isActive) created.id else it.activeIdentityId
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

    fun selectIdentity(identity: UserWorldIdentity) {
        if (_uiState.value.isSwitchingActive) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSwitchingActive = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }

            val result = if (identity.isActive) {
                Result.success(Unit)
            } else {
                setActiveIdentityUseCase(worldId = worldId, identityId = identity.id)
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSwitchingActive = false,
                            activeIdentityId = identity.id,
                            navigateToIdentityId = identity.id
                        )
                    }
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSwitchingActive = false,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigateToIdentityId = null) }
    }

    fun consumeInfo() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
