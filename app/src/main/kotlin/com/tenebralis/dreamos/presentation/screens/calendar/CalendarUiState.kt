package com.tenebralis.dreamos.presentation.screens.calendar

import com.tenebralis.dreamos.domain.model.UserCalendarEvent
import java.time.LocalDate
import java.time.YearMonth

/**
 * 日历 UI 状态
 */
data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val events: List<UserCalendarEvent> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    // 编辑弹窗
    val editingEvent: UserCalendarEvent? = null,
    val isCreating: Boolean = false,
    val isSaving: Boolean = false,

    // 编辑表单
    val editTitle: String = "",
    val editDescription: String = "",
    val editDate: String = "",         // yyyy-MM-dd
    val editStartTime: String = "09:00",
    val editEndTime: String = "10:00",
    val editIsAllDay: Boolean = false,
    val editRepeatRule: String = "none",
    val editAiVisibility: com.tenebralis.dreamos.domain.model.enums.AiVisibility =
        com.tenebralis.dreamos.domain.model.enums.AiVisibility.PRIVATE
) {
    val isEditSheetVisible: Boolean get() = editingEvent != null || isCreating

    /** 选中日期的日程列表 */
    val selectedDateEvents: List<UserCalendarEvent>
        get() = events.filter { event ->
            event.startAt.startsWith(selectedDate.toString())
        }

    /** 当月有日程的日期集合 */
    val datesWithEvents: Set<Int>
        get() = events.mapNotNull { event ->
            val dateStr = event.startAt.take(10)
            try {
                val date = LocalDate.parse(dateStr)
                if (date.year == currentMonth.year && date.monthValue == currentMonth.monthValue) {
                    date.dayOfMonth
                } else null
            } catch (_: Exception) { null }
        }.toSet()
}
