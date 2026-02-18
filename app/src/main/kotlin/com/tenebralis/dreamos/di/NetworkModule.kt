package com.tenebralis.dreamos.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            expectSuccess = false

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000L
                connectTimeoutMillis = 10_000L
                socketTimeoutMillis = 15_000L
            }
        }
    }

    /**
     * AI 专用 HttpClient，超时时间更长以适应大模型推理。
     *
     * 注意：**不安装 ContentNegotiation 插件**。
     * AiChatServiceImpl 手动序列化请求体 (encodeToString) 和手动解析响应体，
     * 如果安装了 ContentNegotiation，Ktor 会拦截 text/event-stream (SSE) 响应并尝试
     * 将其作为 JSON 解析或缓冲整个响应体，导致 bodyAsChannel() 无法逐行流式读取，
     * 从而使打字机效果失效。
     */
    @Provides
    @Singleton
    @AiHttpClient
    fun provideAiHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            expectSuccess = false

            // 不安装 ContentNegotiation — SSE 流式响应需要原始 channel 读取

            install(HttpTimeout) {
                requestTimeoutMillis = 120_000L
                connectTimeoutMillis = 15_000L
                socketTimeoutMillis = 120_000L
            }
        }
    }
}
