package com.tenebralis.dreamos.presentation.navigation

import android.net.Uri

/**
 * 路由定义（密封类）
 *
 * 当前包含 Auth、Home、Settings 及 B3/B4/B5 的占位路由。
 */
sealed class Screen(val route: String) {

    /** 认证页面（登录/注册/OTP） */
    data object Auth : Screen("auth")

    /** DreamOS 主界面（B3 实现） */
    data object Home : Screen("home")

    /** 世界入口（B4 实现） */
    data object World : Screen("world")

    /** 身份入口（B4 实现） */
    data object Identity : Screen("identity")

    /** 存档入口（B4 实现） */
    data object SaveSelect : Screen("save_select")

    /** 会话列表入口（B5 实现） */
    data object ChatList : Screen("chat_list")

    /** 会话详情入口（B5 实现） */
    data object ChatDetail : Screen("chat_detail")

    /** 设置页面（本次实现） */
    data object Settings : Screen("settings")

    /** 连接页面（C2 实现） */
    data object Connection : Screen("connection")

    /** 任务入口（E 实现） */
    data object Task : Screen("task")

    /** 档案入口（E/F 实现） */
    data object Profile : Screen("profile")

    /**
     * 通用功能占位路由
     *
     * 用于 Home 中尚未实现的功能入口（论坛/商店/番茄钟等）。
     */
    data object FeaturePlaceholder : Screen("feature/{featureName}") {
        const val ARG_FEATURE_NAME = "featureName"

        fun createRoute(featureName: String): String {
            return "feature/${Uri.encode(featureName)}"
        }
    }
}
