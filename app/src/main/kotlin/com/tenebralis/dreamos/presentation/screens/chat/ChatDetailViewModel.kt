package com.tenebralis.dreamos.presentation.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.usecase.chat.GetMessagesUseCase
import com.tenebralis.dreamos.domain.usecase.chat.SendLocalMessageUseCase
import com.tenebralis.dreamos.domain.usecase.common.UseCaseErrorMapper
import com.tenebralis.dreamos.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendLocalMessageUseCase: SendLocalMessageUseCase
) : ViewModel() {

    private val conversationId: String =
        checkNotNull(savedStateHandle[Screen.ChatDetail.ARG_CONVERSATION_ID])

    private val _uiState = MutableStateFlow(
        ChatDetailUiState(conversationId = conversationId)
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

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
            ChatDetailEvent.ClearError ->
                _uiState.update { it.copy(errorMessage = null) }

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

    private fun sendMessage(content: String) {
        val normalizedContent = content.trim()
        if (normalizedContent.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请输入消息内容") }
            return
        }
        if (_uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSending = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }

            sendLocalMessageUseCase(
                conversationId = conversationId,
                content = normalizedContent
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            inputText = "",
                            failedContent = null,
                            infoMessage = "消息已发送"
                        )
                    }
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            failedContent = normalizedContent,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }
}
