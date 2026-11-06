package com.shopcart.shopcart.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shopcart.shopcart.R
import com.shopcart.shopcart.appContainer
import com.shopcart.shopcart.data.Order
import com.shopcart.shopcart.data.OrderItem
import com.shopcart.shopcart.data.OrderRepository
import com.shopcart.shopcart.data.OrderStatus
import com.shopcart.shopcart.ui.components.AppIcon
import com.shopcart.shopcart.ui.components.EmptyState
import com.shopcart.shopcart.ui.components.LoadingBox
import com.shopcart.shopcart.ui.components.ProductImage
import com.shopcart.shopcart.ui.components.StatusChip
import com.shopcart.shopcart.ui.components.asDate
import com.shopcart.shopcart.ui.components.asMoney
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class OrdersViewModel(orders: OrderRepository, uid: String) : ViewModel() {
    val orders: StateFlow<List<Order>?> =
        orders.orders(uid).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(onOpenOrder: (String) -> Unit, onBrowse: () -> Unit) {
    val container = appContainer()
    val vm = viewModel { OrdersViewModel(container.orders, container.auth.requireUid()) }
    val orders by vm.orders.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Your orders") }) }) { padding ->
        val list = orders
        when {
            list == null -> LoadingBox(Modifier.padding(padding))
            list.isEmpty() -> EmptyState(
                R.drawable.ic_receipt,
                "No orders yet",
                "When you place an order, you can track it here.",
                Modifier.padding(padding),
                actionLabel = "Start shopping",
                onAction = onBrowse,
            )
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(list, key = { it.id }) { order ->
                    Card(
                        onClick = { onOpenOrder(order.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Order #${order.id.take(8).uppercase()}",
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                )
                                StatusChip(order.status)
                            }
                            Spacer(Modifier.height(6.dp))
                            Row {
                                Text(
                                    order.placedAt.asDate("d MMM yyyy"),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                                val items = order.quantity ?: order.items.sumOf { it.quantity }
                                Text(
                                    listOfNotNull(
                                        if (items > 0) "$items items" else null,
                                        order.amount?.asMoney(),
                                    ).joinToString(" · "),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class OrderDetailViewModel(private val orderId: String, private val orders: OrderRepository) : ViewModel() {
    val order: StateFlow<Order?> =
        orders.order(orderId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    suspend fun legacyItems(): List<OrderItem> = runCatching { orders.legacyItems(orderId) }.getOrDefault(emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(orderId: String, onBack: () -> Unit, onContactSupport: () -> Unit) {
    val container = appContainer()
    val vm = viewModel(key = orderId) { OrderDetailViewModel(orderId, container.orders) }
    val order by vm.order.collectAsStateWithLifecycle()
    var legacyItems by remember { mutableStateOf<List<OrderItem>>(emptyList()) }

    LaunchedEffect(order?.isLegacy) {
        if (order?.isLegacy == true) legacyItems = vm.legacyItems()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order #${orderId.take(8).uppercase()}") },
                navigationIcon = { IconButton(onClick = onBack) { AppIcon(R.drawable.ic_arrow_back, "Back") } },
            )
        },
    ) { padding ->
        val o = order
        if (o == null) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        val items = o.items.ifEmpty { legacyItems }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Timeline(o) }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Items", style = MaterialTheme.typography.titleMedium)
                        items.forEach { item ->
                            Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                ProductImage(item.image, Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
                                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Text(item.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${item.quantity} × ${item.price.asMoney()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(item.total.asMoney())
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Row {
                            Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text((o.amount ?: items.sumOf { it.total }).asMoney(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (o.deliveryAddress.isNotBlank() || o.paymentMethod != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            if (o.deliveryAddress.isNotBlank()) {
                                Text("Delivery address", style = MaterialTheme.typography.titleSmall)
                                Text(o.deliveryAddress, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(12.dp))
                            }
                            o.paymentMethod?.let {
                                Text("Payment", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    when (it) {
                                        "ecocash" -> "EcoCash"
                                        "onemoney" -> "OneMoney"
                                        else -> "Paynow"
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            item {
                OutlinedButton(onClick = onContactSupport, modifier = Modifier.fillMaxWidth()) {
                    AppIcon(R.drawable.ic_chat, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Questions about this order?")
                }
            }
        }
    }
}

@Composable
private fun Timeline(order: Order) {
    val cancelled = order.status == OrderStatus.Cancelled || order.status == OrderStatus.PaymentFailed
    val steps = if (cancelled) {
        listOf(
            "Placed" to order.placedAt,
            order.status.label to (order.cancelledAt.takeIf { it > 0 } ?: order.placedAt),
        )
    } else {
        listOf(
            "Placed" to order.placedAt,
            "Paid" to order.paidAt,
            "Shipped" to order.shippedAt,
            "Delivered" to order.deliveredAt,
        )
    }
    val reached = when {
        cancelled -> 2
        order.status == OrderStatus.Delivered -> 4
        order.status == OrderStatus.Shipped -> 3
        order.status == OrderStatus.New -> 2
        order.isLegacy -> 2
        else -> 1
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Status", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                StatusChip(order.status)
            }
            Spacer(Modifier.height(12.dp))
            steps.forEachIndexed { index, (label, time) ->
                val done = index < reached
                val color = when {
                    cancelled && index == 1 -> MaterialTheme.colorScheme.error
                    done -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    Surface(shape = CircleShape, color = color, modifier = Modifier.size(12.dp)) {}
                    Text(
                        label,
                        fontWeight = if (done) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp).weight(1f),
                    )
                    if (done && time > 0) {
                        Text(
                            time.asDate("d MMM, HH:mm"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
