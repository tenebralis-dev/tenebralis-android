package com.tenebralis.dreamos.domain.usecase.npc

import android.net.Uri
import com.tenebralis.dreamos.data.local.ImageCompressor
import com.tenebralis.dreamos.domain.repository.AvatarStorageRepository
import javax.inject.Inject

/**
 * 上传 NPC 头像
 *
 * 流程：客户端压缩 → 上传到 Supabase Storage → 返回存储路径
 */
class UploadNpcAvatarUseCase @Inject constructor(
    private val imageCompressor: ImageCompressor,
    private val avatarStorage: AvatarStorageRepository
) {

    /**
     * @param npcId NPC ID
     * @param imageUri 本地图片 Uri
     * @return 存储桶内路径（如 {user_id}/npc/{npc_id}/avatar.webp）
     */
    suspend operator fun invoke(npcId: String, imageUri: Uri): Result<String> = runCatching {
        require(npcId.isNotBlank()) { "npcId 不能为空" }

        // 1. 压缩图片
        val compressed = imageCompressor.compressForNpcAvatar(imageUri)

        // 2. 上传到 Storage
        avatarStorage.uploadNpcAvatar(
            npcId = npcId,
            data = compressed.bytes,
            mimeType = compressed.mimeType,
            extension = compressed.extension
        ).getOrThrow()
    }
}
