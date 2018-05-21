package com.shopcart.shopcart;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.shopcart.shopcart.Utils.Utils;
import com.shopcart.shopcart.models.Order;

import java.util.Date;
import java.util.Objects;

public class TrackOrderActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText orderIdEditText;
    private TextView orderIdTv;
    private TextView placedDate;
    private TextView placed_successfully;
    private TextView shipped_successfully;
    private TextView delivered_successfully;

    private ImageView place;
    private ImageView shipped ;
    private ImageView delivered;
    private ImageView searchBtn;

    private String orderId;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_order);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");

        final LayoutInflater inflater =(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View action_bar_view = inflater.inflate(R.layout.custom_tool ,null);
        assert actionBar != null;
        actionBar.setCustomView(action_bar_view);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView toolbarTitle = action_bar_view.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Track Order");

        ImageView drawerToggle = action_bar_view.findViewById(R.id.drawerToggle);
        ImageView cart = action_bar_view.findViewById(R.id.toolbar_cart);
        cart.setVisibility(View.INVISIBLE);
        TextView tv = action_bar_view.findViewById(R.id.tv_number_of_products);
        tv.setVisibility(View.INVISIBLE);
        drawerToggle.setImageResource(R.drawable.ic_action_back);
        drawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        orderIdEditText = findViewById(R.id.search_order);
        orderIdTv = findViewById(R.id.track_order_id);
        placedDate = findViewById(R.id.track_order_date_placed);
        placed_successfully = findViewById(R.id.track_order_placed_successfully);
        place  = findViewById(R.id.track_order_placed);
        shipped  = findViewById(R.id.track_order_shipped);
        delivered = findViewById(R.id.track_order_delivered);
        shipped_successfully = findViewById(R.id.track_order_shipped_tv);
        delivered_successfully = findViewById(R.id.track_order_delivered_tv);
        progressBar = findViewById(R.id.track_order_progress);
        searchBtn = findViewById(R.id.search_order_btn);
        searchBtn.setOnClickListener(this);

    }


    public void searchOrder() {
        orderId = orderIdEditText.getText().toString();
        if(orderId.equals("")){
            showError("Please enter an Order Id");
        }else {
            progressBar.setVisibility(View.VISIBLE);
            db.collection("orders").document(orderId).addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if(documentSnapshot.exists()){
                        Order order = documentSnapshot.toObject(Order.class);
                        if(order.getUser_id().equals(auth.getCurrentUser().getUid())){
                            progressBar.setVisibility(View.GONE);
                            orderIdTv.setText(String.format("Order Id: %s", documentSnapshot.getId()));
                            String dateStringPlaced = DateFormat.format("dd/MM/yyyy", new Date(order.getPlaced_at())).toString();
                            placedDate.setText(String.format("Order placed successfully on: %s", dateStringPlaced));
                            refresh(order,dateStringPlaced);
                        }else {
                            progressBar.setVisibility(View.GONE);
                            showError("Order appears to be registered in a different user name.");
                        }


                    }else {
                        showError("No results found \uD83D\uDE22!");
                    }
                }

                private void refresh(Order order, String dateStringPlaced) {
                    switch (order.getStatus()){
                        case "new" : {
                            place.setBackgroundResource(R.drawable.circlebackgroundyellow);
                            placed_successfully.setText(String.format("Placed Successfully on %s", dateStringPlaced));
                            shipped.setBackgroundResource(R.drawable.circle_disabled);
                            shipped_successfully.setText("");
                            delivered.setBackgroundResource(R.drawable.circle_disabled);
                            delivered_successfully.setText("");
                            break;
                        }
                        case "shipped" : {
                            place.setBackgroundResource(R.drawable.circlebackgroundyellow);
                            placed_successfully.setText(String.format("Placed Successfully on %s", dateStringPlaced));
                            shipped.setBackgroundResource(R.drawable.circlebackgroundpurple);
                            String dateStringShipped = DateFormat.format("dd/MM/yyyy", new Date(order.getShipped_at())).toString();
                            shipped_successfully.setText(String.format("Shipped Successfully on %s", dateStringShipped));
                            delivered.setBackgroundResource(R.drawable.circle_disabled);
                            delivered_successfully.setText("");
                            break;

                        }
                        case "delivered" : {
                            place.setBackgroundResource(R.drawable.circlebackgroundyellow);
                            placed_successfully.setText(String.format("Placed Successfully on %s", dateStringPlaced));
                            shipped.setBackgroundResource(R.drawable.circlebackgroundpurple);
                            String dateStringShipped = DateFormat.format("dd/MM/yyyy", new Date(order.getShipped_at())).toString();
                            shipped_successfully.setText(String.format("Shipped Successfully on %s", dateStringShipped));
                            String dateStringDelivered = DateFormat.format("dd/MM/yyyy", new Date(order.getDelivered_at())).toString();
                            delivered.setBackgroundResource(R.drawable.circlebackgroundgreen);
                            delivered_successfully.setText(String.format("Delivered Successfully on %s", dateStringDelivered));
                            break;
                        }
                        default:{
                            place.setBackgroundResource(R.drawable.circle_disabled);
                            placed_successfully.setText("");
                            shipped.setBackgroundResource(R.drawable.circle_disabled);
                            shipped_successfully.setText("");
                            delivered.setBackgroundResource(R.drawable.circle_disabled);
                            delivered_successfully.setText("");
                            break;
                        }
                    }
                }

            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();

        if (intent != null && orderIdEditText.getText() != null) {
            String id = intent.getStringExtra("order_id");
            orderIdEditText.setText(id);
            searchOrder();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.search_order_btn) {
            searchOrder();
        }
    }

    public void showError(String s) {
        TSnackbar snackbar = TSnackbar.make(findViewById(R.id.content), s, 5000);
        snackbar.setActionTextColor(Color.WHITE);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.red));
        TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }
}
