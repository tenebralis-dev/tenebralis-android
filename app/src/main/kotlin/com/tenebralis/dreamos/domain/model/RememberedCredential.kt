package com.tenebralis.dreamos.domain.model

/**
 * 本地缓存的登录凭证。
 */
data class RememberedCredential(
    val email: String,
    val password: String
)
