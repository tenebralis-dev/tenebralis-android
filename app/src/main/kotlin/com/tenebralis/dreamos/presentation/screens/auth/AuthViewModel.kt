package com.tenebralis.dreamos.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.RememberedCredentialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val rememberedCredentialRepository: RememberedCredentialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    init {
        preloadRememberedCredential()
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged ->
                _uiState.update { it.copy(email = event.email, errorMessage = null) }

            is AuthEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.password, errorMessage = null) }

            is AuthEvent.UsernameChanged ->
                _uiState.update { it.copy(username = event.username, errorMessage = null) }

            is AuthEvent.OtpCodeChanged ->
                _uiState.update { it.copy(otpCode = event.code, errorMessage = null) }

            AuthEvent.ToggleAuthMode ->
                _uiState.update {
                    it.copy(
                        isLogin = !it.isLogin,
                        isOtpStep = false,
                        errorMessage = null,
                        otpCode = ""
                    )
                }

            AuthEvent.TogglePasswordVisibility ->
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

            is AuthEvent.RememberMeChanged -> {
                _uiState.update { it.copy(rememberMe = event.rememberMe) }
                if (!event.rememberMe) {
                    viewModelScope.launch {
                        rememberedCredentialRepository.clearRememberedCredential()
                    }
                }
            }

            AuthEvent.Submit -> submit()
            AuthEvent.VerifyOtp -> verifyOtp()
            AuthEvent.ResendOtp -> resendOtp()

            AuthEvent.BackFromOtp ->
                _uiState.update { it.copy(isOtpStep = false, otpCode = "", errorMessage = null) }

            AuthEvent.ClearError ->
                _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun submit() {
        val state = _uiState.value

        // 前端校验
        val validationError = validateForm(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = if (state.isLogin) {
                authRepository.signIn(state.email.trim(), state.password)
            } else {
                authRepository.signUp(state.email.trim(), state.password, state.username.trim())
            }

            result.fold(
                onSuccess = {
                    if (state.isLogin) {
                        // 登录成功 → sessionState 自动触发路由跳转
                        syncRememberedCredential(state)
                        _uiState.update { it.copy(isLoading = false) }
                    } else {
                        // 注册成功 → 进入 OTP 验证步骤
                        _uiState.update {
                            it.copy(isLoading = false, isOtpStep = true, otpCode = "")
                        }
                        startResendCooldown()
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = parseAuthError(error)
                        )
                    }
                }
            )
        }
    }

    private fun verifyOtp() {
        val state = _uiState.value

        if (state.otpCode.length != 6) {
            _uiState.update { it.copy(errorMessage = "请输入 6 位验证码") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.verifyOtp(state.email.trim(), state.otpCode.trim())
                .fold(
                    onSuccess = {
                        // 验证成功 → sessionState 自动触发路由跳转
                        _uiState.update { it.copy(isLoading = false) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = parseOtpError(error)
                            )
                        }
                    }
                )
        }
    }

    private fun resendOtp() {
        val state = _uiState.value
        if (state.resendCooldownSeconds > 0) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.resendOtp(state.email.trim())
                .fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false) }
                        startResendCooldown()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "重新发送失败：${error.localizedMessage ?: "未知错误"}"
                            )
                        }
                    }
                )
        }
    }

    /** 启动 60 秒重发冷却倒计时 */
    private fun startResendCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (i in 60 downTo 0) {
                _uiState.update { it.copy(resendCooldownSeconds = i) }
                if (i > 0) delay(1000L)
            }
        }
    }

    private fun preloadRememberedCredential() {
        viewModelScope.launch {
            rememberedCredentialRepository.observeRememberedCredential()
                .first()
                ?.let { credential ->
                    _uiState.update {
                        it.copy(
                            email = credential.email,
                            password = credential.password,
                            rememberMe = true
                        )
                    }
                }
        }
    }

    private fun syncRememberedCredential(state: AuthUiState) {
        viewModelScope.launch {
            if (state.rememberMe) {
                rememberedCredentialRepository.saveRememberedCredential(
                    email = state.email.trim(),
                    password = state.password
                )
            } else {
                rememberedCredentialRepository.clearRememberedCredential()
            }
        }
    }

    // ── 表单校验 ──

    private fun validateForm(state: AuthUiState): String? {
        val email = state.email.trim()
        val password = state.password

        if (email.isBlank()) return "请输入邮箱地址"
        if (!isValidEmail(email)) return "邮箱格式不正确"
        if (password.length < 6) return "密码长度至少 6 位"

        if (!state.isLogin) {
            val username = state.username.trim()
            if (username.isBlank()) return "请输入用户名"
            if (username.length < 2) return "用户名至少 2 个字符"
        }

        return null
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // ── 错误解析 ──

    private fun parseAuthError(error: Throwable): String {
        val message = error.message?.lowercase() ?: return "未知错误"
        return when {
            "invalid login credentials" in message -> "邮箱或密码错误"
            "email not confirmed" in message -> "邮箱尚未验证"
            "user already registered" in message -> "该邮箱已注册"
            "network" in message || "timeout" in message -> "网络连接失败，请检查网络"
            "rate limit" in message -> "操作过于频繁，请稍后再试"
            else -> error.localizedMessage ?: "认证失败"
        }
    }

    private fun parseOtpError(error: Throwable): String {
        val message = error.message?.lowercase() ?: return "验证失败"
        return when {
            "otp" in message && ("expired" in message || "invalid" in message) ->
                "验证码无效或已过期，请重新获取"
            "network" in message || "timeout" in message -> "网络连接失败"
            else -> error.localizedMessage ?: "验证码验证失败"
        }
    }
}
