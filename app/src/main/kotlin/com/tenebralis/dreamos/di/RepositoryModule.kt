package com.tenebralis.dreamos.di

import com.tenebralis.dreamos.data.repository.AuthRepositoryImpl
import com.tenebralis.dreamos.data.repository.ApiConnectionRepositoryImpl
import com.tenebralis.dreamos.data.repository.ConnectionSecretRepositoryImpl
import com.tenebralis.dreamos.data.repository.ConversationRepositoryImpl
import com.tenebralis.dreamos.data.repository.IdentityRepositoryImpl
import com.tenebralis.dreamos.data.repository.MessageRepositoryImpl
import com.tenebralis.dreamos.data.repository.NpcRepositoryImpl
import com.tenebralis.dreamos.data.repository.RememberedCredentialRepositoryImpl
import com.tenebralis.dreamos.data.repository.SaveStateRepositoryImpl
import com.tenebralis.dreamos.data.repository.UserRepositoryImpl
import com.tenebralis.dreamos.data.repository.WorldRepositoryImpl
import com.tenebralis.dreamos.domain.repository.ApiConnectionRepository
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.ConnectionSecretRepository
import com.tenebralis.dreamos.domain.repository.ConversationRepository
import com.tenebralis.dreamos.domain.repository.IdentityRepository
import com.tenebralis.dreamos.domain.repository.MessageRepository
import com.tenebralis.dreamos.domain.repository.NpcRepository
import com.tenebralis.dreamos.domain.repository.RememberedCredentialRepository
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import com.tenebralis.dreamos.domain.repository.UserRepository
import com.tenebralis.dreamos.domain.repository.WorldRepository
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

    @Binds
    @Singleton
    abstract fun bindRememberedCredentialRepository(
        impl: RememberedCredentialRepositoryImpl
    ): RememberedCredentialRepository

    @Binds
    @Singleton
    abstract fun bindApiConnectionRepository(
        impl: ApiConnectionRepositoryImpl
    ): ApiConnectionRepository

    @Binds
    @Singleton
    abstract fun bindConnectionSecretRepository(
        impl: ConnectionSecretRepositoryImpl
    ): ConnectionSecretRepository

    @Binds
    @Singleton
    abstract fun bindWorldRepository(
        impl: WorldRepositoryImpl
    ): WorldRepository

    @Binds
    @Singleton
    abstract fun bindIdentityRepository(
        impl: IdentityRepositoryImpl
    ): IdentityRepository

    @Binds
    @Singleton
    abstract fun bindSaveStateRepository(
        impl: SaveStateRepositoryImpl
    ): SaveStateRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        impl: ConversationRepositoryImpl
    ): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindNpcRepository(
        impl: NpcRepositoryImpl
    ): NpcRepository

    // 后续 Phase 的 Repository 在此追加绑定：
    // abstract fun bindApiConnectionRepository(impl: ApiConnectionRepositoryImpl): ApiConnectionRepository
    // abstract fun bindGlobalMemoryRepository(impl: GlobalMemoryRepositoryImpl): GlobalMemoryRepository
    // abstract fun bindUserSettingsRepository(impl: UserSettingsRepositoryImpl): UserSettingsRepository
}
