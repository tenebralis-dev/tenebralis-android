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
import com.tenebralis.dreamos.presentation.screens.settings.SettingsScreen

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

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.World.route) {
            FeaturePlaceholderScreen(title = "世界")
        }
        composable(Screen.Identity.route) {
            FeaturePlaceholderScreen(title = "身份")
        }
        composable(Screen.SaveSelect.route) {
            FeaturePlaceholderScreen(title = "存档")
        }
        composable(Screen.ChatList.route) {
            FeaturePlaceholderScreen(title = "对话列表")
        }
        composable(Screen.ChatDetail.route) {
            FeaturePlaceholderScreen(title = "对话详情")
        }
        composable(Screen.Connection.route) {
            FeaturePlaceholderScreen(title = "连接")
        }
        composable(Screen.Task.route) {
            FeaturePlaceholderScreen(title = "任务")
        }
        composable(Screen.Profile.route) {
            FeaturePlaceholderScreen(title = "档案")
        }
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
