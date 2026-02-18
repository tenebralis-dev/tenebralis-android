package com.tenebralis.dreamos.presentation.screens.pomodoro

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.PomodoroSession
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.PomodoroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroRepository: PomodoroRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartInstant: Instant? = null

    init {
        loadSessions()
    }

    fun onEvent(event: PomodoroEvent) {
        when (event) {
            PomodoroEvent.Start -> startTimer()
            PomodoroEvent.Pause -> pauseTimer()
            PomodoroEvent.Resume -> resumeTimer()
            PomodoroEvent.Reset -> resetTimer()
            PomodoroEvent.Complete -> completeSession()
            is PomodoroEvent.DurationChanged -> {
                if (_uiState.value.timerStatus == TimerStatus.IDLE) {
                    val totalSec = event.minutes * 60
                    _uiState.update {
                        it.copy(
                            durationMinutes = event.minutes,
                            totalSeconds = totalSec,
                            remainingSeconds = totalSec
                        )
                    }
                }
            }
            is PomodoroEvent.TaskDescriptionChanged ->
                _uiState.update { it.copy(taskDescription = event.value) }
            PomodoroEvent.Refresh -> loadSessions()
            PomodoroEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            PomodoroEvent.ClearInfo -> _uiState.update { it.copy(infoMessage = null) }
        }
    }

    // ─── 计时器 ─────────────────────────────────────────────

    private fun startTimer() {
        sessionStartInstant = Instant.now()
        _uiState.update { it.copy(timerStatus = TimerStatus.RUNNING) }
        launchCountdown()
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerStatus = TimerStatus.PAUSED) }
    }

    private fun resumeTimer() {
        _uiState.update { it.copy(timerStatus = TimerStatus.RUNNING) }
        launchCountdown()
    }

    private fun resetTimer() {
        timerJob?.cancel()
        sessionStartInstant = null
        _uiState.update {
            it.copy(
                timerStatus = TimerStatus.IDLE,
                remainingSeconds = it.totalSeconds
            )
        }
    }

    private fun launchCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                delay(1_000L)
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            if (_uiState.value.remainingSeconds <= 0) {
                onEvent(PomodoroEvent.Complete)
            }
        }
    }

    // ─── 完成 session ───────────────────────────────────────

    private fun completeSession() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerStatus = TimerStatus.COMPLETED) }

        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(errorMessage = "当前未登录") }
                return@launch
            }

            val session = PomodoroSession(
                id = UUID.randomUUID().toString(),
                userId = userId,
                startedAt = (sessionStartInstant ?: Instant.now()).toString(),
                endedAt = Instant.now().toString(),
                durationMinutes = _uiState.value.durationMinutes,
                isCompleted = true,
                taskDescription = _uiState.value.taskDescription.trim().ifEmpty { null }
            )

            pomodoroRepository.create(session).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            infoMessage = "🍅 番茄钟完成！已记录 ${session.durationMinutes} 分钟专注",
                            timerStatus = TimerStatus.IDLE,
                            remainingSeconds = it.totalSeconds,
                            taskDescription = ""
                        )
                    }
                    loadSessions()
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to save session", error)
                    _uiState.update {
                        it.copy(errorMessage = "保存失败：${error.message ?: "未知错误"}")
                    }
                }
            )

            sessionStartInstant = null
        }
    }

    // ─── 数据加载 ───────────────────────────────────────────

    private fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            pomodoroRepository.getTodaySessions().collect { result ->
                result.fold(
                    onSuccess = { sessions ->
                        _uiState.update { it.copy(todaySessions = sessions) }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to load today sessions", error)
                    }
                )
            }
        }
        viewModelScope.launch {
            pomodoroRepository.getWeekSessions().fold(
                onSuccess = { sessions ->
                    _uiState.update { it.copy(isLoading = false, weekSessions = sessions) }
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to load week sessions", error)
                    _uiState.update { it.copy(isLoading = false) }
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        private const val TAG = "PomodoroViewModel"
    }
}
