package com.tenebralis.dreamos.di

import com.tenebralis.dreamos.data.repository.AuthRepositoryImpl
import com.tenebralis.dreamos.data.repository.UserRepositoryImpl
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    // 后续 Phase 的 Repository 在此追加绑定：
    // abstract fun bindWorldRepository(impl: WorldRepositoryImpl): WorldRepository
    // abstract fun bindIdentityRepository(impl: IdentityRepositoryImpl): IdentityRepository
    // abstract fun bindSaveStateRepository(impl: SaveStateRepositoryImpl): SaveStateRepository
    // abstract fun bindNpcRepository(impl: NpcRepositoryImpl): NpcRepository
    // abstract fun bindConversationRepository(impl: ConversationRepositoryImpl): ConversationRepository
    // abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository
    // abstract fun bindApiConnectionRepository(impl: ApiConnectionRepositoryImpl): ApiConnectionRepository
    // abstract fun bindGlobalMemoryRepository(impl: GlobalMemoryRepositoryImpl): GlobalMemoryRepository
    // abstract fun bindUserSettingsRepository(impl: UserSettingsRepositoryImpl): UserSettingsRepository
}
