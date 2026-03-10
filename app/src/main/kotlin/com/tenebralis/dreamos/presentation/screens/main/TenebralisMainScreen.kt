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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.tenebralis.dreamos.presentation.navigation.Screen

/**
 * Tenebralis 主界面（IM 风格）
 *
 * 底部 4 个 Tab：对话 / 发现 / 工具 / 我的
 */
@Composable
fun TenebralisMainScreen(
    onNavigateRoute: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        TabItem("对话", Icons.Filled.Chat, Icons.Outlined.Chat),
        TabItem("发现", Icons.Filled.Explore, Icons.Outlined.Explore),
        TabItem("工具", Icons.Filled.Build, Icons.Outlined.Build),
        TabItem("我的", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
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

private data class TabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

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
