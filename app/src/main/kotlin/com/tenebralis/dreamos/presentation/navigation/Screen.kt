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

    /** 梦境入口（自动续梦 → 世界选择） */
    data object DreamEntry : Screen("dream_entry")

    /** 梦境 TRPG 叙事 */
    data object Dream : Screen("dream/{saveId}") {
        const val ARG_SAVE_ID = "saveId"

        fun createRoute(saveId: String): String {
            val normalizedSaveId = saveId.trim()
            require(normalizedSaveId.isNotEmpty()) { "saveId 不能为空" }
            return "dream/${Uri.encode(normalizedSaveId)}"
        }
    }

    /** 世界入口（B4 实现） */
    data object World : Screen("world")

    /** 身份入口（B4 实现） */
    data object Identity : Screen("identity/{worldId}") {
        const val ARG_WORLD_ID = "worldId"

        fun createRoute(worldId: String): String {
            return "identity/${Uri.encode(worldId)}"
        }
    }

    /** 存档入口（B4 实现） */
    data object SaveSelect : Screen("save_select/{worldId}/{identityId}") {
        const val ARG_WORLD_ID = "worldId"
        const val ARG_IDENTITY_ID = "identityId"

        fun createRoute(worldId: String, identityId: String): String {
            return "save_select/${Uri.encode(worldId)}/${Uri.encode(identityId)}"
        }
    }

    /** 会话列表入口（B5 实现） */
    data object ChatList : Screen("chat_list?saveId={saveId}") {
        const val BASE_ROUTE = "chat_list"
        const val ARG_SAVE_ID = "saveId"

        fun createRoute(saveId: String?): String {
            val normalizedSaveId = saveId?.trim()
            return if (normalizedSaveId.isNullOrEmpty()) {
                BASE_ROUTE
            } else {
                "$BASE_ROUTE?saveId=${Uri.encode(normalizedSaveId)}"
            }
        }
    }

    /** 会话详情入口（B5 实现） */
    data object ChatDetail : Screen("chat_detail/{conversationId}") {
        const val ARG_CONVERSATION_ID = "conversationId"

        fun createRoute(conversationId: String): String {
            val normalizedConversationId = conversationId.trim()
            require(normalizedConversationId.isNotEmpty()) { "conversationId 不能为空" }
            return "chat_detail/${Uri.encode(normalizedConversationId)}"
        }
    }

    /** 设置页面（本次实现） */
    data object Settings : Screen("settings")

    /** 连接页面（C2 实现） */
    data object Connection : Screen("connection")

    /** 任务入口（E 实现） */
    data object Task : Screen("task")

    /** 记忆管理页面（M5 实现） */
    data object Memory : Screen("memory")

    /** 档案入口（E/F 实现） */
    data object Profile : Screen("profile")

    /** 备忘管理（M6 实现） */
    data object Notes : Screen("notes")

    /** 日历管理（M6 实现） */
    data object Calendar : Screen("calendar")

    /** 番茄钟（M6 实现） */
    data object Pomodoro : Screen("pomodoro")

    /** 成就页面（M7 实现） */
    data object Achievement : Screen("achievement")

    /** 好感度页面（M7 实现） */
    data object Affinity : Screen("affinity?worldId={worldId}") {
        const val BASE_ROUTE = "affinity"
        const val ARG_WORLD_ID = "worldId"

        fun createRoute(worldId: String? = null): String {
            return if (worldId.isNullOrBlank()) BASE_ROUTE
            else "$BASE_ROUTE?worldId=${Uri.encode(worldId)}"
        }
    }

    /** 钱包页面（M7.5 实现） */
    data object Wallet : Screen("wallet")

    /** 论坛帖子列表（M7.5 实现） */
    data object Forum : Screen("forum")

    /** 论坛帖子详情（M7.5 实现） */
    data object ForumPostDetail : Screen("forum_post/{postId}") {
        const val ARG_POST_ID = "postId"

        fun createRoute(postId: String): String {
            return "forum_post/${Uri.encode(postId)}"
        }
    }

    /** 发帖页面（M7.5 实现） */
    data object CreatePost : Screen("create_post")

    /** 商店页面（M7.5 实现） */
    data object Shop : Screen("shop")

    /**
     * 通用功能占位路由
     *
     * 用于 Home 中尚未实现的功能入口。
     */
    data object FeaturePlaceholder : Screen("feature/{featureName}") {
        const val ARG_FEATURE_NAME = "featureName"

        fun createRoute(featureName: String): String {
            return "feature/${Uri.encode(featureName)}"
        }
    }
}
