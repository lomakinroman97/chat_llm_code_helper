package com.example.chat_llm_code_helper.ui.model

/**
 * Модель сообщения для UI
 */
data class Message(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Роли сообщений
 */
enum class MessageRole {
    USER,           // Сообщение пользователя
    ASSISTANT,      // Ответ ассистента
    SYSTEM          // Системное сообщение
}
