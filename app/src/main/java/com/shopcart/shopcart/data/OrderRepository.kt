package com.shopcart.shopcart.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class OrderRepository(
    private val db: FirebaseFirestore,
    private val functions: FirebaseFunctions,
) {
    fun orders(uid: String): Flow<List<Order>> =
        db.collection("orders")
            .whereEqualTo("user_id", uid)
            .orderBy("placed_at", Query.Direction.DESCENDING)
            .limit(100)
            .asFlow()
            .map { snap -> snap.documents.map { it.toOrder() } }
            .catch { emit(emptyList()) }

    fun order(id: String): Flow<Order?> =
        db.collection("orders").document(id).asFlow()
            .map { if (it.exists()) it.toOrder() else null }

    suspend fun legacyItems(orderId: String): List<OrderItem> =
        db.collection("orders").document(orderId).collection(orderId).get().await()
            .documents.map { it.toLegacyOrderItem() }

    /**
     * Creates the order on the server from the user's cart and starts a
     * Paynow payment. Prices and stock are checked server-side.
     */
    suspend fun placeOrder(method: PaymentMethod, phone: String, address: String): CheckoutResult {
        val result = functions.getHttpsCallable("placeOrder").call(
            mapOf("method" to method.raw, "phone" to phone, "address" to address),
        ).await()
        val data = result.getData() as? Map<*, *> ?: error("Unexpected response from server")
        return CheckoutResult(
            orderId = data["orderId"] as String,
            amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
            redirectUrl = data["redirectUrl"] as? String,
            instructions = data["instructions"] as? String,
        )
    }

    /** Asks the server to poll Paynow. Returns the order's status. */
    suspend fun checkPayment(orderId: String): OrderStatus {
        val result = functions.getHttpsCallable("checkPayment").call(mapOf("orderId" to orderId)).await()
        val data = result.getData() as? Map<*, *>
        return OrderStatus.fromRaw(data?.get("status") as? String)
    }
}
