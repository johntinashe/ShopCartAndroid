package com.shopcart.shopcart

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

class ShopCartApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ORDERS_CHANNEL,
            getString(R.string.channel_orders),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = getString(R.string.channel_orders_description) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val ORDERS_CHANNEL = "orders"
    }
}

val Context.container: AppContainer get() = (applicationContext as ShopCartApp).container

@Composable
fun appContainer(): AppContainer = LocalContext.current.container
