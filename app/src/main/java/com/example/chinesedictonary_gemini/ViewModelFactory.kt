package com.example.chinesedictonary_gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// **關鍵修正！**
// 這裡的建構子參數從 IdiomDao 改為 DictionaryRepository
class DictionaryViewModelFactory(private val repository: DictionaryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 檢查請求的 ViewModel 是否是 DictionaryViewModel
        if (modelClass.isAssignableFrom(DictionaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // **關鍵修正！**
            // 建立 ViewModel 時，傳遞我們收到的 repository
            return DictionaryViewModel(repository) as T
        }
        // 如果是未知的 ViewModel 類型，就拋出錯誤
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
