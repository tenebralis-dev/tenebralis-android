package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.domain.model.SessionState
import com.tenebralis.dreamos.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    override val sessionState: Flow<SessionState> =
        supabase.auth.sessionStatus.map { status ->
            // #region agent log
            debugLog(
                runId = "run-1",
                hypothesisId = "H2",
                location = "AuthRepositoryImpl.kt:sessionState.map",
                message = "SessionStatus observed",
                data = mapOf("statusType" to status::class.simpleName)
            )
            // #endregion
            when (status) {
                is SessionStatus.Authenticated -> SessionState.Authenticated(
                    userId = status.session.user?.id ?: ""
                )
                is SessionStatus.NotAuthenticated -> SessionState.NotAuthenticated
                is SessionStatus.Initializing -> SessionState.Loading
                is SessionStatus.RefreshFailure -> SessionState.Error("会话刷新失败")
            }
        }

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        runCatching {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            // #region agent log
            debugLog(
                runId = "run-1",
                hypothesisId = "H1",
                location = "AuthRepositoryImpl.kt:signIn",
                message = "signInWith(Email) executed",
                data = mapOf("emailLength" to email.length)
            )
            // #endregion
        }

    override suspend fun signUp(
        email: String,
        password: String,
        username: String
    ): Result<Unit> = runCatching {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject {
                put("username", username)
            }
        }
        // #region agent log
        debugLog(
            runId = "run-1",
            hypothesisId = "H1",
            location = "AuthRepositoryImpl.kt:signUp",
            message = "signUpWith(Email) executed",
            data = mapOf("usernameLength" to username.length, "emailLength" to email.length)
        )
        // #endregion
    }

    override suspend fun verifyOtp(email: String, token: String): Result<Unit> =
        runCatching {
            supabase.auth.verifyEmailOtp(
                type = OtpType.Email.SIGNUP,
                email = email,
                token = token
            )
            // #region agent log
            debugLog(
                runId = "run-1",
                hypothesisId = "H3",
                location = "AuthRepositoryImpl.kt:verifyOtp",
                message = "verifyEmailOtp executed",
                data = mapOf("tokenLength" to token.length, "emailLength" to email.length)
            )
            // #endregion
            Unit
        }

    override suspend fun resendOtp(email: String): Result<Unit> =
        runCatching {
            supabase.auth.resendEmail(
                type = OtpType.Email.SIGNUP,
                email = email
            )
            // #region agent log
            debugLog(
                runId = "run-1",
                hypothesisId = "H3",
                location = "AuthRepositoryImpl.kt:resendOtp",
                message = "resendEmail executed",
                data = mapOf("emailLength" to email.length)
            )
            // #endregion
        }

    override suspend fun signOut(): Result<Unit> =
        runCatching {
            supabase.auth.signOut()
        }

    override fun getCurrentUserId(): String? =
        supabase.auth.currentUserOrNull()?.id

    override fun getCurrentUserEmail(): String? =
        supabase.auth.currentUserOrNull()?.email

    // #region agent log
    private fun debugLog(
        runId: String,
        hypothesisId: String,
        location: String,
        message: String,
        data: Map<String, Any?>
    ) {
        runCatching {
            val payload = buildJsonObject {
                put("runId", runId)
                put("hypothesisId", hypothesisId)
                put("location", location)
                put("message", message)
                put("timestamp", System.currentTimeMillis())
                put("data", buildJsonObject {
                    data.forEach { (key, value) ->
                        put(key, JsonPrimitive(value?.toString() ?: "null"))
                    }
                })
            }
            File("c:\\kotlindev\\TenebralisApp\\.cursor\\debug.log")
                .appendText(payload.toString() + "\n")
        }
    }
    // #endregion
}
