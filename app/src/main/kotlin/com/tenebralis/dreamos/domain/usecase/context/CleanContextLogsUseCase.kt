package com.tenebralis.dreamos.domain.usecase.context

import com.tenebralis.dreamos.domain.repository.ContextLogRepository
import com.tenebralis.dreamos.domain.repository.ContextSettingsRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * 清理过期上下文日志。
 *
 * 根据 settings 中的 logRetentionDays 计算截止日期，删除更早的日志。
 */
class CleanContextLogsUseCase @Inject constructor(
    private val contextLogRepository: ContextLogRepository,
    private val contextSettingsRepository: ContextSettingsRepository
) {
    /** 清理过期日志，返回删除条数 */
    suspend operator fun invoke(): Int {
        val settings = contextSettingsRepository.get()
        val cutoff = Instant.now()
            .minus(settings.logRetentionDays.toLong(), ChronoUnit.DAYS)
            .toString()
        return contextLogRepository.deleteBefore(cutoff)
    }

    /** 清空全部日志 */
    suspend fun deleteAll() {
        contextLogRepository.deleteAll()
    }
}
