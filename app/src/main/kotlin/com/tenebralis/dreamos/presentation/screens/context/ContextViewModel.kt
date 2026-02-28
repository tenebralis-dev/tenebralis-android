package com.tenebralis.dreamos.presentation.screens.context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.ContextSettings
import com.tenebralis.dreamos.domain.repository.ContextSettingsRepository
import com.tenebralis.dreamos.domain.usecase.context.CleanContextLogsUseCase
import com.tenebralis.dreamos.domain.usecase.context.GetContextLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ContextViewModel @Inject constructor(
    private val getContextLogsUseCase: GetContextLogsUseCase,
    private val cleanContextLogsUseCase: CleanContextLogsUseCase,
    private val contextSettingsRepository: ContextSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContextUiState())
    val uiState: StateFlow<ContextUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadLogs()
    }

    fun onEvent(event: ContextEvent) {
        when (event) {
            is ContextEvent.SelectTab -> _uiState.update { it.copy(selectedTab = event.index) }
            is ContextEvent.UpdateRecentMessageCount -> updateSettings { copy(recentMessageCount = event.count) }
            is ContextEvent.UpdateMemoryTopN -> updateSettings { copy(memoryTopN = event.topN) }
            is ContextEvent.UpdateMaxTokenEstimate -> updateSettings { copy(maxTokenEstimate = event.max) }
            is ContextEvent.ToggleLayer -> toggleLayer(event.layerId)
            is ContextEvent.ToggleAutoLog -> updateSettings { copy(autoLogEnabled = !autoLogEnabled) }
            is ContextEvent.UpdateLogRetentionDays -> updateSettings { copy(logRetentionDays = event.days) }
            is ContextEvent.CleanOldLogs -> cleanOldLogs()
            is ContextEvent.ClearAllLogs -> clearAllLogs()
            is ContextEvent.FilterByConversation -> filterByConversation(event.conversationId)
            is ContextEvent.ClearFilter -> {
                _uiState.update { it.copy(filterConversationId = null) }
                loadLogs()
            }
            is ContextEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            is ContextEvent.ClearInfo -> _uiState.update { it.copy(infoMessage = null) }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            contextSettingsRepository.getAsFlow().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    private fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getContextLogsUseCase().collect { logs ->
                _uiState.update {
                    it.copy(
                        logs = logs,
                        logCount = logs.size,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun filterByConversation(conversationId: String) {
        _uiState.update { it.copy(filterConversationId = conversationId, isLoading = true) }
        viewModelScope.launch {
            getContextLogsUseCase.byConversation(conversationId).collect { logs ->
                _uiState.update {
                    it.copy(
                        logs = logs,
                        logCount = logs.size,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun updateSettings(update: ContextSettings.() -> ContextSettings) {
        viewModelScope.launch {
            val current = _uiState.value.settings
            val newSettings = current.update()
            contextSettingsRepository.update(newSettings)
        }
    }

    private fun toggleLayer(layerId: String) {
        viewModelScope.launch {
            val current = _uiState.value.settings
            val newLayers = if (layerId in current.enabledLayers) {
                current.enabledLayers - layerId
            } else {
                current.enabledLayers + layerId
            }
            contextSettingsRepository.update(current.copy(enabledLayers = newLayers))
        }
    }

    private fun cleanOldLogs() {
        viewModelScope.launch {
            val deleted = cleanContextLogsUseCase()
            _uiState.update { it.copy(infoMessage = "已清理 $deleted 条过期日志") }
        }
    }

    private fun clearAllLogs() {
        viewModelScope.launch {
            cleanContextLogsUseCase.deleteAll()
            _uiState.update { it.copy(infoMessage = "已删除全部日志") }
        }
    }
}
