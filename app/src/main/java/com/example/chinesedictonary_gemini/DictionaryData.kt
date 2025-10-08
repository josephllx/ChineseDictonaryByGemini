package com.example.chinesedictonary_gemini

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
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

// **修正：補回在解析 JSON 時需要用到的臨時資料類別**
@Serializable
data class PronunciationItem(
    val bopomofo: String?,
    val pinyin: String?,
    val definitions: List<DefinitionItem>?
)


// **TypeConverter 讓 Room 資料庫知道如何儲存 List<DefinitionItem> 這種複雜類型**
class DefinitionListConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromDefinitionList(definitions: List<DefinitionItem>?): String? {
        return definitions?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toDefinitionList(jsonString: String?): List<DefinitionItem>? {
        return jsonString?.let {
            try {
                json.decodeFromString<List<DefinitionItem>>(it)
            } catch (e: Exception) {
                null
            }
        }
    }
}

// **全新：獨立的 Pronunciation Entity，帶有關聯和索引**
@Entity(
    tableName = "pronunciations",
    foreignKeys = [
        ForeignKey(
            entity = Idiom::class,
            parentColumns = ["id"],
            childColumns = ["idiomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["idiomId"]),
        // **關鍵：為 bopomofo 欄位建立索引以加速查詢**
        androidx.room.Index(value = ["bopomofo"])
    ]
)
@TypeConverters(DefinitionListConverter::class)
data class Pronunciation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val idiomId: Int,
    val bopomofo: String?,
    val pinyin: String?,
    val definitions: List<DefinitionItem>?
)


// **更新後的 Idiom Entity (移除了 pronunciations 列表)**
@Entity(tableName = "idioms")
data class Idiom(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val term: String,
    val source: String,
    val radical: String?,
    @ColumnInfo(name = "stroke_count")
    val strokeCount: Int?,
    @ColumnInfo(name = "non_radical_stroke_count")
    val nonRadicalStrokeCount: Int?
)

// **新增：用於查詢的關聯資料類，將 Idiom 和其所有的 Pronunciation 묶在一起**
data class IdiomWithPronunciations(
    @Embedded val idiom: Idiom,
    @Relation(
        parentColumn = "id",
        entityColumn = "idiomId"
    )
    val pronunciations: List<Pronunciation>
)


// (Dictionaries object 保持不變)
object Dictionaries {
    val sourceMap = mapOf(
        "revised" to "重編國語辭典"
    )
}