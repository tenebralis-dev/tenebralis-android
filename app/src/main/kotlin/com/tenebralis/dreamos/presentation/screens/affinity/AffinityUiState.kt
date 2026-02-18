package com.tenebralis.dreamos.presentation.screens.affinity

import com.tenebralis.dreamos.domain.model.Relationship

/**
 * 好感度页面 UI 状态
 */
data class AffinityUiState(
    val relationships: List<Relationship> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
