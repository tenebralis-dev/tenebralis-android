package com.tenebralis.dreamos.di

import com.tenebralis.dreamos.data.remote.ai.AiChatServiceImpl
import com.tenebralis.dreamos.domain.service.AiChatService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindAiChatService(impl: AiChatServiceImpl): AiChatService
}
