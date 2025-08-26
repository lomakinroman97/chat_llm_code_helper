package com.example.chat_llm_code_helper.data.repository

import com.example.chat_llm_code_helper.data.api.ApiClient
import com.example.chat_llm_code_helper.data.model.ChatMessage
import com.example.chat_llm_code_helper.data.model.ChatRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
/**
 * Репозиторий для работы с YandexGPT API
 */
class GptRepository {
    
    private val api = ApiClient.yandexGptApi
    
    // Константы для API
    private val apiKey = ""
    private val folderId = ""
    private val authorization = "Api-Key $apiKey"
    
    /**
     * Отправляет сообщения в YandexGPT API
     * @param messages Список сообщений для отправки
     * @return Ответ от API
     */
    suspend fun sendMessages(messages: List<ChatMessage>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = ChatRequest(messages = messages)
            val response = api.sendMessage(
                authorization = authorization,
                folderId = folderId,
                request = request
            )
            
            val assistantMessage = response.result.alternatives.firstOrNull()?.message?.text
            if (assistantMessage != null) {
                Result.success(assistantMessage)
            } else {
                Result.failure(Exception("Пустой ответ от API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
