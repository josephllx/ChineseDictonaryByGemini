國語辭典 Android 專案完整程式碼這份文件包含了建立一個功能完整的「國語辭典」Android App 所需的所有程式碼與說明。請依照指示將各個部分的程式碼複製到您 Android Studio 專案中的對應檔案。1. 專案建立指南 (README.md)這份文件將指導您如何使用我們產生的程式碼檔案，在 Android Studio 中建立一個功能完整的國語辭典 App。步驟 1：建立新專案打開 Android Studio。點擊 "New Project" (建立新專案)。在手機與平板 (Phone and Tablet) 模板中，選擇 "Empty Activity" (空白活動)，然後點擊 "Next"。設定專案：Name (名稱): DictionaryApp (或任何您喜歡的名稱)Package name (套件名稱): com.example.dictionaryapp (建議使用預設值)Save location (儲存位置): 選擇您要存放專案的資料夾。Language (語言): 請務必選擇 Kotlin。Minimum SDK (最低 SDK 版本): 選擇 API 26: Android 8.0 (Oreo) 或更高版本。Build configuration language (建構設定語言): 請務必選擇 Kotlin DSL (build.gradle.kts)。點擊 "Finish"，等待 Android Studio 完成專案的初始化設定。步驟 2：更新 Gradle Dependencies在 Android Studio 左側的專案瀏覽器中，找到並打開 app/build.gradle.kts 檔案。將下方的 build.gradle.kts 檔案中的 dependencies 區塊內容，完整複製並覆蓋您專案中既有的 dependencies 區塊。點擊畫面上方出現的 "Sync Now" 或大象圖示，讓 Android Studio 下載並同步新的函式庫。步驟 3：建立資料與邏輯檔案在專案瀏覽器中，找到 app/src/main/java/com/example/dictionaryapp 這個路徑。在 dictionaryapp 資料夾上按右鍵，選擇 New -> Kotlin Class/File。建立 DictionaryData.kt:輸入檔案名稱 DictionaryData。將下方的 DictionaryData.kt 檔案內容完整複製貼上。建立 DictionaryViewModel.kt:同樣，在 dictionaryapp 資料夾上按右鍵，建立新檔案 DictionaryViewModel。將下方的 DictionaryViewModel.kt 檔案內容完整複製貼上。步驟 4：更新主畫面程式碼打開 app/src/main/java/com/example/dictionaryapp/MainActivity.kt 檔案。刪除裡面原有的所有程式碼。將下方的 MainActivity.kt 檔案內容完整複製貼上。步驟 5：執行 App恭喜您！所有檔案都已就位。確認您已連接一台實體 Android 裝置，或已啟動一個 Android 模擬器。點擊 Android Studio 工具列中的綠色 "Run 'app'" (►) 按鈕。專案將會編譯並安裝到您的裝置/模擬器上。2. 專案依賴設定 (app/build.gradle.kts)// 檔案路徑: app/build.gradle.kts
// 注意：請只複製 dependencies 區塊的內容來覆蓋您專案中的 dependencies 區塊。
// 其他部分 (plugins, android, buildTypes) 請保留專案的預設值。

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // 新增 ViewModel 和 LiveData 的支援
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.7")

    // 新增 Navigation for Compose 的支援
    implementation("androidx.navigation:navigation-compose:2.7.7")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
3. 資料模型與模擬資料 (app/src/main/java/com/example/dictionaryapp/DictionaryData.kt)package com.example.dictionaryapp

// 定義成語的資料結構
data class Idiom(
    val id: Int,
    val term: String,
    val zhuyin: String,
    val source: String,
    val definition: String
)

// 定義辭典來源的資料
object Dictionaries {
    val sourceMap = mapOf(
        "idioms" to "成語典",
        "revised" to "重編本",
        "concise" to "簡編本",
        "mini" to "小字典"
    )
}

// 模擬的辭典資料庫
// 在真實的 App 中，這些資料會來自於解壓縮後的檔案並存入資料庫 (如 Room)
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
4. App 的大腦 (app/src/main/java/com/example/dictionaryapp/DictionaryViewModel.kt)package com.example.dictionaryapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// App 狀態的定義
sealed class AppState {
    object Loading : AppState() // 首次設定中
    object Ready : AppState()   // App 準備就緒
}

class DictionaryViewModel : ViewModel() {

    // --- 狀態管理 ---
    private val _appState = MutableLiveData<AppState>(AppState.Loading)
    val appState: LiveData<AppState> = _appState

    private val _setupProgress = MutableLiveData(0.1f)
    val setupProgress: LiveData<Float> = _setupProgress

    private val _setupMessage = MutableLiveData("正在下載簡編本...")
    val setupMessage: LiveData<String> = _setupMessage

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _searchResults = MutableLiveData<List<Idiom>>(emptyList())
    val searchResults: LiveData<List<Idiom>> = _searchResults

