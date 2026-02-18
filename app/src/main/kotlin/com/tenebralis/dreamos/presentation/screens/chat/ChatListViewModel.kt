package com.tenebralis.dreamos.presentation.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.Conversation
import com.tenebralis.dreamos.domain.model.Npc
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.NpcRepository
import com.tenebralis.dreamos.domain.usecase.chat.GetConversationsBySaveUseCase
import com.tenebralis.dreamos.domain.usecase.chat.GetNpcsUseCase
import com.tenebralis.dreamos.domain.usecase.chat.GetOrCreateConversationUseCase
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
class ChatListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getConversationsBySaveUseCase: GetConversationsBySaveUseCase,
    private val getOrCreateConversationUseCase: GetOrCreateConversationUseCase,
    private val getNpcsUseCase: GetNpcsUseCase,
    private val npcRepository: NpcRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val saveId: String? = savedStateHandle.get<String>(Screen.ChatList.ARG_SAVE_ID)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    private val _uiState = MutableStateFlow(ChatListUiState(saveId = saveId))
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onEvent(event: ChatListEvent) {
        when (event) {
            ChatListEvent.Refresh -> refresh()
            is ChatListEvent.SelectNpc -> selectNpc(event.npcId)
            is ChatListEvent.OpenConversation -> openConversation(event.conversationId)
            ChatListEvent.ConsumeNavigation ->
                _uiState.update { it.copy(navigateToConversationId = null) }

            ChatListEvent.ClearError ->
                _uiState.update { it.copy(errorMessage = null) }

            ChatListEvent.ClearInfo ->
                _uiState.update { it.copy(infoMessage = null) }

            ChatListEvent.ShowCreateNpcDialog ->
                _uiState.update { it.copy(showCreateNpcDialog = true) }

            ChatListEvent.DismissCreateNpcDialog ->
                _uiState.update { it.copy(showCreateNpcDialog = false) }

            is ChatListEvent.ConfirmCreateNpc ->
                createNpc(event.name, event.description)
        }
    }

    private fun refresh() {
        if (saveId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    conversations = emptyList(),
                    npcs = emptyList()
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val conversationsResult = getConversationsBySaveUseCase(saveId)
            val npcsResult = getNpcsUseCase()
            val conversations = conversationsResult.getOrNull().orEmpty().sortedByLastMessage()
            val npcs = npcsResult.getOrNull().orEmpty().sortedByNpcName()
            val firstError = conversationsResult.exceptionOrNull() ?: npcsResult.exceptionOrNull()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    conversations = conversations,
                    npcs = npcs,
                    errorMessage = firstError?.let(UseCaseErrorMapper::toMessage)
                )
            }
        }
    }

    private fun selectNpc(npcId: String) {
        val normalizedNpcId = npcId.trim()
        if (normalizedNpcId.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "npcId 不能为空") }
            return
        }
        if (saveId == null) {
            _uiState.update { it.copy(errorMessage = "请先进入梦境并选择存档") }
            return
        }
        if (_uiState.value.isCreatingConversation) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCreatingConversation = true,
                    selectedNpcId = normalizedNpcId,
                    errorMessage = null,
                    infoMessage = null
                )
            }

            getOrCreateConversationUseCase(
                saveId = saveId,
                npcId = normalizedNpcId,
                threadKey = GetOrCreateConversationUseCase.DEFAULT_THREAD_KEY
            ).fold(
                onSuccess = { conversation ->
                    _uiState.update {
                        it.copy(
                            isCreatingConversation = false,
                            selectedNpcId = null,
                            navigateToConversationId = conversation.id
                        )
                    }
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isCreatingConversation = false,
                            selectedNpcId = null,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }

    private fun openConversation(conversationId: String) {
        val normalizedConversationId = conversationId.trim()
        if (normalizedConversationId.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "conversationId 不能为空") }
            return
        }
        _uiState.update { it.copy(navigateToConversationId = normalizedConversationId) }
    }

    private fun createNpc(name: String, description: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "NPC 名称不能为空") }
            return
        }
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "当前未登录") }
            return
        }
        if (_uiState.value.isCreatingNpc) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingNpc = true, errorMessage = null) }

            val newNpc = Npc(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = trimmedName,
                description = description.trim().takeIf { it.isNotEmpty() },
                promptNpcText = null,
                personaJson = kotlinx.serialization.json.JsonObject(emptyMap()),
                createdAt = null,
                updatedAt = null
            )

            npcRepository.create(newNpc).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isCreatingNpc = false,
                            showCreateNpcDialog = false,
                            infoMessage = "NPC「${trimmedName}」创建成功"
                        )
                    }
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isCreatingNpc = false,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }
}

private fun List<Conversation>.sortedByLastMessage(): List<Conversation> {
    return sortedByDescending { it.lastMessageAt.orEmpty() }
}

private fun List<Npc>.sortedByNpcName(): List<Npc> {
    return sortedBy { it.name.lowercase() }
}
