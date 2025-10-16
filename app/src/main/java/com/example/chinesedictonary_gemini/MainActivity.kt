package com.example.chinesedictonary_gemini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chinesedictonary_gemini.ui.theme.ChineseDictonary_geminiTheme
import com.example.chinesedictonary_gemini.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChineseDictonary_geminiTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    DictionaryScreen()
                }
            }
        }
    }
}

@Composable
fun DictionaryScreen(viewModel: TranslationViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    DictionaryContent(
        uiState = uiState,
        onInputChange = viewModel::updateInputText,
        onTranslate = {
            focusManager.clearFocus(force = true)
            viewModel.translateCurrentText()
        },
        onClear = viewModel::clearTranslation
    )
}

@Composable
private fun DictionaryContent(
    uiState: TranslationUiState,
    onInputChange: (String) -> Unit,
    onTranslate: () -> Unit,
    onClear: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "使用 Google ML Kit 進行離線英漢翻譯",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = uiState.inputText,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("輸入英文單字或片語") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onTranslate() })
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isModelDownloading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "正在下載離線翻譯模型，請稍候…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onTranslate, enabled = !uiState.isTranslating) {
            Text(text = "翻譯")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onClear) {
            Text(text = "清除翻譯結果")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isTranslating) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "翻譯中…", style = MaterialTheme.typography.bodySmall)
        }

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (uiState.translatedText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "英 → 中", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.inputText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = uiState.translatedText, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DictionaryContentPreview() {
    ChineseDictonary_geminiTheme {
        DictionaryContent(
            uiState = TranslationUiState(
                inputText = "hello",
                translatedText = "你好",
                isModelReady = true
            ),
            onInputChange = {},
            onTranslate = {},
            onClear = {}
        )
    }
}
