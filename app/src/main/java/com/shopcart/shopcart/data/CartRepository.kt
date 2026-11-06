package com.shopcart.shopcart.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Cart lives at carts/{uid}/products/{productId} with `number` (quantity).
 * Prices are always taken from the live product; the server recalculates
 * them again at checkout.
 */
class CartRepository(
    private val db: FirebaseFirestore,
    private val catalog: CatalogRepository,
) {
    private fun cart(uid: String) = db.collection("carts").document(uid).collection("products")

    fun lines(uid: String): Flow<List<CartLine>> =
        combine(cart(uid).asFlow(), catalog.products) { snap, products ->
            val byId = products.associateBy { it.id }
            snap.documents.mapNotNull { doc ->
                val product = byId[doc.id] ?: return@mapNotNull null
                val qty = (doc.get("number") as? Number)?.toInt() ?: 1
                CartLine(product, qty.coerceAtLeast(1))
            }
        }.logErrors("cart", emptyList())

    fun itemCount(uid: String): Flow<Int> =
        cart(uid).asFlow()
            .map { snap -> snap.documents.sumOf { (it.get("number") as? Number)?.toInt() ?: 1 } }
            .logErrors("cartCount", 0)

    /** Adds [quantity] more of [product], capped at the stock level. Returns the new quantity. */
    suspend fun add(uid: String, product: Product, quantity: Int = 1): Int {
        val ref = cart(uid).document(product.id)
        val existing = (ref.get().await().get("number") as? Number)?.toInt() ?: 0
        val next = (existing + quantity).coerceAtMost(product.quantity.coerceAtLeast(0))
        if (next <= 0) return 0
        write(uid, product, next)
        return next
    }

    suspend fun setQuantity(uid: String, product: Product, quantity: Int) {
        if (quantity <= 0) remove(uid, product.id) else write(uid, product, quantity.coerceAtMost(product.quantity))
    }

    suspend fun remove(uid: String, productId: String) {
        cart(uid).document(productId).delete().await()
    }

    private suspend fun write(uid: String, product: Product, quantity: Int) {
        cart(uid).document(product.id).set(
            mapOf(
                "number" to quantity,
                "product_name" to product.name,
                "product_price" to product.price,
                "total_price" to product.price * quantity,
            ),
            SetOptions.merge(),
        ).await()
    }
}
