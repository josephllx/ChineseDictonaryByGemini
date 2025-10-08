package com.example.chinesedictonary_gemini

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IdiomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(idioms: List<Idiom>)

    @Query("SELECT * FROM idioms WHERE term LIKE :query || '%'")
    suspend fun searchByTerm(query: String): List<Idiom>

    @Query("SELECT * FROM idioms WHERE radical = :radical")
    suspend fun searchByRadical(radical: String): List<Idiom>

    // **關鍵修正：查詢更精準，只找第一個字是以該聲母開頭的詞條**
    @Query("SELECT * FROM idioms WHERE pronunciations LIKE '%\"bopomofo\":\"' || :firstChar || '%'")
    suspend fun searchByZhuyinInitial(firstChar: String): List<Idiom>

    @Query("SELECT DISTINCT radical FROM idioms WHERE radical IS NOT NULL AND radical != '' ORDER BY radical")
    suspend fun getDistinctRadicals(): List<String>

    @Query("SELECT * FROM idioms WHERE id = :id")
    suspend fun getIdiomById(id: Int): Idiom?

    @Query("SELECT COUNT(*) FROM idioms")
    suspend fun count(): Int
}

