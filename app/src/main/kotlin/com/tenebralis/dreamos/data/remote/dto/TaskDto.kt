package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * tasks 表 DTO
 */
@Serializable
data class TaskDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("world_id") val worldId: String? = null,
    val name: String,
    val description: String? = null,
    @SerialName("task_type") val taskType: String = "side",
    @SerialName("scope_type") val scopeType: String,
    @SerialName("prompt_task_text") val promptTaskText: String? = null,
    @SerialName("criteria_json") val criteriaJson: String = "{}",
    @SerialName("reward_json") val rewardJson: String = "{}",
    @SerialName("created_source") val createdSource: String = "manual",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
