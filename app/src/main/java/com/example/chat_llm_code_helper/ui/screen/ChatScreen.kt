package com.example.chat_llm_code_helper.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.chat_llm_code_helper.ui.components.LoadingIndicator
import com.example.chat_llm_code_helper.ui.components.MessageBubble
import com.example.chat_llm_code_helper.ui.components.MessageInput
import com.example.chat_llm_code_helper.ui.components.ModeSelector
import com.example.chat_llm_code_helper.ui.model.ChatMode
import com.example.chat_llm_code_helper.ui.viewmodel.ChatViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Основной экран чата
 */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val chatState by viewModel.chatState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    
    // Launcher для выбора файла (только текстовые файлы)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { fileUri ->
            try {
                val fileName = context.contentResolver.query(
                    fileUri, null, null, null, null
                )?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        cursor.getString(nameIndex)
                    } else null
                } ?: "unknown_file"
                
                // Проверяем расширение файла
                val fileExtension = fileName.substringAfterLast('.', "").lowercase()
                val supportedExtensions = listOf(
                    "kt", "java", "py", "js", "ts", "jsx", "tsx", "cpp", "c", "h", "hpp",
                    "cs", "php", "go", "rs", "swift", "rb", "scala", "clj", "hs",
                    "json", "xml", "yaml", "yml", "properties", "gradle", "kts",
                    "md", "txt", "log", "sql", "sh", "bat", "ps1"
                )
                
                if (fileExtension !in supportedExtensions) {
                    viewModel.setError("Неподдерживаемый формат файла. Поддерживаются только текстовые файлы с кодом.")
                    return@let
                }
                
                val inputStream = context.contentResolver.openInputStream(fileUri)
                val content = inputStream?.use { stream ->
                    BufferedReader(InputStreamReader(stream)).use { reader ->
                        reader.readText()
                    }
                } ?: ""
                
                // Проверяем размер файла (лимит ~21,000 символов для YandexGPT)
                val maxFileSize = 21000
                if (content.length > maxFileSize) {
                    viewModel.setError("Файл слишком большой (${content.length} символов). Максимальный размер: $maxFileSize символов.")
                    return@let
                }
                
                // Проверяем, что файл не пустой
                if (content.trim().isEmpty()) {
                    viewModel.setError("Выбранный файл пуст.")
                    return@let
                }
                
                viewModel.attachFile(content, fileName)
                
            } catch (e: Exception) {
                viewModel.setError("Ошибка при чтении файла: ${e.message}")
            }
        }
    }
    
    // Показываем ошибки через Snackbar
    LaunchedEffect(chatState.error) {
        chatState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    // Автопрокрутка к последнему сообщению
    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ModeSelector(
                currentMode = chatState.currentMode,
                onModeChanged = viewModel::switchMode
            )
        },
        bottomBar = {
            Column {
                if (chatState.isLoading) {
                    LoadingIndicator()
                }
                MessageInput(
                    onSendMessage = viewModel::sendMessage,
                    onAttachFile = { filePickerLauncher.launch("*/*") },
                    currentMode = chatState.currentMode,
                    attachedFileName = chatState.attachedFileName,
                    isLoading = chatState.isLoading
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (chatState.messages.isEmpty()) {
                // Показываем приветственное сообщение
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Приветственное сообщение будет добавлено автоматически при выборе режима
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(chatState.messages) { message ->
                        MessageBubble(message = message)
                    }
                }
            }
        }
    }
}
