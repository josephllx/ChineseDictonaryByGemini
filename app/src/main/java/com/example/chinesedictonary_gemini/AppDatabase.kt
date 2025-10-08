package com.example.chinesedictonary_gemini

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// **關鍵修正：版本號 +1，並加入新的 Pronunciation Entity**
@Database(entities = [Idiom::class, Pronunciation::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun idiomDao(): IdiomDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "idiom_database"
                )
                    // **重要：在版本升級時，加入 fallbackToDestructiveMigration()**
                    // 這會讓 App 在更新時自動刪除舊的資料庫並重建，避免崩潰。
                    // 對於使用者來說，就是重新下載一次辭典資料。
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}