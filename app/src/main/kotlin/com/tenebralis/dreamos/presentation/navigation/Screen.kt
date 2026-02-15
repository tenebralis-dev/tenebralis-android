package com.tenebralis.dreamos.presentation.navigation

/**
 * 路由定义（密封类）
 *
 * B2 阶段仅定义 Auth + Home 两个路由，后续 B3/B4 阶段扩展。
 */
sealed class Screen(val route: String) {

    /** 认证页面（登录/注册/OTP） */
    data object Auth : Screen("auth")

    /** DreamOS 主界面（B3 实现） */
    data object Home : Screen("home")

    // 后续路由占位（B3/B4 阶段实现）：
    // data object WorldList : Screen("world_list")
    // data object WorldDetail : Screen("world_detail/{worldId}")
    // data object IdentitySelect : Screen("identity_select/{worldId}")
    // data object SaveSelect : Screen("save_select/{identityId}")
    // data object ChatList : Screen("chat_list/{saveId}")
    // data object ChatDetail : Screen("chat_detail/{conversationId}")
    // data object Settings : Screen("settings")
    // data object Connection : Screen("connection")
}
