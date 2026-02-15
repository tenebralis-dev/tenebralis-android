package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.ConversationMessageDto
import com.tenebralis.dreamos.domain.model.ConversationMessage

fun ConversationMessageDto.toDomain() = ConversationMessage(
    id = id,
    userId = userId,
    conversationId = conversationId,
    seq = seq,
    role = role,
    content = content,
    metadataJson = metadataJson,
    createdAt = createdAt
)

fun ConversationMessage.toDto() = ConversationMessageDto(
    id = id,
    userId = userId,
    conversationId = conversationId,
    seq = seq,
    role = role,
    content = content,
    metadataJson = metadataJson,
    createdAt = createdAt
)
