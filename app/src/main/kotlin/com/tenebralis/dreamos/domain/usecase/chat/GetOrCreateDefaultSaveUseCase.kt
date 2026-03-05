package com.tenebralis.dreamos.domain.usecase.chat

import android.util.Log
import com.tenebralis.dreamos.domain.model.UserWorldIdentity
import com.tenebralis.dreamos.domain.model.World
import com.tenebralis.dreamos.domain.model.WorldSaveState
import com.tenebralis.dreamos.domain.model.enums.WorldStatus
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.IdentityRepository
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import com.tenebralis.dreamos.domain.repository.WorldRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject

/**
 * 获取或创建「现实世界」的默认存档。
 *
 * 在 Tenebralis 的世界观中，「现实」是用户的默认世界 ——
 * 不需要世界观设定，不需要扮演角色，用户就是自己。
 * 该 UseCase 透明地维护「现实世界 → 默认身份 → 默认存档」三件套。
 *
 * 幂等操作：多次调用返回相同的 saveId。
 */
class GetOrCreateDefaultSaveUseCase @Inject constructor(
    private val worldRepository: WorldRepository,
    private val identityRepository: IdentityRepository,
    private val saveStateRepository: SaveStateRepository,
    private val authRepository: AuthRepository
) {

    /**
     * @return 默认存档的 saveId
     */
    suspend operator fun invoke(): Result<String> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        // 1. 获取或创建「现实」世界
        val world = getOrCreateRealityWorld(userId)
        Log.d(TAG, "现实世界: id=${world.id}")

        // 2. 获取或创建默认身份
        val identity = getOrCreateDefaultIdentity(userId, world.id)
        Log.d(TAG, "默认身份: id=${identity.id}")

        // 3. 获取或创建默认存档
        val save = getOrCreateDefaultSave(userId, world.id, identity.id)
        Log.d(TAG, "默认存档: id=${save.id}")

        save.id
    }

    private suspend fun getOrCreateRealityWorld(userId: String): World {
        val existing = worldRepository.getByName(REALITY_WORLD_NAME).getOrNull()
        if (existing != null) return existing

        val newWorld = World(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = REALITY_WORLD_NAME,
            description = "现实世界 — 独立对话的默认上下文",
            status = WorldStatus.ACTIVE,
            promptLoreText = null,
            loreJson = JsonObject(emptyMap()),
            rulesJson = JsonObject(emptyMap()),
            aiContextJson = JsonObject(emptyMap()),
            createdAt = null,
            updatedAt = null
        )
        return worldRepository.create(newWorld).getOrThrow()
    }

    private suspend fun getOrCreateDefaultIdentity(
        userId: String,
        worldId: String
    ): UserWorldIdentity {
        val existingList = identityRepository.getByWorld(worldId).first().getOrNull()
        val existing = existingList?.firstOrNull()
        if (existing != null) return existing

        val newIdentity = UserWorldIdentity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            worldId = worldId,
            identityName = DEFAULT_IDENTITY_NAME,
            isActive = true,
            promptIdentityText = null,
            roleDataJson = JsonObject(emptyMap()),
            personaJson = JsonObject(emptyMap()),
            createdAt = null,
            updatedAt = null
        )
        return identityRepository.create(newIdentity).getOrThrow()
    }

    private suspend fun getOrCreateDefaultSave(
        userId: String,
        worldId: String,
        identityId: String
    ): WorldSaveState {
        val existingList = saveStateRepository.getByIdentity(identityId).first().getOrNull()
        val existing = existingList?.firstOrNull()
        if (existing != null) return existing

        val newSave = WorldSaveState(
            id = UUID.randomUUID().toString(),
            userId = userId,
            worldId = worldId,
            identityId = identityId,
            slot = 1,
            title = "现实",
            summary = null,
            chapter = null,
            stage = null,
            promptProgressText = null,
            stateJson = JsonObject(emptyMap()),
            lastPlayedAt = null,
            createdAt = null,
            updatedAt = null
        )
        return saveStateRepository.create(newSave).getOrThrow()
    }

    companion object {
        private const val TAG = "RealitySave"

        /** 「现实世界」的固定名称标识 */
        const val REALITY_WORLD_NAME = "__reality__"

        private const val DEFAULT_IDENTITY_NAME = "__default__"
    }
}
