package com.example.chinesedictonary_gemini

import android.util.JsonReader
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class DictionarySource(val key: String, val url: String, val name: String)

class DictionaryRepository(val idiomDao: IdiomDao) {

    private val TAG = "RepositoryDebug"

    // **最終版：整合所有穩定、有效的 g0v JSON 連結**
    private val dictionarySources = listOf(
        //DictionarySource("idioms", "https://raw.githubusercontent.com/g0v/moedict-data-tw/master/idiom.json", "成語典"),
        //DictionarySource("concise", "https://raw.githubusercontent.com/g0v/moedict-data-tw/master/concised.json", "簡編本"),
        DictionarySource("revised", "https://raw.githubusercontent.com/g0v/moedict-data/master/dict-revised.json", "重編國語辭典")
    )

    suspend fun setupDatabaseIfNeeded(
        updateProgress: (Float, String) -> Unit
    ) {
        if (idiomDao.count() == 0) {
            Log.d(TAG, "資料庫為空，開始執行首次設定。")
            try {
                val allItems = mutableListOf<Idiom>()
                val totalSteps = dictionarySources.size.toFloat()

                for ((index, source) in dictionarySources.withIndex()) {
                    val progress = (index.toFloat()) / totalSteps
                    Log.d(TAG, "準備下載: ${source.url}")
                    updateProgress(progress, "正在下載 ${source.name}...")

                    val items = downloadAndParse(source.url, source.key)
                    Log.i(TAG, "************ ${source.name} 解析完成, 找到 ${items.size} 筆資料 ************")

                    if (items.isNotEmpty()) {
                        allItems.addAll(items)
                    } else {
                        Log.w(TAG, "警告：從 ${source.name} 解析到的資料數量為 0。")
                    }
                }

                if (allItems.isNotEmpty()) {
                    updateProgress(0.9f, "正在將 ${allItems.size} 筆總資料寫入資料庫 (這一步會很久)...")
                    // 為避免單次交易過大，我們分批插入
                    allItems.chunked(5000).forEach { chunk ->
                        idiomDao.insertAll(chunk)
                        Log.d(TAG, "已插入 ${chunk.size} 筆資料...")
                    }
                    Log.d(TAG, "所有資料寫入完成。")
                } else {
                    Log.e(TAG, "！！！！！！！！！！！！！！！！！！！！！！！！")
                    Log.e(TAG, "警告：所有辭典都沒有解析到任何資料。")
                    Log.e(TAG, "！！！！！！！！！！！！！！！！！！！！！！！！")
                }

                updateProgress(1.0f, "建立索引完成！")
                kotlinx.coroutines.delay(500)

            } catch (e: Exception) {
                Log.e(TAG, "設定資料庫時發生嚴重錯誤", e)
                updateProgress(1.0f, "錯誤: ${e.message}")
                kotlinx.coroutines.delay(3000)
            }
        } else {
            Log.d(TAG, "資料庫已存在 (${idiomDao.count()} 筆資料)，跳過首次設定。")
        }
    }

