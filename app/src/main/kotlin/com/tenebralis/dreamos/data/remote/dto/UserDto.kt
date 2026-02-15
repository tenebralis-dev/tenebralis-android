package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * users 表 DTO
 *
 * 注意：注册时由 Supabase trigger（handle_new_user）自动创建，客户端不手动 insert。
 */
@Serializable
data class UserDto(
    val id: String,
    val username: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null,
    @SerialName("system_level") val systemLevel: Int = 1,
    @SerialName("exp_points") val expPoints: Long = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
