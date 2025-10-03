package com.example.chinesedictonary_gemini

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// @Database 註解告訴 Room 這是資料庫的總設定檔
// entities = [...] 陣列裡要列出所有歸這個資料庫管轄的 @Entity class
// version 是資料庫版本號，未來如果修改資料表結構，需要升級這個版本號
@Database(entities = [Idiom::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // 這個抽象函式讓 Room 知道如何取得 DAO
    abstract fun idiomDao(): IdiomDao

    // companion object 類似 Java 的 static
    // 我們用它來建立一個全域唯一的資料庫實例 (Singleton 模式)
    companion object {
        // @Volatile 確保多個執行緒都能正確讀取到 instance 的最新值
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // 如果 INSTANCE 不是 null，就直接回傳
            // 否則，就建立一個新的資料庫實例
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "idiom_database" // 資料庫檔案的名稱
                ).build()
                INSTANCE = instance
                // 回傳 instance
                instance
            }
        }
    }
}