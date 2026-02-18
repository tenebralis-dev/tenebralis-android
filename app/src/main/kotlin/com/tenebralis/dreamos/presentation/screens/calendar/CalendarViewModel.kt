package com.tenebralis.dreamos.presentation.screens.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.UserCalendarEvent
import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun onEvent(event: CalendarUiEvent) {
        when (event) {
            is CalendarUiEvent.MonthChanged -> {
                _uiState.update { it.copy(currentMonth = event.yearMonth) }
                loadEvents()
            }
            is CalendarUiEvent.DateSelected ->
                _uiState.update { it.copy(selectedDate = event.date) }

            CalendarUiEvent.StartCreate -> {
                val selectedDate = _uiState.value.selectedDate
                _uiState.update {
                    it.copy(
                        isCreating = true,
                        editingEvent = null,
                        editTitle = "",
                        editDescription = "",
                        editDate = selectedDate.toString(),
                        editStartTime = "09:00",
                        editEndTime = "10:00",
                        editIsAllDay = false,
                        editRepeatRule = "none",
                        editAiVisibility = AiVisibility.PRIVATE
                    )
                }
            }
            is CalendarUiEvent.StartEdit -> {
                val e = event.event
                val dateStr = e.startAt.take(10)
                val startTime = if (e.startAt.length >= 16) e.startAt.substring(11, 16) else "09:00"
                val endTime = e.endAt?.let { if (it.length >= 16) it.substring(11, 16) else "10:00" } ?: "10:00"
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        editingEvent = e,
                        editTitle = e.title,
                        editDescription = e.description.orEmpty(),
                        editDate = dateStr,
                        editStartTime = startTime,
                        editEndTime = endTime,
                        editIsAllDay = e.isAllDay,
                        editRepeatRule = e.repeatRule ?: "none",
                        editAiVisibility = e.aiVisibility
                    )
                }
            }
            CalendarUiEvent.DismissEdit ->
                _uiState.update { it.copy(isCreating = false, editingEvent = null) }
            CalendarUiEvent.SaveEvent -> saveEvent()

            is CalendarUiEvent.TitleChanged ->
                _uiState.update { it.copy(editTitle = event.value) }
            is CalendarUiEvent.DescriptionChanged ->
                _uiState.update { it.copy(editDescription = event.value) }
            is CalendarUiEvent.DateChanged ->
                _uiState.update { it.copy(editDate = event.value) }
            is CalendarUiEvent.StartTimeChanged ->
                _uiState.update { it.copy(editStartTime = event.value) }
            is CalendarUiEvent.EndTimeChanged ->
                _uiState.update { it.copy(editEndTime = event.value) }
            is CalendarUiEvent.AllDayChanged ->
                _uiState.update { it.copy(editIsAllDay = event.value) }
            is CalendarUiEvent.RepeatRuleChanged ->
                _uiState.update { it.copy(editRepeatRule = event.value) }
            is CalendarUiEvent.AiVisibilityChanged ->
                _uiState.update { it.copy(editAiVisibility = event.value) }

            is CalendarUiEvent.Delete -> deleteEvent(event.eventId)

            CalendarUiEvent.ClearError ->
                _uiState.update { it.copy(errorMessage = null) }
            CalendarUiEvent.ClearInfo ->
                _uiState.update { it.copy(infoMessage = null) }
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val yearMonth = _uiState.value.currentMonth.toString()
            calendarRepository.getByMonth(yearMonth).collect { result ->
                result.fold(
                    onSuccess = { events ->
                        _uiState.update { it.copy(isLoading = false, events = events) }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to load events", error)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "加载日程失败：${error.message ?: "未知错误"}"
                            )
                        }
                    }
                )
            }
        }
    }

    private fun saveEvent() {
        val state = _uiState.value
        val title = state.editTitle.trim()
        if (title.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "标题不能为空") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val startAt = if (state.editIsAllDay) {
                "${state.editDate}T00:00:00"
            } else {
                "${state.editDate}T${state.editStartTime}:00"
            }
            val endAt = if (state.editIsAllDay) {
                null
            } else {
                "${state.editDate}T${state.editEndTime}:00"
            }

            val result = if (state.isCreating) {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "当前未登录") }
                    return@launch
                }
                val newEvent = UserCalendarEvent(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = title,
                    description = state.editDescription.trim().ifEmpty { null },
                    startAt = startAt,
                    endAt = endAt,
                    isAllDay = state.editIsAllDay,
                    repeatRule = state.editRepeatRule.takeIf { it != "none" },
                    aiVisibility = state.editAiVisibility
                )
                calendarRepository.create(newEvent)
            } else {
                val editing = state.editingEvent ?: run {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "编辑状态异常") }
                    return@launch
                }
                val updated = editing.copy(
                    title = title,
                    description = state.editDescription.trim().ifEmpty { null },
                    startAt = startAt,
                    endAt = endAt,
                    isAllDay = state.editIsAllDay,
                    repeatRule = state.editRepeatRule.takeIf { it != "none" },
                    aiVisibility = state.editAiVisibility
                )
                calendarRepository.update(updated)
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isCreating = false,
                            editingEvent = null,
                            infoMessage = if (state.isCreating) "日程已创建" else "日程已更新"
                        )
                    }
                    loadEvents()
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to save event", error)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "保存失败：${error.message ?: "未知错误"}"
                        )
                    }
                }
            )
        }
    }

    private fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            calendarRepository.delete(eventId).fold(
                onSuccess = {
                    _uiState.update { it.copy(infoMessage = "日程已删除") }
                    loadEvents()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = "删除失败：${error.message ?: "未知错误"}")
                    }
                }
            )
        }
    }

    companion object {
        private const val TAG = "CalendarViewModel"
    }
}
