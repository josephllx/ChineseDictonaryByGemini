package com.example.chinesedictonary_gemini

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// **全新的資料模型，用來描述一個完整的定義**
@Serializable
data class DefinitionItem(
    val type: String?,
    val def: String?,
    val example: List<String>?,
    val quote: List<String>?,
    val link: List<String>?
)

// **關鍵修正：為 PronunciationItem 加上 @Serializable 註解**
@Serializable
data class PronunciationItem(
    val bopomofo: String?,
    val pinyin: String?,
    val definitions: List<DefinitionItem>?
)

// **TypeConverter 讓 Room 資料庫知道如何儲存 List<PronunciationItem> 這種複雜類型**
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromPronunciationList(pronunciations: List<PronunciationItem>): String {
        return json.encodeToString(pronunciations)
    }

    @TypeConverter
    fun toPronunciationList(jsonString: String): List<PronunciationItem> {
        return try {
            json.decodeFromString<List<PronunciationItem>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// **更新後的 Idiom Entity**
@Entity(tableName = "idioms")
@TypeConverters(Converters::class)
data class Idiom(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val term: String,

    val pronunciations: List<PronunciationItem>,

    val source: String,

    val radical: String?,
    val strokeCount: Int?,
    val nonRadicalStrokeCount: Int?
)

object Dictionaries {
    val sourceMap = mapOf(
        "revised" to "重編國語辭典"
    )
}
