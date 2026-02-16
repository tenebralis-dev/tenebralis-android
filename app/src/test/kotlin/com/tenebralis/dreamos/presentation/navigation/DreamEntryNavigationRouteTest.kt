package com.tenebralis.dreamos.presentation.navigation

import com.tenebralis.dreamos.presentation.screens.home.dreamOsDockItems
import org.junit.Assert.assertEquals
import org.junit.Test

class DreamEntryNavigationRouteTest {

    @Test
    fun `dream entry route and dock route are configured correctly`() {
        assertEquals("dream_entry", Screen.DreamEntry.route)

        val dreamDockRoute = dreamOsDockItems()
            .first { it.title == "梦境" }
            .route

        assertEquals(Screen.DreamEntry.route, dreamDockRoute)
    }
}
