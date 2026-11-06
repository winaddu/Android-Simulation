package com.example.ui.council

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class ChatMessageItem(
    val role: String, // "user" or "model"
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCouncilScreen() {
    val coroutineScope = rememberCoroutineScope()
    var selectedModel by remember { mutableStateOf("gemini-3.8-flash") }
    var inputMessage by remember { mutableStateOf("") }

    val messages = remember { mutableStateListOf<ChatMessageItem>(
        ChatMessageItem("model", "Greetings! I am your AI Council lead. Choose a model above, then ask me anything or request strategy & code reviews.")
    ) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    val model = remember(selectedModel) {
        Firebase.ai.generativeModel(selectedModel)
    }

    val chatSession = remember(model) {
        model.startChat()
    }

    fun sendMessage() {
        val prompt = inputMessage.trim()
        if (prompt.isEmpty() || isLoading) return
        inputMessage = ""
        errorMessage = null
        messages.add(ChatMessageItem("user", prompt))
        isLoading = true

        coroutineScope.launch {
            try {
                val response = withTimeout(60_000L) {
                    chatSession.sendMessage(prompt)
                }
                val reply = response.text ?: "No response received."
                messages.add(ChatMessageItem("model", reply))
            } catch (e: TimeoutCancellationException) {
                errorMessage = "Request timed out after 60 seconds."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AiCouncil", "Chat generation failed", e)
                errorMessage = e.localizedMessage ?: "AI generation failed."
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🤖 AI Council & Chat") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Select AI Model:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("gemini-3.8-flash", "gemini-4-argon", "gemini-3.1-flash-lite").forEach { m ->
                            FilterChip(
                                selected = selectedModel == m,
                                onClick = { selectedModel = m },
                                label = { Text(m.replace("gemini-", "")) }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isUser) "You" else "AI Council ($selectedModel)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Ask AI Council...") },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(26.dp)
                    )

                    IconButton(
                        onClick = { sendMessage() },
                        enabled = !isLoading && inputMessage.isNotBlank(),
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(26.dp))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Message",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
