package com.example.chinesedictonary_gemini

import android.util.JsonReader
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class DictionarySource(val key: String, val url: String, val name: String, val approxSize: Int, val weight: Float)

class DictionaryRepository(val idiomDao: IdiomDao) {

    private val TAG = "RepositoryDebug"

    // **關鍵更新：為每個辭典加入預估大小和進度權重**
    private val dictionarySources = listOf(
        DictionarySource("idioms", "https://raw.githubusercontent.com/g0v/moedict-data-tw/master/idiom.json", "成語典", 5000, 0.1f),
        DictionarySource("concise", "https://raw.githubusercontent.com/g0v/moedict-data-tw/master/concised.json", "簡編本", 40000, 0.2f),
        DictionarySource("revised", "https://raw.githubusercontent.com/g0v/moedict-data/master/dict-revised.json", "重編國語辭典", 163000, 0.6f)
    )

    suspend fun setupDatabaseIfNeeded(
        updateProgress: (Float, String) -> Unit
    ) {
        if (idiomDao.count() == 0) {
            Log.d(TAG, "資料庫為空，開始執行首次設定。")
            try {
                val allItems = mutableListOf<Idiom>()
                var overallProgress = 0f

                for (source in dictionarySources) {
                    val currentStepStartProgress = overallProgress
                    Log.d(TAG, "準備下載: ${source.url}")
                    updateProgress(currentStepStartProgress, "正在下載 ${source.name}...")

                    val items = downloadAndParse(source.url, source.key) { parseProgress ->
                        // **關鍵更新：根據解析進度，計算在總進度中的位置**
                        val progressInStep = parseProgress * source.weight
                        updateProgress(currentStepStartProgress + progressInStep, "正在處理 ${source.name}...")
                    }

                    overallProgress += source.weight

                    Log.i(TAG, "************ ${source.name} 解析完成, 找到 ${items.size} 筆資料 ************")

                    if (items.isNotEmpty()) {
                        allItems.addAll(items)
                    } else {
                        Log.w(TAG, "警告：從 ${source.name} 解析到的資料數量為 0。")
                    }
                }

                if (allItems.isNotEmpty()) {
                    updateProgress(0.9f, "正在將 ${allItems.size} 筆總資料寫入資料庫 (這一步會很久)...")
                    allItems.chunked(5000).forEach { chunk ->
                        idiomDao.insertAll(chunk)
                        Log.d(TAG, "已插入 ${chunk.size} 筆資料...")
                    }
                    Log.d(TAG, "所有資料寫入完成。")
                } else {
                    Log.e(TAG, "警告：所有辭典都沒有解析到任何資料。")
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

    private suspend fun downloadAndParse(urlString: String, sourceKey: String, onProgress: (Float) -> Unit): List<Idiom> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "downloadAndParse: 開始執行 $urlString")
                val url = URL(urlString)
                val connection = url.openConnection() as HttpsURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connectTimeout = 60000
                connection.readTimeout = 600000
                connection.connect()

                if (connection.responseCode != HttpsURLConnection.HTTP_OK) {
                    Log.e(TAG, "downloadAndParse: 連線失敗 (回應碼: ${connection.responseCode}) for $urlString")
                    return@withContext emptyList<Idiom>()
                }

                Log.d(TAG, "downloadAndParse: 連線成功，準備讀取 JSON 串流。")

                val sourceInfo = dictionarySources.first { it.key == sourceKey }

                val idioms = connection.inputStream.use { inputStream ->
                    parseJsonStream(inputStream, sourceKey, sourceInfo.approxSize, onProgress)
                }

                Log.d(TAG, "downloadAndParse: JSON 讀取與解析完畢，總共找到 ${idioms.size} 筆資料。")
                idioms
            } catch (e: Exception) {
                Log.e(TAG, "downloadAndParse: 下載或解析時失敗 for $urlString", e)
                emptyList<Idiom>()
            }
        }
    }

    private fun parseJsonStream(inputStream: InputStream, source: String, totalCount: Int, onProgress: (Float) -> Unit): List<Idiom> {
        val reader = JsonReader(inputStream.bufferedReader())
        val idioms = mutableListOf<Idiom>()
        var count = 0

        try {
            reader.beginArray()
            while (reader.hasNext()) {
                readIdiomObject(reader, source)?.let { idioms.add(it) }
                count++
                if (count > 0 && count % 500 == 0) { // 每 500 筆更新一次進度
                    val progress = count.toFloat() / totalCount.toFloat()
                    onProgress(progress.coerceAtMost(1.0f)) // 確保進度不超過 1.0
                }
            }
            reader.endArray()
        } catch (e: Exception) {
            Log.e(TAG, "($source) 串流解析時發生錯誤", e)
        } finally {
            reader.close()
        }

        onProgress(1.0f) // 確保結束時進度為 100%
        Log.i(TAG, "($source) 串流解析完成，總共 ${idioms.size} 筆。")
        return idioms
    }

    private fun readIdiomObject(reader: JsonReader, source: String): Idiom? {
        var title = ""
        var finalZhuyin = ""
        var finalDefinition = ""
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
                        if (finalDefinition.isBlank()) {
                            val (zhuyin, definition) = readHeteronymObject(reader)
                            if (finalZhuyin.isBlank()) finalZhuyin = zhuyin
                            if (finalDefinition.isBlank()) finalDefinition = definition
                        } else {
                            reader.skipValue()
                        }
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

        return if (title.isNotBlank() && finalDefinition.isNotBlank()) {
            Idiom(
                term = title,
                zhuyin = finalZhuyin,
                source = source,
                definition = finalDefinition,
                radical = radical,
                strokeCount = strokeCount,
                nonRadicalStrokeCount = nonRadicalStrokeCount
            )
        } else {
            if (title.isNotBlank()) {
                Log.w(TAG, "詞條 '$title' 找不到有效的定義，已跳過。")
            }
            null
        }
    }

    private fun readHeteronymObject(reader: JsonReader): Pair<String, String> {
        var zhuyin = ""
        var definition = ""
        try {
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "bopomofo" -> zhuyin = reader.nextString()
                    "definitions" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            if (definition.isBlank()) {
                                definition = readDefinitionObject(reader)
                            } else {
                                reader.skipValue()
                            }
                        }
                        reader.endArray()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        } catch (e: Exception) {
            Log.e(TAG, "readHeteronymObject: 解析異讀音物件失敗", e)
        }
        return Pair(zhuyin, definition)
    }

    private fun readDefinitionObject(reader: JsonReader): String {
        var def = ""
        var quote = ""
        var link = ""
        try {
            reader.beginObject()
            while (reader.hasNext()) {
                when(reader.nextName()){
                    "def" -> def = reader.nextString()
                    "quote" -> {
                        reader.beginArray()
                        if(reader.hasNext() && quote.isBlank()) quote = reader.nextString()
                        while(reader.hasNext()) reader.skipValue()
                        reader.endArray()
                    }
                    "link" -> {
                        reader.beginArray()
                        if(reader.hasNext() && link.isBlank()) link = reader.nextString()
                        while(reader.hasNext()) reader.skipValue()
                        reader.endArray()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        } catch (e: Exception) {
            Log.e(TAG, "readDefinitionObject: 解析定義物件失敗", e)
        }
        return when {
            def.isNotBlank() -> def
            quote.isNotBlank() -> quote
            link.isNotBlank() -> link
            else -> ""
        }
    }
}
