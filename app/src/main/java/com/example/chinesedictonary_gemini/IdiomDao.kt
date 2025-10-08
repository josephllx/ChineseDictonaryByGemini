package com.example.chinesedictonary_gemini

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface IdiomDao {

    // **新增：一次性插入多個 Idiom 並返回它們的 ID 列表**
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdioms(idioms: List<Idiom>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPronunciations(pronunciations: List<Pronunciation>)

    // 詞彙查詢 (保持不變)
    @Transaction
    @Query("SELECT * FROM idioms WHERE term LIKE :query || '%'")
    suspend fun searchByTerm(query: String): List<IdiomWithPronunciations>

    // 部首查詢 (更新返回類型)
    @Transaction
    @Query("SELECT * FROM idioms WHERE radical = :radical")
    suspend fun searchByRadical(radical: String): List<IdiomWithPronunciations>

    // 全新、高效的注音查詢 (保持不變)
    @Transaction
    @Query("""
        SELECT * FROM idioms 
        WHERE id IN (
            SELECT DISTINCT idiomId FROM pronunciations WHERE bopomofo LIKE :firstChar || '%'
        )
    """)
    suspend fun searchByZhuyinInitial(firstChar: String): List<IdiomWithPronunciations>

    // (getDistinctRadicals 保持不變)
    @Query("SELECT DISTINCT radical FROM idioms WHERE radical IS NOT NULL AND radical != '' ORDER BY radical")
    suspend fun getDistinctRadicals(): List<String>

    // ID 查詢 (更新返回類型)
    @Transaction
    @Query("SELECT * FROM idioms WHERE id = :id")
    suspend fun getIdiomById(id: Int): IdiomWithPronunciations?

    // (count 保持不變)
    @Query("SELECT COUNT(*) FROM idioms")
    suspend fun count(): Int
}