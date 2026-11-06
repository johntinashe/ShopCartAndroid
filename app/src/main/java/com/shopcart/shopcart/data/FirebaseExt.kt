package com.shopcart.shopcart.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Suspends until a Play Services [Task] completes. */
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        val error = task.exception
        when {
            error != null -> cont.resumeWithException(error)
            task.isCanceled -> cont.cancel()
            else -> {
                @Suppress("UNCHECKED_CAST")
                cont.resume(task.result as T)
            }
        }
    }
}

/** Live query results. The flow fails if the listener errors (e.g. permission denied). */
fun Query.asFlow(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}

/** Live document updates. */
fun DocumentReference.asFlow(): Flow<DocumentSnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}

/**
 * Logs a failed Firestore listener (e.g. PERMISSION_DENIED, missing index)
 * to Logcat under the "ShopCart" tag, then continues with [fallback] so the
 * screen shows an empty state instead of crashing.
 */
fun <T> Flow<T>.logErrors(what: String, fallback: T): Flow<T> = catch { e ->
    android.util.Log.e("ShopCart", "Loading $what failed", e)
    emit(fallback)
}
