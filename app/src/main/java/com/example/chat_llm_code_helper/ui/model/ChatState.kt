package com.example.chat_llm_code_helper.ui.model

/**
 * Состояние чата
 */
data class ChatState(
    val messages: List<Message> = emptyList(),
    val currentMode: ChatMode = ChatMode.FREE_CHAT,
    val isLoading: Boolean = false,
    val error: String? = null,
    val attachedFileContent: String? = null,
    val attachedFileName: String? = null
)
