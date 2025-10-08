package com.example.chinesedictonary_gemini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
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
import androidx.compose.ui.input.pointer.pointerInput
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

private val EInkBackgroundColor = Color.White
private val EInkSurfaceColor = Color.White
private val EInkTextColor = Color.Black
private val EInkTextColorSecondary = Color(0xFF555555)
private val EInkBorderColor = Color(0xFFDDDDDD)

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
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EInkColorScheme,
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
            is AppState.SettingUp -> SetupScreen(progress = state.progress, message = state.message)
            is AppState.Ready -> AppNavigation(viewModel)
            null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EInkTextColor)
            }
        }
    }
}

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
    val searchResults by viewModel.searchResults.observeAsState(emptyList())
    val selectedSearchType by viewModel.selectedSearchType.observeAsState("詞彙")
    val searchOptions = listOf("詞彙", "部首", "注音")

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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                searchOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = searchOptions.size),
                        onClick = { viewModel.changeSearchType(label) },
                        selected = label == selectedSearchType,
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.onBackground,
                            activeContentColor = MaterialTheme.colorScheme.background,
                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = SegmentedButtonDefaults.borderStroke(color = EInkBorderColor, width = 1.dp)
                    ) {
                        Text(label, fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedSearchType) {
                "詞彙" -> {
                    val searchQuery by viewModel.searchQuery.observeAsState("")
                    WordSearchContent(
                        searchQuery = searchQuery,
                        onQueryChange = { viewModel.performSearch(it) },
                        searchResults = searchResults,
                        onItemClick = { idiomId -> navController.navigate("detail/$idiomId") }
                    )
                }
                "部首" -> {
                    val radicals by viewModel.radicals.observeAsState(emptyList())
                    val selectedRadical by viewModel.selectedRadical.observeAsState()
                    RadicalSearchContent(
                        radicals = radicals,
                        selectedRadical = selectedRadical,
                        searchResults = searchResults,
                        onRadicalClick = { viewModel.performSearch(it) },
                        onItemClick = { idiomId -> navController.navigate("detail/$idiomId") }
                    )
                }
                "注音" -> {
                    val searchQuery by viewModel.searchQuery.observeAsState("")
                    ZhuyinSearchContent(
                        searchQuery = searchQuery,
                        onQueryChange = { viewModel.performSearch(it) },
                        searchResults = searchResults,
                        onItemClick = { idiomId -> navController.navigate("detail/$idiomId") }
                    )
                }
            }
        }
    }
}

