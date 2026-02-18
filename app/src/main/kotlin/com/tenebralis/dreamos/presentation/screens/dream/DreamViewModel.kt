package com.tenebralis.dreamos.presentation.screens.dream

import android.util.Log

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.usecase.chat.GetMessagesUseCase
import com.tenebralis.dreamos.domain.usecase.chat.NoApiKeyException
import com.tenebralis.dreamos.domain.usecase.chat.NoConnectionException
import com.tenebralis.dreamos.domain.usecase.chat.SendMessageUseCase
import com.tenebralis.dreamos.domain.usecase.common.UseCaseErrorMapper
import com.tenebralis.dreamos.domain.usecase.dream.EnterDreamUseCase
import com.tenebralis.dreamos.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DreamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val enterDreamUseCase: EnterDreamUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val saveId: String =
        checkNotNull(savedStateHandle[Screen.Dream.ARG_SAVE_ID])

    private val _uiState = MutableStateFlow(DreamUiState())
    val uiState: StateFlow<DreamUiState> = _uiState.asStateFlow()

    /** 已初始化的 conversationId，由 EnterDream 设置 */
    private var conversationId: String? = null

    init {
        initializeDream()
    }

    fun onEvent(event: DreamEvent) {
        when (event) {
            DreamEvent.Refresh -> refreshMessages()
            DreamEvent.Send -> sendMessage(_uiState.value.inputText.trim())
            DreamEvent.RetrySend -> retrySend()
            DreamEvent.RetryAiCall -> retryAiCall()
            DreamEvent.ClearAiError ->
                _uiState.update { it.copy(aiErrorMessage = null) }
            DreamEvent.ClearError ->
                _uiState.update { it.copy(errorMessage = null) }
            DreamEvent.ClearInfo ->
                _uiState.update { it.copy(infoMessage = null) }
            is DreamEvent.InputChanged ->
                _uiState.update { it.copy(inputText = event.text, errorMessage = null) }
            DreamEvent.ToggleContext ->
                _uiState.update { it.copy(isContextExpanded = !it.isContextExpanded) }
        }
    }

    // ─── 初始化 ─────────────────────────────────────────────

    private fun initializeDream() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitializing = true, errorMessage = null) }

            enterDreamUseCase(saveId).fold(
                onSuccess = { session ->
                    Log.d(TAG, "EnterDream success: world=${session.world.name}, npc=${session.narratorNpc.name}, conv=${session.conversation.id}")
                    conversationId = session.conversation.id
                    _uiState.update {
                        it.copy(
                            isInitializing = false,
                            session = session
                        )
                    }
                    refreshMessages()
                },
                onFailure = { error ->
                    Log.e(TAG, "EnterDream failed for saveId=$saveId", error)
                    _uiState.update {
                        it.copy(
                            isInitializing = false,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }

    // ─── 消息刷新 ───────────────────────────────────────────

    private fun refreshMessages() {
        val cid = conversationId ?: return
        viewModelScope.launch {
            getMessagesUseCase(cid).fold(
                onSuccess = { messages ->
                    _uiState.update {
                        it.copy(messages = messages.sortedBy { m -> m.seq })
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = UseCaseErrorMapper.toMessage(error))
                    }
                }
            )
        }
    }

    // ─── 发送消息 ───────────────────────────────────────────

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

    private fun sendMessage(content: String) {
        val cid = conversationId
        if (cid == null) {
            _uiState.update { it.copy(errorMessage = "梦境尚未初始化") }
            return
        }
        val normalizedContent = content.trim()
        if (normalizedContent.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请输入你的行动") }
            return
        }
        if (_uiState.value.isSending || _uiState.value.isAiResponding) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSending = true,
                    isAiResponding = true,
                    errorMessage = null,
                    aiErrorMessage = null,
                    infoMessage = null
                )
            }

            sendMessageUseCase(
                conversationId = cid,
                content = normalizedContent
            ).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            isAiResponding = false,
                            inputText = "",
                            failedContent = null,
                            aiErrorMessage = result.aiError,
                            infoMessage = if (result.aiError == null) null else "消息已发送，但叙事者回复失败"
                        )
                    }
                    refreshMessages()
                },
                onFailure = { error ->
                    val errorMsg = when (error) {
                        is NoConnectionException -> error.message
                        is NoApiKeyException -> error.message
                        else -> UseCaseErrorMapper.toMessage(error)
                    }
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            isAiResponding = false,
                            failedContent = normalizedContent,
                            errorMessage = errorMsg
                        )
                    }
                }
            )
        }
    }

    companion object {
        private const val TAG = "DreamViewModel"
    }
}