    private suspend fun downloadAndParse(urlString: String, source: String): List<Idiom> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "downloadAndParse: 開始執行 $urlString")
                val url = URL(urlString)
                val connection = url.openConnection() as HttpsURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connectTimeout = 60000 // 60 秒
                connection.readTimeout = 600000 // 10 分鐘
                connection.connect()

                if (connection.responseCode != HttpsURLConnection.HTTP_OK) {
                    Log.e(TAG, "downloadAndParse: 連線失敗 (回應碼: ${connection.responseCode}) for $urlString")
                    return@withContext emptyList<Idiom>()
                }

                Log.d(TAG, "downloadAndParse: 連線成功，準備讀取 JSON 串流。")

                val idioms = connection.inputStream.use { inputStream ->
                    parseJsonStream(inputStream, source)
                }

                Log.d(TAG, "downloadAndParse: JSON 讀取與解析完畢，總共找到 ${idioms.size} 筆資料。")
                idioms
            } catch (e: Exception) {
                Log.e(TAG, "downloadAndParse: 下載或解析時失敗 for $urlString", e)
                emptyList<Idiom>()
            }
        }
    }

    private fun parseJsonStream(inputStream: InputStream, source: String): List<Idiom> {
        val reader = JsonReader(inputStream.bufferedReader())
        val idioms = mutableListOf<Idiom>()
        var count = 0

        try {
            reader.beginArray()
            while (reader.hasNext()) {
                readIdiomObject(reader, source)?.let { idioms.add(it) }
                count++
                if (count > 0 && count % 5000 == 0) {
                    Log.d(TAG, "($source) 已解析 $count 筆資料...")
                }
            }
            reader.endArray()
        } catch (e: Exception) {
            Log.e(TAG, "($source) 串流解析時發生錯誤", e)
        } finally {
            reader.close()
        }

        Log.i(TAG, "($source) 串流解析完成，總共 ${idioms.size} 筆。")
        return idioms
    }

    private fun readIdiomObject(reader: JsonReader, source: String): Idiom? {
        var title = ""
        val pronunciations = mutableListOf<PronunciationItem>()
        var radical: String? = null
        var strokeCount: Int? = null
        var nonRadicalStrokeCount: Int? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "title" -> title = reader.nextString()
                "radical" -> radical = reader.nextString()
                "stroke_count" -> strokeCount = reader.nextInt()
                "non_radical_stroke_count" -> nonRadicalStrokeCount = reader.nextInt()
                "heteronyms" -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        readHeteronymObject(reader)?.let { pronunciations.add(it) }
                    }
                    reader.endArray()
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (title.startsWith("{[") && title.endsWith("]}")) {
            return null
        }

        return if (title.isNotBlank() && pronunciations.any { it.definitions?.isNotEmpty() == true }) {
            Idiom(
                term = title,
                pronunciations = pronunciations,
                source = source,
                radical = radical,
                strokeCount = strokeCount,
                nonRadicalStrokeCount = nonRadicalStrokeCount
            )
        } else {
            if (title.isNotBlank()) {
                Log.w(TAG, "詞條 '$title' 找不到任何有效的定義，已跳過。")
            }
            null
        }
    }

    private fun readHeteronymObject(reader: JsonReader): PronunciationItem? {
        var bopomofo: String? = null
        var pinyin: String? = null
        val definitions = mutableListOf<DefinitionItem>()

        try {
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "bopomofo" -> bopomofo = reader.nextString()
                    "pinyin" -> pinyin = reader.nextString()
                    "definitions" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            readDefinitionObject(reader)?.let { definitions.add(it) }
                        }
                        reader.endArray()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        } catch (e: Exception) {
            Log.e(TAG, "readHeteronymObject: 解析異讀音物件失敗", e)
            return null
        }

        return if (definitions.isNotEmpty()) {
            PronunciationItem(bopomofo, pinyin, definitions)
        } else {
            null
        }
    }

    private fun readDefinitionObject(reader: JsonReader): DefinitionItem? {
        var type: String? = null
        var def: String? = null
        val examples = mutableListOf<String>()
        val quotes = mutableListOf<String>()
        val links = mutableListOf<String>()

        try {
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "type" -> type = reader.nextString()
                    "def" -> def = reader.nextString()
                    "example" -> {
                        reader.beginArray()
                        while(reader.hasNext()) examples.add(reader.nextString())
                        reader.endArray()
                    }
                    "quote" -> {
                        reader.beginArray()
                        while(reader.hasNext()) quotes.add(reader.nextString())
                        reader.endArray()
                    }
                    "link" -> {
                        reader.beginArray()
                        while(reader.hasNext()) links.add(reader.nextString())
                        reader.endArray()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        } catch (e: Exception) {
            Log.e(TAG, "readDefinitionObject: 解析定義物件失敗", e)
            return null
        }

        val finalDef = when {
            !def.isNullOrBlank() -> def
            quotes.isNotEmpty() -> quotes.joinToString("\n")
            links.isNotEmpty() -> links.joinToString("\n")
            else -> null
        }

        return if (finalDef != null) {
            DefinitionItem(type, finalDef, examples.ifEmpty { null }, quotes.ifEmpty { null }, links.ifEmpty { null })
        } else {
            null
        }
    }
}
