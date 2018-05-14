package com.shopcart.shopcart;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.shopcart.shopcart.Utils.Utils;
import com.shopcart.shopcart.adapters.MessageAdapter;
import com.shopcart.shopcart.models.Message;
import com.shopcart.shopcart.models.User;
import com.stfalcon.chatkit.messages.MessageInput;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messagesView;
    private List<Message> messagesList;
    private MessageAdapter messageAdapter;
    private String mId;
    private String current_user;

    private FirebaseAuth auth;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        messagesView = findViewById(R.id.messageRecyclerView);
        messagesList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        current_user = null;

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        if(auth.getCurrentUser()!= null)
            current_user= auth.getCurrentUser().getUid();

        MessageInput input = findViewById(R.id.inputMessage);

        messageAdapter = new MessageAdapter(messagesList,ChatActivity.this,current_user ,mId);
        messagesView.setHasFixedSize(true);
        layoutManager.setStackFromEnd(true);
        messagesView.setLayoutManager(layoutManager);
        messagesView.setAdapter(messageAdapter);

        loadMessages();

        input.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message
                send_message(input.toString());
                return true;
            }
        });

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

        TextView toolbarTitle = action_bar_view.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Customer Service");

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        database.collection("users").document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                      User user = documentSnapshot.toObject(User.class);
                      Map<String,String> map = new HashMap<>();
                      map.put("username", user.getName() + " " + user.getSurname());
                      database.collection("messages").document(auth.getCurrentUser().getUid())
                              .set(map);
                    }
                });
    }

    private void send_message(String s) {

        if(!TextUtils.isEmpty(s)){
            if (!Utils.testForConnection(this)){
                Toast.makeText(this, "Please connect to the internet", Toast.LENGTH_SHORT).show();
            }else {

                Map messageMap = new HashMap();
                messageMap.put("message", s);
                messageMap.put("seen",false);
                messageMap.put("type","text");
                messageMap.put("from",current_user);
                messageMap.put("mId",mId);
                messageMap.put("time",FieldValue.serverTimestamp());

                database.collection("messages").document(auth.getCurrentUser().getUid()).collection("userQueries").add(messageMap);

            }

        }else{
            Toast.makeText(ChatActivity.this,"You cant send a blank message", Toast.LENGTH_LONG).show();
        }
    }

    private void loadMessages() {
        database.collection("messages").document(auth.getCurrentUser().getUid()).collection("userQueries")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()){
                            if(documentChange.getType() == DocumentChange.Type.ADDED) {
                                Message mes = documentChange.getDocument().toObject(Message.class);
                                messagesList.add(mes);
                                messagesView.smoothScrollToPosition(messagesView.getAdapter().getItemCount()-1);
                                messageAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

    }
}
