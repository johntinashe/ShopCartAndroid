package com.shopcart.shopcart;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.shopcart.shopcart.models.Product;
import com.shopcart.shopcart.models.Purchase;
import com.shopcart.shopcart.models.User;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReceiptDialog extends BottomSheetDialogFragment {

    FirebaseFirestore db;
    FirebaseAuth auth;
    String id;
    float total;
    ImageView barcode;
    FirestoreRecyclerAdapter adapter;
    RecyclerView recyclerView;
    TextView textViewTotal,label,recId ,date ,userName;
    Long millisecond;

    public ReceiptDialog() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        final View view = inflater.inflate(R.layout.receipt_dialog, container, false);
        barcode = view.findViewById(R.id.barcode);
        String text= id;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.CODE_128,700,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            barcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        textViewTotal = view.findViewById(R.id.receipt_total);
        DecimalFormat df = new DecimalFormat("#.##");
        textViewTotal.setText(String.format("$%s", df.format(total)));
        label = view.findViewById(R.id.numberPurchased);

        recId = view.findViewById(R.id.receipt_id);
        recId.setText(id);

        date = view.findViewById(R.id.receipt_date);
        String dateString = DateFormat.format("dd/MM/yyyy", new Date(millisecond)).toString();
        date.setText(dateString);

        userName = view.findViewById(R.id.receipt_name);

        recyclerView = view.findViewById(R.id.receipt_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getItems();
        getUserName();
        return view;
    }

    public void getUserName(){
        if(getActivity() != null && auth.getCurrentUser() != null)
        db.collection("users").document(auth.getCurrentUser().getUid()).addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    User user = documentSnapshot.toObject(User.class);
                    userName.setText(String.format("%s Hi,", user.getName()));
                }
            }
        });
    }

    public void setId(String id , float total , Long millisecond){
        this.id = id;
        this.total = total;
        this.millisecond = millisecond;
    }

    public void getItems(){

        Query query = db.collection("orders").document(id).collection(id);

        final FirestoreRecyclerOptions<Purchase> options = new FirestoreRecyclerOptions.Builder<Purchase>()
                .setQuery(query, Purchase.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Purchase,ReceiptHolder>(options){

            @Override
            public ReceiptHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.receipt_single_item,parent,false);
                return new ReceiptHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ReceiptHolder holder, int position, Purchase model) {

                label.setText(String.format("you have purchased %d items in our store.Thank you!", options.getSnapshots().size()));
                Map<String ,Object> map = new HashMap<>();
                map =( Map<String,Object>)model.getData();
                if(map != null){
                    final DecimalFormat df = new DecimalFormat("#.##");
                    holder.total.setText("$"+df.format((double)map.get("total_price")));

                    if(getActivity() != null){
                        final Map<String, Object> finalMap = map;
                        db.collection("products").document(model.getId()).addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                             if(documentSnapshot.exists()){
                                 Product product = documentSnapshot.toObject(Product.class);
                                 Picasso.with(getContext()).load(product.getProduct_thumb_image()).into(holder.circularImageView);
                                 long qty =(long) finalMap.get("number");
                                 holder.name.setText(product.getProduct_name() + "(x"+ qty +")");
                             }
                            }
                        });
                    }
                }
            }
        };
        recyclerView.setAdapter(adapter);

    }

    private static class ReceiptHolder extends RecyclerView.ViewHolder {
        TextView total,name;
        View view;
        CircularImageView circularImageView;

        ReceiptHolder(View itemView) {
            super(itemView);
            view = itemView;

            circularImageView = view.findViewById(R.id.receiptImg);
            total = view.findViewById(R.id.receiptPrice);
            name = view.findViewById(R.id.receiptName);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
