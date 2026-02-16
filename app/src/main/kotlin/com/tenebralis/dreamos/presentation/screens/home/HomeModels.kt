package com.tenebralis.dreamos.presentation.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.tenebralis.dreamos.presentation.navigation.Screen

data class HomeAppItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

data class HomeDockItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

fun dreamOsHomePages(): List<List<HomeAppItem>> {
    return listOf(
        listOf(
            HomeAppItem("好感", Icons.Filled.Favorite, Screen.FeaturePlaceholder.createRoute("好感")),
            HomeAppItem("身份", Icons.Filled.Person, Screen.World.route),
            HomeAppItem("世界", Icons.Filled.Public, Screen.World.route),
            HomeAppItem("论坛", Icons.Filled.Forum, Screen.FeaturePlaceholder.createRoute("论坛")),
            HomeAppItem("商店", Icons.Filled.Storefront, Screen.FeaturePlaceholder.createRoute("商店")),
            HomeAppItem("成就", Icons.Filled.EmojiEvents, Screen.FeaturePlaceholder.createRoute("成就"))
        ),
        listOf(
            HomeAppItem("备忘", Icons.Filled.Description, Screen.FeaturePlaceholder.createRoute("备忘")),
            HomeAppItem("账本", Icons.Filled.AccountBalanceWallet, Screen.FeaturePlaceholder.createRoute("账本")),
            HomeAppItem("相册", Icons.Filled.PhotoLibrary, Screen.FeaturePlaceholder.createRoute("相册")),
            HomeAppItem("日历", Icons.Filled.Event, Screen.FeaturePlaceholder.createRoute("日历")),
            HomeAppItem("番茄钟", Icons.Filled.Timer, Screen.FeaturePlaceholder.createRoute("番茄钟")),
            HomeAppItem("音乐", Icons.Filled.MusicNote, Screen.FeaturePlaceholder.createRoute("音乐"))
        ),
        listOf(
            HomeAppItem("自定义", Icons.Filled.Tune, Screen.FeaturePlaceholder.createRoute("自定义")),
            HomeAppItem("连接", Icons.Filled.Link, Screen.Connection.route),
            HomeAppItem("记忆", Icons.Filled.Bookmark, Screen.FeaturePlaceholder.createRoute("记忆")),
            HomeAppItem("设置", Icons.Filled.Settings, Screen.Settings.route)
        )
    )
}

fun dreamOsDockItems(): List<HomeDockItem> {
    return listOf(
        HomeDockItem("梦境", Icons.Filled.Home, Screen.DreamEntry.route),
        HomeDockItem("对话", Icons.Filled.Chat, Screen.ChatList.createRoute(saveId = null)),
        HomeDockItem("任务", Icons.Filled.Assignment, Screen.Task.route),
        HomeDockItem("档案", Icons.Filled.AccountCircle, Screen.Profile.route)
    )
}
