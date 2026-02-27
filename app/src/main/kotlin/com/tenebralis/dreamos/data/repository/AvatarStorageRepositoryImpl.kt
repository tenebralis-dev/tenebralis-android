package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.domain.repository.AvatarStorageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

/**
 * Supabase Storage 头像仓库实现
 *
 * 桶名：avatars（私有）
 * 路径规则：{user_id}/npc/{npc_id}/avatar.{ext}
 */
class AvatarStorageRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AvatarStorageRepository {

    override suspend fun uploadNpcAvatar(
        npcId: String,
        data: ByteArray,
        mimeType: String,
        extension: String
    ): Result<String> = runCatching {
        val userId = requireCurrentUserId()
        val path = "$userId/npc/$npcId/avatar.$extension"

        supabase.storage.from(BUCKET_AVATARS).upload(path, data) {
            upsert = true
        }

        path
    }

    override suspend fun deleteNpcAvatar(npcId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        val path = "$userId/npc/$npcId/avatar.webp"
        supabase.storage.from(BUCKET_AVATARS).delete(listOf(path))
    }

    override suspend fun createSignedUrl(path: String): Result<String> = runCatching {
        supabase.storage.from(BUCKET_AVATARS)
            .createSignedUrl(path, 1.hours)
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val BUCKET_AVATARS = "avatars"
    }
}
