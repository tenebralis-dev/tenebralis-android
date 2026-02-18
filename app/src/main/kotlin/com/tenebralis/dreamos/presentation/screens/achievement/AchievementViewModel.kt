package com.tenebralis.dreamos.presentation.screens.achievement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.enums.AchievementStatus
import com.tenebralis.dreamos.domain.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    fun onEvent(event: AchievementEvent) {
        when (event) {
            AchievementEvent.Refresh -> loadAchievements()
            is AchievementEvent.SwitchTab -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
                loadAchievements()
            }
            AchievementEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val statusFilter = when (_uiState.value.selectedTab) {
                AchievementTab.ALL -> null
                AchievementTab.UNLOCKED -> AchievementStatus.UNLOCKED
                AchievementTab.LOCKED -> AchievementStatus.LOCKED
            }

            achievementRepository.getUserAchievements(statusFilter).collect { result ->
                result.onSuccess { achievements ->
                    _uiState.update {
                        it.copy(userAchievements = achievements, isLoading = false)
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "加载成就失败", isLoading = false)
                    }
                }
            }
        }
    }
}
