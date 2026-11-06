package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.ChatSession
import com.example.data.model.Shortcut
import com.example.data.model.Workspace
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository(private val context: Context) {
    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    }

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google.")
    }

    // Workspaces
    fun observeWorkspaces(): Flow<List<Workspace>> = flow {
        val uid = try { requireUserId() } catch (e: Exception) { return@flow emit(emptyList()) }
        db.collection("users").document(uid).collection("workspaces")
            .snapshots()
            .map { snap -> snap.toObjects(Workspace::class.java) }
            .collect { emit(it) }
    }

    suspend fun addWorkspace(title: String, description: String, category: String) {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("workspaces").document()
        val data = mapOf(
            "userId" to uid,
            "title" to title,
            "description" to description,
            "category" to category,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        ref.set(data).await()
    }

    suspend fun deleteWorkspace(workspaceId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("workspaces").document(workspaceId).delete().await()
    }

    // Shortcuts / Launcher Apps
    fun observeShortcuts(): Flow<List<Shortcut>> = flow {
        val uid = try { requireUserId() } catch (e: Exception) { return@flow emit(emptyList()) }
        db.collection("users").document(uid).collection("shortcuts")
            .snapshots()
            .map { snap -> snap.toObjects(Shortcut::class.java) }
            .collect { emit(it) }
    }

    suspend fun addShortcut(title: String, url: String, icon: String, workspaceId: String) {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("shortcuts").document()
        val data = mapOf(
            "userId" to uid,
            "title" to title,
            "url" to url,
            "icon" to icon,
            "workspaceId" to workspaceId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        ref.set(data).await()
    }

    suspend fun deleteShortcut(shortcutId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("shortcuts").document(shortcutId).delete().await()
    }

    // AI Chat Sessions
    fun observeChatSessions(): Flow<List<ChatSession>> = flow {
        val uid = try { requireUserId() } catch (e: Exception) { return@flow emit(emptyList()) }
        db.collection("users").document(uid).collection("chats")
            .snapshots()
            .map { snap -> snap.toObjects(ChatSession::class.java) }
            .collect { emit(it) }
    }

    suspend fun createChatSession(title: String, modelName: String, systemInstruction: String): String {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("chats").document()
        val data = mapOf(
            "userId" to uid,
            "title" to title,
            "modelName" to modelName,
            "systemInstruction" to systemInstruction,
            "createdAt" to FieldValue.serverTimestamp()
        )
        ref.set(data).await()
        return ref.id
    }
}
