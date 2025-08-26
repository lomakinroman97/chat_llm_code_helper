package com.example.chat_llm_code_helper.data.model

import com.google.gson.annotations.SerializedName

/**
 * Модель запроса к API YandexGPT
 */
data class ChatRequest(
    @SerializedName("modelUri")
    val modelUri: String = "gpt://b1gp9fidpabmov8j1rid/yandexgpt-lite",
    @SerializedName("messages")
    val messages: List<ChatMessage>
)
