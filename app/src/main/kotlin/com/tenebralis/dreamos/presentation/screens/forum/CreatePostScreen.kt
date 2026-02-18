package com.tenebralis.dreamos.presentation.screens.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebralis.dreamos.domain.model.enums.AuthorType
import com.tenebralis.dreamos.domain.model.enums.CommentingMode
import com.tenebralis.dreamos.domain.model.enums.ForumVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onBack: () -> Unit,
    viewModel: ForumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var worldId by remember { mutableStateOf("") }
    var selectedAuthorType by remember { mutableStateOf(AuthorType.USER) }
    var selectedVisibility by remember { mutableStateOf(ForumVisibility.WORLD) }
    var selectedCommentingMode by remember { mutableStateOf(CommentingMode.ALL) }
    var authorTypeExpanded by remember { mutableStateOf(false) }
    var visibilityExpanded by remember { mutableStateOf(false) }
    var commentingModeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ForumEvent.DismissError)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ForumEvent.DismissSuccess)
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发帖") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 世界 ID
            OutlinedTextField(
                value = worldId,
                onValueChange = { worldId = it },
                label = { Text("世界 ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 标题
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 内容
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                maxLines = 8
            )

            // 作者类型下拉
            ExposedDropdownMenuBox(
                expanded = authorTypeExpanded,
                onExpandedChange = { authorTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = when (selectedAuthorType) {
                        AuthorType.USER -> "用户"
                        AuthorType.NPC -> "NPC"
                        AuthorType.IDENTITY -> "身份"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("作者类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(authorTypeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = authorTypeExpanded,
                    onDismissRequest = { authorTypeExpanded = false }
                ) {
                    AuthorType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(when (type) {
                                    AuthorType.USER -> "用户"
                                    AuthorType.NPC -> "NPC"
                                    AuthorType.IDENTITY -> "身份"
                                })
                            },
                            onClick = {
                                selectedAuthorType = type
                                authorTypeExpanded = false
                            }
                        )
                    }
                }
            }

            // 可见性下拉
            ExposedDropdownMenuBox(
                expanded = visibilityExpanded,
                onExpandedChange = { visibilityExpanded = it }
            ) {
                OutlinedTextField(
                    value = when (selectedVisibility) {
                        ForumVisibility.WORLD -> "世界可见"
                        ForumVisibility.PRIVATE -> "私密"
                        ForumVisibility.ARCHIVED -> "已归档"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("可见性") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(visibilityExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = visibilityExpanded,
                    onDismissRequest = { visibilityExpanded = false }
                ) {
                    ForumVisibility.entries.forEach { vis ->
                        DropdownMenuItem(
                            text = {
                                Text(when (vis) {
                                    ForumVisibility.WORLD -> "世界可见"
                                    ForumVisibility.PRIVATE -> "私密"
                                    ForumVisibility.ARCHIVED -> "已归档"
                                })
                            },
                            onClick = {
                                selectedVisibility = vis
                                visibilityExpanded = false
                            }
                        )
                    }
                }
            }

            // 评论模式下拉
            ExposedDropdownMenuBox(
                expanded = commentingModeExpanded,
                onExpandedChange = { commentingModeExpanded = it }
            ) {
                OutlinedTextField(
                    value = when (selectedCommentingMode) {
                        CommentingMode.ALL -> "所有人可评论"
                        CommentingMode.AI_ONLY -> "仅 AI/NPC"
                        CommentingMode.USER_ONLY -> "仅用户"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("评论模式") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(commentingModeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = commentingModeExpanded,
                    onDismissRequest = { commentingModeExpanded = false }
                ) {
                    CommentingMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(when (mode) {
                                    CommentingMode.ALL -> "所有人可评论"
                                    CommentingMode.AI_ONLY -> "仅 AI/NPC"
                                    CommentingMode.USER_ONLY -> "仅用户"
                                })
                            },
                            onClick = {
                                selectedCommentingMode = mode
                                commentingModeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 发布按钮
            Button(
                onClick = {
                    viewModel.onEvent(
                        ForumEvent.CreatePost(
                            worldId = worldId.trim(),
                            title = title.trim(),
                            content = content.trim(),
                            authorType = selectedAuthorType,
                            visibility = selectedVisibility,
                            commentingMode = selectedCommentingMode
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && content.isNotBlank()
                    && worldId.isNotBlank() && !uiState.isLoading
            ) {
                Text("发布")
            }
        }
    }
}
