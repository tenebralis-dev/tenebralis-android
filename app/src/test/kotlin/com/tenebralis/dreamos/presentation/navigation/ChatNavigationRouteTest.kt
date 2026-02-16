package com.tenebralis.dreamos.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatNavigationRouteTest {

    @Test
    fun `chat list route supports with and without saveId`() {
        assertEquals("chat_list", Screen.ChatList.createRoute(saveId = null))
        assertEquals("chat_list", Screen.ChatList.createRoute(saveId = "   "))
        assertEquals("chat_list?saveId=save-1", Screen.ChatList.createRoute(saveId = "save-1"))
        assertEquals("chat_list?saveId=save%2F1", Screen.ChatList.createRoute(saveId = "save/1"))
        assertEquals("chat_list?saveId={saveId}", Screen.ChatList.route)
    }

    @Test
    fun `chat detail route requires conversationId`() {
        assertEquals("chat_detail/conversation-1", Screen.ChatDetail.createRoute("conversation-1"))
        assertEquals("chat_detail/conversation%2F1", Screen.ChatDetail.createRoute("conversation/1"))
        assertTrue(Screen.ChatDetail.route.contains("{conversationId}"))

        assertThrows(IllegalArgumentException::class.java) {
            Screen.ChatDetail.createRoute("   ")
        }
    }
}
