package com.example.chat_llm_code_helper.data.api

import com.example.chat_llm_code_helper.data.model.ChatRequest
import com.example.chat_llm_code_helper.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API интерфейс для работы с YandexGPT
 */
interface YandexGptApi {
    @POST("foundationModels/v1/completion")
    suspend fun sendMessage(
        @Header("Authorization") authorization: String,
        @Header("x-folder-id") folderId: String,
        @Body request: ChatRequest
    ): ChatResponse
}
