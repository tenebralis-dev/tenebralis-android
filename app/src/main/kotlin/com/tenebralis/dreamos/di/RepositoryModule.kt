package com.tenebralis.dreamos.di

import com.tenebralis.dreamos.data.repository.AchievementRepositoryImpl
import com.tenebralis.dreamos.data.repository.AvatarStorageRepositoryImpl
import com.tenebralis.dreamos.data.repository.AuthRepositoryImpl
import com.tenebralis.dreamos.data.repository.CurrencyRepositoryImpl
import com.tenebralis.dreamos.data.repository.FontRepositoryImpl
import com.tenebralis.dreamos.data.repository.ApiConnectionRepositoryImpl
import com.tenebralis.dreamos.data.repository.ConnectionSecretRepositoryImpl
import com.tenebralis.dreamos.data.repository.ConversationRepositoryImpl
import com.tenebralis.dreamos.data.repository.ForumRepositoryImpl
import com.tenebralis.dreamos.data.repository.GlobalMemoryRepositoryImpl
import com.tenebralis.dreamos.data.repository.IdentityRepositoryImpl
import com.tenebralis.dreamos.data.repository.MessageRepositoryImpl
import com.tenebralis.dreamos.data.repository.NpcRepositoryImpl
import com.tenebralis.dreamos.data.repository.RelationshipRepositoryImpl
import com.tenebralis.dreamos.data.repository.RememberedCredentialRepositoryImpl
import com.tenebralis.dreamos.data.repository.SaveStateRepositoryImpl
import com.tenebralis.dreamos.data.repository.ShopRepositoryImpl
import com.tenebralis.dreamos.data.repository.TaskRepositoryImpl
import com.tenebralis.dreamos.data.repository.UserRepositoryImpl
import com.tenebralis.dreamos.data.repository.WorldNpcPersonaRepositoryImpl
import com.tenebralis.dreamos.data.repository.WorldRepositoryImpl
import com.tenebralis.dreamos.domain.repository.AchievementRepository
import com.tenebralis.dreamos.domain.repository.AvatarStorageRepository
import com.tenebralis.dreamos.domain.repository.ApiConnectionRepository
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.CurrencyRepository
import com.tenebralis.dreamos.domain.repository.FontRepository
import com.tenebralis.dreamos.domain.repository.ConnectionSecretRepository
import com.tenebralis.dreamos.domain.repository.ConversationRepository
import com.tenebralis.dreamos.domain.repository.ForumRepository
import com.tenebralis.dreamos.domain.repository.GlobalMemoryRepository
import com.tenebralis.dreamos.domain.repository.IdentityRepository
import com.tenebralis.dreamos.domain.repository.MessageRepository
import com.tenebralis.dreamos.domain.repository.NpcRepository
import com.tenebralis.dreamos.domain.repository.RelationshipRepository
import com.tenebralis.dreamos.domain.repository.RememberedCredentialRepository
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import com.tenebralis.dreamos.domain.repository.ShopRepository
import com.tenebralis.dreamos.domain.repository.TaskRepository
import com.tenebralis.dreamos.domain.repository.UserRepository
import com.tenebralis.dreamos.domain.repository.WorldNpcPersonaRepository
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

    @Binds
    @Singleton
    abstract fun bindGlobalMemoryRepository(
        impl: GlobalMemoryRepositoryImpl
    ): GlobalMemoryRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: com.tenebralis.dreamos.data.repository.NoteRepositoryImpl
    ): com.tenebralis.dreamos.domain.repository.NoteRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        impl: com.tenebralis.dreamos.data.repository.CalendarRepositoryImpl
    ): com.tenebralis.dreamos.domain.repository.CalendarRepository

    @Binds
    @Singleton
    abstract fun bindPomodoroRepository(
        impl: com.tenebralis.dreamos.data.repository.PomodoroRepositoryImpl
    ): com.tenebralis.dreamos.domain.repository.PomodoroRepository

    // ─── M7: 经济 + 任务 + 成就 + 好感度 ──────────────────

    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(
        impl: CurrencyRepositoryImpl
    ): CurrencyRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(
        impl: AchievementRepositoryImpl
    ): AchievementRepository

    @Binds
    @Singleton
    abstract fun bindRelationshipRepository(
        impl: RelationshipRepositoryImpl
    ): RelationshipRepository

    @Binds
    @Singleton
    abstract fun bindWorldNpcPersonaRepository(
        impl: WorldNpcPersonaRepositoryImpl
    ): WorldNpcPersonaRepository

    @Binds
    @Singleton
    abstract fun bindForumRepository(
        impl: ForumRepositoryImpl
    ): ForumRepository

    @Binds
    @Singleton
    abstract fun bindShopRepository(
        impl: ShopRepositoryImpl
    ): ShopRepository

    // ─── 自定义字体 ────────────────────────────────────────

    @Binds
    @Singleton
    abstract fun bindFontRepository(
        impl: FontRepositoryImpl
    ): FontRepository

    // ─── Storage ──────────────────────────────────────────

    @Binds
    @Singleton
    abstract fun bindAvatarStorageRepository(
        impl: AvatarStorageRepositoryImpl
    ): AvatarStorageRepository

    // ─── AI Preset ────────────────────────────────────────

    @Binds
    @Singleton
    abstract fun bindAiPresetRepository(
        impl: com.tenebralis.dreamos.data.repository.AiPresetRepositoryImpl
    ): com.tenebralis.dreamos.domain.repository.AiPresetRepository
}
