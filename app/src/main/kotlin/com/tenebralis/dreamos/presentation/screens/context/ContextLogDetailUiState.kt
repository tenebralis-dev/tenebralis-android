package com.tenebralis.dreamos.presentation.screens.context

import com.tenebralis.dreamos.domain.model.ContextLog

data class ContextLogDetailUiState(
    val log: ContextLog? = null,
    val isLoading: Boolean = true,
    val expandedLayers: Set<String> = emptySet(),
    val showFullPrompt: Boolean = false,
    val errorMessage: String? = null
)
