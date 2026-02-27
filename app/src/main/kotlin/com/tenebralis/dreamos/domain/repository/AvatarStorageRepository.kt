package com.tenebralis.dreamos.domain.repository

/**
 * 头像存储仓库接口
 *
 * 封装 Supabase Storage 的上传 / 删除 / 签名 URL 操作
 */
interface AvatarStorageRepository {

    /** 上传 NPC 头像，返回存储路径（桶内路径） */
    suspend fun uploadNpcAvatar(npcId: String, data: ByteArray, mimeType: String, extension: String): Result<String>

    /** 删除 NPC 头像 */
    suspend fun deleteNpcAvatar(npcId: String): Result<Unit>

    /** 为存储路径生成签名 URL（1 小时有效期） */
    suspend fun createSignedUrl(path: String): Result<String>
}
