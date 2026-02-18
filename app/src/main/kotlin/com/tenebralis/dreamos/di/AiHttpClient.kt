package com.tenebralis.dreamos.di

import javax.inject.Qualifier

/**
 * Hilt 限定符：标记用于 AI 调用的长超时 HttpClient。
 *
 * AI 生成响应可能需要较长时间（requestTimeout = 120s），
 * 与普通请求的 15s 超时隔离。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AiHttpClient
