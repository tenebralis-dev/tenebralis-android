package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType

/**
 * 用户备忘（领域模型）
 *
 * 对应表：user_notes
 */
data class UserNote(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val scopeType: ScopeType = ScopeType.GLOBAL,
    val scopeId: String? = null,
    val aiVisibility: AiVisibility = AiVisibility.PRIVATE,
    val isPinned: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
