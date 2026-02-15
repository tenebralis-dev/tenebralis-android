package com.tenebralis.dreamos.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tenebralis.dreamos.domain.model.SessionState
import com.tenebralis.dreamos.presentation.screens.auth.AuthScreen

/**
 * DreamOS 导航图
 *
 * 根据 [sessionState] 控制路由跳转：
 * - Authenticated → Home
 * - NotAuthenticated → Auth
 *
 * B2 阶段 Home 为占位，B3 阶段实现完整 DreamOS 桌面。
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
            // 占位：B3 阶段实现 DreamOS 桌面（三页 Pager + Dock）
            HomeScreenPlaceholder()
        }
    }

    // 监听认证状态变化，执行路由跳转
    LaunchedEffect(sessionState) {
        when (sessionState) {
            is SessionState.Authenticated -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is SessionState.NotAuthenticated -> {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> { /* Loading / Error 不做路由跳转 */ }
        }
    }
}

/**
 * Home 占位界面（B3 阶段替换为完整 DreamOS 桌面）
 */
@Composable
private fun HomeScreenPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "界影浮光 · Dream OS",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
