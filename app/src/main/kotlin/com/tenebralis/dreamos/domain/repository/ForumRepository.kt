package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.ForumComment
import com.tenebralis.dreamos.domain.model.ForumPost
import com.tenebralis.dreamos.domain.model.enums.AuthorType
import com.tenebralis.dreamos.domain.model.enums.CommentingMode
import com.tenebralis.dreamos.domain.model.enums.ForumVisibility
import kotlinx.coroutines.flow.Flow

/**
 * 论坛仓库接口
 *
 * 对应表：forum_posts + forum_comments
 */
interface ForumRepository {

    /** 获取帖子列表（按世界筛选，置顶优先+时间倒序） */
    fun getPosts(worldId: String? = null): Flow<Result<List<ForumPost>>>

    /** 获取单条帖子 */
    suspend fun getPostById(postId: String): Result<ForumPost>

    /** 发帖 */
    suspend fun createPost(
        worldId: String,
        title: String,
        content: String,
        authorType: AuthorType = AuthorType.USER,
        worldNpcPersonaId: String? = null,
        visibility: ForumVisibility = ForumVisibility.WORLD,
        commentingMode: CommentingMode = CommentingMode.ALL
    ): Result<ForumPost>

    /** 获取帖子评论列表 */
    fun getComments(postId: String): Flow<Result<List<ForumComment>>>

    /** 发表评论 */
    suspend fun createComment(
        postId: String,
        content: String,
        authorType: AuthorType = AuthorType.USER,
        worldNpcPersonaId: String? = null,
        parentCommentId: String? = null
    ): Result<ForumComment>
}