@Composable
fun WordSearchContent(searchQuery: String, onQueryChange: (String) -> Unit, searchResults: List<Idiom>, onItemClick: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(value = searchQuery, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(fontSize = 36.sp), placeholder = { Text("請輸入詞語...", fontSize = 36.sp) }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", modifier = Modifier.size(36.dp)) }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EInkTextColor, unfocusedBorderColor = EInkBorderColor, cursorColor = EInkTextColor))
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxSize()) {
            if (searchQuery.isBlank()) {
                Text("請開始輸入以查詢...", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp))
            } else if (searchResults.isEmpty()) {
                Text("查無結果", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(searchResults) { idiom ->
                        IdiomListItem(idiom = idiom, onClick = { onItemClick(idiom.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun RadicalSearchContent(radicals: List<String>, selectedRadical: String?, searchResults: List<Idiom>, onRadicalClick: (String) -> Unit, onItemClick: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(border = BorderStroke(1.dp, EInkBorderColor), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 60.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                items(radicals) { radical ->
                    Button(
                        onClick = { onRadicalClick(radical) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (radical == selectedRadical) EInkTextColor else EInkSurfaceColor,
                            contentColor = if (radical == selectedRadical) EInkBackgroundColor else EInkTextColor
                        ),
                        border = BorderStroke(1.dp, EInkBorderColor),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(radical, fontSize = 28.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedRadical == null) {
            Text("請選擇一個部首...", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp))
        } else if (searchResults.isEmpty()) {
            Text("查無結果", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(searchResults) { idiom ->
                    IdiomListItem(idiom = idiom, onClick = { onItemClick(idiom.id) })
                }
            }
        }
    }
}

@Composable
fun ZhuyinSearchContent(searchQuery: String, onQueryChange: (String) -> Unit, searchResults: List<Idiom>, onItemClick: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(value = searchQuery, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(fontSize = 36.sp), placeholder = { Text("請使用下方鍵盤輸入...", fontSize = 36.sp) }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EInkTextColor, unfocusedBorderColor = EInkBorderColor))

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (searchQuery.isNotBlank() && searchResults.isEmpty()) {
                item { Text("查無結果", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 32.dp)) }
            } else {
                items(searchResults) { idiom ->
                    ZhuyinListItem(
                        idiom = idiom,
                        searchQuery = searchQuery,
                        onClick = { onItemClick(idiom.id) }
                    )
                }
            }
        }

        ZhuyinKeyboard(
            onKeyPress = { key -> onQueryChange(searchQuery + key) },
            onBackspace = { onQueryChange(searchQuery.dropLast(1)) },
            onClear = { onQueryChange("") }
        )
    }
}

@Composable
fun ZhuyinKeyboard(onKeyPress: (String) -> Unit, onBackspace: () -> Unit, onClear: () -> Unit) {
    // **最終版：倚天26鍵 (ETen-26) QWERTY 注音排列**
    // 根據使用者指定 ㄧㄨㄩ -> ujm 等精確對應
    val keys = listOf(
        // Number Row (對應 1-0, -)
        listOf("ㄅ", "ㄉ", "ˇ", "ˋ", "ㄓ", "ˊ", "˙", "ㄚ", "ㄞ", "ㄢ", "ㄦ"),
        // QWERTY Row (對應 Q-P)
        listOf("ㄆ", "ㄊ", "ㄍ", "ㄐ", "ㄔ", "ㄗ", "ㄧ", "ㄛ", "ㄟ", "ㄣ"),
        // ASDF Row (對應 A-;)
        listOf("ㄇ", "ㄋ", "ㄎ", "ㄑ", "ㄕ", "ㄘ", "ㄨ", "ㄜ", "ㄠ", "ㄤ"),
        // ZXCV Row (對應 Z-/)
        listOf("ㄈ", "ㄌ", "ㄏ", "ㄒ", "ㄖ", "ㄙ", "ㄩ", "ㄝ", "ㄡ", "ㄥ")
    )

    Card(border = BorderStroke(1.dp, EInkBorderColor), modifier = Modifier.padding(top = 8.dp)) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { key ->
                        Button(
                            onClick = { onKeyPress(key) },
                            modifier = Modifier.height(55.dp).weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EInkSurfaceColor, contentColor = EInkTextColor),
                            border = BorderStroke(1.dp, EInkBorderColor),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(key, fontSize = 24.sp)
                        }
                    }
                }
            }
            // Final row for Space, Clear, Backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(onClick = { onKeyPress(" ") }, modifier = Modifier.height(55.dp).weight(8f), shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = EInkSurfaceColor, contentColor = EInkTextColor), border = BorderStroke(1.dp, EInkBorderColor)) { Text("空白", fontSize = 28.sp) }
                Button(onClick = onClear, modifier = Modifier.height(55.dp).weight(1.5f), shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = EInkSurfaceColor, contentColor = EInkTextColor), border = BorderStroke(1.dp, EInkBorderColor)) { Text("C", fontSize = 28.sp) }
                Button(onClick = onBackspace, modifier = Modifier.height(55.dp).weight(1.5f).pointerInput(Unit) { detectTapGestures(onLongPress = { onClear() }) }, shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = EInkSurfaceColor, contentColor = EInkTextColor), border = BorderStroke(1.dp, EInkBorderColor)) { Text("⌫", fontSize = 28.sp) }
            }
        }
    }
}

