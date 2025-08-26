package com.example.chat_llm_code_helper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chat_llm_code_helper.ui.model.ChatMode

/**
 * Компонент для ввода сообщений
 */
@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    onAttachFile: () -> Unit,
    currentMode: ChatMode,
    attachedFileName: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Кнопка прикрепления файла (активна только в режиме фикса багов)
        if (currentMode == ChatMode.BUG_FIX) {
            IconButton(
                onClick = onAttachFile,
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Прикрепить файл",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Поле ввода сообщения
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            placeholder = {
                Text(
                    text = if (currentMode == ChatMode.BUG_FIX) {
                        "Опишите баг"
                    } else {
                        "Введите сообщение"
                    }
                )
            },
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            maxLines = 4
        )
        
        // Кнопка отправки
        IconButton(
            onClick = {
                if (messageText.isNotBlank()) {
                    onSendMessage(messageText)
                    messageText = ""
                }
            },
            enabled = !isLoading && messageText.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Отправить",
                modifier = Modifier.size(24.dp)
            )
        }
    }
    
    // Отображение прикрепленного файла
    if (attachedFileName != null) {
        Text(
            text = "Прикреплен файл: $attachedFileName",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }
}
