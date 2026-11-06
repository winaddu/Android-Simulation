package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Workspace(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Work",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class Shortcut(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val url: String = "",
    val icon: String = "bookmark",
    val workspaceId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class ChatSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val modelName: String = "gemini-3.8-flash",
    val systemInstruction: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class ChatMessage(
    @DocumentId val id: String = "",
    val role: String = "user",
    val content: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)
