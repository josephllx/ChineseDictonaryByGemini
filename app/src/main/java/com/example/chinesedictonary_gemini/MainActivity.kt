package com.example.chinesedictonary_gemini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.text.TextStyle
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

// **電子紙專用主題：高對比白底黑字**
private val EInkBackgroundColor = Color.White
private val EInkSurfaceColor = Color.White
private val EInkTextColor = Color.Black
private val EInkTextColorSecondary = Color(0xFF555555) // 深灰色
private val EInkBorderColor = Color(0xFFDDDDDD) // 淺灰色邊框

private val EInkColorScheme = lightColorScheme(
    background = EInkBackgroundColor,
    surface = EInkSurfaceColor,
    onBackground = EInkTextColor,
    onSurface = EInkTextColor,
    onSurfaceVariant = EInkTextColorSecondary,
    primary = EInkTextColor,
    outline = EInkBorderColor
)


@Composable
fun AppTheme(
    // **電子紙專用：強制使用淺色主題**
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = EInkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
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
                    CircularProgressIndicator(color = EInkTextColor)
                }
            }
        }
    }
}

// --- 畫面元件 (Composables) ---

@Composable
fun SetupScreen(progress: Float, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(60.dp), strokeWidth = 5.dp, color = EInkTextColor)
        Spacer(modifier = Modifier.height(32.dp))
        Text("首次設定", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))
        Text("正在為您準備辭典資料...", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = EInkTextColor,
            trackColor = EInkBorderColor
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(message, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                title = { Text("國語辭典", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 32.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 36.sp),
                placeholder = { Text("請輸入詞語...", fontSize = 36.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", modifier = Modifier.size(36.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EInkTextColor,
                    unfocusedBorderColor = EInkBorderColor,
                    cursorColor = EInkTextColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                if (searchQuery.isBlank()) {
                    Text("請開始輸入以查詢...", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp))
                } else if (searchResults.isEmpty()) {
                    Text("查無結果", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, EInkBorderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(idiom.term, fontSize = 36.sp)
            Icon(Icons.Default.ChevronRight, contentDescription = "查看詳情", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(36.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        val currentIdiom = idiom
        if (currentIdiom != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(currentIdiom.term, fontSize = 64.sp, fontWeight = FontWeight.Bold)
                        if (currentIdiom.term.length == 1 && currentIdiom.radical != null && currentIdiom.strokeCount != null) {
                            Text(
                                text = "[${currentIdiom.radical}部-${currentIdiom.nonRadicalStrokeCount ?: 0}畫-共${currentIdiom.strokeCount}畫]",
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                items(currentIdiom.pronunciations) { pronunciation ->
                    if (currentIdiom.pronunciations.size > 1 && currentIdiom.pronunciations.indexOf(pronunciation) > 0) {
                        Divider(modifier = Modifier.padding(vertical = 24.dp), color = EInkBorderColor, thickness = 2.dp)
                    }
                    pronunciation.bopomofo?.let {
                        Text(it, fontSize = 36.sp, color = EInkTextColor, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    pronunciation.definitions?.forEach { defItem ->
                        Column(modifier = Modifier.padding(bottom = 24.dp)) {
                            defItem.type?.let {
                                Text("[$it]", fontSize = 28.sp, color = EInkTextColorSecondary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            defItem.def?.let {
                                Text(it, fontSize = 36.sp, lineHeight = 56.sp)
                            }
                            // 只顯示第一個範例和引證，避免畫面過於雜亂
                            defItem.example?.firstOrNull()?.let { example ->
                                Text("例：$example", fontSize = 32.sp, color = EInkTextColorSecondary, modifier = Modifier.padding(top = 8.dp))
                            }
                            defItem.quote?.firstOrNull()?.let { quote ->
                                Text(quote, fontSize = 32.sp, color = EInkTextColorSecondary, modifier = Modifier.padding(top = 8.dp))
                            }
                            defItem.link?.firstOrNull()?.let { link ->
                                Text(link, fontSize = 32.sp, color = EInkTextColorSecondary, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EInkTextColor)
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
@Preview(showBackground = true, name = "電子紙預覽", widthDp = 380, heightDp = 800)
@Composable
fun EInkPreview() {
    AppTheme(darkTheme = false) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    }
}
