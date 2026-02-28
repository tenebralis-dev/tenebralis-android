package com.tenebralis.dreamos.presentation.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
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
            HomeAppItem("好感", Icons.Filled.Favorite, Screen.Affinity.createRoute()),
            HomeAppItem("身份", Icons.Filled.Person, Screen.NpcList.route),
            HomeAppItem("任务", Icons.Filled.Assignment, Screen.Task.route),
            HomeAppItem("论坛", Icons.Filled.Forum, Screen.Forum.route),
            HomeAppItem("商店", Icons.Filled.Storefront, Screen.Shop.route),
            HomeAppItem("成就", Icons.Filled.EmojiEvents, Screen.Achievement.route)
        ),
        listOf(
            HomeAppItem("备忘", Icons.Filled.Description, Screen.Notes.route),
            HomeAppItem("钱包", Icons.Filled.AccountBalanceWallet, Screen.Wallet.route),
            HomeAppItem("相册", Icons.Filled.PhotoLibrary, Screen.FeaturePlaceholder.createRoute("相册")),
            HomeAppItem("日历", Icons.Filled.Event, Screen.Calendar.route),
            HomeAppItem("番茄钟", Icons.Filled.Timer, Screen.Pomodoro.route),
            HomeAppItem("音乐", Icons.Filled.MusicNote, Screen.FeaturePlaceholder.createRoute("音乐"))
        ),
        listOf(
            HomeAppItem("预设", Icons.Filled.Layers, Screen.Preset.route),
            HomeAppItem("上下文", Icons.Filled.DataObject, Screen.Context.route),
            HomeAppItem("自定义", Icons.Filled.Tune, Screen.Customize.route),
            HomeAppItem("连接", Icons.Filled.Link, Screen.Connection.route),
            HomeAppItem("记忆", Icons.Filled.Bookmark, Screen.Memory.route),
            HomeAppItem("设置", Icons.Filled.Settings, Screen.Settings.route)
        )
    )
}

fun dreamOsDockItems(): List<HomeDockItem> {
    return listOf(
        HomeDockItem("世界", Icons.Filled.Public, Screen.World.route),
        HomeDockItem("梦境", Icons.Filled.Home, Screen.DreamEntry.route),
        HomeDockItem("对话", Icons.Filled.Chat, Screen.ChatList.createRoute(saveId = null)),
        HomeDockItem("档案", Icons.Filled.AccountCircle, Screen.Profile.route)
    )
}