    init {
        // 模擬首次啟動的設定流程
        // 在真實 App 中，這裡會執行下載、解壓縮、建立資料庫索引等耗時操作
        viewModelScope.launch {
            delay(800)
            _setupMessage.value = "正在下載小字典..."
            _setupProgress.value = 0.3f
            delay(800)
            _setupMessage.value = "正在下載重編本..."
            _setupProgress.value = 0.6f
            delay(800)
            _setupMessage.value = "正在下載成語典..."
            _setupProgress.value = 0.85f
            delay(1000)
            _setupMessage.value = "建立索引檔案..."
            _setupProgress.value = 1.0f
            delay(500)
            _appState.value = AppState.Ready
        }
    }

    // --- 邏輯處理 ---

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            // 在真實 App 中，這裡是對資料庫進行查詢
            _searchResults.value = mockIdiomList.filter { it.term.startsWith(query) }
        }
    }

    fun getIdiomById(id: Int): Idiom? {
        // 在真實 App 中，這裡是根據 ID 從資料庫讀取單筆資料
        return mockIdiomList.find { it.id == id }
    }
}
5. 主畫面與 UI 介面 (app/src/main/java/com/example/dictionaryapp/MainActivity.kt)package com.example.dictionaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// 主題顏色
private val DarkBackgroundColor = Color(0xFF121212)
private val DarkSurfaceColor = Color(0xFF1E1E1E)
private val TextColor = Color.White
private val TextColorSecondary = Color.Gray
private val AccentColor = Color(0xFF3B82F6)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = DarkBackgroundColor,
            surface = DarkSurfaceColor,
            onBackground = TextColor,
            onSurface = TextColor,
            primary = AccentColor,
        ),
        typography = Typography(
            bodyLarge = LocalTextStyle.current.copy(fontFamily = LocalTextStyle.current.fontFamily, letterSpacing = 0.5.sp),
        ),
        content = content
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                DictionaryApp()
            }
        }
    }
}

@Composable
fun DictionaryApp(viewModel: DictionaryViewModel = viewModel()) {
    val appState by viewModel.appState.observeAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (appState) {
            is AppState.Loading -> {
                val progress by viewModel.setupProgress.observeAsState(0f)
                val message by viewModel.setupMessage.observeAsState("")
                SetupScreen(progress = progress, message = message)
            }
            is AppState.Ready -> {
                AppNavigation()
            }
            null -> { // 處理初始 null 狀態
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// --- 畫面元件 (Composables) ---

@Composable
fun SetupScreen(progress: Float, message: String) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progressAnimation")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("首次設定", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextColor)
        Spacer(modifier = Modifier.height(8.dp))
        Text("正在為您準備辭典資料...", fontSize = 14.sp, color = TextColorSecondary)
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, fontSize = 12.sp, color = TextColorSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, viewModel: DictionaryViewModel = viewModel()) {
    val searchQuery by viewModel.searchQuery.observeAsState("")
    val searchResults by viewModel.searchResults.observeAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("國語辭典", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurfaceColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("請輸入成語...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (searchQuery.isBlank()) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("請開始輸入以查詢...", color = TextColorSecondary)
                 }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults) { idiom ->
                        IdiomListItem(idiom = idiom, onClick = {
                            navController.navigate("detail/${idiom.id}")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun IdiomListItem(idiom: Idiom, onClick: () -> Unit) {
    val sourceName = Dictionaries.sourceMap[idiom.source] ?: "未知"
    val sourceColor = when(idiom.source) {
        "idioms" -> Color(0xFFDC2626) // red-600
        "revised" -> Color(0xFF0284C7) // sky-600
        "concise" -> Color(0xFF16A34A) // green-600
        "mini" -> Color(0xFFD97706) // yellow-600
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(idiom.term, fontSize = 18.sp)
            Text(
                text = sourceName,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(color = sourceColor, shape = RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, idiomId: Int, viewModel: DictionaryViewModel = viewModel()) {
    val idiom = viewModel.getIdiomById(idiomId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* 標題留空 */ },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurfaceColor)
            )
        }
    ) { paddingValues ->
        if (idiom != null) {
            val sourceName = Dictionaries.sourceMap[idiom.source] ?: "未知"
            val sourceColor = when (idiom.source) {
                "idioms" -> Color(0xFFDC2626)
                "revised" -> Color(0xFF0284C7)
                "concise" -> Color(0xFF16A34A)
                "mini" -> Color(0xFFD97706)
                else -> Color.Gray
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(idiom.term, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(idiom.zhuyin, fontSize = 18.sp, color = TextColorSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = sourceName,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(color = sourceColor, shape = RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.DarkGray)
                Text("詳細說明", fontSize = 14.sp, color = TextColorSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(idiom.definition, fontSize = 18.sp, lineHeight = 28.sp)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("找不到該成語的資料。")
            }
        }
    }
}


// --- 導航設定 (Navigation) ---

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable(
            route = "detail/{idiomId}",
            arguments = listOf(navArgument("idiomId") { type = NavType.IntType })
        ) { backStackEntry ->
            val idiomId = backStackEntry.arguments?.getInt("idiomId") ?: -1
            DetailScreen(navController = navController, idiomId = idiomId)
        }
    }
}

// --- 預覽 (Preview) ---
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DefaultPreview() {
    AppTheme {
        DictionaryApp()
    }
}
