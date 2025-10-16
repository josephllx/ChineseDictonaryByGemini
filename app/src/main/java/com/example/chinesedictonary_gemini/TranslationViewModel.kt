package com.example.chinesedictonary_gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * UI data shown on the dictionary screen.
 */
data class TranslationUiState(
    val inputText: String = "",
    val translatedText: String = "",
    val isModelDownloading: Boolean = false,
    val isModelReady: Boolean = false,
    val isTranslating: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel responsible for downloading the ML Kit translation model and performing lookups.
 */
class TranslationViewModel : ViewModel() {

    private val translator: Translator = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.CHINESE)
            .build()
    )

    private val _uiState = MutableStateFlow(TranslationUiState())
    val uiState: StateFlow<TranslationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { ensureModelReady() }
    }

    fun updateInputText(newText: String) {
        _uiState.update { current ->
            current.copy(inputText = newText, errorMessage = null)
        }
    }

    fun clearTranslation() {
        _uiState.update { current ->
            current.copy(translatedText = "", errorMessage = null)
        }
    }

    fun translateCurrentText() {
        val textToTranslate = _uiState.value.inputText.trim()
        if (textToTranslate.isEmpty()) {
            _uiState.update { it.copy(translatedText = "", errorMessage = "請先輸入英文單字或片語") }
            return
        }

        viewModelScope.launch {
            if (!ensureModelReady()) {
                return@launch
            }

            _uiState.update { it.copy(isTranslating = true, errorMessage = null) }
            try {
                val translated = translator.translate(textToTranslate).await()
                _uiState.update {
                    it.copy(
                        translatedText = translated,
                        isTranslating = false,
                        errorMessage = null
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isTranslating = false,
                        errorMessage = "翻譯失敗：${error.localizedMessage ?: error.message ?: "未知錯誤"}"
                    )
                }
            }
        }
    }

    private suspend fun ensureModelReady(): Boolean {
        if (_uiState.value.isModelReady) {
            return true
        }

        _uiState.update { it.copy(isModelDownloading = true, errorMessage = null) }
        return try {
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions).await()
            _uiState.update { it.copy(isModelDownloading = false, isModelReady = true) }
            true
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    isModelDownloading = false,
                    errorMessage = "模型下載失敗：${error.localizedMessage ?: error.message ?: "未知錯誤"}"
                )
            }
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        translator.close()
    }
}
