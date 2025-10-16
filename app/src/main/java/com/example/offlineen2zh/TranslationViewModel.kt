package com.example.offlineen2zh

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
 * Immutable view data for the translation screen.
 */
data class TranslationUiState(
    val query: String = "",
    val translation: String? = null,
    val lastTranslatedInput: String? = null,
    val isModelDownloading: Boolean = false,
    val isModelDownloaded: Boolean = false,
    val isTranslating: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Coordinates downloading the ML Kit on-device translation model and running lookups.
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
        viewModelScope.launch { downloadModelIfNecessary() }
    }

    fun updateQuery(text: String) {
        _uiState.update { current ->
            current.copy(query = text, errorMessage = null)
        }
    }

    fun clearTranslation() {
        _uiState.update { current ->
            current.copy(translation = null, lastTranslatedInput = null, errorMessage = null)
        }
    }

    fun translate() {
        val textToTranslate = _uiState.value.query.trim()
        if (textToTranslate.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "請先輸入要翻譯的英文內容。") }
            return
        }

        viewModelScope.launch {
            if (!downloadModelIfNecessary()) {
                return@launch
            }

            _uiState.update { it.copy(isTranslating = true, errorMessage = null) }
            try {
                val translated = translator.translate(textToTranslate).await()
                _uiState.update {
                    it.copy(
                        translation = translated,
                        lastTranslatedInput = textToTranslate,
                        isTranslating = false
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isTranslating = false,
                        errorMessage = error.localizedMessage?.let { message ->
                            "翻譯失敗：$message"
                        } ?: "翻譯失敗，請稍後再試。"
                    )
                }
            }
        }
    }

    fun retryModelDownload() {
        viewModelScope.launch { downloadModelIfNecessary(force = true) }
    }

    fun consumeErrorMessage() {
        if (_uiState.value.errorMessage != null) {
            _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private suspend fun downloadModelIfNecessary(force: Boolean = false): Boolean {
        val current = _uiState.value
        if (current.isModelDownloaded && !force) {
            return true
        }

        _uiState.update { it.copy(isModelDownloading = true, errorMessage = null) }
        return try {
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions).await()
            _uiState.update {
                it.copy(
                    isModelDownloading = false,
                    isModelDownloaded = true
                )
            }
            true
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    isModelDownloading = false,
                    isModelDownloaded = false,
                    errorMessage = error.localizedMessage?.let { message ->
                        "模型下載失敗：$message"
                    } ?: "模型下載失敗，請檢查網路連線。"
                )
            }
            false
        }
    }

    override fun onCleared() {
        translator.close()
        super.onCleared()
    }
}
