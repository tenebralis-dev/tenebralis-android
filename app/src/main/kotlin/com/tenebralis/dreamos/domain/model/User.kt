package com.tenebralis.dreamos.domain.model

/**
 * 用户档案（领域模型）
 *
 * 对应表：users
 */
data class User(
    val id: String,
    val username: String?,
    val displayName: String?,
    val avatarUrl: String?,
    val bio: String?,
    val systemLevel: Int,
    val expPoints: Long,
    val createdAt: String?,
    val updatedAt: String?
)
