package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.ConversationDto
import com.tenebralis.dreamos.domain.model.Conversation

fun ConversationDto.toDomain() = Conversation(
    id = id,
    userId = userId,
    saveId = saveId,
    npcId = npcId,
    threadKey = threadKey,
    title = title,
    summary = summary,
    pinnedContextText = pinnedContextText,
    presetId = presetId,
    apiConnectionId = apiConnectionId,
    lastMessageAt = lastMessageAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Conversation.toDto() = ConversationDto(
    id = id,
    userId = userId,
    saveId = saveId,
    npcId = npcId,
    threadKey = threadKey,
    title = title,
    summary = summary,
    pinnedContextText = pinnedContextText,
    presetId = presetId,
    apiConnectionId = apiConnectionId,
    lastMessageAt = lastMessageAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)
