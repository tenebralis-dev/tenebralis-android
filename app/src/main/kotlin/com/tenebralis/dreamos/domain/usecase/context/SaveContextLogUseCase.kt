package com.tenebralis.dreamos.domain.usecase.context

import com.tenebralis.dreamos.domain.model.ContextLayer
import com.tenebralis.dreamos.domain.model.ContextLog
import com.tenebralis.dreamos.domain.repository.ContextLogRepository
import com.tenebralis.dreamos.domain.repository.ContextSettingsRepository
import java.time.Instant
import javax.inject.Inject

/**
 * 保存上下文日志。
 *
 * 在每次 AI 调用后异步调用，将组装后的各层内容保存到本地 Room 数据库。
 * 使用字符数 ÷ 4 估算 token 数。
 */
class SaveContextLogUseCase @Inject constructor(
    private val contextLogRepository: ContextLogRepository,
    private val contextSettingsRepository: ContextSettingsRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        layers: Map<String, ContextLayer>,
        fullPromptText: String
    ) {
        val settings = contextSettingsRepository.get()
        if (!settings.autoLogEnabled) return

        val totalTokens = estimateTokens(fullPromptText)

        val log = ContextLog(
            conversationId = conversationId,
            createdAt = Instant.now().toString(),
            totalTokensEstimate = totalTokens,
            layers = layers,
            fullPromptText = fullPromptText
        )
        contextLogRepository.save(log)
    }

    companion object {
        /** 简单 token 估算：字符数 ÷ 4 */
        fun estimateTokens(text: String): Int = (text.length / 4).coerceAtLeast(1)
    }
}
