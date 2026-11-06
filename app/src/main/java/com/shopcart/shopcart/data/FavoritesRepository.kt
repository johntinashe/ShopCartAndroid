package com.shopcart.shopcart.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FavoritesRepository(
    private val db: FirebaseFirestore,
    private val catalog: CatalogRepository,
) {
    private fun favorites(uid: String) = db.collection("users").document(uid).collection("favorites")

    fun ids(uid: String): Flow<Set<String>> =
        favorites(uid).asFlow()
            .map { snap -> snap.documents.map { it.id }.toSet() }
            .logErrors("favoritesIds", emptySet())

    fun products(uid: String): Flow<List<Product>> =
        combine(ids(uid), catalog.products) { ids, products -> products.filter { it.id in ids } }

    suspend fun setFavorite(uid: String, product: Product, favorite: Boolean) {
        val ref = favorites(uid).document(product.id)
        if (favorite) {
            ref.set(
                mapOf(
                    "product_id" to product.id,
                    "product_name" to product.name,
                    "product_image" to product.listImage,
                    "product_price" to product.price,
                ),
            ).await()
        } else {
            ref.delete().await()
        }
    }
}
