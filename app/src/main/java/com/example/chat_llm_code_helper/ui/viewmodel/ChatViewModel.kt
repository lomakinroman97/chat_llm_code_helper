package com.example.chat_llm_code_helper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chat_llm_code_helper.data.model.ChatMessage
import com.example.chat_llm_code_helper.data.repository.GptRepository
import com.example.chat_llm_code_helper.ui.model.ChatMode
import com.example.chat_llm_code_helper.ui.model.ChatState
import com.example.chat_llm_code_helper.ui.model.Message
import com.example.chat_llm_code_helper.ui.model.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для управления состоянием чата
 */
class ChatViewModel : ViewModel() {
    
    private val repository = GptRepository()
    
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()
    
    // Системные промпты для разных режимов
    private val freeChatSystemMessage = "Режим свободного общения активирован. Вы можете общаться с ассистентом на любые темы."
    
    private val bugFixSystemMessage = "Активирован режим фикса багов. Прикрепите файл с исходным кодом и опишите в сообщении природу бага."
    
    private val bugFixSystemPrompt = """
        Ты - эксперт-программист. Пользователь присылает тебе исходный код и описание проблемы. 
        Твоя задача - проанализировать код, найти причину ошибки (бага) и предложить исправление. 
        Предоставь исправленный код и краткое пояснение к решению. 
        Отвечай только на вопросы, связанные с программированием. 
        Если вопрос не о коде, вежливо откажись отвечать.
    """.trimIndent()
    
    /**
     * Переключает режим чата
     */
    fun switchMode(newMode: ChatMode) {
        val currentState = _chatState.value
        if (currentState.currentMode == newMode) return
        
        val systemMessage = when (newMode) {
            ChatMode.FREE_CHAT -> freeChatSystemMessage
            ChatMode.BUG_FIX -> bugFixSystemMessage
        }
        
        val systemMsg = Message(
            role = MessageRole.SYSTEM,
            text = systemMessage
        )
        
        _chatState.value = currentState.copy(
            currentMode = newMode,
            messages = currentState.messages + systemMsg,
            attachedFileContent = null,
            attachedFileName = null
        )
    }
    
    /**
     * Отправляет сообщение пользователя
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val currentState = _chatState.value
        
        // Проверяем общий размер запроса перед отправкой
        val estimatedTokens = estimateTokens(text, currentState.attachedFileContent)
        val maxTokens = 6000 // Безопасный лимит для YandexGPT
        
        if (estimatedTokens > maxTokens) {
            _chatState.value = currentState.copy(
                error = "Запрос слишком большой (примерно $estimatedTokens токенов). Максимум: $maxTokens токенов. Попробуйте сократить описание или выбрать меньший файл."
            )
            return
        }
        
        // Добавляем сообщение пользователя
        val userMessage = Message(
            role = MessageRole.USER,
            text = text
        )
        
        val updatedMessages = currentState.messages + userMessage
        
        _chatState.value = currentState.copy(
            messages = updatedMessages,
            isLoading = true,
            error = null
        )
        
        // Отправляем запрос к API
        viewModelScope.launch {
            try {
                val apiMessages = prepareApiMessages(updatedMessages, currentState.currentMode)
                val result = repository.sendMessages(apiMessages)
                
                result.fold(
                    onSuccess = { response ->
                        val assistantMessage = Message(
                            role = MessageRole.ASSISTANT,
                            text = response
                        )
                        
                        _chatState.value = _chatState.value.copy(
                            messages = _chatState.value.messages + assistantMessage,
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        _chatState.value = _chatState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Произошла ошибка"
                        )
                    }
                )
            } catch (e: Exception) {
                _chatState.value = _chatState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Произошла ошибка"
                )
            }
        }
    }
    
    /**
     * Прикрепляет файл
     */
    fun attachFile(content: String, fileName: String) {
        _chatState.value = _chatState.value.copy(
            attachedFileContent = content,
            attachedFileName = fileName
        )
    }
    
    /**
     * Очищает ошибку
     */
    fun clearError() {
        _chatState.value = _chatState.value.copy(error = null)
    }
    
    /**
     * Устанавливает ошибку
     */
    fun setError(message: String) {
        _chatState.value = _chatState.value.copy(error = message)
    }
    
    /**
     * Подготавливает сообщения для отправки в API
     */
    private fun prepareApiMessages(
        messages: List<Message>,
        mode: ChatMode
    ): List<ChatMessage> {
        val apiMessages = mutableListOf<ChatMessage>()
        var systemPromptAdded = false
        
        for (message in messages) {
            when (message.role) {
                MessageRole.SYSTEM -> {
                    // Системные сообщения UI не отправляем в API
                    continue
                }
                MessageRole.USER -> {
                    val messageText = if (mode == ChatMode.BUG_FIX && !systemPromptAdded) {
                        systemPromptAdded = true
                        val currentState = _chatState.value
                        val fileContent = currentState.attachedFileContent
                        
                        if (fileContent != null) {
                            "$bugFixSystemPrompt\n\nИсходный код:\n```\n$fileContent\n```\n\nОписание проблемы: ${message.text}"
                        } else {
                            "$bugFixSystemPrompt\n\n${message.text}"
                        }
                    } else {
                        message.text
                    }
                    
                    apiMessages.add(ChatMessage(role = "user", text = messageText))
                }
                MessageRole.ASSISTANT -> {
                    apiMessages.add(ChatMessage(role = "assistant", text = message.text))
                }
            }
        }
        
        return apiMessages
    }
    
    /**
     * Оценивает количество токенов в тексте
     * Примерное соотношение: 1 токен ≈ 4 символа для русского/английского текста
     */
    private fun estimateTokens(userText: String, fileContent: String?): Int {
        val systemPromptTokens = 200 // Примерный размер системного промпта
        val userTextTokens = (userText.length / 4.0).toInt()
        val fileContentTokens = if (fileContent != null) (fileContent.length / 4.0).toInt() else 0
        
        return systemPromptTokens + userTextTokens + fileContentTokens
    }
}
