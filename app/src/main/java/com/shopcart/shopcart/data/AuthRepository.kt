package com.shopcart.shopcart.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val messaging: FirebaseMessaging,
) {
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged { a, b -> a?.uid == b?.uid }

    /** Synchronous snapshot, used as the initial value before the listener fires. */
    val userNow: FirebaseUser? get() = auth.currentUser
    val uid: String? get() = auth.currentUser?.uid
    fun requireUid(): String = auth.currentUser?.uid ?: error("Not signed in")

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        syncDeviceToken()
    }

    suspend fun register(name: String, surname: String, email: String, phone: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val uid = result.user?.uid ?: error("Account was not created")
        db.collection("users").document(uid).set(
            mapOf(
                "name" to name.trim(),
                "surname" to surname.trim(),
                "email" to email.trim(),
                "phoneNumber" to phone.trim(),
                "address" to "",
                "membership" to "standard",
                "created" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
        syncDeviceToken()
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    fun signOut() {
        val current = uid
        if (current != null) {
            // Stop sending this device notifications for the old account.
            db.collection("users").document(current)
                .update("deviceToken", null)
                .addOnCompleteListener { }
        }
        auth.signOut()
    }

    fun profile(uid: String): Flow<UserProfile> =
        db.collection("users").document(uid).asFlow()
            .map { it.toUserProfile(fallbackEmail = auth.currentUser?.email.orEmpty()) }

    suspend fun updateProfile(uid: String, name: String, surname: String, phone: String, address: String) {
        db.collection("users").document(uid).set(
            mapOf(
                "name" to name.trim(),
                "surname" to surname.trim(),
                "phoneNumber" to phone.trim(),
                "address" to address.trim(),
            ),
            SetOptions.merge(),
        ).await()
    }

    /**
     * Stores this device's FCM token so order updates reach the phone.
     * FCM is moving to installation-ID targeting (FirebaseMessaging.register());
     * tokens still work and the onOrderStatus function sends to them.
     */
    @Suppress("DEPRECATION")
    suspend fun syncDeviceToken(token: String? = null) {
        val current = uid ?: return
        val value = token ?: runCatching { messaging.token.await() }.getOrNull() ?: return
        db.collection("users").document(current)
            .set(mapOf("deviceToken" to value), SetOptions.merge())
            .await()
    }
}
