package com.tenebralis.dreamos.presentation.screens.dreamentry

import com.tenebralis.dreamos.domain.model.UserWorldIdentity
import com.tenebralis.dreamos.domain.model.World
import com.tenebralis.dreamos.domain.model.WorldSaveState
import com.tenebralis.dreamos.domain.model.enums.WorldStatus
import com.tenebralis.dreamos.domain.repository.IdentityRepository
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import com.tenebralis.dreamos.domain.repository.WorldRepository
import com.tenebralis.dreamos.domain.usecase.dream.ResolveDreamEntryUseCase
import com.tenebralis.dreamos.domain.usecase.identity.GetIdentitiesUseCase
import com.tenebralis.dreamos.domain.usecase.save.GetSaveStatesUseCase
import com.tenebralis.dreamos.domain.usecase.world.GetWorldsUseCase
import com.tenebralis.dreamos.presentation.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class DreamEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `emits navigation once and keeps null after consume`() = runTest {
        val viewModel = DreamEntryViewModel(
            resolveDreamEntryUseCase = buildResolveDreamEntryUseCase(saveId = "save-1")
        )

        assertEquals(
            Screen.ChatList.createRoute(saveId = "save-1"),
            viewModel.uiState.value.navigateRoute
        )

        viewModel.consumeNavigation()

        assertNull(viewModel.uiState.value.navigateRoute)
        assertFalse(viewModel.uiState.value.isResolving)
    }
}

private fun buildResolveDreamEntryUseCase(saveId: String): ResolveDreamEntryUseCase {
    val world = World(
        id = "world-1",
        userId = "user-1",
        name = "world",
        description = null,
        status = WorldStatus.ACTIVE,
        promptLoreText = null,
        loreJson = JsonObject(emptyMap()),
        rulesJson = JsonObject(emptyMap()),
        aiContextJson = JsonObject(emptyMap()),
        createdAt = null,
        updatedAt = null
    )
    val identity = UserWorldIdentity(
        id = "identity-1",
        userId = "user-1",
        worldId = world.id,
        identityName = "identity",
        isActive = true,
        promptIdentityText = null,
        roleDataJson = JsonObject(emptyMap()),
        personaJson = JsonObject(emptyMap()),
        createdAt = null,
        updatedAt = null
    )
    val saveState = WorldSaveState(
        id = saveId,
        userId = "user-1",
        worldId = world.id,
        identityId = identity.id,
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

    val worldRepository = object : WorldRepository {
        override fun getWorlds(): Flow<Result<List<World>>> = flowOf(Result.success(listOf(world)))
        override suspend fun getById(worldId: String): Result<World> = Result.success(world)
        override suspend fun create(world: World): Result<World> = Result.success(world)
        override suspend fun update(world: World): Result<World> = Result.success(world)
        override suspend fun delete(worldId: String): Result<Unit> = Result.success(Unit)
        override suspend fun getByName(name: String): Result<World?> = Result.success(null)
    }
    val identityRepository = object : IdentityRepository {
        override fun getByWorld(worldId: String): Flow<Result<List<UserWorldIdentity>>> {
            return flowOf(Result.success(listOf(identity)))
        }

        override suspend fun getById(identityId: String): Result<UserWorldIdentity> {
            return Result.success(identity)
        }

        override suspend fun create(identity: UserWorldIdentity): Result<UserWorldIdentity> {
            return Result.success(identity)
        }

        override suspend fun update(identity: UserWorldIdentity): Result<UserWorldIdentity> {
            return Result.success(identity)
        }

        override suspend fun setActive(worldId: String, identityId: String): Result<Unit> {
            return Result.success(Unit)
        }
    }
    val saveStateRepository = object : SaveStateRepository {
        override fun getByIdentity(identityId: String): Flow<Result<List<WorldSaveState>>> {
            return flowOf(Result.success(listOf(saveState)))
        }

        override suspend fun getById(saveId: String): Result<WorldSaveState> {
            return Result.success(saveState)
        }

        override suspend fun create(saveState: WorldSaveState): Result<WorldSaveState> {
            return Result.success(saveState)
        }

        override suspend fun update(saveState: WorldSaveState): Result<WorldSaveState> {
            return Result.success(saveState)
        }
    }

    return ResolveDreamEntryUseCase(
        getWorldsUseCase = GetWorldsUseCase(worldRepository),
        getIdentitiesUseCase = GetIdentitiesUseCase(identityRepository),
        getSaveStatesUseCase = GetSaveStatesUseCase(saveStateRepository)
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
