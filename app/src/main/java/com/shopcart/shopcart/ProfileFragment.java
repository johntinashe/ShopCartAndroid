package com.shopcart.shopcart;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.shopcart.shopcart.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore user;

    private TextView name;
    private TextView email;
    private TextView membership;
    private TextView phone_number;
    private TextView address;
    private ImageView changeProfile;
    private View view;
    private CircleImageView circleImageView;
    private Activity activity;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        user = FirebaseFirestore.getInstance();

        name = view.findViewById(R.id.profile_name);
        email = view.findViewById(R.id.profile_phone_email);
        phone_number = view.findViewById(R.id.profile_phone_number);
        address = view.findViewById(R.id.profile_address);
        membership = view.findViewById(R.id.profile_membership);
        changeProfile = view.findViewById(R.id.change_profile_image);
        circleImageView = view.findViewById(R.id.circleImageView);
        activity = getActivity();
        getUserProfile();
        return view;
    }


    public void getUserProfile() {

        if (auth.getCurrentUser() != null) {
            user.collection("users").document(auth.getCurrentUser().getUid())
                    .addSnapshotListener(activity, new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, final FirebaseFirestoreException e) {
                            if(documentSnapshot.exists()){
                                final User user = documentSnapshot.toObject(User.class);
                                name.setText(String.format("%s %s", user.getName(), user.getSurname()));
                                phone_number.setText(user.getPhoneNumber());
                                address.setText(user.getAddress());
                                membership.setText(user.getMembership());
                                email.setText(auth.getCurrentUser().getEmail());
                                Picasso.with(getContext()).load(user.getThumb_image()).placeholder(R.drawable.avatar).networkPolicy(NetworkPolicy.OFFLINE).into(circleImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getContext()).load(user.getThumb_image()).placeholder(R.drawable.avatar).into(circleImageView);
                                    }
                                });
                            }else {
                                TSnackbar snackbar = TSnackbar.make(view.findViewById(R.id.profile_fragment), e.getMessage(), 5000);
                                snackbar.setActionTextColor(Color.WHITE);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(getResources().getColor(R.color.red));
                                TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                                textView.setTextColor(Color.WHITE);
                                snackbar.show();
                            }
                        }
                    });
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Activity activity = getActivity();
               if(activity != null){
                   chooseImage(activity);
               }
            }
        });
    }

    public void chooseImage(Activity activity){
        Intent gallery_intent = new Intent();
        gallery_intent.setType("image/*");
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        if(gallery_intent.resolveActivity(activity.getPackageManager()) != null){
            activity.startActivityForResult(Intent.createChooser(gallery_intent,"Select Image"),1);
        }
    }

}
