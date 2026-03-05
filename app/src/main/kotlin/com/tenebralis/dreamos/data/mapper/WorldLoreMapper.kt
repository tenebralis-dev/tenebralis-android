package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.WorldLoreBookDto
import com.tenebralis.dreamos.data.remote.dto.WorldLoreEntryDto
import com.tenebralis.dreamos.domain.model.EntryPosition
import com.tenebralis.dreamos.domain.model.EntryRole
import com.tenebralis.dreamos.domain.model.SelectiveLogic
import com.tenebralis.dreamos.domain.model.WorldLoreBook
import com.tenebralis.dreamos.domain.model.WorldLoreBookSource
import com.tenebralis.dreamos.domain.model.WorldLoreEntry
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

// ─── WorldLoreBook ───────────────────────────────────

fun WorldLoreBookDto.toDomain() = WorldLoreBook(
    id = id,
    name = name,
    description = description,
    source = WorldLoreBookSource.fromString(source),
    sourceFileName = sourceFileName,
    isEnabled = isEnabled,
    displayOrder = displayOrder,
    globalScanDepth = globalScanDepth,
    globalCaseSensitive = globalCaseSensitive,
    globalMatchWholeWords = globalMatchWholeWords,
    tokenBudget = tokenBudget,
    storagePath = storagePath,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun WorldLoreBook.toDto(userId: String) = WorldLoreBookDto(
    id = id,
    userId = userId,
    name = name,
    description = description,
    source = source.name.lowercase(),
    sourceFileName = sourceFileName,
    isEnabled = isEnabled,
    displayOrder = displayOrder,
    globalScanDepth = globalScanDepth,
    globalCaseSensitive = globalCaseSensitive,
    globalMatchWholeWords = globalMatchWholeWords,
    tokenBudget = tokenBudget,
    storagePath = storagePath,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ─── WorldLoreEntry ──────────────────────────────────

fun WorldLoreEntryDto.toDomain() = WorldLoreEntry(
    id = id,
    bookId = bookId,
    uid = uid,
    comment = comment,
    content = content,
    keys = keysJson.map { it.jsonPrimitive.content },
    secondaryKeys = secondaryKeysJson.map { it.jsonPrimitive.content },
    constant = constant,
    selective = selective,
    selectiveLogic = SelectiveLogic.fromInt(selectiveLogic),
    order = entryOrder,
    position = EntryPosition.fromInt(position),
    disabled = disable,
    probability = probability,
    useProbability = useProbability,
    depth = depth,
    role = role?.let { EntryRole.fromInt(it) },
    group = entryGroup,
    groupOverride = groupOverride,
    groupWeight = groupWeight,
    scanDepth = scanDepth,
    caseSensitive = caseSensitive,
    matchWholeWords = matchWholeWords,
    sticky = sticky,
    cooldown = cooldown,
    delay = delay,
    excludeRecursion = excludeRecursion,
    preventRecursion = preventRecursion,
    useGroupScoring = useGroupScoring,
    displayIndex = displayIndex,
    automationId = automationId
)

fun WorldLoreEntry.toDto(userId: String) = WorldLoreEntryDto(
    id = id,
    userId = userId,
    bookId = bookId,
    uid = uid,
    comment = comment,
    content = content,
    keysJson = JsonArray(keys.map { JsonPrimitive(it) }),
    secondaryKeysJson = JsonArray(secondaryKeys.map { JsonPrimitive(it) }),
    constant = constant,
    selective = selective,
    selectiveLogic = selectiveLogic.value,
    entryOrder = order,
    position = position.value,
    disable = disabled,
    probability = probability,
    useProbability = useProbability,
    depth = depth,
    role = role?.value,
    entryGroup = group,
    groupOverride = groupOverride,
    groupWeight = groupWeight,
    scanDepth = scanDepth,
    caseSensitive = caseSensitive,
    matchWholeWords = matchWholeWords,
    sticky = sticky,
    cooldown = cooldown,
    delay = delay,
    excludeRecursion = excludeRecursion,
    preventRecursion = preventRecursion,
    useGroupScoring = useGroupScoring,
    displayIndex = displayIndex,
    automationId = automationId
)
