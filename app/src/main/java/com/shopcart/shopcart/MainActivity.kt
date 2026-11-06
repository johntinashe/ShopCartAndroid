package com.shopcart.shopcart

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.shopcart.shopcart.ui.ShopCartRoot
import com.shopcart.shopcart.ui.theme.ShopCartTheme

class MainActivity : ComponentActivity() {

    /** Order to open when launched from a notification. */
    private val pendingOrderId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingOrderId.value = intent?.getStringExtra(EXTRA_ORDER_ID)
        setContent {
            ShopCartTheme {
                ShopCartRoot(
                    pendingOrderId = pendingOrderId.value,
                    onPendingOrderHandled = { pendingOrderId.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(EXTRA_ORDER_ID)?.let { pendingOrderId.value = it }
    }

    companion object {
        const val EXTRA_ORDER_ID = "orderId"
    }
}
