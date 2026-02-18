package com.tenebralis.dreamos.presentation.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.usecase.chat.GetMessagesUseCase
import com.tenebralis.dreamos.domain.usecase.chat.SendMessageUseCase
import com.tenebralis.dreamos.domain.usecase.chat.StreamEvent
import com.tenebralis.dreamos.domain.usecase.common.UseCaseErrorMapper
import com.tenebralis.dreamos.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val conversationId: String =
        checkNotNull(savedStateHandle[Screen.ChatDetail.ARG_CONVERSATION_ID])

    private val _uiState = MutableStateFlow(
        ChatDetailUiState(conversationId = conversationId)
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    /** 当前流式生成 Job，用于支持 StopStreaming 取消 */
    private var streamJob: Job? = null

    init {
        refresh()
    }

    fun onEvent(event: ChatDetailEvent) {
        when (event) {
            ChatDetailEvent.Refresh -> refresh()
            is ChatDetailEvent.InputChanged ->
                _uiState.update { it.copy(inputText = event.value, errorMessage = null) }

            ChatDetailEvent.Send -> sendMessage(_uiState.value.inputText.trim())
            ChatDetailEvent.RetrySend -> retrySend()
            ChatDetailEvent.RetryAiCall -> retryAiCall()
            ChatDetailEvent.StopStreaming -> stopStreaming()
            ChatDetailEvent.ClearError ->
                _uiState.update { it.copy(errorMessage = null) }

            ChatDetailEvent.ClearAiError ->
                _uiState.update { it.copy(aiErrorMessage = null) }

            ChatDetailEvent.ClearInfo ->
                _uiState.update { it.copy(infoMessage = null) }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getMessagesUseCase(conversationId).fold(
                onSuccess = { messages ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = messages.sortedBy { message -> message.seq }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }

    private fun retrySend() {
        val content = _uiState.value.failedContent
        if (content.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "没有可重试的消息") }
            return
        }
        sendMessage(content)
    }

    private fun retryAiCall() {
        val lastUserContent = _uiState.value.messages
            .lastOrNull { it.role == com.tenebralis.dreamos.domain.model.enums.MessageRole.USER }
            ?.content

        if (lastUserContent.isNullOrBlank()) {
            _uiState.update { it.copy(aiErrorMessage = "没有可重试的消息") }
            return
        }
        _uiState.update { it.copy(aiErrorMessage = null) }
        sendMessage(lastUserContent)
    }

    private fun stopStreaming() {
        streamJob?.cancel()
        streamJob = null
        // 取消后 UseCase 的 CancellationException 处理会保存部分内容
        // UI 状态在 onCompletion 中统一清理
    }

    private fun sendMessage(content: String) {
        val normalizedContent = content.trim()
        if (normalizedContent.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请输入消息内容") }
            return
        }
        if (_uiState.value.isSending || _uiState.value.isAiResponding) return

        streamJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSending = true,
                    isAiResponding = true,
                    errorMessage = null,
                    aiErrorMessage = null,
                    streamingContent = null,
                    infoMessage = null
                )
            }

            sendMessageUseCase.invokeStream(
                conversationId = conversationId,
                content = normalizedContent
            ).catch { error ->
                _uiState.update {
                    it.copy(
                        failedContent = normalizedContent,
                        errorMessage = UseCaseErrorMapper.toMessage(error)
                    )
                }
            }.onCompletion {
                // 无论正常结束、错误或取消，统一清理流式状态
                _uiState.update {
                    it.copy(
                        isSending = false,
                        isAiResponding = false,
                        streamingContent = null
                    )
                }
                streamJob = null
                // 刷新消息列表以获取落库的消息
                refresh()
            }.collect { event ->
                when (event) {
                    is StreamEvent.UserMessageSaved -> {
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                inputText = "",
                                failedContent = null,
                                messages = it.messages + event.message
                            )
                        }
                    }

                    is StreamEvent.AiChunk -> {
                        _uiState.update {
                            it.copy(streamingContent = event.textSoFar)
                        }
                    }

                    is StreamEvent.AiCompleted -> {
                        _uiState.update {
                            it.copy(
                                streamingContent = null,
                                messages = it.messages + event.assistant
                            )
                        }
                    }

                    is StreamEvent.AiError -> {
                        _uiState.update {
                            it.copy(
                                aiErrorMessage = event.error,
                                infoMessage = "消息已发送，但 AI 回复失败"
                            )
                        }
                    }
                }
            }
        }
    }
}

