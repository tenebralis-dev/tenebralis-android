package com.tenebralis.dreamos.presentation.screens.context

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.usecase.context.GetContextLogsUseCase
import com.tenebralis.dreamos.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ContextLogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getContextLogsUseCase: GetContextLogsUseCase
) : ViewModel() {

    private val logId: Long = savedStateHandle.get<String>(Screen.ContextLogDetail.ARG_LOG_ID)
        ?.toLongOrNull() ?: -1L

    private val _uiState = MutableStateFlow(ContextLogDetailUiState())
    val uiState: StateFlow<ContextLogDetailUiState> = _uiState.asStateFlow()

    init {
        loadLog()
    }

    private fun loadLog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val log = getContextLogsUseCase.getById(logId)
            _uiState.update {
                it.copy(
                    log = log,
                    isLoading = false,
                    errorMessage = if (log == null) "日志未找到" else null
                )
            }
        }
    }

    fun toggleLayer(layerId: String) {
        _uiState.update {
            val newSet = if (layerId in it.expandedLayers) {
                it.expandedLayers - layerId
            } else {
                it.expandedLayers + layerId
            }
            it.copy(expandedLayers = newSet)
        }
    }

    fun toggleFullPrompt() {
        _uiState.update { it.copy(showFullPrompt = !it.showFullPrompt) }
    }
}
