package com.tenebralis.dreamos.presentation.screens.npc

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.Npc
import com.tenebralis.dreamos.domain.model.PersonaJsonData
import com.tenebralis.dreamos.domain.model.avatarPath
import com.tenebralis.dreamos.domain.model.avatarUrl
import com.tenebralis.dreamos.domain.repository.AvatarStorageRepository
import com.tenebralis.dreamos.domain.repository.NpcRepository
import com.tenebralis.dreamos.domain.usecase.common.UseCaseErrorMapper
import com.tenebralis.dreamos.domain.usecase.npc.UpdateNpcUseCase
import com.tenebralis.dreamos.domain.usecase.npc.UploadNpcAvatarUseCase
import com.tenebralis.dreamos.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

data class NpcEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,

    // 基本信息
    val name: String = "",
    val description: String = "",

    // 头像
    val avatarUrl: String = "",           // 直链 URL（用于展示）
    val avatarPath: String = "",          // 存储桶内路径
    val isAvatarDialogVisible: Boolean = false,
    val avatarDialogInput: String = "",
    val isUploading: Boolean = false,

    // 角色卡参数
    val personality: String = "",
    val scenario: String = "",
    val mesExample: String = "",
    val systemPrompt: String = "",
    val postHistoryInstructions: String = "",
    val firstMessage: String = "",

    // 元信息（只读展示）
    val creatorNotes: String = "",
    val creator: String = "",
    val characterVersion: String = "",
    val tags: List<String> = emptyList(),
    val source: String = "",

    // 消息
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    // 内部
    val originalNpc: Npc? = null
)

