package com.tenebralis.dreamos.presentation.screens.calendar

import com.tenebralis.dreamos.domain.model.UserCalendarEvent
import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import java.time.LocalDate
import java.time.YearMonth

/**
 * 日历 UI 事件
 */
sealed interface CalendarUiEvent {
    // 导航
    data class MonthChanged(val yearMonth: YearMonth) : CalendarUiEvent
    data class DateSelected(val date: LocalDate) : CalendarUiEvent

    // 编辑
    data object StartCreate : CalendarUiEvent
    data class StartEdit(val event: UserCalendarEvent) : CalendarUiEvent
    data object DismissEdit : CalendarUiEvent
    data object SaveEvent : CalendarUiEvent

    // 编辑表单字段
    data class TitleChanged(val value: String) : CalendarUiEvent
    data class DescriptionChanged(val value: String) : CalendarUiEvent
    data class DateChanged(val value: String) : CalendarUiEvent
    data class StartTimeChanged(val value: String) : CalendarUiEvent
    data class EndTimeChanged(val value: String) : CalendarUiEvent
    data class AllDayChanged(val value: Boolean) : CalendarUiEvent
    data class RepeatRuleChanged(val value: String) : CalendarUiEvent
    data class AiVisibilityChanged(val value: AiVisibility) : CalendarUiEvent

    // 操作
    data class Delete(val eventId: String) : CalendarUiEvent

    data object ClearError : CalendarUiEvent
    data object ClearInfo : CalendarUiEvent
}
