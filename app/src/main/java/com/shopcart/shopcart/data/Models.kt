package com.shopcart.shopcart.data

data class Product(
    val id: String,
    val name: String,
    val categoryId: String,
    val price: Double,
    val shortDescription: String = "",
    val description: String = "",
    val quantity: Int = 0,
    val active: Boolean = true,
    val featured: Boolean = false,
    val image: String = "",
    val thumbnail: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val ingredients: String = "",
    val nutrition: String = "",
    val sku: String = "",
) {
    val inStock: Boolean get() = quantity > 0
    val listImage: String get() = thumbnail.ifBlank { image }
}

data class Category(
    val id: String,
    val name: String,
    val description: String = "",
    val image: String = "",
    val active: Boolean = true,
)

data class CartLine(val product: Product, val quantity: Int) {
    val total: Double get() = product.price * quantity
}

data class OrderItem(
    val productId: String,
    val name: String,
    val image: String,
    val price: Double,
    val quantity: Int,
    val total: Double,
)

enum class OrderStatus(val raw: String, val label: String) {
    PendingPayment("pending_payment", "Awaiting payment"),
    PaymentFailed("payment_failed", "Payment failed"),
    New("new", "Paid · preparing"),
    Shipped("shipped", "On the way"),
    Delivered("delivered", "Delivered"),
    Cancelled("cancelled", "Cancelled"),
    Unknown("", "Processing");

    companion object {
        fun fromRaw(raw: String?): OrderStatus = entries.firstOrNull { it.raw == raw } ?: Unknown
    }
}

data class Order(
    val id: String,
    val status: OrderStatus,
    val placedAt: Long,
    val amount: Double?,
    val quantity: Int?,
    val items: List<OrderItem>,
    val deliveryAddress: String,
    val paymentMethod: String?,
    val paidAt: Long = 0,
    val shippedAt: Long = 0,
    val deliveredAt: Long = 0,
    val cancelledAt: Long = 0,
) {
    /** Orders from the 2018 app kept items in a subcollection instead of `items`. */
    val isLegacy: Boolean get() = items.isEmpty() && paymentMethod == null
}

data class Review(
    val id: String,
    val productId: String,
    val userId: String,
    val userName: String,
    val text: String,
    val rating: Int,
    val time: Long,
)

data class UserProfile(
    val id: String,
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val image: String = "",
) {
    val fullName: String get() = listOf(name, surname).filter { it.isNotBlank() }.joinToString(" ")
}

data class ChatMessage(
    val id: String,
    val text: String,
    val from: String,
    val time: Long,
    val seen: Boolean,
)

enum class PaymentMethod(val raw: String, val label: String, val needsPhone: Boolean) {
    EcoCash("ecocash", "EcoCash", true),
    OneMoney("onemoney", "OneMoney", true),
    Web("web", "Paynow checkout (cards, ZIPIT, other wallets)", false),
}

data class CheckoutResult(
    val orderId: String,
    val amount: Double,
    val redirectUrl: String?,
    val instructions: String?,
)
