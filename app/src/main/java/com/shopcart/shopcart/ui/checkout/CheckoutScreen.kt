package com.shopcart.shopcart.ui.checkout

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.functions.FirebaseFunctionsException
import com.shopcart.shopcart.R
import com.shopcart.shopcart.appContainer
import com.shopcart.shopcart.data.AuthRepository
import com.shopcart.shopcart.data.CartLine
import com.shopcart.shopcart.data.CartRepository
import com.shopcart.shopcart.data.CheckoutResult
import com.shopcart.shopcart.data.OrderRepository
import com.shopcart.shopcart.data.PaymentMethod
import com.shopcart.shopcart.data.UserProfile
import com.shopcart.shopcart.ui.components.AppIcon
import com.shopcart.shopcart.ui.components.LoadingBox
import com.shopcart.shopcart.ui.components.asMoney
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val ZW_MOBILE = Regex("^07[1-8]\\d{7}$")

data class CheckoutState(val lines: List<CartLine>, val profile: UserProfile)

class CheckoutViewModel(
    cart: CartRepository,
    auth: AuthRepository,
    private val orders: OrderRepository,
) : ViewModel() {
    private val uid = auth.requireUid()

    val state: StateFlow<CheckoutState?> = combine(
        cart.lines(uid),
        auth.profile(uid).catch { emit(UserProfile(uid)) },
    ) { lines, profile -> CheckoutState(lines, profile) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var result by mutableStateOf<CheckoutResult?>(null)
        private set

    fun pay(method: PaymentMethod, phone: String, address: String) {
        if (busy) return
        viewModelScope.launch {
            busy = true
            error = null
            try {
                result = orders.placeOrder(method, phone.replace(" ", ""), address)
            } catch (e: FirebaseFunctionsException) {
                error = e.message ?: "Payment could not be started."
            } catch (e: Exception) {
                error = "Payment could not be started. Check your connection and try again."
            } finally {
                busy = false
            }
        }
    }

    fun consumeResult() { result = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(onBack: () -> Unit, onPaymentStarted: (CheckoutResult) -> Unit) {
    val container = appContainer()
    val context = LocalContext.current
    val vm = viewModel { CheckoutViewModel(container.cart, container.auth, container.orders) }
    val state by vm.state.collectAsStateWithLifecycle()

    var method by rememberSaveable { mutableStateOf(PaymentMethod.EcoCash) }
    var phone by rememberSaveable { mutableStateOf<String?>(null) }
    var address by rememberSaveable { mutableStateOf<String?>(null) }

    // Prefill from the profile once it arrives.
    LaunchedEffect(state?.profile) {
        state?.profile?.let {
            if (phone == null) phone = it.phone
            if (address == null) address = it.address
        }
    }

    LaunchedEffect(vm.result) {
        val result = vm.result ?: return@LaunchedEffect
        vm.consumeResult()
        // Web checkout: hand off to Paynow in the browser; the payment screen
        // keeps watching the order until it's paid.
        result.redirectUrl?.let { url ->
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
        }
        onPaymentStarted(result)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = { IconButton(onClick = onBack) { AppIcon(R.drawable.ic_arrow_back, "Back") } },
            )
        },
    ) { padding ->
        val s = state
        if (s == null) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        val total = s.lines.sumOf { it.total }
        val phoneValue = phone.orEmpty()
        val addressValue = address.orEmpty()
        val phoneValid = !method.needsPhone || ZW_MOBILE.matches(phoneValue.replace(" ", ""))
        val canPay = s.lines.isNotEmpty() && addressValue.isNotBlank() && phoneValid && !vm.busy

        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SectionCard("Order summary") {
                    s.lines.forEach { line ->
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text("${line.quantity} × ${line.product.name}", modifier = Modifier.weight(1f))
                            Text(line.total.asMoney())
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row {
                        Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(total.asMoney(), fontWeight = FontWeight.Bold)
                    }
                }

                SectionCard("Delivery address") {
                    OutlinedTextField(
                        value = addressValue,
                        onValueChange = { address = it },
                        placeholder = { Text("House number, street, suburb, city") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                SectionCard("Pay with") {
                    Column(Modifier.selectableGroup()) {
                        PaymentMethod.entries.forEach { m ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(selected = method == m, onClick = { method = m }, role = Role.RadioButton)
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = method == m, onClick = null)
                                Text(m.label, modifier = Modifier.padding(start = 12.dp))
                            }
                        }
                    }
                    if (method.needsPhone) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phoneValue,
                            onValueChange = { phone = it },
                            label = { Text("${method.label} number") },
                            placeholder = { Text("0771234567") },
                            isError = phoneValue.isNotBlank() && !phoneValid,
                            supportingText = {
                                Text("You'll get a prompt on this phone to enter your PIN.")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(
                            "You'll be taken to Paynow's secure page to finish paying.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { vm.pay(method, phoneValue, addressValue) },
                    enabled = canPay,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(52.dp),
                ) {
                    if (vm.busy) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Pay ${total.asMoney()}")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
