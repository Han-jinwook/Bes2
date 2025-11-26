package com.bes2.app.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    texts: List<String>,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    typingDelay: Long = 100L,
    pauseDelay: Long = 2000L
) {
    var textIndex by remember { mutableStateOf(0) }
    var textToDisplay by remember { mutableStateOf("") }
    
    LaunchedEffect(texts) {
        while(true) {
            val currentText = texts[textIndex % texts.size]
            
            // Typing effect
            currentText.forEachIndexed { index, _ ->
                textToDisplay = currentText.substring(0, index + 1)
                delay(typingDelay)
            }
            
            // Pause at full text
            delay(pauseDelay)
            
            // Deleting effect (Optional, but looks cool) or just clear
            // Let's just clear for simplicity or type next one?
            // Usually deleting looks more "AI-ish"
            for (i in currentText.length downTo 0) {
                textToDisplay = currentText.substring(0, i)
                delay(typingDelay / 2)
            }
            
            delay(500) // Short pause before next word
            textIndex++
        }
    }

    Text(
        text = textToDisplay,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}
