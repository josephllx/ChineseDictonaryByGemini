package com.example.chinesedictonary_gemini

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// **關鍵修正：在 @Database 註解中加入 @TypeConverters**
@Database(entities = [Idiom::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}