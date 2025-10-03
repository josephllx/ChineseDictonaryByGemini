// 檔案路徑: app/src/main/java/com/example/chinesedictonary_gemini/IdiomDao.kt

package com.example.chinesedictonary_gemini

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao // 告訴 Room 這是我們的「水電工程圖」(Data Access Object)
interface IdiomDao {

    /**
     * 新增一筆成語。
     * OnConflictStrategy.REPLACE 的意思是：如果插入的資料主鍵已經存在，就直接覆蓋掉舊的。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(idiom: Idiom)

    /**
     * 一次性新增整個列表的成語。這在首次建立資料庫時非常有用。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(idioms: List<Idiom>)

    /**
     * 根據使用者輸入的字串來查詢成語。
     * ":query || '%'" 是一個 SQL 語法，意思是「查詢 term 欄位開頭是 :query 的所有資料」。
     * 這完美實現了我們的「即時搜尋」功能。
     */
    @Query("SELECT * FROM idioms WHERE term LIKE :query || '%'")
    suspend fun searchIdioms(query: String): List<Idiom>

    /**
     * 根據 ID 查詢單一一筆成語，用於詳情頁面。
     */
    @Query("SELECT * FROM idioms WHERE id = :id")
    suspend fun getIdiomById(id: Int): Idiom?

    /**
     * 檢查資料庫是否為空。我們可以用這個來判斷是否需要執行首次設定。
     */
    @Query("SELECT COUNT(*) FROM idioms")
    suspend fun count(): Int
}