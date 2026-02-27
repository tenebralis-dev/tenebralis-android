package com.tenebralis.dreamos.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tenebralis.dreamos.domain.model.SessionState
import com.tenebralis.dreamos.presentation.screens.home.HomeScreen
import com.tenebralis.dreamos.presentation.screens.auth.AuthScreen
import com.tenebralis.dreamos.presentation.screens.chat.ChatDetailScreen
import com.tenebralis.dreamos.presentation.screens.chat.ChatListScreen
import com.tenebralis.dreamos.presentation.screens.connection.ConnectionScreen
import com.tenebralis.dreamos.presentation.screens.dreamentry.DreamEntryScreen
import com.tenebralis.dreamos.presentation.screens.dream.DreamScreen
import com.tenebralis.dreamos.presentation.screens.identity.IdentityScreen
import com.tenebralis.dreamos.presentation.screens.save.SaveSelectScreen
import com.tenebralis.dreamos.presentation.screens.settings.SettingsScreen
import com.tenebralis.dreamos.presentation.screens.world.WorldScreen

/**
 * DreamOS 导航图
 *
 * 根据 [sessionState] 控制路由跳转：
 * - Authenticated → Home
 * - NotAuthenticated → Auth
 */
@Composable
fun DreamOsNavGraph(
    sessionState: SessionState,
    navController: NavHostController = rememberNavController()
) {
    // 根据认证状态决定起始路由
    val startDestination = when (sessionState) {
        is SessionState.Authenticated -> Screen.Home.route
        else -> Screen.Auth.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            AuthScreen()
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onRouteNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.DreamEntry.route) {
            DreamEntryScreen(
                onNavigateRoute = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.DreamEntry.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.Dream.route,
            arguments = listOf(
                navArgument(Screen.Dream.ARG_SAVE_ID) {
                    type = NavType.StringType
                }
            )
        ) {
            DreamScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.World.route) {
            WorldScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToIdentity = { worldId ->
                    navController.navigate(Screen.Identity.createRoute(worldId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Screen.Identity.route,
            arguments = listOf(
                navArgument(Screen.Identity.ARG_WORLD_ID) {
                    type = NavType.StringType
                }
            )
        ) {
            IdentityScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToSaveSelect = { worldId, identityId ->
                    navController.navigate(
                        Screen.SaveSelect.createRoute(
                            worldId = worldId,
                            identityId = identityId
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Screen.SaveSelect.route,
            arguments = listOf(
                navArgument(Screen.SaveSelect.ARG_WORLD_ID) {
                    type = NavType.StringType
                },
                navArgument(Screen.SaveSelect.ARG_IDENTITY_ID) {
                    type = NavType.StringType
                }
            )
        ) {
            SaveSelectScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToChatList = { saveId ->
                    navController.navigate(Screen.ChatList.createRoute(saveId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Screen.ChatList.route,
            arguments = listOf(
                navArgument(Screen.ChatList.ARG_SAVE_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            ChatListScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToChatDetail = { conversationId ->
                    navController.navigate(Screen.ChatDetail.createRoute(conversationId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument(Screen.ChatDetail.ARG_CONVERSATION_ID) {
                    type = NavType.StringType
                }
            )
        ) {
            ChatDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Connection.route) {
            ConnectionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        // ─── 任务（M7）──────────────────────────────────────
        composable(Screen.Task.route) {
            com.tenebralis.dreamos.presentation.screens.task.TaskScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ─── 成就（M7）──────────────────────────────────────
        composable(Screen.Achievement.route) {
            com.tenebralis.dreamos.presentation.screens.achievement.AchievementScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ─── 好感度（M7）────────────────────────────────────
        composable(
            route = Screen.Affinity.route,
            arguments = listOf(
                navArgument(Screen.Affinity.ARG_WORLD_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            com.tenebralis.dreamos.presentation.screens.affinity.AffinityScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            FeaturePlaceholderScreen(title = "档案")
        }

        // ─── 记忆管理 ────────────────────────────────────────
        composable(Screen.Memory.route) {
            com.tenebralis.dreamos.presentation.screens.memory.MemoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ─── 备忘管理（M6）──────────────────────────────────
        composable(Screen.Notes.route) {
            com.tenebralis.dreamos.presentation.screens.notes.NoteScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ─── 日历管理（M6）──────────────────────────────────
        composable(Screen.Calendar.route) {
            com.tenebralis.dreamos.presentation.screens.calendar.CalendarScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ─── 番茄钟（M6）────────────────────────────────────
        composable(Screen.Pomodoro.route) {
            com.tenebralis.dreamos.presentation.screens.pomodoro.PomodoroScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ─── 钱包（M7.5）──────────────────────────────────────
        composable(Screen.Wallet.route) {
            com.tenebralis.dreamos.presentation.screens.wallet.WalletScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ─── 论坛（M7.5）──────────────────────────────────────
        composable(Screen.Forum.route) {
            com.tenebralis.dreamos.presentation.screens.forum.ForumScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPost = { postId ->
                    navController.navigate(Screen.ForumPostDetail.createRoute(postId)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.CreatePost.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.ForumPostDetail.route,
            arguments = listOf(
                navArgument(Screen.ForumPostDetail.ARG_POST_ID) {
                    type = NavType.StringType
                }
            )
        ) {
            com.tenebralis.dreamos.presentation.screens.forum.ForumPostDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CreatePost.route) {
            com.tenebralis.dreamos.presentation.screens.forum.CreatePostScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ─── 商店（M7.5）──────────────────────────────────────
        composable(Screen.Shop.route) {
            com.tenebralis.dreamos.presentation.screens.shop.ShopScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ─── 自定义（壁纸/图标/配色/字体）────────────────────────
        composable(Screen.Customize.route) {
            com.tenebralis.dreamos.presentation.screens.customize.CustomizeScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFont = {
                    navController.navigate(Screen.FontPicker.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // ─── 字体选择（自定义模块）────────────────────────────────
        composable(Screen.FontPicker.route) {
            com.tenebralis.dreamos.presentation.screens.font.FontPickerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ─── NPC 管理（角色卡）────────────────────────────────
        composable(Screen.NpcList.route) {
            com.tenebralis.dreamos.presentation.screens.npc.NpcListScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToEdit = { npcId ->
                    navController.navigate(Screen.NpcEdit.createRoute(npcId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // ─── NPC 编辑详情 ─────────────────────────────────────
        composable(
            route = Screen.NpcEdit.route,
            arguments = listOf(
                navArgument(Screen.NpcEdit.ARG_NPC_ID) {
                    type = NavType.StringType
                }
            )
        ) {
            com.tenebralis.dreamos.presentation.screens.npc.NpcEditScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ─── 预设管理（兼容 SillyTavern 预设）───────────────────────
        composable(Screen.Preset.route) {
            FeaturePlaceholderScreen(title = "预设")
        }

        // ─── 通用功能占位 ────────────────────────────────────
        composable(
            route = Screen.FeaturePlaceholder.route,
            arguments = listOf(
                navArgument(Screen.FeaturePlaceholder.ARG_FEATURE_NAME) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val featureName = backStackEntry.arguments
                ?.getString(Screen.FeaturePlaceholder.ARG_FEATURE_NAME)
                ?.ifBlank { null }
                ?.let(Uri::decode)
                ?: "功能"
            FeaturePlaceholderScreen(title = featureName)
        }
    }

    // 监听认证状态变化，执行路由跳转
    LaunchedEffect(sessionState) {
        when (sessionState) {
            is SessionState.Authenticated -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is SessionState.NotAuthenticated -> {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> { /* Loading / Error 不做路由跳转 */ }
        }
    }
}

/**
 * 通用功能占位界面
 */
@Composable
private fun FeaturePlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title 功能开发中",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
