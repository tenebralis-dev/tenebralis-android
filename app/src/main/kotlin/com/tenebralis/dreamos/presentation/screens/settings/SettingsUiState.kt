package com.tenebralis.dreamos.presentation.screens.settings

data class SettingsUiState(
    val isLogoutDialogVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    // 用户信息
    val username: String? = null,
    val email: String? = null,
    val createdAt: String? = null,
    val isUserLoading: Boolean = true,
    // 用户协议弹窗
    val isAgreementDialogVisible: Boolean = false
)
