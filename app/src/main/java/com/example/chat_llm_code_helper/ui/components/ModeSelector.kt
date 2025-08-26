package com.example.chat_llm_code_helper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chat_llm_code_helper.ui.model.ChatMode

/**
 * Компонент для выбора режима чата
 */
@Composable
fun ModeSelector(
    currentMode: ChatMode,
    onModeChanged: (ChatMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = currentMode == ChatMode.FREE_CHAT,
                onClick = { onModeChanged(ChatMode.FREE_CHAT) },
                colors = RadioButtonDefaults.colors()
            )
            Text(
                text = "free",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = currentMode == ChatMode.BUG_FIX,
                onClick = { onModeChanged(ChatMode.BUG_FIX) },
                colors = RadioButtonDefaults.colors()
            )
            Text(
                text = "bugfix",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
