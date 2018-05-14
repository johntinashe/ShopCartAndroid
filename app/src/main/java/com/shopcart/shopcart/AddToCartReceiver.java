package com.shopcart.shopcart;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shopcart.shopcart.models.Product;

import java.util.HashMap;
import java.util.Map;

public class AddToCartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent.getAction() != null)
        if(intent.getAction().equals("ADD_TO_CART")){
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            int i = intent.getIntExtra("notid",0);
            if(notificationManager != null && i >0 ){
                notificationManager.cancel(i);
            }
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            if(intent.getStringExtra("product_id") != null){
                final String id = intent.getStringExtra("product_id");
                db.collection("products").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                       if(task.isSuccessful()){
                          if(task.getResult().exists()){
                              Map<String,Object> map = new HashMap<>();
                              final Product product = task.getResult().toObject(Product.class);
                              map.put("number",1);
                              map.put("total_price",product.getProduct_price());
                              db.collection("carts").document("164150776121ddcc").collection("products")
                                      .document(id)
                                      .set(map)
                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                                          @Override
                                          public void onComplete(@NonNull Task<Void> task) {
                                              if(task.isSuccessful()){
                                                  Toast.makeText(context, product.getProduct_name() + " has been added to cart", Toast.LENGTH_SHORT).show();
                                              }
                                          }
                                      });
                          }
                       }else{
                           Toast.makeText(context, "Sorry adding failed", Toast.LENGTH_SHORT).show();
                       }
                    }
                });
            }
        }
    }
}
