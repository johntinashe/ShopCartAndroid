package com.shopcart.shopcart.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shopcart.shopcart.MainActivity
import com.shopcart.shopcart.R
import com.shopcart.shopcart.ShopCartApp
import com.shopcart.shopcart.container
import kotlinx.coroutines.launch

/** Receives order-status pushes sent by the onOrderStatus Cloud Function. */
class ShopCartMessagingService : FirebaseMessagingService() {

    // Deprecated in favour of onRegistered (installation IDs); kept until the
    // backend sends by installation ID. See AuthRepository.syncDeviceToken.
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        container.appScope.launch { runCatching { container.auth.syncDeviceToken(token) } }
    }

    // Called only while the app is in the foreground; in the background the
    // system shows the notification payload itself.
    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification ?: return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val orderId = message.data["orderId"]
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (orderId != null) putExtra(MainActivity.EXTRA_ORDER_ID, orderId)
        }
        val pending = PendingIntent.getActivity(
            this,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val built = NotificationCompat.Builder(this, ShopCartApp.ORDERS_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        NotificationManagerCompat.from(this).notify(orderId.hashCode(), built)
    }
}
