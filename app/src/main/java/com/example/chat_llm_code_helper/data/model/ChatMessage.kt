package com.example.chat_llm_code_helper.data.model

import com.google.gson.annotations.SerializedName

/**
 * Модель сообщения для API YandexGPT
 */
data class ChatMessage(
    @SerializedName("role")
    val role: String, // "user", "assistant", "system"
    @SerializedName("text")
    val text: String
)
