package com.example.chinesedictonary_gemini

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity 告訴 Room 這是一個資料庫的資料表
// @PrimaryKey 告訴 Room 'id' 是唯一的主鍵
@Entity(tableName = "idioms")
data class Idiom(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // 主鍵最好由資料庫自動產生
    val term: String,
    val zhuyin: String,
    val source: String,
    val definition: String,
    // **新增欄位，儲存部首與筆畫資訊**
    val radical: String?,
    val strokeCount: Int?,
    val nonRadicalStrokeCount: Int?
)

// 定義辭典來源的資料
object Dictionaries {
    val sourceMap = mapOf(
        //"idioms" to "成語典",
        "revised" to "重編本",
        //"concise" to "簡編本",
        //"mini" to "小字典"
    )
}

// 模擬的辭典資料庫
// 在真實的 App 中，這些資料會來自於解壓縮後的檔案並存入資料庫 (如 Room)
/*
val mockIdiomList = listOf(
    Idiom(1, "一元復始", "ㄧ ㄩㄢˊ ㄈㄨˋ ㄕˇ", "idioms", "指新的一年開始。"),
    Idiom(2, "一丁不識", "ㄧ ㄉㄧㄥ ㄅㄨˋ ㄕˊ", "idioms", "形容不識字或毫無學問。"),
    Idiom(3, "一刀兩斷", "ㄧ ㄉㄠ ㄌㄧㄤˇ ㄉㄨㄢˋ", "idioms", "比喻乾脆地斷絕關係。"),
    Idiom(4, "一五一十", "ㄧ ㄨˇ ㄧ ㄕˊ", "idioms", "比喻把事情從頭到尾詳細說出來，沒有一絲隱瞞。"),
    Idiom(5, "一日三秋", "ㄧ ㄖˋ ㄙㄢ ㄑㄧㄡ", "idioms", "比喻思慕心切，一天不見，如隔三年。"),
    Idiom(6, "一木難支", "ㄧ ㄇㄨˋ ㄋㄢˊ ㄓ", "revised", "比喻事情非常重大，非一人之力所能支持。"),
    Idiom(7, "一毛不拔", "ㄧ ㄇㄠˊ ㄅㄨˋ ㄅㄚˊ", "revised", "比喻人非常吝嗇自私。"),
    Idiom(8, "一氣呵成", "ㄧ ㄑㄧˋ ㄏㄜ ㄔㄥˊ", "revised", "比喻文章或繪畫的氣勢流暢，首尾貫串。也比喻事情的進行順利，一口氣完成。"),
    Idiom(9, "一清二楚", "ㄧ ㄑㄧㄥ ㄦˋ ㄔㄨˇ", "concise", "十分清楚、明白。"),
    Idiom(10, "一盤散沙", "ㄧ ㄆㄢˊ ㄙㄢˇ ㄕㄚ", "concise", "比喻人心渙散，缺乏凝合的力量，不能團結起來。"),
    Idiom(11, "一諾千金", "ㄧ ㄋㄨㄛˋ ㄑㄧㄢ ㄐㄧㄣ", "concise", "形容信守承諾，說話算數。"),
    Idiom(12, "三心二意", "ㄙㄢ ㄒㄧㄣ ㄦˋ ㄧˋ", "idioms", "形容人意志不堅定，常常改變主意。"),
    Idiom(13, "人山人海", "ㄖㄣˊ ㄕㄢ ㄖㄣˊ ㄏㄞˇ", "mini", "形容許許多多的人聚集在一起。"),
    Idiom(14, "水滴石穿", "ㄕㄨㄟˇ ㄉㄧ ㄕˊ ㄔㄨㄢ", "mini", "比喻只要有恆心，不斷努力，事情一定會成功。")
)
*/
