package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.UserDto
import com.tenebralis.dreamos.domain.model.User
import com.tenebralis.dreamos.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : UserRepository {

    override fun getCurrentUser(): Flow<Result<User>> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: return@flow emit(Result.failure(Exception("未登录")))

        val dto = supabase.from("users")
            .select { filter { eq("id", userId) } }
            .decodeSingle<UserDto>()

        emit(Result.success(dto.toDomain()))
    }.catch { emit(Result.failure(it)) }

    override suspend fun updateProfile(user: User): Result<User> = runCatching {
        val dto = supabase.from("users")
            .update(user.toDto()) {
                filter { eq("id", user.id) }
                select()
            }
            .decodeSingle<UserDto>()

        dto.toDomain()
    }
}
