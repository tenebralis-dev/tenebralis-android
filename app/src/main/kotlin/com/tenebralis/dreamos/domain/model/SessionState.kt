package com.tenebralis.dreamos.domain.model

/**
 * 认证会话状态（密封接口）
 *
 * 用于 UI 层订阅 Auth 状态变化，控制路由守卫和启动流程。
 */
sealed interface SessionState {
    /** 正在从本地存储加载 session */
    data object Loading : SessionState

    /** 已认证，携带用户 ID */
    data class Authenticated(val userId: String) : SessionState

    /** 未认证（未登录或已登出） */
    data object NotAuthenticated : SessionState

    /** 认证过程中的错误 */
    data class Error(val message: String) : SessionState
}
