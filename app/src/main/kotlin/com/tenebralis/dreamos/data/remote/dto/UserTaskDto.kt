package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * user_tasks 表 DTO
 */
@Serializable
data class UserTaskDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("task_id") val taskId: String,
    @SerialName("scope_type") val scopeType: String,
    @SerialName("save_id") val saveId: String? = null,
    val status: String = "not_started",
    @SerialName("progress_json") val progressJson: String = "{}",
    @SerialName("progress_value") val progressValue: Double = 0.0,
    @SerialName("evidence_json") val evidenceJson: String = "{}",
    @SerialName("last_evaluated_at") val lastEvaluatedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