@Composable
fun IdiomListItem(idiom: Idiom, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, EInkBorderColor)) {
        Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(idiom.term, fontSize = 36.sp)
            Icon(Icons.Default.ChevronRight, contentDescription = "查看詳情", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ZhuyinListItem(idiom: Idiom, searchQuery: String, onClick: () -> Unit) {
    val queryParts = searchQuery.trim().split(" ").filter { it.isNotEmpty() }
    val matchingPronunciation = idiom.pronunciations.firstOrNull { p ->
        val bopomofoParts = p.bopomofo?.trim()?.split(" ")?.filter { it.isNotEmpty() }
        if (bopomofoParts == null || bopomofoParts.size < queryParts.size) return@firstOrNull false
        var matches = true
        for(i in queryParts.indices) {
            if(!bopomofoParts[i].startsWith(queryParts[i])) {
                matches = false
                break
            }
        }
        matches
    }

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
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(idiom.term, fontSize = 36.sp)
                (matchingPronunciation?.bopomofo ?: idiom.pronunciations.firstOrNull()?.bopomofo)?.let {
                    Text(
                        text = it,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "查看詳情", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, idiomId: Int, viewModel: DictionaryViewModel) {
    val idiom by viewModel.getIdiomById(idiomId).observeAsState()
    Scaffold(topBar = { TopAppBar(title = { }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(36.dp)) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) }) { paddingValues ->
        val currentIdiom = idiom
        if (currentIdiom != null) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp)) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(currentIdiom.term, fontSize = 64.sp, fontWeight = FontWeight.Bold)
                        if (currentIdiom.term.length == 1 && currentIdiom.radical != null && currentIdiom.strokeCount != null) {
                            Text(text = "[${currentIdiom.radical}部-${currentIdiom.nonRadicalStrokeCount ?: 0}畫-共${currentIdiom.strokeCount}畫]", fontSize = 32.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                items(currentIdiom.pronunciations) { pronunciation ->
                    if (currentIdiom.pronunciations.size > 1) {
                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = EInkBorderColor)
                    }
                    pronunciation.bopomofo?.let {
                        Text(it, fontSize = 36.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    pronunciation.definitions?.forEach { defItem ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, EInkBorderColor)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                defItem.type?.let {
                                    Text("[$it]", fontSize = 28.sp, color = EInkTextColorSecondary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                val lineHeight = 52.sp
                                defItem.def?.let { Text(it, fontSize = 36.sp, lineHeight = 56.sp) }
                                defItem.example?.forEach { example -> Text("例：$example", fontSize = 32.sp, color = EInkTextColorSecondary, modifier = Modifier.padding(top = 8.dp), lineHeight = lineHeight) }
                                defItem.quote?.forEach { quote -> Text(quote, fontSize = 32.sp, color = EInkTextColorSecondary, modifier = Modifier.padding(top = 8.dp), lineHeight = lineHeight) }
                                defItem.link?.forEach { link -> Text(link, fontSize = 32.sp, color = EInkTextColorSecondary, modifier = Modifier.padding(top = 8.dp), lineHeight = lineHeight) }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
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

@Composable
fun AppNavigation(viewModel: DictionaryViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "search") {
        composable("search") { SearchScreen(navController = navController, viewModel = viewModel) }
        composable(route = "detail/{idiomId}", arguments = listOf(navArgument("idiomId") { type = NavType.IntType })) { backStackEntry ->
            val idiomId = backStackEntry.arguments?.getInt("idiomId") ?: -1
            DetailScreen(navController = navController, idiomId = idiomId, viewModel = viewModel)
        }
    }
}

@Preview(showBackground = true, name = "電子紙預覽", widthDp = 380, heightDp = 800)
@Composable
fun EInkPreview() {
    AppTheme(darkTheme = false) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    }
}