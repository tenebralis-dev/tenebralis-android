package com.tenebralis.dreamos.data.parser

import com.tenebralis.dreamos.domain.model.CharacterCardData
import com.tenebralis.dreamos.domain.model.RegexScriptData
import com.tenebralis.dreamos.domain.model.WorldBookData
import com.tenebralis.dreamos.domain.model.WorldBookEntryData
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

/**
 * 角色卡解析器
 *
 * 支持 JSON 和 PNG（内嵌 tEXt chunk）两种格式。
 * 自动检测旧格式（含 data 子对象）并转换为统一的 [CharacterCardData]。
 */
object CharacterCardParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // ─── JSON 解析 ────────────────────────────────────────

    /**
     * 从 JSON 字符串解析角色卡。
     * 自动检测是否为旧格式（含 `data` 子对象）并做转换。
     */
    fun parseFromJson(jsonString: String): CharacterCardData {
        val root = json.parseToJsonElement(jsonString).jsonObject
        return if (root.containsKey("data")) {
            convertFromLegacy(root)
        } else {
            convertFromNew(root)
        }
    }

    // ─── PNG 解析 ─────────────────────────────────────────

    /**
     * 从 PNG 文件解析角色卡。
     *
     * @return Pair(角色卡数据, PNG 图片完整字节)
     */
    fun parseFromPng(inputStream: InputStream): Pair<CharacterCardData, ByteArray> {
        val pngBytes = inputStream.readBytes()
        val charaText = extractPngTextChunk(pngBytes, "chara")
            ?: throw IllegalArgumentException("PNG 文件中未找到 chara tEXt chunk")

        val decodedJson = String(
            android.util.Base64.decode(charaText, android.util.Base64.DEFAULT),
            StandardCharsets.UTF_8
        )
        return parseFromJson(decodedJson) to pngBytes
    }

    // ─── 旧格式转换 ──────────────────────────────────────

    private fun convertFromLegacy(raw: JsonObject): CharacterCardData {
        val data = raw["data"]?.jsonObject ?: JsonObject(emptyMap())

        val name = data.str("name") ?: raw.str("name") ?: ""
        val description = data.str("description") ?: raw.str("description") ?: ""
        val avatar = raw.str("avatar") ?: raw.str("avatar_url") ?: data.str("avatar") ?: ""

        val firstMes = data.str("first_mes") ?: raw.str("first_mes") ?: ""
        val altGreetings = data["alternate_greetings"]?.jsonArrayOrNull()
            ?: raw["alternate_greetings"]?.jsonArrayOrNull()
            ?: emptyList()
        val messages = buildList {
            if (firstMes.isNotBlank()) add(firstMes)
            addAll(altGreetings.mapNotNull { it.str() })
        }

        val worldBook = (raw["worldBook"]?.jsonObjectOrNull()
            ?: data["character_book"]?.jsonObjectOrNull())?.let { parseWorldBook(it) }

        val regexScripts = (raw["regexScripts"]?.jsonArrayOrNull()
            ?: data["extensions"]?.jsonObjectOrNull()?.get("regex_scripts")?.jsonArrayOrNull()
            ?: emptyList()).mapNotNull { parseRegexScript(it) }

        val chatDate = raw.str("chatDate") ?: raw.str("chat") ?: ""
        val createDate = raw.str("createDate") ?: raw.str("create_date") ?: ""

        return CharacterCardData(
            name = name,
            description = description,
            avatar = avatar,
            messages = messages,
            worldBook = worldBook,
            regexScripts = regexScripts,
            other = extractOtherFields(data),
            chatDate = chatDate,
            createDate = createDate
        )
    }

    // ─── 新格式解析 ──────────────────────────────────────

    private fun convertFromNew(raw: JsonObject): CharacterCardData {
        val name = raw.str("name") ?: ""
        val description = raw.str("description") ?: ""
        val avatar = raw.str("avatar") ?: ""

        val messages = raw["message"]?.jsonArrayOrNull()
            ?.mapNotNull { it.str() }
            ?: emptyList()

        val worldBook = raw["worldBook"]?.jsonObjectOrNull()?.let { parseWorldBook(it) }
        val regexScripts = raw["regexScripts"]?.jsonArrayOrNull()
            ?.mapNotNull { parseRegexScript(it) }
            ?: emptyList()

        return CharacterCardData(
            name = name,
            description = description,
            avatar = avatar,
            messages = messages,
            worldBook = worldBook,
            regexScripts = regexScripts,
            other = emptyMap(),
            chatDate = raw.str("chatDate") ?: "",
            createDate = raw.str("createDate") ?: ""
        )
    }

    // ─── 世界书解析 ──────────────────────────────────────

    private fun parseWorldBook(obj: JsonObject): WorldBookData {
        val entries = obj["entries"]?.jsonArrayOrNull()?.mapNotNull { entry ->
            val e = entry.jsonObjectOrNull() ?: return@mapNotNull null
            WorldBookEntryData(
                index = e["index"]?.jsonPrimitive?.intOrNull ?: 0,
                name = e.str("name") ?: "",
                content = e.str("content") ?: "",
                enabled = e["enabled"]?.jsonPrimitive?.booleanOrNull ?: true,
                activationMode = e.str("activationMode") ?: "keyword",
                key = e["key"]?.jsonArrayOrNull()?.mapNotNull { it.str() } ?: emptyList(),
                secondaryKey = e["secondaryKey"]?.jsonArrayOrNull()?.mapNotNull { it.str() } ?: emptyList(),
                selectiveLogic = e.str("selectiveLogic") ?: "andAny",
                order = e["order"]?.jsonPrimitive?.intOrNull ?: 0,
                depth = e["depth"]?.jsonPrimitive?.intOrNull ?: 0,
                position = e.str("position") ?: "",
                role = e.str("role"),
                caseSensitive = e["caseSensitive"]?.jsonPrimitive?.booleanOrNull,
                excludeRecursion = e["excludeRecursion"]?.jsonPrimitive?.booleanOrNull ?: false,
                preventRecursion = e["preventRecursion"]?.jsonPrimitive?.booleanOrNull ?: false,
                probability = e["probability"]?.jsonPrimitive?.intOrNull ?: 100,
                other = emptyMap()
            )
        } ?: emptyList()

        return WorldBookData(
            name = obj.str("name") ?: "",
            entries = entries
        )
    }

    // ─── 正则脚本解析 ────────────────────────────────────

    private fun parseRegexScript(element: JsonElement): RegexScriptData? {
        val obj = element.jsonObjectOrNull() ?: return null
        return RegexScriptData(
            id = obj.str("id") ?: "",
            name = obj.str("name") ?: "",
            enabled = obj["enabled"]?.jsonPrimitive?.booleanOrNull ?: true,
            findRegex = obj.str("findRegex") ?: "",
            replaceRegex = obj.str("replaceRegex") ?: "",
            trimRegex = obj["trimRegex"]?.jsonArrayOrNull()?.mapNotNull { it.str() } ?: emptyList(),
            targets = obj["targets"]?.jsonArrayOrNull()?.mapNotNull { it.str() } ?: emptyList(),
            view = obj["view"]?.jsonArrayOrNull()?.mapNotNull { it.str() } ?: emptyList(),
            runOnEdit = obj["runOnEdit"]?.jsonPrimitive?.booleanOrNull ?: false,
            macroMode = obj.str("macroMode") ?: "none",
            minDepth = obj["minDepth"]?.jsonPrimitive?.intOrNull,
            maxDepth = obj["maxDepth"]?.jsonPrimitive?.intOrNull
        )
    }

    // ─── PNG tEXt chunk 提取 ─────────────────────────────

    /**
     * 从 PNG 字节中提取指定 key 的 tEXt chunk 值。
     */
    private fun extractPngTextChunk(pngBytes: ByteArray, targetKey: String): String? {
        // PNG signature: 8 bytes
        if (pngBytes.size < 8) return null
        var offset = 8

        while (offset + 8 < pngBytes.size) {
            val length = ByteBuffer.wrap(pngBytes, offset, 4).int
            val typeBytes = pngBytes.copyOfRange(offset + 4, offset + 8)
            val type = String(typeBytes, StandardCharsets.US_ASCII)

            val dataStart = offset + 8
            val dataEnd = dataStart + length

            if (type == "tEXt" && dataEnd <= pngBytes.size) {
                val chunkData = pngBytes.copyOfRange(dataStart, dataEnd)
                val nullIndex = chunkData.indexOf(0.toByte())
                if (nullIndex > 0) {
                    val key = String(chunkData, 0, nullIndex, StandardCharsets.ISO_8859_1)
                    if (key == targetKey) {
                        return String(
                            chunkData,
                            nullIndex + 1,
                            chunkData.size - nullIndex - 1,
                            StandardCharsets.ISO_8859_1
                        )
                    }
                }
            }

            if (type == "IEND") break

            // length + type(4) + data(length) + crc(4)
            offset = dataEnd + 4
        }
        return null
    }

    // ─── 辅助工具 ────────────────────────────────────────

    private val KNOWN_DATA_FIELDS = setOf(
        "name", "description", "personality", "scenario",
        "first_mes", "alternate_greetings", "mes_example",
        "creator_notes", "system_prompt", "post_history_instructions",
        "character_book", "tags", "creator", "character_version", "extensions"
    )

    private fun extractOtherFields(data: JsonObject): Map<String, String> {
        val result = mutableMapOf<String, String>()
        // 存储 personality, scenario 等到 other 中（保留原始值用于提示词组装）
        data.str("personality")?.let { result["personality"] = it }
        data.str("scenario")?.let { result["scenario"] = it }
        data.str("mes_example")?.let { result["mes_example"] = it }
        data.str("system_prompt")?.let { result["system_prompt"] = it }
        data.str("post_history_instructions")?.let { result["post_history_instructions"] = it }
        data.str("creator_notes")?.let { result["creator_notes"] = it }
        data.str("creator")?.let { result["creator"] = it }
        data.str("character_version")?.let { result["character_version"] = it }
        return result
    }

    // ─── JsonElement 扩展 ────────────────────────────────

    private fun JsonObject.str(key: String): String? =
        this[key]?.jsonPrimitive?.takeIf { it.isString }?.content

    private fun JsonElement.str(): String? =
        try { jsonPrimitive.takeIf { it.isString }?.content } catch (_: Exception) { null }

    private fun JsonElement.jsonObjectOrNull(): JsonObject? =
        try { jsonObject } catch (_: Exception) { null }

    private fun JsonElement.jsonArrayOrNull(): List<JsonElement>? =
        try { jsonArray.toList() } catch (_: Exception) { null }
}
