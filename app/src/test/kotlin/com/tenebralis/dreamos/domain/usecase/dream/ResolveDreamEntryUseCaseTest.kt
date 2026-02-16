package com.tenebralis.dreamos.domain.usecase.dream

import com.tenebralis.dreamos.domain.model.UserWorldIdentity
import com.tenebralis.dreamos.domain.model.World
import com.tenebralis.dreamos.domain.model.WorldSaveState
import com.tenebralis.dreamos.domain.model.enums.WorldStatus
import com.tenebralis.dreamos.domain.repository.IdentityRepository
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import com.tenebralis.dreamos.domain.repository.WorldRepository
import com.tenebralis.dreamos.domain.usecase.identity.GetIdentitiesUseCase
import com.tenebralis.dreamos.domain.usecase.save.GetSaveStatesUseCase
import com.tenebralis.dreamos.domain.usecase.world.GetWorldsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveDreamEntryUseCaseTest {

    @Test
    fun `returns world selection when user has no worlds`() = runBlocking {
        val useCase = buildUseCase(
            worldsResult = Result.success(emptyList()),
            identitiesResultByWorldId = emptyMap(),
            saveStatesResultByIdentityId = emptyMap()
        )

        val destination = useCase()

        assertTrue(destination is DreamEntryDestination.WorldSelection)
    }

    @Test
    fun `returns identity selection when world exists but no identity`() = runBlocking {
        val world = buildWorld(id = "world-1")
        val useCase = buildUseCase(
            worldsResult = Result.success(listOf(world)),
            identitiesResultByWorldId = mapOf(world.id to Result.success(emptyList())),
            saveStatesResultByIdentityId = emptyMap()
        )

        val destination = useCase()

        assertEquals(
            DreamEntryDestination.IdentitySelection(worldId = "world-1"),
            destination
        )
    }

    @Test
    fun `returns save selection when identity exists but no save`() = runBlocking {
        val world = buildWorld(id = "world-1")
        val identity = buildIdentity(id = "identity-1", worldId = world.id, isActive = true)
        val useCase = buildUseCase(
            worldsResult = Result.success(listOf(world)),
            identitiesResultByWorldId = mapOf(world.id to Result.success(listOf(identity))),
            saveStatesResultByIdentityId = mapOf(identity.id to Result.success(emptyList()))
        )

        val destination = useCase()

        assertEquals(
            DreamEntryDestination.SaveSelection(
                worldId = world.id,
                identityId = identity.id
            ),
            destination
        )
    }

    @Test
    fun `returns conversation selection when world identity and save are all available`() = runBlocking {
        val world = buildWorld(id = "world-1")
        val activeIdentity = buildIdentity(id = "identity-active", worldId = world.id, isActive = true)
        val inactiveIdentity = buildIdentity(id = "identity-inactive", worldId = world.id, isActive = false)
        val saveNewest = buildSaveState(id = "save-new", worldId = world.id, identityId = activeIdentity.id)
        val saveOld = buildSaveState(id = "save-old", worldId = world.id, identityId = activeIdentity.id)

        val useCase = buildUseCase(
            worldsResult = Result.success(listOf(world)),
            identitiesResultByWorldId = mapOf(
                world.id to Result.success(listOf(inactiveIdentity, activeIdentity))
            ),
            saveStatesResultByIdentityId = mapOf(
                activeIdentity.id to Result.success(listOf(saveNewest, saveOld))
            )
        )

        val destination = useCase()

        assertEquals(
            DreamEntryDestination.ConversationSelection(saveId = "save-new"),
            destination
        )
    }

    @Test
    fun `returns error when loading worlds fails`() = runBlocking {
        val useCase = buildUseCase(
            worldsResult = Result.failure(IllegalStateException("world failed")),
            identitiesResultByWorldId = emptyMap(),
            saveStatesResultByIdentityId = emptyMap()
        )

        val destination = useCase()

        assertEquals(
            DreamEntryDestination.Error("world failed"),
            destination
        )
    }

    @Test
    fun `returns error when loading identities or save states fails`() = runBlocking {
        val world = buildWorld(id = "world-1")
        val identity = buildIdentity(id = "identity-1", worldId = world.id, isActive = true)

        val identityFailedUseCase = buildUseCase(
            worldsResult = Result.success(listOf(world)),
            identitiesResultByWorldId = mapOf(
                world.id to Result.failure<List<UserWorldIdentity>>(
                    IllegalStateException("identity failed")
                )
            ),
            saveStatesResultByIdentityId = emptyMap()
        )
        val saveFailedUseCase = buildUseCase(
            worldsResult = Result.success(listOf(world)),
            identitiesResultByWorldId = mapOf(world.id to Result.success(listOf(identity))),
            saveStatesResultByIdentityId = mapOf(
                identity.id to Result.failure<List<WorldSaveState>>(
                    IllegalStateException("save failed")
                )
            )
        )

        val identityDestination = identityFailedUseCase()
        val saveDestination = saveFailedUseCase()

        assertEquals(
            DreamEntryDestination.Error("identity failed"),
            identityDestination
        )
        assertEquals(
            DreamEntryDestination.Error("save failed"),
            saveDestination
        )
    }
}

