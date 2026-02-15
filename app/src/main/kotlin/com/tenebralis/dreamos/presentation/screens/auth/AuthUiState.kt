package com.tenebralis.dreamos.presentation.screens.auth

/**
 * Auth 页面 UI 状态
 *
 * 支持三步流程：登录 → 注册 → OTP 验证
 */
data class AuthUiState(
    // ── 表单字段 ──
    val email: String = "",
    val password: String = "",
    val username: String = "",         // 仅注册时使用
    val otpCode: String = "",          // OTP 验证码

    // ── 模式与步骤 ──
    val isLogin: Boolean = true,       // true = 登录模式, false = 注册模式
    val isOtpStep: Boolean = false,    // true = 正在进行 OTP 验证步骤

    // ── 加载与错误 ──
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // ── UI 交互 ──
    val isPasswordVisible: Boolean = false,

    // ── OTP 重发倒计时 ──
    val resendCooldownSeconds: Int = 0 // > 0 时禁用重发按钮
)
