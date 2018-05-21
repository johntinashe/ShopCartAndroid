package com.shopcart.shopcart;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class StockNotifications extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessaging" ;
    private FirebaseAuth mAuth;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null) {
            final String notiTitle = remoteMessage.getData().get("name");
            final String notiBody = remoteMessage.getData().get("msg");
            final String notiAction = remoteMessage.getData().get("clickaction");
            final String prodId = remoteMessage.getData().get("product_id");
            String url = remoteMessage.getData().get("product_thumb");


            Intent addToCartIntent = new Intent(this, AddToCartReceiver.class);
            addToCartIntent.setAction("ADD_TO_CART");
            addToCartIntent.putExtra("product_id", prodId);
            addToCartIntent.putExtra("user_id",mAuth.getCurrentUser().getUid());

            Bitmap bitmap = getBitmapFromURL(url);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(notiTitle)
                            .setAutoCancel(true)
                            .setLargeIcon(bitmap)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentText(notiBody);
            mBuilder.setVibrate(new long[] { 500, 500});
            mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            int mNotificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            addToCartIntent.putExtra("notid",mNotificationId);
            PendingIntent addPendingIntent =
                    PendingIntent.getBroadcast(this, mNotificationId+1, addToCartIntent, 0);

            mBuilder.addAction(R.drawable.ic_action_cart, "Add To Cart",addPendingIntent);
            Intent resultIntent = new Intent(notiAction);
            resultIntent.putExtra("product_id",prodId);

            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            getApplicationContext(),
                            mNotificationId,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            assert mNotifyMgr != null;
            mNotifyMgr.notify(mNotificationId, mBuilder.build());


            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            }

            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            }
        }


    }

    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
          //  StrictMode.setThreadPolicy(policy);
            connection.connect();
            InputStream input = connection.getInputStream();

            return  BitmapFactory.decodeStream(input);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