private fun buildUseCase(
    worldsResult: Result<List<World>>,
    identitiesResultByWorldId: Map<String, Result<List<UserWorldIdentity>>>,
    saveStatesResultByIdentityId: Map<String, Result<List<WorldSaveState>>>
): ResolveDreamEntryUseCase {
    val worldRepository = StubWorldRepository(worldsResult)
    val identityRepository = StubIdentityRepository { worldId ->
        identitiesResultByWorldId[worldId]
            ?: Result.failure<List<UserWorldIdentity>>(
                IllegalStateException("identity mock missing for $worldId")
            )
    }
    val saveStateRepository = StubSaveStateRepository { identityId ->
        saveStatesResultByIdentityId[identityId]
            ?: Result.failure<List<WorldSaveState>>(
                IllegalStateException("save mock missing for $identityId")
            )
    }

    return ResolveDreamEntryUseCase(
        getWorldsUseCase = GetWorldsUseCase(worldRepository),
        getIdentitiesUseCase = GetIdentitiesUseCase(identityRepository),
        getSaveStatesUseCase = GetSaveStatesUseCase(saveStateRepository)
    )
}

private class StubWorldRepository(
    private val worldsResult: Result<List<World>>
) : WorldRepository {

    override fun getWorlds(): Flow<Result<List<World>>> = flowOf(worldsResult)

    override suspend fun getById(worldId: String): Result<World> {
        return Result.failure(UnsupportedOperationException("Not needed in this test"))
    }

    override suspend fun create(world: World): Result<World> {
        return Result.failure(UnsupportedOperationException("Not needed in this test"))
    }

    override suspend fun update(world: World): Result<World> {
        return Result.failure(UnsupportedOperationException("Not needed in this test"))
    }

    override suspend fun delete(worldId: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Not needed in this test"))
    }
}

private class StubIdentityRepository(
    private val onGetByWorld: (String) -> Result<List<UserWorldIdentity>>
) : IdentityRepository {

    override fun getByWorld(worldId: String): Flow<Result<List<UserWorldIdentity>>> {
        return flowOf(onGetByWorld(worldId))
    }

    override suspend fun create(identity: UserWorldIdentity): Result<UserWorldIdentity> {
        return Result.failure(UnsupportedOperationException("Not needed in this test"))
    }

    override suspend fun update(identity: UserWorldIdentity): Result<UserWorldIdentity> {
        return Result.failure(UnsupportedOperationException("Not needed in this test"))
    }

    override suspend fun setActive(worldId: String, identityId: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Not needed in this test"))
    }
}

private class StubSaveStateRepository(
    private val onGetByIdentity: (String) -> Result<List<WorldSaveState>>
) : SaveStateRepository {

    override fun getByIdentity(identityId: String): Flow<Result<List<WorldSaveState>>> {
        return flowOf(onGetByIdentity(identityId))
    }

    override suspend fun create(saveState: WorldSaveState): Result<WorldSaveState> {
        return Result.failure(UnsupportedOperationException("Not needed in this test"))
    }

    override suspend fun update(saveState: WorldSaveState): Result<WorldSaveState> {
        return Result.failure(UnsupportedOperationException("Not needed in this test"))
    }
}

private fun buildWorld(id: String): World {
    return World(
        id = id,
        userId = "user-1",
        name = "world-$id",
        description = null,
        status = WorldStatus.ACTIVE,
        promptLoreText = null,
        loreJson = JsonObject(emptyMap()),
        rulesJson = JsonObject(emptyMap()),
        aiContextJson = JsonObject(emptyMap()),
        createdAt = null,
        updatedAt = null
    )
}

private fun buildIdentity(
    id: String,
    worldId: String,
    isActive: Boolean
): UserWorldIdentity {
    return UserWorldIdentity(
        id = id,
        userId = "user-1",
        worldId = worldId,
        identityName = "identity-$id",
        isActive = isActive,
        promptIdentityText = null,
        roleDataJson = JsonObject(emptyMap()),
        personaJson = JsonObject(emptyMap()),
        createdAt = null,
        updatedAt = null
    )
}

private fun buildSaveState(
    id: String,
    worldId: String,
    identityId: String
): WorldSaveState {
    return WorldSaveState(
        id = id,
        userId = "user-1",
        worldId = worldId,
        identityId = identityId,
        slot = 1,
        title = null,
        summary = null,
        chapter = null,
        stage = null,
        promptProgressText = null,
        stateJson = JsonObject(emptyMap()),
        lastPlayedAt = null,
        createdAt = null,
        updatedAt = null
    )
}
