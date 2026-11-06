package com.shopcart.shopcart.ui.cart

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shopcart.shopcart.R
import com.shopcart.shopcart.appContainer
import com.shopcart.shopcart.data.CartLine
import com.shopcart.shopcart.data.CartRepository
import com.shopcart.shopcart.ui.components.EmptyState
import com.shopcart.shopcart.ui.components.LoadingBox
import com.shopcart.shopcart.ui.components.ProductImage
import com.shopcart.shopcart.ui.components.QuantityStepper
import com.shopcart.shopcart.ui.components.asMoney
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(private val cart: CartRepository, private val uid: String) : ViewModel() {
    val lines: StateFlow<List<CartLine>?> =
        cart.lines(uid).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setQuantity(line: CartLine, quantity: Int) {
        viewModelScope.launch { runCatching { cart.setQuantity(uid, line.product, quantity) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(onCheckout: () -> Unit, onOpenProduct: (String) -> Unit, onBrowse: () -> Unit) {
    val container = appContainer()
    val vm = viewModel { CartViewModel(container.cart, container.auth.requireUid()) }
    val lines by vm.lines.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Your cart") }) }) { padding ->
        val list = lines
        when {
            list == null -> LoadingBox(Modifier.padding(padding))
            list.isEmpty() -> EmptyState(
                R.drawable.ic_cart,
                "Your cart is empty",
                "Browse the shop and add something you like.",
                Modifier.padding(padding),
                actionLabel = "Start shopping",
                onAction = onBrowse,
            )
            else -> Column(Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(list, key = { it.product.id }) { line ->
                        CartRow(line, onOpen = { onOpenProduct(line.product.id) }, onQuantity = { vm.setQuantity(line, it) })
                    }
                }
                val total = list.sumOf { it.total }
                val count = list.sumOf { it.quantity }
                val overStock = list.any { it.quantity > it.product.quantity }
                Surface(tonalElevation = 3.dp) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Subtotal · $count items", modifier = Modifier.weight(1f))
                            Text(total.asMoney(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        if (overStock) {
                            Text(
                                "Some items have less stock than you selected.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onCheckout,
                            enabled = !overStock,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                        ) { Text("Checkout") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartRow(line: CartLine, onOpen: () -> Unit, onQuantity: (Int) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(
                url = line.product.listImage,
                modifier = Modifier.size(76.dp).clip(RoundedCornerShape(10.dp)).clickable(onClick = onOpen),
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(line.product.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    line.product.price.asMoney() + " each",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                QuantityStepper(value = line.quantity, onChange = onQuantity, max = line.product.quantity, min = 0)
            }
            Text(line.total.asMoney(), fontWeight = FontWeight.Bold)
        }
    }
}
