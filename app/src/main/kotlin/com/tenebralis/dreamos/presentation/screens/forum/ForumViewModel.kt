package com.tenebralis.dreamos.presentation.screens.forum

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.repository.ForumRepository
import com.tenebralis.dreamos.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val forumRepository: ForumRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForumUiState())
    val uiState: StateFlow<ForumUiState> = _uiState.asStateFlow()

    private val postId: String? = savedStateHandle[Screen.ForumPostDetail.ARG_POST_ID]

    init {
        if (postId != null) {
            loadPostDetail(postId)
        } else {
            loadPosts()
        }
    }

    fun onEvent(event: ForumEvent) {
        when (event) {
            ForumEvent.RefreshPosts -> loadPosts()
            is ForumEvent.LoadPostDetail -> loadPostDetail(event.postId)
            is ForumEvent.CreatePost -> createPost(event)
            is ForumEvent.CreateComment -> createComment(event)
            ForumEvent.DismissError -> _uiState.update { it.copy(error = null) }
            ForumEvent.DismissSuccess -> _uiState.update { it.copy(successMessage = null) }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            forumRepository.getPosts().collect { result ->
                result.onSuccess { posts ->
                    _uiState.update { it.copy(posts = posts, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "加载帖子失败", isLoading = false)
                    }
                }
            }
        }
    }

    private fun loadPostDetail(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            forumRepository.getPostById(postId)
                .onSuccess { post ->
                    _uiState.update { it.copy(selectedPost = post, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "加载帖子失败", isLoading = false)
                    }
                }

            // 加载评论
            forumRepository.getComments(postId).collect { result ->
                result.onSuccess { comments ->
                    _uiState.update { it.copy(comments = comments) }
                }
            }
        }
    }

    private fun createPost(event: ForumEvent.CreatePost) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            forumRepository.createPost(
                worldId = event.worldId,
                title = event.title,
                content = event.content,
                authorType = event.authorType,
                worldNpcPersonaId = event.worldNpcPersonaId,
                visibility = event.visibility,
                commentingMode = event.commentingMode
            ).onSuccess {
                _uiState.update {
                    it.copy(isLoading = false, successMessage = "帖子已发布")
                }
                loadPosts()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(error = e.message ?: "发帖失败", isLoading = false)
                }
            }
        }
    }

    private fun createComment(event: ForumEvent.CreateComment) {
        viewModelScope.launch {
            forumRepository.createComment(
                postId = event.postId,
                content = event.content,
                authorType = event.authorType,
                worldNpcPersonaId = event.worldNpcPersonaId,
                parentCommentId = event.parentCommentId
            ).onSuccess {
                _uiState.update { it.copy(successMessage = "评论已发布") }
                loadPostDetail(event.postId)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "评论失败") }
            }
        }
    }
}
