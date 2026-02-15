package com.tenebralis.dreamos.presentation.screens.settings

data class SettingsUiState(
    val isLogoutDialogVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)
