package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

/**
 * 认证仓库接口
 *
 * 提供登录、注册（含 OTP 验证）、登出和 session 状态监听。
 */
interface AuthRepository {

    /** 认证状态流，UI 层订阅以控制路由守卫 */
    val sessionState: Flow<SessionState>

    /** 邮箱 + 密码登录 */
    suspend fun signIn(email: String, password: String): Result<Unit>

    /**
     * 邮箱 + 密码注册
     *
     * 注册成功后 Supabase 会发送 OTP 验证码到用户邮箱，
     * 需要调用 [verifyOtp] 完成验证。
     */
    suspend fun signUp(email: String, password: String, username: String): Result<Unit>

    /**
     * 验证邮箱 OTP 验证码
     *
     * @param email 注册时使用的邮箱
     * @param token 用户收到的 6 位验证码
     */
    suspend fun verifyOtp(email: String, token: String): Result<Unit>

    /**
     * 重新发送 OTP 验证码
     *
     * @param email 注册时使用的邮箱
     */
    suspend fun resendOtp(email: String): Result<Unit>

    /** 登出 */
    suspend fun signOut(): Result<Unit>

    /** 获取当前用户 ID（未登录返回 null） */
    fun getCurrentUserId(): String?

    /** 获取当前用户邮箱（未登录返回 null） */
    fun getCurrentUserEmail(): String?
}
