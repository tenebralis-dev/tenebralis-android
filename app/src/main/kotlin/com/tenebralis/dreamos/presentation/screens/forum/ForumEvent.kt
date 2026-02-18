package com.tenebralis.dreamos.presentation.screens.forum

import com.tenebralis.dreamos.domain.model.enums.AuthorType
import com.tenebralis.dreamos.domain.model.enums.CommentingMode
import com.tenebralis.dreamos.domain.model.enums.ForumVisibility

/**
 * 论坛页面事件
 */
sealed interface ForumEvent {
    data object RefreshPosts : ForumEvent
    data class LoadPostDetail(val postId: String) : ForumEvent
    data class CreatePost(
        val worldId: String,
        val title: String,
        val content: String,
        val authorType: AuthorType = AuthorType.USER,
        val worldNpcPersonaId: String? = null,
        val visibility: ForumVisibility = ForumVisibility.WORLD,
        val commentingMode: CommentingMode = CommentingMode.ALL
    ) : ForumEvent
    data class CreateComment(
        val postId: String,
        val content: String,
        val authorType: AuthorType = AuthorType.USER,
        val worldNpcPersonaId: String? = null,
        val parentCommentId: String? = null
    ) : ForumEvent
    data object DismissError : ForumEvent
    data object DismissSuccess : ForumEvent
}
