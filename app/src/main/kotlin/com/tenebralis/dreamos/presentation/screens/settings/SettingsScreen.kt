package com.tenebralis.dreamos.presentation.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebralis.dreamos.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(SettingsEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ─── 账号信息 ────────────────────────────────────
            SettingsSectionTitle("账号信息")
            AccountInfoCard(uiState = uiState)

            // ─── 通知提醒 ────────────────────────────────────
            SettingsSectionTitle("通知提醒")
            SettingsItemCard(
                icon = Icons.Filled.Notifications,
                title = "推送通知",
                subtitle = "提醒策略与免打扰（后续接入）"
            )

            // ─── 关于 ───────────────────────────────────────
            SettingsSectionTitle("关于")
            SettingsItemCard(
                icon = Icons.Filled.Code,
                title = "开源地址",
                subtitle = "GitHub · kirenath/tenebralis-android",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/kirenath/tenebralis-android")
                    )
                    context.startActivity(intent)
                }
            )
            SettingsItemCard(
                icon = Icons.Filled.Description,
                title = "用户协议",
                subtitle = "查看 Tenebralis 用户协议",
                onClick = { viewModel.onEvent(SettingsEvent.ShowAgreementDialog) }
            )
            SettingsItemCard(
                icon = Icons.Filled.Info,
                title = "版本",
                subtitle = BuildConfig.VERSION_NAME
            )

            // ─── 危险操作 ────────────────────────────────────
            SettingsSectionTitle("危险操作")
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "退出登录",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "退出后将回到登录页",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                        )
                    }
                    Button(
                        onClick = { viewModel.onEvent(SettingsEvent.ShowLogoutDialog) },
                        enabled = !uiState.isSubmitting
                    ) {
                        Text("登出")
                    }
                }
            }
        }
    }

    // ─── 登出确认弹窗 ──────────────────────────────────
    if (uiState.isLogoutDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isSubmitting) {
                    viewModel.onEvent(SettingsEvent.DismissLogoutDialog)
                }
            },
            title = { Text("确认登出") },
            text = { Text("你确定要退出当前账号吗？") },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isSubmitting,
                    onClick = { viewModel.onEvent(SettingsEvent.ConfirmLogout) }
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("确认登出")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isSubmitting,
                    onClick = { viewModel.onEvent(SettingsEvent.DismissLogoutDialog) }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // ─── 用户协议弹窗 ──────────────────────────────────
    if (uiState.isAgreementDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SettingsEvent.DismissAgreementDialog) },
            title = { Text("用户协议") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = """
欢迎使用 Tenebralis（以下简称"本应用"）。请您在使用前仔细阅读以下条款：

1. 服务说明
本应用为用户提供基于 AI 的沉浸式角色扮演体验。所有功能均以"现状"提供，开发者不对服务的持续可用性作出保证。

2. 用户行为
用户应遵守当地法律法规，不得利用本应用进行违法活动。用户对其在本应用中生成的内容负全部责任。

3. 隐私保护
本应用仅收集提供服务所必需的信息（如邮箱、用户名）。我们不会向第三方出售或出租您的个人信息。

4. 知识产权
本应用的源代码以开源协议发布，用户在该协议范围内享有相关权利。用户生成的原创内容归用户所有。

5. 免责声明
本应用中 AI 生成的内容不代表开发者的观点或立场。开发者不对 AI 生成内容的准确性、合法性承担责任。

6. 条款修改
开发者保留随时修改本协议的权利。修改后的条款将在应用内公告后生效。

如有疑问，请通过 GitHub Issues 联系我们。
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(SettingsEvent.DismissAgreementDialog) }
                ) {
                    Text("已阅读")
                }
            }
        )
    }
}

// ─── 账号信息卡片 ────────────────────────────────────
@Composable
private fun AccountInfoCard(uiState: SettingsUiState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isUserLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                // 用户名
                AccountInfoRow(
                    icon = Icons.Filled.Person,
                    label = "用户名",
                    value = uiState.username ?: "未设置"
                )
                // 邮箱
                AccountInfoRow(
                    icon = Icons.Filled.Email,
                    label = "邮箱",
                    value = uiState.email ?: "未绑定"
                )
                // 注册时间
                AccountInfoRow(
                    icon = Icons.Filled.Schedule,
                    label = "注册时间",
                    value = uiState.createdAt?.take(10) ?: "未知"
                )
            }
        }
    }
}

@Composable
private fun AccountInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─── 通用 Section Title ────────────────────────────────
@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

// ─── 通用设置项卡片（支持 icon + 点击） ───────────────────
@Composable
private fun SettingsItemCard(
    icon: ImageVector? = null,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
