package com.shopcart.shopcart;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.shopcart.shopcart.Utils.Utils;
import com.shopcart.shopcart.models.CartProduct;
import com.shopcart.shopcart.models.Product;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private ImageView cart;
    private TextView numberProductsToolbar,numberOfProducts,totalToPay;
    private String androidId;
    private FirestoreRecyclerAdapter adapter;
    private RecyclerView cartRecyclerView;
    private FirebaseFirestore database;
    private boolean totalCheck;
    private Button checkout;
    private FirebaseAuth auth;
    private ArcProgress arcProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        androidId = auth.getCurrentUser().getUid();

        final LayoutInflater inflater =(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View action_bar_view = inflater.inflate(R.layout.custom_tool ,null);
        assert actionBar != null;
        actionBar.setCustomView(action_bar_view);
        cart = action_bar_view.findViewById(R.id.toolbar_cart);
        numberProductsToolbar = action_bar_view.findViewById(R.id.tv_number_of_products);
        numberOfProducts = findViewById(R.id.number_of_items);
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setHasFixedSize(true);
        totalToPay = findViewById(R.id.totalToPay);
        checkout = findViewById(R.id.checkoutBtn);

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(Utils.testForConnection(getApplicationContext())){
                   Intent buyIntent = new Intent(getApplicationContext(),PaymentActivity.class);
                   buyIntent.putExtra("purchase_name","First Purchase");
                   buyIntent.putExtra("price",totalToPay.getText().toString().replaceAll("DT",""));
                   startActivity(buyIntent);
               }else {
                   Toast.makeText(CartActivity.this, "Make sure you have internet!", Toast.LENGTH_SHORT).show();
               }
            }
        });

        NavigationView navigationView = findViewById(R.id.navigationMenu);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        ImageView drawerToggle = action_bar_view.findViewById(R.id.drawerToggle);
        LinearLayout linearLayout = findViewById(R.id.logout);

        Utils.setNavigationView(
                navigationView,
                drawerLayout,
                auth,
                drawerToggle,
                CartActivity.this,
                linearLayout
        );

        if( Utils.header != null){
            ImageView img =  Utils.header.findViewById(R.id.userProfileImgHeader);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setImage(CartActivity.this);
                }
            });

            arcProgress = Utils.header.findViewById(R.id.arc_progress);
        }

    }

    public void getCartProducts(){
        Query query = FirebaseFirestore.getInstance()
                .collection("carts")
                .document(androidId)
                .collection("products");

        final FirestoreRecyclerOptions<CartProduct> options = new FirestoreRecyclerOptions.Builder<CartProduct>()
                .setQuery(query, CartProduct.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<CartProduct, ProductHolder>(options){

            @Override
            public ProductHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_single_cart,parent,false);
                return new ProductHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ProductHolder holder, final int position, final CartProduct model) {
                final DocumentReference cartAmount = database.collection("carts").document(androidId);
                final DecimalFormat df = new DecimalFormat("#.##");
                final DocumentReference document  = database.collection("products")
                        .document(options.getSnapshots().getSnapshot(position).getId());
                final FirebaseFirestore db = FirebaseFirestore.getInstance();

                document.get().addOnSuccessListener(CartActivity.this,new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        final Product product = documentSnapshot.toObject(Product.class);
                        holder.name.setText(product.getProduct_name());
                        holder.short_desc.setText(product.getProduct_short_desc());
                        holder.qty_tv.setText(model.getNumber()+"");
                        holder.qty_tv.setSelection(holder.qty_tv.getText().length());
                        final float total = product.getProduct_price() * model.getNumber();
                        getTotal();
                        holder.updateQty.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String n = holder.qty_tv.getText().toString();
                                if(!TextUtils.isEmpty(n)){
                                    int i = Integer.parseInt(n);
                                    if(i>0 && i<=product.getProduct_quantity()){
                                        Map<String,Integer> map = new HashMap<>();
                                        map.put("number",i);
                                        db.collection("carts").document(androidId).collection("products").document(options.getSnapshots()
                                                .getSnapshot(position).getId())
                                                .set(map, SetOptions.merge());

                                    }else if(i<1){
                                        Toast.makeText(CartActivity.this, "Quantity should be greater than 0 ", Toast.LENGTH_SHORT).show();
                                    }else if(i> product.getProduct_quantity()){
                                        Toast.makeText(CartActivity.this, "Quantity should be less than or equal to " + product.getProduct_quantity(), Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    Toast.makeText(CartActivity.this, "Quantity should be greater than 0 ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        holder.totalPrice.setText(df.format(total)+" DT");
                        CartProduct cartProduct = new CartProduct(model.getNumber(),total);
                        cartAmount.collection("products")
                                  .document(documentSnapshot.getId())
                                  .set(cartProduct);
                        Picasso.with(getApplicationContext()).load(product.getProduct_thumb_image()).networkPolicy(NetworkPolicy.OFFLINE)
                                .into(holder.product_cart_imag, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getApplicationContext()).load(product.getProduct_thumb_image()).into(holder.product_cart_imag);
                                    }
                                });
                    }
                });
                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Button yes ,no;

                        final Dialog dialog = new Dialog(CartActivity.this);
                        Rect displayRectangle = new Rect();
                        Window window = getWindow();
                        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

                        LayoutInflater inflater = getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.alert_dialog_custom, null);
                        dialogView.setMinimumWidth((int)(displayRectangle.width() * 0.9f));
                        dialog.setContentView(dialogView);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setCanceledOnTouchOutside(true);
                        dialog.show();

                        yes = dialogView.findViewById(R.id.yesBtn);
                        no = dialogView.findViewById(R.id.noBtn);

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                delete(options.getSnapshots().getSnapshot(position).getId());
                            }
                        });

                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
            }
        };

        cartRecyclerView.setAdapter(adapter);
    }


    public void getTotal(){
        final float[] t = {0};
        final DecimalFormat df = new DecimalFormat("#.##");
        database.collection("carts").document(androidId)
                .collection("products")
                .addSnapshotListener(CartActivity.this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                       if(documentSnapshots != null) {
                           if(documentSnapshots.isEmpty()){
                               totalToPay.setText("0.00 DT");
                               checkout.setClickable(false);
                               checkout.setBackgroundColor(Color.GRAY);
                           }else {
                               for(DocumentChange snapshot : documentSnapshots.getDocumentChanges()){
                                   if(snapshot.getType() == DocumentChange.Type.ADDED){
                                       CartProduct product = snapshot.getDocument().toObject(CartProduct.class);
                                       t[0] = t[0] + product.getTotal_price();
                                       totalToPay.setText(df.format(t[0])+" DT");
                                       checkout.setClickable(true);
                                       checkout.setBackgroundResource(R.drawable.ripple_green_btn);
                                   }
                                   if(snapshot.getType()==DocumentChange.Type.MODIFIED){
                                       CartProduct product = snapshot.getDocument().toObject(CartProduct.class);

                                       t[0] = t[0] + product.getTotal_price();
                                       totalToPay.setText(df.format(t[0])+" DT");
                                       checkout.setClickable(true);
                                       checkout.setBackgroundResource(R.drawable.ripple_green_btn);
                                   }
                                   if(snapshot.getType()==DocumentChange.Type.REMOVED){
                                       CartProduct product = snapshot.getDocument().toObject(CartProduct.class);
                                       t[0] = t[0] - product.getTotal_price();
                                       totalToPay.setText(df.format(t[0])+"");
                                   }

                               }
                           }
                       }
                       }
                });
    }



    public void getCart(){
        Query query = FirebaseFirestore.getInstance()
                .collection("carts")
                .document(androidId)
                .collection("products");

        query.addSnapshotListener(CartActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                   // Toast.makeText(CartActivity.this, "Sorry Something Happened", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert query snapshot to a list of chats
                int n =0;
                assert snapshot != null;
                List<CartProduct> products = snapshot.toObjects(CartProduct.class);
                for(int i=0;i<products.size();i++){
                    n = n + products.get(i).getNumber();
                }
                numberProductsToolbar.setText(String.format("%d",n));
                if(products.size()>1){
                    n=0;
                    for(int i=0;i<products.size();i++){
                        n = n + products.get(i).getNumber();
                    }
                    numberOfProducts.setText(String.format("You have %d items ready in your cart!",n));
                }
                else if(products.size()==1)
                    numberOfProducts.setText(R.string.one_item);
                else
                    numberOfProducts.setText(R.string.empty_cart);
            }
        });

    }

    private static class ProductHolder extends RecyclerView.ViewHolder {
        TextView qty,name,totalPrice,short_desc;
        View view;
        EditText qty_tv;
        ImageView updateQty;
        ImageView deleteBtn;
        ImageView product_cart_imag;
        ProductHolder(View itemView) {
            super(itemView);
            view = itemView;
            name = view.findViewById(R.id.product_name);
            short_desc = view.findViewById(R.id.product_short_desc);
            totalPrice = view.findViewById(R.id.product_price);
            deleteBtn = view.findViewById(R.id.removeCart);
            product_cart_imag = view.findViewById(R.id.product_thumb);

            updateQty = view.findViewById(R.id.updateQuantity);
            qty_tv = view.findViewById(R.id.quantityEditText);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        getCart();
        getCartProducts();
        getTotal();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }



    public void delete(String idProduct){

       database.collection("carts").document(androidId)
               .collection("products")
               .document(idProduct)
               .delete()
               .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                       adapter.notifyDataSetChanged();
                       getTotal();
                   }
               });

    }

    public void setImage(Activity activity){
        Intent gallery_intent = new Intent();
        gallery_intent.setType("image/*");
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(gallery_intent,"Select Image"),1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.uploadImage(data,requestCode,resultCode,this,auth,arcProgress);
    }


}
