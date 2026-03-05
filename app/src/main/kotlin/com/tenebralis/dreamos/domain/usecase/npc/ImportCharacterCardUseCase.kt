package com.tenebralis.dreamos.domain.usecase.npc

import com.tenebralis.dreamos.data.parser.CharacterCardParser
import com.tenebralis.dreamos.domain.model.CharacterCardData
import com.tenebralis.dreamos.domain.model.Npc
import com.tenebralis.dreamos.domain.model.PersonaJsonData
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.NpcRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

/**
 * 导入角色卡为 NPC
 *
 * 支持 JSON 和 PNG 文件格式。
 * 返回 [ImportResult] 指示导入结果或同名冲突。
 */
class ImportCharacterCardUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val npcRepository: NpcRepository
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    sealed class ImportResult {
        /** 导入成功 */
        data class Success(val npc: Npc) : ImportResult()

        /** 同名冲突，需用户选择处理方式 */
        data class Conflict(
            val existingNpc: Npc,
            val cardData: CharacterCardData,
            val pngBytes: ByteArray?
        ) : ImportResult()
    }

    /**
     * 解析并导入角色卡文件。
     *
     * @param inputStream 文件输入流
     * @param fileName 文件名（用于判断格式）
     */
    suspend operator fun invoke(
        inputStream: InputStream,
        fileName: String
    ): Result<ImportResult> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        val (cardData, pngBytes) = parseFile(inputStream, fileName)

        // 同名冲突检测
        val existing = npcRepository.getByName(cardData.name).getOrNull()
        if (existing != null) {
            return@runCatching ImportResult.Conflict(
                existingNpc = existing,
                cardData = cardData,
                pngBytes = pngBytes
            )
        }

        val npc = createNpcFromCard(userId, cardData)
        val created = npcRepository.create(npc).getOrThrow()
        ImportResult.Success(created)
    }

    /**
     * 覆盖更新已有 NPC
     */
    suspend fun overwrite(
        existingNpcId: String,
        cardData: CharacterCardData
    ): Result<Npc> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        val npc = createNpcFromCard(userId, cardData).copy(id = existingNpcId)
        npcRepository.update(npc).getOrThrow()
    }

    /**
     * 使用新名称导入（避免冲突）
     */
    suspend fun importWithRename(
        cardData: CharacterCardData,
        newName: String
    ): Result<Npc> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        val renamed = cardData.copy(name = newName.trim())
        val npc = createNpcFromCard(userId, renamed)
        npcRepository.create(npc).getOrThrow()
    }

    // ─── 内部方法 ────────────────────────────────────────

    private fun parseFile(
        inputStream: InputStream,
        fileName: String
    ): Pair<CharacterCardData, ByteArray?> {
        val lowerName = fileName.lowercase()
        return when {
            lowerName.endsWith(".png") -> {
                val (card, bytes) = CharacterCardParser.parseFromPng(inputStream)
                card to bytes
            }
            lowerName.endsWith(".json") -> {
                val jsonString = inputStream.bufferedReader().readText()
                CharacterCardParser.parseFromJson(jsonString) to null
            }
            else -> throw IllegalArgumentException("不支持的文件格式：$fileName（仅支持 .json 和 .png）")
        }
    }

    private fun createNpcFromCard(userId: String, card: CharacterCardData): Npc {
        // 构建 prompt_npc_text：description + personality + scenario
        val promptParts = buildList {
            add(card.description)
            card.other["personality"]?.takeIf { it.isNotBlank() }?.let { add("性格：$it") }
            card.other["scenario"]?.takeIf { it.isNotBlank() }?.let { add("场景：$it") }
        }
        val promptText = promptParts.joinToString("\n\n")

        // 构建 persona_json
        val avatarRaw = card.avatar.takeIf { it.isNotBlank() }
        val isUrl = avatarRaw?.let {
            it.startsWith("http://", ignoreCase = true) ||
                it.startsWith("https://", ignoreCase = true)
        } ?: false

        // 序列化 world_book
        val characterBookJson = card.worldBook?.let { wb ->
            val entriesArray = wb.entries.map { entry ->
                JsonObject(buildMap {
                    put("index", JsonPrimitive(entry.index))
                    put("name", JsonPrimitive(entry.name))
                    put("content", JsonPrimitive(entry.content))
                    put("enabled", JsonPrimitive(entry.enabled))
                    put("activationMode", JsonPrimitive(entry.activationMode))
                    put("key", JsonArray(entry.key.map { JsonPrimitive(it) }))
                    put("secondaryKey", JsonArray(entry.secondaryKey.map { JsonPrimitive(it) }))
                    put("selectiveLogic", JsonPrimitive(entry.selectiveLogic))
                    put("order", JsonPrimitive(entry.order))
                    put("depth", JsonPrimitive(entry.depth))
                    put("position", JsonPrimitive(entry.position))
                    entry.role?.let { put("role", JsonPrimitive(it)) }
                    entry.caseSensitive?.let { put("caseSensitive", JsonPrimitive(it)) }
                    put("excludeRecursion", JsonPrimitive(entry.excludeRecursion))
                    put("preventRecursion", JsonPrimitive(entry.preventRecursion))
                    put("probability", JsonPrimitive(entry.probability))
                })
            }
            JsonObject(mapOf(
                "name" to JsonPrimitive(wb.name),
                "entries" to JsonArray(entriesArray)
            ))
        }

        // 序列化 regex_scripts
        val regexScriptsJson = card.regexScripts.map { rs ->
            JsonObject(buildMap {
                put("id", JsonPrimitive(rs.id))
                put("name", JsonPrimitive(rs.name))
                put("enabled", JsonPrimitive(rs.enabled))
                put("findRegex", JsonPrimitive(rs.findRegex))
                put("replaceRegex", JsonPrimitive(rs.replaceRegex))
                put("trimRegex", JsonArray(rs.trimRegex.map { JsonPrimitive(it) }))
                put("targets", JsonArray(rs.targets.map { JsonPrimitive(it) }))
                put("view", JsonArray(rs.view.map { JsonPrimitive(it) }))
                put("runOnEdit", JsonPrimitive(rs.runOnEdit))
                put("macroMode", JsonPrimitive(rs.macroMode))
                rs.minDepth?.let { put("minDepth", JsonPrimitive(it)) }
                rs.maxDepth?.let { put("maxDepth", JsonPrimitive(it)) }
            })
        }

        // 解析 depth_prompt（从 other 中的 extensions 透传）
        val depthPrompt = card.other["depth_prompt_text"]?.let { prompt ->
            com.tenebralis.dreamos.domain.model.DepthPromptData(
                prompt = prompt,
                depth = card.other["depth_prompt_depth"]?.toIntOrNull() ?: 4,
                role = card.other["depth_prompt_role"] ?: "system"
            )
        }

        val persona = PersonaJsonData(
            source = "sillytavern",
            sourceFormatVersion = "v2",
            avatarFile = avatarRaw,
            avatarUrl = if (isUrl) avatarRaw else null,
            firstMessage = card.messages.firstOrNull(),
            alternateGreetings = card.messages.drop(1),
            personality = card.other["personality"],
            scenario = card.other["scenario"],
            mesExample = card.other["mes_example"],
            systemPrompt = card.other["system_prompt"],
            postHistoryInstructions = card.other["post_history_instructions"],
            creatorNotes = card.other["creator_notes"],
            creator = card.other["creator"],
            characterVersion = card.other["character_version"],
            tags = card.tags,
            depthPrompt = depthPrompt,
            characterBook = characterBookJson,
            regexScripts = regexScriptsJson
        )
        val personaJsonString = json.encodeToString(persona)
        val personaJsonObject = json.parseToJsonElement(personaJsonString).jsonObject

        return Npc(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = card.name,
            description = card.description,
            promptNpcText = promptText,
            personaJson = personaJsonObject,
            createdAt = null,
            updatedAt = null
        )
    }
}
