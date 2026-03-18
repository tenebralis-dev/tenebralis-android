package com.tenebralis.dreamos.presentation.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tenebralis.dreamos.presentation.components.DockTab
import com.tenebralis.dreamos.presentation.components.TenebralisDock
import com.tenebralis.dreamos.presentation.navigation.Screen

/**
 * Tenebralis 主界面（IM 风格）
 *
 * 底部 4 个 Tab：对话 / 发现 / 工具 / 我的
 * 使用 Telegram 风格浮动 Dock 导航栏
 */
@Composable
fun TenebralisMainScreen(
    onNavigateRoute: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        DockTab("对话", Icons.Filled.Chat, Icons.Outlined.Chat),
        DockTab("发现", Icons.Filled.Explore, Icons.Outlined.Explore),
        DockTab("工具", Icons.Filled.Build, Icons.Outlined.Build),
        DockTab("我的", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Scaffold(
        bottomBar = {
            TenebralisDock(
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                tabs = tabs
            )
        }
    ) { innerPadding ->
        Crossfade(
            targetState = selectedTab,
            animationSpec = tween(durationMillis = 200),
            label = "tab_crossfade"
        ) { tab ->
            when (tab) {
                0 -> ConversationsTab(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateRoute = onNavigateRoute
                )
                1 -> DiscoverTab(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateRoute = onNavigateRoute
                )
                2 -> ToolsTab(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateRoute = onNavigateRoute
                )
                3 -> ProfileTab(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateRoute = onNavigateRoute
                )
            }
        }
    }
}

/**
 * 对话 Tab — 直接复用 ChatList 路由入口
 *
 * 作为主 Tab 时不显示返回按钮，点击对话项导航到 ChatDetail。
 */
@Composable
private fun ConversationsTab(
    modifier: Modifier = Modifier,
    onNavigateRoute: (String) -> Unit
) {
    // 直接导航到 ChatList（不带 saveId）
    com.tenebralis.dreamos.presentation.screens.chat.ChatListScreen(
        modifier = modifier,
        isRootTab = true,
        onBackClick = { /* 主 Tab 不需要返回 */ },
        onNavigateToChatDetail = { conversationId ->
            onNavigateRoute(Screen.ChatDetail.createRoute(conversationId))
        }
    )
}
