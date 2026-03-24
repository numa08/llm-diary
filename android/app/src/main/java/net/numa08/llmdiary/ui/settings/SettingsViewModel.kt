package net.numa08.llmdiary.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.numa08.llmdiary.data.local.ModelPreferences
import net.numa08.llmdiary.llm.LlmEngine
import net.numa08.llmdiary.worker.DiaryWorkScheduler
import javax.inject.Inject

data class SettingsUiState(
    val modelPath: String? = null,
    val isModelLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val modelPreferences: ModelPreferences,
    private val llmEngine: LlmEngine,
    private val diaryWorkScheduler: DiaryWorkScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val modelPath: StateFlow<String?> = modelPreferences.modelPath
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            modelPreferences.modelPath.collect { path ->
                _uiState.value = _uiState.value.copy(
                    modelPath = path,
                    isModelLoaded = llmEngine.isModelLoaded(),
                )
            }
        }
    }

    fun onModelFileSelected(uri: Uri, resolvedPath: String) {
        viewModelScope.launch {
            modelPreferences.setModelPath(resolvedPath)
            _uiState.value = _uiState.value.copy(modelPath = resolvedPath)
        }
    }

    fun loadModel() {
        val path = _uiState.value.modelPath ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                llmEngine.loadModel(path)
                _uiState.value = _uiState.value.copy(
                    isModelLoaded = true,
                    isLoading = false,
                )
                // Re-schedule work with the new model path
                diaryWorkScheduler.scheduleDailyDiaryGeneration(path)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "モデルの読み込みに失敗しました",
                )
            }
        }
    }

    fun unloadModel() {
        viewModelScope.launch {
            try {
                llmEngine.unloadModel()
                _uiState.value = _uiState.value.copy(isModelLoaded = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "モデルの解放に失敗しました",
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
