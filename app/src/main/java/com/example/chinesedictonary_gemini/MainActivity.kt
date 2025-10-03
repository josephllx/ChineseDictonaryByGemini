package com.example.chinesedictonary_gemini

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
import androidx.compose.ui.platform.LocalContext
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
        // **關鍵修正：使用我們在 Type.kt 中定義的新字體主題**
        typography = AppTypography,
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
fun DictionaryApp() {
    val application = LocalContext.current.applicationContext as DictionaryApplication
    val viewModel: DictionaryViewModel = viewModel(
        factory = DictionaryViewModelFactory(application.repository)
    )
    val appState by viewModel.appState.observeAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = appState) {
            is AppState.SettingUp -> {
                SetupScreen(progress = state.progress, message = state.message)
            }
            is AppState.Ready -> {
                AppNavigation(viewModel)
            }
            null -> {
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
fun SearchScreen(navController: NavController, viewModel: DictionaryViewModel) {
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
                placeholder = { Text("請輸入詞語...") },
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

            Box(modifier = Modifier.fillMaxSize()) {
                if (searchQuery.isBlank()) {
                    Text("請開始輸入以查詢...", color = TextColorSecondary, modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp))
                } else if (searchResults.isEmpty()) {
                    Text("查無結果", color = TextColorSecondary, modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp))
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
}

@Composable
fun IdiomListItem(idiom: Idiom, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceColor)
    ) {
        // **關鍵修正：移除來源標示，介面更簡潔**
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(idiom.term, fontSize = 18.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, idiomId: Int, viewModel: DictionaryViewModel) {
    val idiom by viewModel.getIdiomById(idiomId).observeAsState()

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
        val currentIdiom = idiom
        if (currentIdiom != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // **關鍵修正：新增部首與筆畫顯示**
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(currentIdiom.term, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    if (currentIdiom.term.length == 1 && currentIdiom.radical != null && currentIdiom.strokeCount != null && currentIdiom.nonRadicalStrokeCount != null) {
                        Text(
                            text = "[${currentIdiom.radical}部-${currentIdiom.nonRadicalStrokeCount}畫-共${currentIdiom.strokeCount}畫]",
                            fontSize = 16.sp,
                            color = TextColorSecondary,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(currentIdiom.zhuyin, fontSize = 18.sp, color = TextColorSecondary)

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.DarkGray)

                Text("詳細說明", fontSize = 14.sp, color = TextColorSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                // 字體大小已由 AppTypography 控制
                Text(currentIdiom.definition, lineHeight = 28.sp)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}


// --- 導航設定 (Navigation) ---

@Composable
fun AppNavigation(viewModel: DictionaryViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = "detail/{idiomId}",
            arguments = listOf(navArgument("idiomId") { type = NavType.IntType })
        ) { backStackEntry ->
            val idiomId = backStackEntry.arguments?.getInt("idiomId") ?: -1
            DetailScreen(navController = navController, idiomId = idiomId, viewModel = viewModel)
        }
    }
}

// --- 預覽 (Preview) ---
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DefaultPreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackgroundColor)) {
            Text("預覽模式", color=Color.White, modifier = Modifier.align(Alignment.Center))
        }
    }
}

