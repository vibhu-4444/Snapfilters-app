package com.snapfilters.feature.editor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapfilters.core.database.entity.SaveMode
import com.snapfilters.feature.editor.export.ExportResult
import com.snapfilters.feature.editor.export.ImageExporter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

sealed interface EditorUiEvent {
    data class ExportComplete(val success: Boolean, val message: String) : EditorUiEvent
}

data class EditorUiState(
    val selectedFilterId: String? = "meridian",
    val filterIntensity: Float = 1.0f, // 0.0f - 1.0f (FR-FLT-02)
    val isDraggingSlider: Boolean = false,
    val showSaveSheet: Boolean = false,
    val isExporting: Boolean = false,
    val saveMode: SaveMode = SaveMode.FILTERED_ONLY
)

class EditorViewModel(
    private val imageExporter: ImageExporter? = null,
    private val sourceFile: File? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditorUiEvent>()
    val events: SharedFlow<EditorUiEvent> = _events.asSharedFlow()

    fun selectFilter(filterId: String?) {
        _uiState.update { it.copy(selectedFilterId = filterId) }
    }

    fun setFilterIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0.0f, 1.0f)
        _uiState.update { it.copy(filterIntensity = clamped) }
    }

    fun setDraggingSlider(isDragging: Boolean) {
        _uiState.update { it.copy(isDraggingSlider = isDragging) }
    }

    fun openSaveSheet() {
        _uiState.update { it.copy(showSaveSheet = true) }
    }

    fun dismissSaveSheet() {
        _uiState.update { it.copy(showSaveSheet = false) }
    }

    fun setSaveMode(saveMode: SaveMode) {
        _uiState.update { it.copy(saveMode = saveMode) }
    }

    fun confirmSave() {
        val file = sourceFile ?: return
        val currentState = _uiState.value

        _uiState.update { it.copy(isExporting = true, showSaveSheet = false) }

        viewModelScope.launch {
            val result = imageExporter?.exportPhoto(
                sourceFile = file,
                filterId = currentState.selectedFilterId,
                intensity = currentState.filterIntensity,
                saveMode = currentState.saveMode
            )

            _uiState.update { it.copy(isExporting = false) }

            when (result) {
                is ExportResult.Success -> {
                    _events.emit(EditorUiEvent.ExportComplete(true, "Photograph saved successfully."))
                }
                is ExportResult.Error -> {
                    _events.emit(EditorUiEvent.ExportComplete(false, result.message))
                }
                null -> {
                    _events.emit(EditorUiEvent.ExportComplete(true, "Photograph saved."))
                }
            }
        }
    }
}
