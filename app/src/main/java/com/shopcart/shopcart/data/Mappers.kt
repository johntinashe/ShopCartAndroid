package com.shopcart.shopcart.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

// Firestore documents are mapped by hand instead of toObject() so that data
// written by the 2018 app (mixed number/string/timestamp types) can't crash
// the new app.

private fun Any?.asText(): String = when (this) {
    null -> ""
    is String -> this
    else -> toString()
}

private fun Any?.asDouble(): Double = when (this) {
    is Number -> toDouble()
    is String -> toDoubleOrNull() ?: 0.0
    else -> 0.0
}

private fun Any?.asMillis(): Long = when (this) {
    is Number -> toLong()
    is Timestamp -> toDate().time
    is Date -> time
    is String -> toLongOrNull() ?: 0L
    else -> 0L
}

private fun Any?.asBoolean(default: Boolean): Boolean = this as? Boolean ?: default

private fun DocumentSnapshot.text(field: String) = get(field).asText()
private fun DocumentSnapshot.double(field: String) = get(field).asDouble()
private fun DocumentSnapshot.millis(field: String) = get(field).asMillis()
private fun DocumentSnapshot.bool(field: String, default: Boolean) = get(field).asBoolean(default)

fun DocumentSnapshot.toProduct() = Product(
    id = id,
    name = text("product_name"),
    categoryId = text("product_category_id"),
    price = double("product_price"),
    shortDescription = text("product_short_desc"),
    description = text("product_description"),
    quantity = double("product_quantity").toInt(),
    active = bool("product_status", true),
    featured = bool("product_featured", false),
    image = text("product_image"),
    thumbnail = text("product_thumb_image"),
    rating = double("product_rating"),
    reviewCount = double("product_review_count").toInt(),
    ingredients = text("product_ingredients"),
    nutrition = text("product_nutritional_facts"),
    sku = text("product_id_sku"),
)

fun DocumentSnapshot.toCategory() = Category(
    id = id,
    name = text("name"),
    description = text("description"),
    image = text("catimage"),
    active = bool("active", true),
)

fun DocumentSnapshot.toOrder(): Order {
    val payment = get("payment") as? Map<*, *>
    val items = (get("items") as? List<*>).orEmpty().mapNotNull { raw ->
        val m = raw as? Map<*, *> ?: return@mapNotNull null
        OrderItem(
            productId = m["product_id"].asText(),
            name = m["product_name"].asText(),
            image = m["product_thumb_image"].asText(),
            price = m["product_price"].asDouble(),
            quantity = m["number"].asDouble().toInt(),
            total = m["total_price"].asDouble(),
        )
    }
    return Order(
        id = id,
        status = OrderStatus.fromRaw(get("status") as? String),
        placedAt = millis("placed_at"),
        amount = (get("amount") as? Number)?.toDouble(),
        quantity = (get("quantity") as? Number)?.toInt(),
        items = items,
        deliveryAddress = text("delivery_address"),
        paymentMethod = payment?.get("method")?.asText(),
        paidAt = millis("paid_at"),
        shippedAt = millis("shipped_at"),
        deliveredAt = millis("delivered_at"),
        cancelledAt = millis("cancelled_at"),
    )
}

/** Items stored by the 2018 app under orders/{id}/{id}. */
fun DocumentSnapshot.toLegacyOrderItem(): OrderItem {
    val quantity = double("number").toInt().coerceAtLeast(1)
    val total = double("total_price")
    return OrderItem(
        productId = id,
        name = text("product_name").ifBlank { "Item" },
        image = "",
        price = total / quantity,
        quantity = quantity,
        total = total,
    )
}

fun DocumentSnapshot.toReview() = Review(
    id = id,
    productId = text("prodId"),
    userId = text("user_id"),
    userName = text("user_name").ifBlank { "Customer" },
    text = text("review"),
    rating = double("rating").toInt().coerceIn(0, 5),
    time = millis("time"),
)

fun DocumentSnapshot.toUserProfile(fallbackEmail: String = "") = UserProfile(
    id = id,
    name = text("name"),
    surname = text("surname"),
    email = text("email").ifBlank { fallbackEmail },
    phone = text("phoneNumber"),
    address = text("address"),
    image = text("thumb_image").ifBlank { text("image") },
)

fun DocumentSnapshot.toChatMessage() = ChatMessage(
    id = id,
    text = text("message"),
    from = text("from"),
    time = millis("time"),
    seen = bool("seen", false),
)
