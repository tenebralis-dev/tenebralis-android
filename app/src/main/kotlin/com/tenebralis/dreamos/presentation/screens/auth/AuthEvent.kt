package com.tenebralis.dreamos.presentation.screens.auth

/**
 * Auth 页面 UI 事件
 */
sealed interface AuthEvent {
    // ── 表单输入 ──
    data class EmailChanged(val email: String) : AuthEvent
    data class PasswordChanged(val password: String) : AuthEvent
    data class UsernameChanged(val username: String) : AuthEvent
    data class OtpCodeChanged(val code: String) : AuthEvent

    // ── 模式切换 ──
    /** 登录 ↔ 注册模式切换 */
    data object ToggleAuthMode : AuthEvent

    /** 密码可见性切换 */
    data object TogglePasswordVisibility : AuthEvent

    /** “记住我”勾选状态变化 */
    data class RememberMeChanged(val rememberMe: Boolean) : AuthEvent

    // ── 提交操作 ──
    /** 提交登录或注册 */
    data object Submit : AuthEvent

    /** 提交 OTP 验证码 */
    data object VerifyOtp : AuthEvent

    /** 重新发送 OTP 验证码 */
    data object ResendOtp : AuthEvent

    // ── 导航 ──
    /** 从 OTP 步骤返回注册页面 */
    data object BackFromOtp : AuthEvent

    // ── 状态清理 ──
    data object ClearError : AuthEvent
}
