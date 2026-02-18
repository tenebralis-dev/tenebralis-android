package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.UserNote
import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType
import kotlinx.coroutines.flow.Flow

/**
 * 备忘仓库接口
 *
 * 对应表：user_notes
 */
interface NoteRepository {

    /** 获取当前用户的所有备忘 */
    fun getAll(): Flow<Result<List<UserNote>>>

    /** 创建新备忘 */
    suspend fun create(note: UserNote): Result<UserNote>

    /** 更新备忘 */
    suspend fun update(note: UserNote): Result<UserNote>

    /** 删除备忘 */
    suspend fun delete(noteId: String): Result<Unit>

    /**
     * 获取用于 AI 上下文的备忘
     *
     * 仅返回 aiVisibility 在 [visibleSet] 中的备忘。
     */
    suspend fun getForContext(
        visibleSet: Set<AiVisibility>,
        scopeType: ScopeType? = null,
        scopeId: String? = null,
        limit: Int = 5
    ): Result<List<UserNote>>
}
