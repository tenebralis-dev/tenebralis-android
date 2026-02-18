package com.tenebralis.dreamos.presentation.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.enums.TaskStatus
import com.tenebralis.dreamos.domain.repository.TaskRepository
import com.tenebralis.dreamos.domain.usecase.task.CompleteTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun onEvent(event: TaskEvent) {
        when (event) {
            TaskEvent.Refresh -> loadTasks()
            is TaskEvent.SwitchTab -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
                loadTasks()
            }
            is TaskEvent.StartTask -> startTask(event.taskId)
            is TaskEvent.CompleteTask -> completeTask(event.userTaskId)
            TaskEvent.DismissError -> _uiState.update { it.copy(error = null, rewardMessage = null) }
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val statusFilter = when (_uiState.value.selectedTab) {
                TaskTab.ALL -> null
                TaskTab.IN_PROGRESS -> TaskStatus.IN_PROGRESS
                TaskTab.COMPLETED -> TaskStatus.COMPLETED
            }

            taskRepository.getUserTasks(statusFilter).collect { result ->
                result.onSuccess { tasks ->
                    _uiState.update { it.copy(userTasks = tasks, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "加载任务失败", isLoading = false)
                    }
                }
            }
        }
    }

    private fun startTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.startTask(taskId, saveId = null)
                .onSuccess { loadTasks() }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "开始任务失败") }
                }
        }
    }

    private fun completeTask(userTaskId: String) {
        viewModelScope.launch {
            completeTaskUseCase(userTaskId)
                .onSuccess { result ->
                    if (result.hasReward) {
                        _uiState.update { it.copy(rewardMessage = result.rewardSummary()) }
                    }
                    loadTasks()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "完成任务失败") }
                }
        }
    }
}
