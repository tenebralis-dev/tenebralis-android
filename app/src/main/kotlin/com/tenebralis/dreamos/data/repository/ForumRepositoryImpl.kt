package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.ForumCommentDto
import com.tenebralis.dreamos.data.remote.dto.ForumPostDto
import com.tenebralis.dreamos.domain.model.ForumComment
import com.tenebralis.dreamos.domain.model.ForumPost
import com.tenebralis.dreamos.domain.model.enums.AuthorType
import com.tenebralis.dreamos.domain.model.enums.CommentingMode
import com.tenebralis.dreamos.domain.model.enums.ForumVisibility
import com.tenebralis.dreamos.domain.repository.ForumRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ForumRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ForumRepository {

    // ─── 帖子 ─────────────────────────────────────────────

    override fun getPosts(worldId: String?): Flow<Result<List<ForumPost>>> =
        flow {
            emit(runCatching {
                val userId = requireCurrentUserId()
                supabase.from(TABLE_POSTS)
                    .select {
                        filter {
                            eq("user_id", userId)
                            if (worldId != null) {
                                eq("world_id", worldId)
                            }
                        }
                        order("is_pinned", Order.DESCENDING)
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<ForumPostDto>()
                    .map { it.toDomain() }
            })
        }.catch { emit(Result.failure(it)) }

    override suspend fun getPostById(postId: String): Result<ForumPost> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_POSTS)
            .select {
                filter {
                    eq("id", postId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<ForumPostDto>()
            .toDomain()
    }

    override suspend fun createPost(
        worldId: String,
        title: String,
        content: String,
        authorType: AuthorType,
        worldNpcPersonaId: String?,
        visibility: ForumVisibility,
        commentingMode: CommentingMode
    ): Result<ForumPost> = runCatching {
        val userId = requireCurrentUserId()

        // NPC 校验：author_type = NPC 时 world_npc_persona_id 非空
        if (authorType == AuthorType.NPC) {
            require(!worldNpcPersonaId.isNullOrBlank()) {
                "NPC 发帖必须指定 world_npc_persona_id"
            }
        }

        val post = ForumPost(
            id = UUID.randomUUID().toString(),
            userId = userId,
            worldId = worldId,
            authorType = authorType,
            worldNpcPersonaId = worldNpcPersonaId,
            title = title,
            content = content,
            visibility = visibility,
            commentingMode = commentingMode
        )
        supabase.from(TABLE_POSTS)
            .insert(post.toDto()) { select() }
            .decodeSingle<ForumPostDto>()
            .toDomain()
    }

    // ─── 评论 ─────────────────────────────────────────────

    override fun getComments(postId: String): Flow<Result<List<ForumComment>>> =
        flow {
            emit(runCatching {
                val userId = requireCurrentUserId()
                supabase.from(TABLE_COMMENTS)
                    .select {
                        filter {
                            eq("post_id", postId)
                            eq("user_id", userId)
                        }
                        order("created_at", Order.ASCENDING)
                    }
                    .decodeList<ForumCommentDto>()
                    .map { it.toDomain() }
            })
        }.catch { emit(Result.failure(it)) }

    override suspend fun createComment(
        postId: String,
        content: String,
        authorType: AuthorType,
        worldNpcPersonaId: String?,
        parentCommentId: String?
    ): Result<ForumComment> = runCatching {
        val userId = requireCurrentUserId()

        // NPC 校验
        if (authorType == AuthorType.NPC) {
            require(!worldNpcPersonaId.isNullOrBlank()) {
                "NPC 评论必须指定 world_npc_persona_id"
            }
        }

        val comment = ForumComment(
            id = UUID.randomUUID().toString(),
            userId = userId,
            postId = postId,
            parentCommentId = parentCommentId,
            authorType = authorType,
            worldNpcPersonaId = worldNpcPersonaId,
            content = content
        )
        supabase.from(TABLE_COMMENTS)
            .insert(comment.toDto()) { select() }
            .decodeSingle<ForumCommentDto>()
            .toDomain()
    }

    // ─── 内部 ─────────────────────────────────────────────

    private fun requireCurrentUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")

    private companion object {
        const val TABLE_POSTS = "forum_posts"
        const val TABLE_COMMENTS = "forum_comments"
    }
}
