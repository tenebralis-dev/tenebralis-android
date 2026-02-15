package com.tenebralis.dreamos.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.ShowLogoutDialog -> {
                _uiState.update { it.copy(isLogoutDialogVisible = true) }
            }
            SettingsEvent.DismissLogoutDialog -> {
                if (_uiState.value.isSubmitting) return
                _uiState.update { it.copy(isLogoutDialogVisible = false) }
            }
            SettingsEvent.ConfirmLogout -> confirmLogout()
            SettingsEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun confirmLogout() {
        if (_uiState.value.isSubmitting) return

        _uiState.update {
            it.copy(
                isSubmitting = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            authRepository.signOut()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isLogoutDialogVisible = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.localizedMessage ?: "登出失败，请稍后重试"
                        )
                    }
                }
        }
    }
}
