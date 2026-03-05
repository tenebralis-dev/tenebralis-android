package com.tenebralis.dreamos.presentation.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.Conversation
import com.tenebralis.dreamos.domain.model.Npc
import com.tenebralis.dreamos.domain.repository.AiPresetRepository
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.NpcRepository
import com.tenebralis.dreamos.domain.usecase.chat.GetConversationsBySaveUseCase
import com.tenebralis.dreamos.domain.usecase.chat.GetNpcsUseCase
import com.tenebralis.dreamos.domain.usecase.chat.GetOrCreateConversationUseCase
import com.tenebralis.dreamos.domain.usecase.chat.GetOrCreateDefaultSaveUseCase
import com.tenebralis.dreamos.domain.usecase.common.UseCaseErrorMapper
import com.tenebralis.dreamos.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getConversationsBySaveUseCase: GetConversationsBySaveUseCase,
    private val getOrCreateConversationUseCase: GetOrCreateConversationUseCase,
    private val getNpcsUseCase: GetNpcsUseCase,
    private val getOrCreateDefaultSaveUseCase: GetOrCreateDefaultSaveUseCase,
    private val npcRepository: NpcRepository,
    private val authRepository: AuthRepository,
    private val aiPresetRepository: AiPresetRepository
) : ViewModel() {

    private val saveId: String? = savedStateHandle.get<String>(Screen.ChatList.ARG_SAVE_ID)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    /** 实际使用的 saveId：可能来自导航参数，也可能来自默认存档 */
    private var resolvedSaveId: String? = saveId

    private val _uiState = MutableStateFlow(ChatListUiState(saveId = saveId))
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        if (saveId != null) {
            refresh()
        } else {
            resolveDefaultSave()
        }
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

            ChatListEvent.DismissPresetPicker ->
                _uiState.update { it.copy(showPresetPicker = false, pendingNpcId = null) }

            is ChatListEvent.ConfirmPresetSelection ->
                confirmPresetSelection(event.presetId)

            is ChatListEvent.ShowNewThreadDialog ->
                showNewThreadDialog(event.npcId)

            ChatListEvent.DismissNewThreadDialog ->
                _uiState.update {
                    it.copy(
                        showNewThreadDialog = false,
                        newThreadNpcId = null,
                        newThreadDefaultName = ""
                    )
                }

            is ChatListEvent.ConfirmNewThread ->
                confirmNewThread(event.threadName)
        }
    }

    private fun resolveDefaultSave() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitializing = true) }
            getOrCreateDefaultSaveUseCase().fold(
                onSuccess = { defaultSaveId ->
                    resolvedSaveId = defaultSaveId
                    _uiState.update { it.copy(saveId = defaultSaveId, isInitializing = false) }
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isInitializing = false,
                            errorMessage = "初始化失败：${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun refresh() {
        val effectiveSaveId = resolvedSaveId
        if (effectiveSaveId == null) {
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

            val conversationsResult = getConversationsBySaveUseCase(effectiveSaveId)
            val npcsResult = getNpcsUseCase()
            val conversations = conversationsResult.getOrNull().orEmpty().sortedByLastMessage()
            val npcs = npcsResult.getOrNull().orEmpty().sortedByNpcName()
            val firstError = conversationsResult.exceptionOrNull() ?: npcsResult.exceptionOrNull()

            // 加载可用预设
            val presets = runCatching {
                aiPresetRepository.getByUser().first().getOrThrow()
            }.getOrNull().orEmpty()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    conversations = conversations,
                    npcs = npcs,
                    availablePresets = presets,
                    errorMessage = firstError?.let(UseCaseErrorMapper::toMessage)
                )
            }
        }
    }

    /**
     * 选择 NPC：
     * - 如果有可用预设 → 弹出预设选择器
     * - 如果无可用预设 → 直接创建/进入对话（presetId = null）
     */
    private fun selectNpc(npcId: String) {
        val normalizedNpcId = npcId.trim()
        if (normalizedNpcId.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "npcId 不能为空") }
            return
        }
        val effectiveSaveId = resolvedSaveId
        if (effectiveSaveId == null) {
            _uiState.update { it.copy(errorMessage = "请先进入梦境并选择存档") }
            return
        }
        if (_uiState.value.isCreatingConversation) return

        if (_uiState.value.availablePresets.isNotEmpty()) {
            // 有预设 → 弹出选择器
            _uiState.update {
                it.copy(
                    showPresetPicker = true,
                    pendingNpcId = normalizedNpcId
                )
            }
        } else {
            // 无预设 → 直接创建对话
            createConversation(normalizedNpcId, presetId = null)
        }
    }

    /**
     * 用户在预设选择器中确认选择（包括"不使用预设"→ presetId = null）。
     */
    private fun confirmPresetSelection(presetId: String?) {
        val npcId = _uiState.value.pendingNpcId
        if (npcId == null) {
            _uiState.update { it.copy(showPresetPicker = false, errorMessage = "未选择 NPC") }
            return
        }
        _uiState.update { it.copy(showPresetPicker = false, pendingNpcId = null) }
        createConversation(npcId, presetId)
    }

    /**
     * 打开新建线程弹窗，自动生成默认名称。
     * 统计该 NPC 已有的会话数量 n，默认名为 "thread-{n+1}"。
     */
    private fun showNewThreadDialog(npcId: String) {
        val normalizedNpcId = npcId.trim()
        if (normalizedNpcId.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "npcId 不能为空") }
            return
        }
        val existingCount = _uiState.value.conversations.count { it.npcId == normalizedNpcId }
        val defaultName = "thread-${existingCount + 1}"
        _uiState.update {
            it.copy(
                showNewThreadDialog = true,
                newThreadNpcId = normalizedNpcId,
                newThreadDefaultName = defaultName
            )
        }
    }

    /**
     * 确认创建新线程。
     */
    private fun confirmNewThread(threadName: String) {
        val npcId = _uiState.value.newThreadNpcId
        if (npcId == null) {
            _uiState.update {
                it.copy(
                    showNewThreadDialog = false,
                    errorMessage = "未选择 NPC"
                )
            }
            return
        }
        val normalizedName = threadName.trim()
        if (normalizedName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "线程名称不能为空") }
            return
        }
        _uiState.update {
            it.copy(
                showNewThreadDialog = false,
                newThreadNpcId = null,
                newThreadDefaultName = ""
            )
        }
        createConversation(npcId, presetId = null, threadKey = normalizedName)
    }

    /**
     * 创建或进入对话（内部统一入口）。
     */
    private fun createConversation(
        npcId: String,
        presetId: String?,
        threadKey: String = GetOrCreateConversationUseCase.DEFAULT_THREAD_KEY
    ) {
        val effectiveSaveId = resolvedSaveId ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCreatingConversation = true,
                    selectedNpcId = npcId,
                    errorMessage = null,
                    infoMessage = null
                )
            }

            getOrCreateConversationUseCase(
                saveId = effectiveSaveId,
                npcId = npcId,
                threadKey = threadKey,
                presetId = presetId
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
