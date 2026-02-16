package com.tenebralis.dreamos.domain.usecase.connection

data class ConnectionTestResult(
    val success: Boolean,
    val statusCode: Int?,
    val elapsedMs: Long,
    val message: String
)
