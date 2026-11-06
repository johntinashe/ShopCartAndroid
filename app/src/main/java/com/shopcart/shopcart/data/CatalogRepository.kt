package com.shopcart.shopcart.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.CoroutineScope

class CatalogRepository(
    private val db: FirebaseFirestore,
    appScope: CoroutineScope,
) {
    /** One shared listener on the catalogue for the whole app. */
    val products: Flow<List<Product>> = db.collection("products").asFlow()
        .map { snap -> snap.documents.map { it.toProduct() }.filter { it.active }.sortedBy { it.name.lowercase() } }
        .catch { emit(emptyList()) }
        .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    val categories: Flow<List<Category>> = db.collection("categories").asFlow()
        .map { snap -> snap.documents.map { it.toCategory() }.filter { it.active }.sortedBy { it.name.lowercase() } }
        .catch { emit(emptyList()) }
        .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    fun product(id: String): Flow<Product?> =
        db.collection("products").document(id).asFlow()
            .map { if (it.exists()) it.toProduct() else null }

    fun reviews(productId: String): Flow<List<Review>> =
        db.collection("reviews")
            .whereEqualTo("prodId", productId)
            .orderBy("time", Query.Direction.DESCENDING)
            .limit(30)
            .asFlow()
            .map { snap -> snap.documents.map { it.toReview() } }
            .catch { emit(emptyList()) }

    suspend fun addReview(product: Product, userId: String, userName: String, rating: Int, text: String) {
        db.collection("reviews").add(
            mapOf(
                "prodId" to product.id,
                "prodName" to product.name,
                "user_id" to userId,
                "user_name" to userName.ifBlank { "Customer" },
                "rating" to rating.coerceIn(1, 5),
                "review" to text.trim(),
                "time" to System.currentTimeMillis(),
            ),
        ).await()
    }
}
