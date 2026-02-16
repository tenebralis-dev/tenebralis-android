package com.tenebralis.dreamos.domain.usecase.connection

import kotlinx.serialization.json.JsonObject

data class ConnectionDraft(
    val name: String,
    val serviceType: String,
    val baseUrl: String,
    val defaultModel: String?,
    val systemPrompt: String?,
    val paramsJson: JsonObject,
    val headersTemplateJson: JsonObject,
    val configJson: JsonObject = JsonObject(emptyMap())
)
