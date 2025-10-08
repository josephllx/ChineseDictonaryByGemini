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

class DictionaryViewModel(private val repository: DictionaryRepository) : ViewModel() {

    private val _appState = MutableLiveData<AppState>()
    val appState: LiveData<AppState> = _appState

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _searchResults = MutableLiveData<List<Idiom>>(emptyList())
    val searchResults: LiveData<List<Idiom>> = _searchResults

    private val _radicals = MutableLiveData<List<String>>()
    val radicals: LiveData<List<String>> = _radicals

    private val _selectedRadical = MutableLiveData<String?>(null)
    val selectedRadical: LiveData<String?> = _selectedRadical

    private val _selectedSearchType = MutableLiveData("詞彙")
    val selectedSearchType: LiveData<String> = _selectedSearchType

    init {
        viewModelScope.launch {
            repository.setupDatabaseIfNeeded { progress, message ->
                _appState.postValue(AppState.SettingUp(progress, message))
            }
            loadRadicals()
            _appState.postValue(AppState.Ready)
        }
    }

    private fun loadRadicals() {
        viewModelScope.launch(Dispatchers.IO) {
            _radicals.postValue(repository.idiomDao.getDistinctRadicals())
        }
    }

    fun changeSearchType(newType: String) {
        _selectedSearchType.value = newType
        clearSearch()
    }

    // 輔助函式：根據注音符號獲取聲調順序
    private fun getToneOrder(syllable: String): Int {
        return when {
            syllable.endsWith("ˊ") -> 2 // 二聲
            syllable.endsWith("ˇ") -> 3 // 三聲
            syllable.endsWith("ˋ") -> 4 // 四聲
            syllable.endsWith("˙") -> 5 // 輕聲
            else -> 1 // 一聲 (沒有符號)
        }
    }

    fun performSearch(query: String) {
        val type = _selectedSearchType.value ?: "詞彙"
        _searchQuery.value = query

        viewModelScope.launch(Dispatchers.IO) {
            if (type == "部首") {
                _selectedRadical.postValue(query)
            }

            val results = if (query.isBlank()) {
                emptyList()
            } else {
                when (type) {
                    "詞彙" -> repository.idiomDao.searchByTerm(query)
                    "部首" -> repository.idiomDao.searchByRadical(query)
                    "注音" -> {
                        val trimmedQuery = query.trim()
                        val firstChar = trimmedQuery.firstOrNull()?.toString() ?: ""

                        if (firstChar.isBlank()) {
                            emptyList()
                        } else {
                            val roughResults = repository.idiomDao.searchByZhuyinInitial(firstChar)
                            val filteredResults = filterResultsByFullZhuyin(roughResults, query)

                            val queryParts = trimmedQuery.split(" ").filter { it.isNotEmpty() }
                            if (queryParts.size == 1) {
                                // **最終版三層排序邏輯**
                                filteredResults.sortedWith(
                                    compareBy<Idiom> { it.term.length } // 1. 按詞長排序
                                        .thenBy { idiom -> // 2. 按第一個音節的長度排序
                                            val p = idiom.pronunciations.firstOrNull { p -> p.bopomofo?.trim()?.startsWith(trimmedQuery) == true }
                                            p?.bopomofo?.trim()?.split(" ")?.firstOrNull()?.length ?: 99
                                        }
                                        .thenBy { idiom -> // 3. 按聲調排序
                                            val p = idiom.pronunciations.firstOrNull { p -> p.bopomofo?.trim()?.startsWith(trimmedQuery) == true }
                                            val syllable = p?.bopomofo?.trim()?.split(" ")?.firstOrNull()
                                            syllable?.let { getToneOrder(it) } ?: 99
                                        }
                                )
                            } else {
                                filteredResults
                            }
                        }
                    }
                    else -> emptyList()
                }
            }
            withContext(Dispatchers.Main) {
                _searchResults.value = results
            }
        }
    }

    private fun filterResultsByFullZhuyin(results: List<Idiom>, originalQuery: String): List<Idiom> {
        val trimmedQuery = originalQuery.trim()
        if (trimmedQuery.isEmpty()) return emptyList()

        val isExactSyllableSearch = originalQuery.endsWith(" ") && trimmedQuery.split(" ").filter { it.isNotEmpty() }.size == 1

        return results.filter { idiom ->
            idiom.pronunciations.any { pronunciation ->
                val bopomofo = pronunciation.bopomofo?.trim() ?: return@any false

                if (!bopomofo.startsWith(trimmedQuery)) {
                    return@any false
                }

                if (isExactSyllableSearch) {
                    val firstSyllable = bopomofo.split(" ").firstOrNull() ?: ""
                    val hasTone = firstSyllable.any { it in "ˊˇˋ˙" }

                    if (hasTone && firstSyllable.length == trimmedQuery.length + 1) {
                        return@any false
                    }
                }
                true
            }
        }
    }


    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _selectedRadical.value = null
    }

    fun getIdiomById(id: Int): LiveData<Idiom?> {
        val result = MutableLiveData<Idiom?>()
        viewModelScope.launch(Dispatchers.IO) {
            val idiom = repository.idiomDao.getIdiomById(id)
            withContext(Dispatchers.Main) {
                result.value = idiom
            }
        }
        return result
    }
}