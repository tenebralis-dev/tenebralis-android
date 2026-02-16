package com.tenebralis.dreamos.presentation.screens.dreamentry

data class DreamEntryUiState(
    val isResolving: Boolean = true,
    val errorMessage: String? = null,
    val navigateRoute: String? = null
)
