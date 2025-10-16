package com.example.offlineen2zh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlineen2zh.ui.theme.OfflineDictionaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OfflineDictionaryApp() }
    }
}

@Composable
private fun OfflineDictionaryApp() {
    OfflineDictionaryTheme {
        DictionaryRoute()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DictionaryRoute(viewModel: TranslationViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeErrorMessage()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        DictionaryScreen(
            state = uiState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            onQueryChange = viewModel::updateQuery,
            onTranslate = viewModel::translate,
            onClear = viewModel::clearTranslation,
            onRetryDownload = viewModel::retryModelDownload
        )
    }
}

@Composable
private fun DictionaryScreen(
    state: TranslationUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    onQueryChange: (String) -> Unit,
    onTranslate: () -> Unit,
    onClear: () -> Unit,
    onRetryDownload: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .padding(contentPadding)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(id = R.string.description_headline),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(id = R.string.description_body),
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.input_label)) },
            placeholder = { Text(text = stringResource(id = R.string.input_placeholder)) },
            supportingText = {
                Text(text = stringResource(id = R.string.input_supporting_text))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus(force = true)
                onTranslate()
            })
        )

        TranslationActions(
            isTranslating = state.isTranslating,
            onTranslate = {
                focusManager.clearFocus(force = true)
                onTranslate()
            },
            onClear = onClear
        )

        ModelStatusSection(
            isModelReady = state.isModelDownloaded,
            isDownloading = state.isModelDownloading,
            onRetryDownload = onRetryDownload
        )

        TranslationResult(state = state)
    }
}

@Composable
private fun TranslationActions(
    isTranslating: Boolean,
    onTranslate: () -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onTranslate,
            enabled = !isTranslating
        ) {
            if (isTranslating) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(18.dp)
                        .wrapContentSize(Alignment.Center),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(id = R.string.translating_label))
            } else {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.GTranslate,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.translate_action))
            }
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClear
        ) {
            Text(text = stringResource(id = R.string.clear_action))
        }
    }
}

@Composable
private fun ModelStatusSection(
    isModelReady: Boolean,
    isDownloading: Boolean,
    onRetryDownload: () -> Unit
) {
    when {
        isDownloading -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(id = R.string.model_downloading_label))
                CircularProgressIndicator()
            }
        }

        isModelReady -> {
            AssistChip(
                onClick = {},
                label = { Text(text = stringResource(id = R.string.model_ready_label)) },
                leadingIcon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.GTranslate,
                        contentDescription = null
                    )
                }
            )
        }

        else -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(id = R.string.model_missing_label))
                OutlinedButton(onClick = onRetryDownload) {
                    Text(text = stringResource(id = R.string.retry_download_action))
                }
            }
        }
    }
}

@Composable
private fun TranslationResult(state: TranslationUiState) {
    when {
        state.translation != null && state.lastTranslatedInput != null -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(id = R.string.translation_source_title),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = state.lastTranslatedInput,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.translation_result_title),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = state.translation,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }

        !state.isTranslating -> {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.translation_placeholder_message),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
