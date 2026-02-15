package com.tenebralis.dreamos.presentation.screens.settings

sealed interface SettingsEvent {
    data object ShowLogoutDialog : SettingsEvent
    data object DismissLogoutDialog : SettingsEvent
    data object ConfirmLogout : SettingsEvent
    data object ClearError : SettingsEvent
}
