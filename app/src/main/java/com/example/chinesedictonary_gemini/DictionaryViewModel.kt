package com.example.chinesedictonary_gemini

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class AppState {
    data class SettingUp(val progress: Float, val message: String) : AppState()
    object Ready : AppState()
}

// **關鍵修正：這個 ViewModel 現在依賴 Repository**
class DictionaryViewModel(private val repository: DictionaryRepository) : ViewModel() {

    private val _appState = MutableLiveData<AppState>()
    val appState: LiveData<AppState> = _appState

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _searchResults = MutableLiveData<List<Idiom>>(emptyList())
    val searchResults: LiveData<List<Idiom>> = _searchResults

    init {
        viewModelScope.launch {
            // **將所有繁重的工作都委託給 Repository**
            repository.setupDatabaseIfNeeded { progress, message ->
                // Repository 會透過這個 callback 來更新 UI 進度
                _appState.postValue(AppState.SettingUp(progress, message))
            }
            _appState.postValue(AppState.Ready)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch(Dispatchers.IO) {
            val results = if (query.isBlank()) {
                emptyList()
            } else {
                // **搜尋工作也是透過 Repository 間接操作資料庫**
                repository.idiomDao.searchIdioms(query)
            }
            withContext(Dispatchers.Main) {
                _searchResults.value = results
            }
        }
    }

    fun getIdiomById(id: Int): LiveData<Idiom?> {
        val result = MutableLiveData<Idiom?>()
        viewModelScope.launch(Dispatchers.IO) {
            // **取得單筆資料也是透過 Repository**
            val idiom = repository.idiomDao.getIdiomById(id)
            withContext(Dispatchers.Main) {
                result.value = idiom
            }
        }
        return result
    }
}