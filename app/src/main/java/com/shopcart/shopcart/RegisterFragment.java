package com.shopcart.shopcart;


import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.shopcart.shopcart.models.User;

import dmax.dialog.SpotsDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {
    private View view;

    //FirebaseFirestore
    private FirebaseFirestore database;
    private FirebaseAuth auth;


    //edit text
    private EditText surname,name,password,email,address,phoneNumber;
    private Button register;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =inflater.inflate(R.layout.fragment_register, container, false);
        surname = view.findViewById(R.id.tv_surname);
        name = view.findViewById(R.id.tv_name);
        password = view.findViewById(R.id.tv_password);
        email = view.findViewById(R.id.tv_email);
        address = view.findViewById(R.id.tv_address);
        phoneNumber = view.findViewById(R.id.tv_phone_number);
        register = view.findViewById(R.id.registerBtn);
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validator();
            }
        });
        return view;
    }



    public void validator(){
        String sEmail,sPassword,sName,sSurname,sAddress,sPhoneNumber;

        sEmail = email.getText().toString();
        sAddress = address.getText().toString();
        sName = name.getText().toString();
        sPhoneNumber = phoneNumber.getText().toString();
        sSurname = surname.getText().toString();
        sPassword = password.getText().toString();

        if(TextUtils.isEmpty(sEmail) || TextUtils.isEmpty(sPhoneNumber) || TextUtils.isEmpty(sPassword)
                ||TextUtils.isEmpty(sAddress) || TextUtils.isEmpty(sSurname) ||TextUtils.isEmpty(sName))
        {
            TSnackbar snackbar = TSnackbar.make(view.findViewById(R.id.content),R.string.error, 5000);
            snackbar.setActionTextColor(Color.WHITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(getResources().getColor(R.color.red));
            TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackbar.show();
        }else{
            if(sPassword.length()<6){
                TSnackbar snackbar = TSnackbar.make(view.findViewById(R.id.content),R.string.shortpass, 3000);
                snackbar.setActionTextColor(Color.WHITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.gray));
                TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            }else {
               register(sName,sEmail,sPassword,sAddress,sSurname,sPhoneNumber);
            }
        }
    }


    public void register(final String nm , String em, String pass , final String ad , final String sur , final String ph){

        final AlertDialog dialog = new SpotsDialog(getContext(),R.style.Custom);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        auth.createUserWithEmailAndPassword(em, pass).addOnSuccessListener(getActivity(), new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                String token = FirebaseInstanceId.getInstance().getToken();
                User user = new User(nm,ad,ph,sur,token,"Silver","default","default");
                database.collection("users").document(authResult.getUser().getUid())
                        .set(user).addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        if(auth.getCurrentUser()!=null)
                        if(!auth.getCurrentUser().isEmailVerified()){
                            auth.getCurrentUser().sendEmailVerification();
                            TSnackbar snackbar = TSnackbar.make(view.findViewById(R.id.content),R.string.checkEmail, 3000);
                            snackbar.setActionTextColor(Color.WHITE);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(getResources().getColor(R.color.green_bg));
                            TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                            auth.signOut();
                            email.setText("");
                            password.setText("");
                            phoneNumber.setText("");
                            surname.setText("");
                            name.setText("");
                            address.setText("");
                        }
                    }
                });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                TSnackbar snackbar = TSnackbar.make(view.findViewById(R.id.content),e.getMessage(), 5000);
                snackbar.setActionTextColor(Color.WHITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.red));
                TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
                dialog.dismiss();
            }
        });

    }

}
