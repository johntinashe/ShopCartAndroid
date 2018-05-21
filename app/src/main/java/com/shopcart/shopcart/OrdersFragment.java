package com.shopcart.shopcart;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.shopcart.shopcart.models.Order;
import com.shopcart.shopcart.models.Payment;
import com.shopcart.shopcart.models.Product;
import com.shopcart.shopcart.models.Purchase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class OrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirestoreRecyclerAdapter adapter ,recyclerAdapter;
    private Activity activity;

    public OrdersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        activity = getActivity();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.orders_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getOrders(auth.getCurrentUser().getUid());
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    void getOrders(String id) {

        Query query = db.collection("orders").whereEqualTo("user_id",id);

        final FirestoreRecyclerOptions<Order> options = new FirestoreRecyclerOptions.Builder<Order>()
                .setQuery(query, Order.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<Order,OrdersFragment.OrdersViewHolder>(options){

            @Override
            public OrdersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.order_single_item,parent,false);
                return new OrdersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final OrdersViewHolder holder, int position, Order model) {
                final String id = options.getSnapshots().getSnapshot(position).getId();
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                holder.itemsRecyclerView.setLayoutManager(linearLayoutManager);
                getItems(id,holder.itemsRecyclerView);
                holder.id.setText(id);
                recyclerAdapter.startListening();
                db.collection("users").document(auth.getCurrentUser().getUid()).collection("payments")
                        .document(id)
                        .get()
                        .addOnCompleteListener(activity, new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.getResult() != null) {
                                    Payment payment = task.getResult().toObject(Payment.class);
                                    holder.subTotal.setText("$"+(payment.getPrice()-10));
                                    holder.totalAmount.setText("$"+ payment.getPrice()+"");
                                }
                            }
                        });

                holder.track.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(),TrackOrderActivity.class);
                        intent.putExtra("order_id",id);
                        startActivity(intent);
                    }
                });

                switch (model.getStatus()) {
                    case "new" : {
                        holder.img.setImageResource(R.drawable.ic_basket);
                        holder.desc.setText("order placed");
                        break;
                    }
                    case "shipped": {
                        holder.img.setImageResource(R.drawable.ic_shipping);
                        holder.desc.setText("order shipped");
                        break;
                    }
                    case "delivered" : {
                        holder.img.setImageResource(R.drawable.ic_home_outline);
                        holder.desc.setText("order delivered");
                        break;
                    }
                }
            }
        };
        recyclerView.setAdapter(adapter);

    }


    public void getItems(String id, RecyclerView orderv){

        Query query = db.collection("orders").document(id).collection(id);

        final FirestoreRecyclerOptions<Purchase> options = new FirestoreRecyclerOptions.Builder<Purchase>()
                .setQuery(query, Purchase.class)
                .build();

        recyclerAdapter = new FirestoreRecyclerAdapter<Purchase,OrdersFragment.OrderHolder>(options){

            @Override
            public OrdersFragment.OrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.order_item_single,parent,false);
                return new OrdersFragment.OrderHolder(view);
            }

            @Override
            protected void onBindViewHolder(final OrdersFragment.OrderHolder holder, int position, Purchase model) {
                Map<String ,Object> map = new HashMap<>();
                map =( Map<String,Object>)model.getData();
                if(map != null){
                    final DecimalFormat df = new DecimalFormat("#.##");
                    //  holder.total.setText("$"+df.format((double)map.get("total_price")));

                    if(getActivity() != null){
                        final Map<String, Object> finalMap = map;
                        db.collection("products").document(model.getId()).addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                if(documentSnapshot.exists()){
                                    final Product product = documentSnapshot.toObject(Product.class);
                                    long qty =(long) finalMap.get("number");
                                    holder.qty.setText(qty+"");
                                    holder.name.setText(product.getProduct_name());
                                    holder.price.setText("$"+ df.format((double)finalMap.get("total_price")));
                                    holder.priceEach.setText("$"+df.format(product.getProduct_price())+" x ");
                                    Picasso.with(getContext()).load(product.getProduct_thumb_image()).networkPolicy(NetworkPolicy.OFFLINE)
                                            .into(holder.prdImg, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(getContext()).load(product.getProduct_thumb_image()).into(holder.prdImg);
                                                }
                                            });
                                }
                            }
                        });
                    }
                }
            }
        };
        orderv.setAdapter(recyclerAdapter);

    }

    private static class OrderHolder extends RecyclerView.ViewHolder {
        TextView qty, name , price ,priceEach;
        View view;
        ImageView prdImg;

        OrderHolder(View itemView) {
            super(itemView);
            view = itemView;

            qty = view.findViewById(R.id.order_qty);
            name = view.findViewById(R.id.order_item_product_name);
            price = view.findViewById(R.id.order_price_tv);
            priceEach = view.findViewById(R.id.order_product_price);
            prdImg = view.findViewById(R.id.order_product_image);

        }
    }


    public static class OrdersViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        RecyclerView itemsRecyclerView;
        TextView id, desc ,totalAmount ,subTotal;
        View view;
        Button track;

        OrdersViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            img = view.findViewById(R.id.icon_status);
            id = view.findViewById(R.id.order_id);
            desc = view.findViewById(R.id.order_desc);
            totalAmount = view.findViewById(R.id.order_total);
            subTotal = view.findViewById(R.id.sub_total);

            itemsRecyclerView = view.findViewById(R.id.recycler_view_order_items);
            itemsRecyclerView.setHasFixedSize(true);
            track = view.findViewById(R.id.order_track_button);
        }

    }

}
