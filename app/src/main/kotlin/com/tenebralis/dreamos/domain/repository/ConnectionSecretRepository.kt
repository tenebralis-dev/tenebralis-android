package com.tenebralis.dreamos.domain.repository

/**
 * 连接密钥本地安全存储。
 *
 * API Key 不入库，仅按 connectionId 在本地加密保存。
 */
interface ConnectionSecretRepository {

    /** 保存或更新指定连接的 API Key。 */
    suspend fun saveSecret(connectionId: String, apiKey: String): Result<Unit>

    /** 读取指定连接的 API Key；不存在时返回 null。 */
    suspend fun getSecret(connectionId: String): Result<String?>

    /** 清除指定连接的 API Key。 */
    suspend fun clearSecret(connectionId: String): Result<Unit>
}
