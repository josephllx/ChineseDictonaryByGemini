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

    private val _searchResults = MutableLiveData<List<IdiomWithPronunciations>>(emptyList())
    val searchResults: LiveData<List<IdiomWithPronunciations>> = _searchResults

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

    private fun getToneOrder(syllable: String): Int {
        return when {
            syllable.endsWith("ˊ") -> 2
            syllable.endsWith("ˇ") -> 3
            syllable.endsWith("ˋ") -> 4
            syllable.endsWith("˙") -> 5
            else -> 1
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
                            val filteredAndSortedResults = filterAndSortZhuyinResults(roughResults, query)
                            filteredAndSortedResults
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

    private fun filterAndSortZhuyinResults(results: List<IdiomWithPronunciations>, originalQuery: String): List<IdiomWithPronunciations> {
        // **關鍵修正：建立一個正規表示式來匹配所有類型的空白**
        val whitespaceRegex = Regex("\\s+")
        // 將使用者的查詢正規化，替換所有空白為標準半形空白
        val normalizedQuery = originalQuery.trim().replace(whitespaceRegex, " ")

        if (normalizedQuery.isEmpty()) return emptyList()

        val isExactSyllableSearch = originalQuery.endsWith(" ") && normalizedQuery.split(" ").size == 1

        val filtered = results.mapNotNull { idiomWithPronunciations ->
            val matchingPronunciations = idiomWithPronunciations.pronunciations.filter { p ->
                val bopomofoRaw = p.bopomofo?.trim() ?: return@filter false
                // **關鍵修正：同樣將資料庫中的注音字串正規化**
                val normalizedBopomofo = bopomofoRaw.replace(whitespaceRegex, " ")

                if (!normalizedBopomofo.startsWith(normalizedQuery)) return@filter false

                if (isExactSyllableSearch) {
                    val firstSyllable = normalizedBopomofo.split(" ").firstOrNull() ?: ""
                    val hasTone = firstSyllable.any { it in "ˊˇˋ˙" }
                    if (hasTone && firstSyllable.length == normalizedQuery.length + 1) {
                        return@filter false
                    }
                }
                true
            }

            if (matchingPronunciations.isNotEmpty()) {
                idiomWithPronunciations.copy(pronunciations = matchingPronunciations)
            } else {
                null
            }
        }

        val queryParts = normalizedQuery.split(" ")
        return if (queryParts.size == 1) {
            filtered.sortedWith(
                compareBy<IdiomWithPronunciations> { it.idiom.term.length }
                    .thenBy {
                        val p = it.pronunciations.first()
                        val bopomofo = p.bopomofo?.trim()?.replace(whitespaceRegex, " ") ?: ""
                        bopomofo.split(" ").firstOrNull()?.length ?: 99
                    }
                    .thenBy {
                        val p = it.pronunciations.first()
                        val bopomofo = p.bopomofo?.trim()?.replace(whitespaceRegex, " ") ?: ""
                        val syllable = bopomofo.split(" ").firstOrNull()
                        syllable?.let { getToneOrder(it) } ?: 99
                    }
            )
        } else {
            filtered
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _selectedRadical.value = null
    }

    fun getIdiomById(id: Int): LiveData<IdiomWithPronunciations?> {
        val result = MutableLiveData<IdiomWithPronunciations?>()
        viewModelScope.launch(Dispatchers.IO) {
            val idiom = repository.idiomDao.getIdiomById(id)
            withContext(Dispatchers.Main) {
                result.value = idiom
            }
        }
        return result
    }
}