package com.tenebralis.dreamos.data.remote.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 测试 [AiChatServiceImpl.parseSseChunkContent] 的 SSE 行解析逻辑。
 *
 * 不依赖网络，纯字符串解析单测。
 */
class SseParserTest {

    private val service = AiChatServiceImpl(
        httpClient = io.ktor.client.HttpClient()
    )

    // ── 正常场景 ──

    @Test
    fun `parse normal chunk with content`() {
        val payload = """{"id":"chatcmpl-1","choices":[{"index":0,"delta":{"content":"你好"},"finish_reason":null}]}"""
        val result = service.parseSseChunkContent(payload)
        assertEquals("你好", result)
    }

    @Test
    fun `parse chunk with role only returns null`() {
        // 首个 chunk 通常只有 role，没有 content
        val payload = """{"id":"chatcmpl-1","choices":[{"index":0,"delta":{"role":"assistant"},"finish_reason":null}]}"""
        val result = service.parseSseChunkContent(payload)
        assertNull(result)
    }

    @Test
    fun `parse chunk with empty content returns empty string`() {
        val payload = """{"id":"chatcmpl-1","choices":[{"index":0,"delta":{"content":""},"finish_reason":null}]}"""
        val result = service.parseSseChunkContent(payload)
        assertEquals("", result)
    }

    @Test
    fun `parse finish chunk returns null content`() {
        // 最后一个 chunk 的 delta 通常为空
        val payload = """{"id":"chatcmpl-1","choices":[{"index":0,"delta":{},"finish_reason":"stop"}]}"""
        val result = service.parseSseChunkContent(payload)
        assertNull(result)
    }

    // ── 异常场景 ──

    @Test
    fun `parse invalid json returns null`() {
        val result = service.parseSseChunkContent("not-valid-json")
        assertNull(result)
    }

    @Test
    fun `parse empty string returns null`() {
        val result = service.parseSseChunkContent("")
        assertNull(result)
    }

    @Test
    fun `parse empty choices returns null`() {
        val payload = """{"id":"chatcmpl-1","choices":[]}"""
        val result = service.parseSseChunkContent(payload)
        assertNull(result)
    }

    // ── 中文与特殊字符 ──

    @Test
    fun `parse chunk with chinese characters`() {
        val payload = """{"id":"chatcmpl-1","choices":[{"index":0,"delta":{"content":"这是一段包含特殊字符的文本：😀🎉"},"finish_reason":null}]}"""
        val result = service.parseSseChunkContent(payload)
        assertEquals("这是一段包含特殊字符的文本：😀🎉", result)
    }

    @Test
    fun `parse chunk with newline in content`() {
        val payload = """{"id":"chatcmpl-1","choices":[{"index":0,"delta":{"content":"第一行\n第二行"},"finish_reason":null}]}"""
        val result = service.parseSseChunkContent(payload)
        assertEquals("第一行\n第二行", result)
    }
}
