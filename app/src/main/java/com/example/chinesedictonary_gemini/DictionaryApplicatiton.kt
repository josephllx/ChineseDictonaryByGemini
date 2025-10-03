package com.example.chinesedictonary_gemini
import android.app.Application

class DictionaryApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    // 新增 Repository 的實例
    val repository: DictionaryRepository by lazy { DictionaryRepository(database.idiomDao()) }
}