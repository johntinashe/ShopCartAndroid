package com.shopcart.shopcart;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.androidadvance.topsnackbar.TSnackbar;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.shopcart.shopcart.Utils.GetLastSeen;
import com.shopcart.shopcart.Utils.Utils;
import com.shopcart.shopcart.models.Favorite;
import com.shopcart.shopcart.models.Payment;
import com.stripe.model.Charge;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class PaymentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirestoreRecyclerAdapter adapter;
    private View viw;
    private Activity activity;

    public PaymentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payments, container, false);
        auth  = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        activity = getActivity();


        recyclerView = view.findViewById(R.id.payments_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getPayments();
        viw = view;
        return view;
    }


    public void getPayments(){
        Query query = db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("payments")
                .orderBy("time", Query.Direction.DESCENDING);

        final FirestoreRecyclerOptions<Payment> options = new FirestoreRecyclerOptions.Builder<Payment>()
                .setQuery(query, Payment.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<Payment,PaymentViewHolder>(options){

            @Override
            public PaymentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.payment_single_item,parent,false);
                return new PaymentViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final PaymentViewHolder holder, final int position, final Payment model) {
                String id  = options.getSnapshots().getSnapshot(position).getId();
                Map<String ,Object> map = new HashMap<>();
                map =( Map<String,Object>)model.getCharge();
                if(map != null){
                    if((boolean)map.get("paid")){
                        String checkedMark = "\u2713";
                        int colorGreen = Color.rgb(0,200,83);
                        TextDrawable drawable = TextDrawable.builder()
                                .buildRound(checkedMark,colorGreen);
                        holder.img.setImageDrawable(drawable);
                        holder.divider.setBackgroundColor(colorGreen);
                    }else {
                        int colorRed = Color.rgb(213,0,0);
                        TextDrawable drawable = TextDrawable.builder()
                                .buildRound("X",colorRed);
                        holder.img.setImageDrawable(drawable);
                        holder.divider.setBackgroundColor(colorRed);
                    }
                    holder.id.setText(id);
                    holder.id.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(getActivity() != null) {
                                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Order Id", holder.id.getText().toString());
                                    if (clipboard != null) {
                                        clipboard.setPrimaryClip(clip);
                                        showSuccess("Copied to Clipboard :" +holder.id.getText().toString() );
                                    }
                            }
                        }
                    });
                    holder.setTime((long)map.get("created"),getContext());
                    holder.amount.setText(String.format("$%d", model.getPrice()));

                    holder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ReceiptDialog  receiptDialog = new ReceiptDialog();
                            receiptDialog.setId(options.getSnapshots().getSnapshot(position).getId() , model.getPrice(),model.getTime());
                            if(getFragmentManager() != null)
                            receiptDialog.show(getFragmentManager(),"receipt");
                        }
                    });
                }


            }
        };

        recyclerView.setAdapter(adapter);
    }



    public static class PaymentViewHolder extends RecyclerView.ViewHolder{

        CircularImageView img;
        TextView id, time ,amount;
        View view;
        View divider;
         PaymentViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            img = view.findViewById(R.id.payment_img);
            id = view.findViewById(R.id.payment_id);
            time = view.findViewById(R.id.payment_date);
            amount = view.findViewById(R.id.totalAmount);
            divider = view.findViewById(R.id.divider);
        }

        public void setTime(long t , Context context){
             time.setText(GetLastSeen.getTimeAgo(t, context));
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

    public void showSuccess(String s) {
        TSnackbar snackbar = TSnackbar.make(viw.findViewById(R.id.paymentsFrame), s, 3000);
        snackbar.setActionTextColor(Color.WHITE);
        View snackbarView = snackbar.getView();
        int colorGreen = Color.rgb(0,200,83);
        snackbarView.setBackgroundColor(colorGreen);
        TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }
}
