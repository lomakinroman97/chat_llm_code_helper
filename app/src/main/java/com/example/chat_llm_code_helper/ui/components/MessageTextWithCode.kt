package com.example.chat_llm_code_helper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

/**
 * Компонент для отображения текста сообщения с блоками кода
 */
@Composable
fun MessageTextWithCode(
    text: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    
    // Парсим текст и извлекаем блоки кода
    val parsedContent = parseTextWithCodeBlocks(text)
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        parsedContent.forEach { content ->
            when (content) {
                is TextContent -> {
                    if (content.text.isNotBlank()) {
                        Text(
                            text = content.text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                is CodeContent -> {
                    CodeBlock(
                        code = content.code,
                        language = content.language,
                        onCopyClick = {
                            clipboardManager.setText(AnnotatedString(content.code))
                        }
                    )
                }
            }
        }
    }
}

/**
 * Парсит текст и извлекает блоки кода
 */
private fun parseTextWithCodeBlocks(text: String): List<ContentItem> {
    val result = mutableListOf<ContentItem>()
    val regex = Regex("```(\\w+)?\\n([\\s\\S]*?)```")
    var lastIndex = 0
    
    regex.findAll(text).forEach { matchResult ->
        // Добавляем текст до блока кода
        val textBefore = text.substring(lastIndex, matchResult.range.first)
        if (textBefore.isNotBlank()) {
            result.add(TextContent(textBefore.trim()))
        }
        
        // Добавляем блок кода
        val language = matchResult.groupValues[1].ifEmpty { "" }
        val code = matchResult.groupValues[2].trim()
        result.add(CodeContent(code, language))
        
        lastIndex = matchResult.range.last + 1
    }
    
    // Добавляем оставшийся текст
    if (lastIndex < text.length) {
        val remainingText = text.substring(lastIndex).trim()
        if (remainingText.isNotBlank()) {
            result.add(TextContent(remainingText))
        }
    }
    
    return result
}

/**
 * Базовый класс для элементов контента
 */
sealed class ContentItem

/**
 * Обычный текстовый контент
 */
data class TextContent(val text: String) : ContentItem()

/**
 * Блок кода
 */
data class CodeContent(val code: String, val language: String) : ContentItem()