@HiltViewModel
class NpcEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val npcRepository: NpcRepository,
    private val updateNpcUseCase: UpdateNpcUseCase,
    private val uploadNpcAvatarUseCase: UploadNpcAvatarUseCase,
    private val avatarStorageRepository: AvatarStorageRepository
) : ViewModel() {

    private val npcId: String = checkNotNull(savedStateHandle[Screen.NpcEdit.ARG_NPC_ID])
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _uiState = MutableStateFlow(NpcEditUiState())
    val uiState: StateFlow<NpcEditUiState> = _uiState.asStateFlow()

    init {
        loadNpc()
    }

    private fun loadNpc() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            npcRepository.getById(npcId).fold(
                onSuccess = { npc ->
                    val persona = try {
                        json.decodeFromJsonElement(
                            PersonaJsonData.serializer(),
                            npc.personaJson
                        )
                    } catch (_: Exception) {
                        PersonaJsonData()
                    }

                    // 确定展示用的头像 URL
                    val directUrl = npc.avatarUrl ?: ""
                    val storagePath = npc.avatarPath ?: ""

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            originalNpc = npc,
                            name = npc.name,
                            description = npc.description ?: "",
                            avatarUrl = directUrl,
                            avatarPath = storagePath,
                            personality = persona.personality ?: "",
                            scenario = persona.scenario ?: "",
                            mesExample = persona.mesExample ?: "",
                            systemPrompt = persona.systemPrompt ?: "",
                            postHistoryInstructions = persona.postHistoryInstructions ?: "",
                            firstMessage = persona.firstMessage ?: "",
                            creatorNotes = persona.creatorNotes ?: "",
                            creator = persona.creator ?: "",
                            characterVersion = persona.characterVersion ?: "",
                            tags = persona.tags,
                            source = persona.source ?: ""
                        )
                    }

                    // 如果没有直链但有存储路径，生成签名 URL
                    if (directUrl.isBlank() && storagePath.isNotBlank()) {
                        resolveSignedUrl(storagePath)
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

    /**
     * 为存储路径生成签名 URL 并更新到 avatarUrl
     */
    private fun resolveSignedUrl(path: String) {
        viewModelScope.launch {
            avatarStorageRepository.createSignedUrl(path).fold(
                onSuccess = { signedUrl ->
                    _uiState.update { it.copy(avatarUrl = signedUrl) }
                },
                onFailure = { /* 签名失败静默处理，头像显示兜底占位 */ }
            )
        }
    }

    // ─── 字段更新 ──────────────────────────────────────────

    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun updatePersonality(value: String) = _uiState.update { it.copy(personality = value) }
    fun updateScenario(value: String) = _uiState.update { it.copy(scenario = value) }
    fun updateMesExample(value: String) = _uiState.update { it.copy(mesExample = value) }
    fun updateSystemPrompt(value: String) = _uiState.update { it.copy(systemPrompt = value) }
    fun updatePostHistoryInstructions(value: String) =
        _uiState.update { it.copy(postHistoryInstructions = value) }
    fun updateFirstMessage(value: String) = _uiState.update { it.copy(firstMessage = value) }

    // ─── 头像弹窗 ──────────────────────────────────────────

    fun showAvatarDialog() {
        _uiState.update {
            it.copy(isAvatarDialogVisible = true, avatarDialogInput = it.avatarUrl)
        }
    }

    fun dismissAvatarDialog() {
        _uiState.update { it.copy(isAvatarDialogVisible = false) }
    }

    fun updateAvatarDialogInput(value: String) {
        _uiState.update { it.copy(avatarDialogInput = value) }
    }

    /**
     * 确认直链 URL → 清空 avatarPath（直链优先）
     */
    fun confirmAvatarUrl() {
        val url = _uiState.value.avatarDialogInput.trim()
        _uiState.update {
            it.copy(
                avatarUrl = url,
                avatarPath = "",           // 直链模式，清空存储路径
                isAvatarDialogVisible = false
            )
        }
    }

    /**
     * 从相册选择图片后触发上传
     */
    fun onImagePicked(uri: Uri) {
        if (_uiState.value.isUploading) return

        _uiState.update { it.copy(isUploading = true, isAvatarDialogVisible = false) }

        viewModelScope.launch {
            uploadNpcAvatarUseCase(npcId, uri).fold(
                onSuccess = { storagePath ->
                    // 上传成功 → 生成签名 URL 用于展示
                    avatarStorageRepository.createSignedUrl(storagePath).fold(
                        onSuccess = { signedUrl ->
                            _uiState.update {
                                it.copy(
                                    isUploading = false,
                                    avatarPath = storagePath,
                                    avatarUrl = signedUrl,     // 签名 URL 仅展示用，不存库
                                    infoMessage = "头像上传成功"
                                )
                            }
                        },
                        onFailure = {
                            _uiState.update {
                                it.copy(
                                    isUploading = false,
                                    avatarPath = storagePath,
                                    avatarUrl = "",
                                    infoMessage = "头像上传成功（预览加载失败）"
                                )
                            }
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            errorMessage = "头像上传失败：${error.message}"
                        )
                    }
                }
            )
        }
    }

    // ─── 保存 ─────────────────────────────────────────────

    fun save() {
        val state = _uiState.value
        val original = state.originalNpc ?: return
        if (state.isSaving) return

        val trimmedName = state.name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "NPC 名称不能为空") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            // 反序列化原始 persona，保留不可编辑的字段
            val oldPersona = try {
                json.decodeFromJsonElement(PersonaJsonData.serializer(), original.personaJson)
            } catch (_: Exception) {
                PersonaJsonData()
            }

            // 确定最终的 avatar 字段值
            val storagePath = state.avatarPath.trim().takeIf { it.isNotBlank() }
            val directUrl = if (storagePath != null) {
                // 使用上传的图片时，avatar_url 不存直链（展示靠签名 URL）
                null
            } else {
                state.avatarUrl.trim().takeIf { it.isNotBlank() }
            }

            val updatedPersona = oldPersona.copy(
                avatarUrl = directUrl,
                avatarPath = storagePath,
                avatarFile = directUrl ?: oldPersona.avatarFile,
                personality = state.personality.takeIf { it.isNotBlank() },
                scenario = state.scenario.takeIf { it.isNotBlank() },
                mesExample = state.mesExample.takeIf { it.isNotBlank() },
                systemPrompt = state.systemPrompt.takeIf { it.isNotBlank() },
                postHistoryInstructions = state.postHistoryInstructions.takeIf { it.isNotBlank() },
                firstMessage = state.firstMessage.takeIf { it.isNotBlank() }
            )

            val personaJsonString = json.encodeToString(PersonaJsonData.serializer(), updatedPersona)
            val personaJsonObject: JsonObject = json.parseToJsonElement(personaJsonString).jsonObject

            // 组装 prompt_npc_text
            val promptParts = buildList {
                add(state.description.trim())
                state.personality.trim().takeIf { it.isNotBlank() }?.let { add("性格：$it") }
                state.scenario.trim().takeIf { it.isNotBlank() }?.let { add("场景：$it") }
            }

            val updatedNpc = original.copy(
                name = trimmedName,
                description = state.description.trim().takeIf { it.isNotBlank() },
                promptNpcText = promptParts.joinToString("\n\n"),
                personaJson = personaJsonObject
            )

            updateNpcUseCase(updatedNpc).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            infoMessage = "已保存"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = UseCaseErrorMapper.toMessage(error)
                        )
                    }
                }
            )
        }
    }

    fun consumeError() = _uiState.update { it.copy(errorMessage = null) }
    fun consumeInfo() = _uiState.update { it.copy(infoMessage = null) }
}
