package com.shopcart.shopcart.ui.checkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shopcart.shopcart.R
import com.shopcart.shopcart.appContainer
import com.shopcart.shopcart.data.Order
import com.shopcart.shopcart.data.OrderRepository
import com.shopcart.shopcart.data.OrderStatus
import com.shopcart.shopcart.ui.components.AppIcon
import com.shopcart.shopcart.ui.components.asMoney
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentViewModel(private val orderId: String, private val orders: OrderRepository) : ViewModel() {
    val order: StateFlow<Order?> =
        orders.order(orderId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var checking by mutableStateOf(false)
        private set
    var timedOut by mutableStateOf(false)
        private set

    init {
        // Paynow also notifies the server directly; polling is the fallback
        // for when that callback is slow. ~3 minutes, every 5 seconds.
        viewModelScope.launch {
            repeat(36) {
                delay(5_000)
                if (order.value?.status != OrderStatus.PendingPayment && order.value != null) return@launch
                runCatching { orders.checkPayment(orderId) }
            }
            timedOut = true
        }
    }

    fun checkNow() {
        if (checking) return
        viewModelScope.launch {
            checking = true
            runCatching { orders.checkPayment(orderId) }
            checking = false
        }
    }
}

@Composable
fun PaymentScreen(
    orderId: String,
    instructions: String?,
    onDone: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val container = appContainer()
    val vm = viewModel(key = orderId) { PaymentViewModel(orderId, container.orders) }
    val order by vm.order.collectAsStateWithLifecycle()
    val status = order?.status ?: OrderStatus.PendingPayment

    BackHandler { onDone(orderId) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(Modifier.widthIn(max = 420.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                when (status) {
                    OrderStatus.PendingPayment, OrderStatus.Unknown -> {
                        CircularProgressIndicator(Modifier.size(56.dp))
                        Spacer(Modifier.height(24.dp))
                        Title("Waiting for payment")
                        Body(
                            instructions?.takeIf { it.isNotBlank() }
                                ?: "Approve the payment on your phone, or finish paying on the Paynow page. This screen updates automatically.",
                        )
                        order?.amount?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it.asMoney(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(24.dp))
                        OutlinedButton(onClick = vm::checkNow, enabled = !vm.checking) {
                            Text(if (vm.checking) "Checking…" else "I've paid – check now")
                        }
                        if (vm.timedOut) {
                            Spacer(Modifier.height(8.dp))
                            Body("Still waiting? It can take a few minutes. You can leave this screen; we'll notify you when the payment is confirmed.")
                        }
                        TextButton(onClick = { onDone(orderId) }) { Text("View order") }
                    }

                    OrderStatus.PaymentFailed -> {
                        AppIcon(R.drawable.ic_error, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Title("Payment didn't go through")
                        Body("The payment was cancelled or declined. Nothing was charged. Your cart is still saved.")
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Try again") }
                    }

                    else -> {
                        AppIcon(R.drawable.ic_check_circle, null, Modifier.size(72.dp), tint = Color(0xFF1B6B4A))
                        Spacer(Modifier.height(16.dp))
                        Title("Payment received")
                        Body("Thanks! Your order is confirmed and we're preparing it now.")
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { onDone(orderId) }, modifier = Modifier.fillMaxWidth()) { Text("View order") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Title(text: String) {
    Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
}

@Composable
private fun Body(text: String) {
    Spacer(Modifier.height(8.dp))
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}
