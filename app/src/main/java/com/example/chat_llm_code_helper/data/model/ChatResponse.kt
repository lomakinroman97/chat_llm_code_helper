package com.example.chat_llm_code_helper.data.model

import com.google.gson.annotations.SerializedName

/**
 * Модель ответа от API YandexGPT
 */
data class ChatResponse(
    val result: ChatResult
)

data class ChatResult(
    val alternatives: List<Alternative>,
    val usage: Usage
)

data class Alternative(
    val message: ChatMessage,
    val status: String
)

data class Usage(
    @SerializedName("inputTextTokens")
    val inputTextTokens: Int,
    @SerializedName("completionTokens")
    val completionTokens: Int,
    @SerializedName("totalTokens")
    val totalTokens: Int
)
