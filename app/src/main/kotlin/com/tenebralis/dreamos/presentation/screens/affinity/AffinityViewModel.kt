package com.tenebralis.dreamos.presentation.screens.affinity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.repository.RelationshipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AffinityViewModel @Inject constructor(
    private val relationshipRepository: RelationshipRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** 从导航参数获取 worldId（可选，为空时加载所有） */
    private val worldId: String? = savedStateHandle["worldId"]

    private val _uiState = MutableStateFlow(AffinityUiState())
    val uiState: StateFlow<AffinityUiState> = _uiState.asStateFlow()

    init {
        if (worldId != null) loadRelationships()
    }

    fun onEvent(event: AffinityEvent) {
        when (event) {
            AffinityEvent.Refresh -> loadRelationships()
            AffinityEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadRelationships() {
        val wId = worldId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            relationshipRepository.getRelationships(wId).collect { result ->
                result.onSuccess { list ->
                    _uiState.update { it.copy(relationships = list, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "加载好感度失败", isLoading = false)
                    }
                }
            }
        }
    }
}
