package com.tenebralis.dreamos.data.remote.dto

import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * user_notes 表 DTO
 */
@Serializable
data class UserNoteDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    @SerialName("scope_type") val scopeType: ScopeType = ScopeType.GLOBAL,
    @SerialName("scope_id") val scopeId: String? = null,
    @SerialName("ai_visibility") val aiVisibility: AiVisibility = AiVisibility.PRIVATE,
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
