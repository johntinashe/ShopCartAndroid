package com.shopcart.shopcart

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.shopcart.shopcart.data.AuthRepository
import com.shopcart.shopcart.data.CartRepository
import com.shopcart.shopcart.data.CatalogRepository
import com.shopcart.shopcart.data.ChatRepository
import com.shopcart.shopcart.data.FavoritesRepository
import com.shopcart.shopcart.data.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual dependency injection: one instance of each repository for the app.
 * Small enough that a DI framework would add more than it saves.
 */
class AppContainer {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val firestore = FirebaseFirestore.getInstance()

    val auth = AuthRepository(FirebaseAuth.getInstance(), firestore, FirebaseMessaging.getInstance())
    val catalog = CatalogRepository(firestore, appScope)
    val cart = CartRepository(firestore, catalog)
    val favorites = FavoritesRepository(firestore, catalog)
    val orders = OrderRepository(firestore, FirebaseFunctions.getInstance())
    val chat = ChatRepository(firestore)
}
