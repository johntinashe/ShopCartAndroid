package com.shopcart.shopcart.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Customer support chat: messages/{uid}/userQueries. */
class ChatRepository(private val db: FirebaseFirestore) {

    private fun thread(uid: String) = db.collection("messages").document(uid).collection("userQueries")

    fun messages(uid: String): Flow<List<ChatMessage>> =
        thread(uid)
            .orderBy("time", Query.Direction.ASCENDING)
            .limitToLast(300)
            .asFlow()
            .map { snap -> snap.documents.map { it.toChatMessage() } }
            .logErrors("chat", emptyList())

    suspend fun send(uid: String, displayName: String, text: String) {
        val now = System.currentTimeMillis()
        thread(uid).add(
            mapOf(
                "message" to text,
                "from" to uid,
                "time" to now,
                "seen" to false,
                "type" to "text",
            ),
        ).await()
        db.collection("messages").document(uid).set(
            mapOf(
                "userid" to uid,
                "username" to displayName,
                "lastMsg" to text,
                "time" to now,
                "last_from" to "customer",
                "unread_admin" to FieldValue.increment(1),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun markRepliesSeen(uid: String, messages: List<ChatMessage>) {
        val unseen = messages.filter { it.from != uid && !it.seen }
        if (unseen.isEmpty()) return
        val batch = db.batch()
        unseen.forEach { batch.update(thread(uid).document(it.id), "seen", true) }
        batch.commit().await()
    }
}
