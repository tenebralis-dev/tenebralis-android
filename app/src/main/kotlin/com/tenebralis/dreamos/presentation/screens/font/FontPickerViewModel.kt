package com.tenebralis.dreamos.presentation.screens.font

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.data.repository.FontRepositoryImpl
import com.tenebralis.dreamos.domain.model.FontItem
import com.tenebralis.dreamos.domain.model.enums.FontSource
import com.tenebralis.dreamos.domain.repository.FontRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FontPickerViewModel @Inject constructor(
    private val fontRepository: FontRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FontPickerUiState())
    val uiState: StateFlow<FontPickerUiState> = _uiState.asStateFlow()

    init {
        loadFonts()
        observeSelectedFont()
    }

    fun onEvent(event: FontPickerEvent) {
        when (event) {
            FontPickerEvent.Refresh -> loadFonts()
            is FontPickerEvent.SwitchTab -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
                loadFonts()
                observeSelectedFont()
            }
            is FontPickerEvent.DownloadFont -> downloadFont(event.item)
            is FontPickerEvent.SelectFont -> selectFont(event.fontId)
            is FontPickerEvent.ShowDeleteDialog -> {
                _uiState.update { it.copy(showDeleteDialog = event.item) }
            }
            FontPickerEvent.DismissDeleteDialog -> {
                _uiState.update { it.copy(showDeleteDialog = null) }
            }
            is FontPickerEvent.ConfirmDelete -> deleteFont(event.item)
            is FontPickerEvent.ImportLocalFont -> importLocalFont(event.uri, event.displayName)
            FontPickerEvent.ShowUrlImportDialog -> {
                _uiState.update { it.copy(showUrlImportDialog = true) }
            }
            FontPickerEvent.DismissUrlImportDialog -> {
                _uiState.update { it.copy(showUrlImportDialog = false) }
            }
            is FontPickerEvent.ConfirmUrlImport -> importUrlFont(event.url, event.displayName)
            FontPickerEvent.DismissError -> _uiState.update { it.copy(error = null) }
            FontPickerEvent.DismissSuccess -> _uiState.update { it.copy(successMessage = null) }
        }
    }

    private fun loadFonts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            fontRepository.getAllFonts(_uiState.value.selectedTab.category).collectLatest { result ->
                result
                    .onSuccess { fonts ->
                        _uiState.update { it.copy(fonts = fonts, isLoading = false) }
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                error = e.message ?: "加载字体列表失败",
                                isLoading = false,
                                // 仍然显示内置字体
                                fonts = fontRepository.getBuiltInFonts()
                                    .filter { f -> f.category == _uiState.value.selectedTab.category }
                            )
                        }
                    }
            }
        }
    }

    private fun observeSelectedFont() {
        viewModelScope.launch {
            fontRepository.getSelectedFontId(_uiState.value.selectedTab.category)
                .collectLatest { fontId ->
                    _uiState.update {
                        it.copy(selectedFontId = fontId ?: FontRepositoryImpl.BUILT_IN_FONT_ID)
                    }
                }
        }
    }

    private fun downloadFont(item: FontItem) {
        viewModelScope.launch {
            fontRepository.downloadFont(item).collectLatest { progress ->
                _uiState.update {
                    val downloading = it.downloadingFonts.toMutableMap()
                    if (progress.isComplete) {
                        downloading.remove(item.id)
                    } else {
                        downloading[item.id] = progress.fraction
                    }
                    it.copy(downloadingFonts = downloading)
                }
                if (progress.isComplete) {
                    _uiState.update { it.copy(successMessage = "${item.displayName} 下载完成") }
                    loadFonts() // 刷新列表以更新 isDownloaded 状态
                }
            }
        }
    }

    private fun selectFont(fontId: String) {
        viewModelScope.launch {
            fontRepository.setSelectedFontId(_uiState.value.selectedTab.category, fontId)
        }
    }

    private fun deleteFont(item: FontItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = null) }
            fontRepository.deleteFont(item.id, item.fileName)
                .onSuccess {
                    // 若删除的是当前选中字体，回退到内置
                    if (_uiState.value.selectedFontId == item.id) {
                        fontRepository.setSelectedFontId(
                            _uiState.value.selectedTab.category,
                            FontRepositoryImpl.BUILT_IN_FONT_ID
                        )
                    }
                    _uiState.update { it.copy(successMessage = "${item.displayName} 已删除") }
                    loadFonts()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "删除失败") }
                }
        }
    }

    private fun importLocalFont(uri: Uri, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            fontRepository.importLocalFont(uri, displayName, _uiState.value.selectedTab.category)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "导入成功")
                    }
                    loadFonts()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "导入失败")
                    }
                }
        }
    }

    private fun importUrlFont(url: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(showUrlImportDialog = false) }
            fontRepository.importUrlFont(url, displayName, _uiState.value.selectedTab.category)
                .collectLatest { progress ->
                    if (progress.isComplete) {
                        _uiState.update { it.copy(successMessage = "导入成功") }
                        loadFonts()
                    }
                }
        }
    }
}
