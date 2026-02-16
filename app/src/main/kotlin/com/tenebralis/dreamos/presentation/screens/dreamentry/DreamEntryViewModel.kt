package com.tenebralis.dreamos.presentation.screens.dreamentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.usecase.dream.DreamEntryDestination
import com.tenebralis.dreamos.domain.usecase.dream.ResolveDreamEntryUseCase
import com.tenebralis.dreamos.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DreamEntryViewModel @Inject constructor(
    private val resolveDreamEntryUseCase: ResolveDreamEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DreamEntryUiState())
    val uiState: StateFlow<DreamEntryUiState> = _uiState.asStateFlow()

    init {
        resolveDreamEntry()
    }

    fun retry() {
        resolveDreamEntry()
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigateRoute = null) }
    }

    private fun resolveDreamEntry() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isResolving = true,
                    errorMessage = null,
                    navigateRoute = null
                )
            }

            val destination = runCatching { resolveDreamEntryUseCase() }
                .getOrElse { error ->
                    val message = error.message?.trim().orEmpty()
                    DreamEntryDestination.Error(
                        message.ifEmpty { "解析梦境入口失败，请重试" }
                    )
                }
            _uiState.update { state ->
                when (destination) {
                    DreamEntryDestination.WorldSelection ->
                        state.copy(isResolving = false, navigateRoute = Screen.World.route)

                    is DreamEntryDestination.IdentitySelection ->
                        state.copy(
                            isResolving = false,
                            navigateRoute = Screen.Identity.createRoute(destination.worldId)
                        )

                    is DreamEntryDestination.SaveSelection ->
                        state.copy(
                            isResolving = false,
                            navigateRoute = Screen.SaveSelect.createRoute(
                                worldId = destination.worldId,
                                identityId = destination.identityId
                            )
                        )

                    is DreamEntryDestination.ConversationSelection ->
                        state.copy(
                            isResolving = false,
                            navigateRoute = Screen.ChatList.createRoute(destination.saveId)
                        )

                    is DreamEntryDestination.Error ->
                        state.copy(
                            isResolving = false,
                            errorMessage = destination.message
                                .ifBlank { "解析梦境入口失败，请重试" }
                        )
                }
            }
        }
    }
}
