package com.tenebralis.dreamos.presentation.screens.forum

import com.tenebralis.dreamos.domain.model.ForumComment
import com.tenebralis.dreamos.domain.model.ForumPost

/**
 * 论坛页面 UI 状态
 */
data class ForumUiState(
    val posts: List<ForumPost> = emptyList(),
    val selectedPost: ForumPost? = null,
    val comments: List<ForumComment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
