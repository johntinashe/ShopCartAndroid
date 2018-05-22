package com.shopcart.shopcart;


import android.app.AlertDialog;
import android.content.Intent;
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

import dmax.dialog.SpotsDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    private EditText email,password;
    private View view;

    private FirebaseAuth auth;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =inflater.inflate(R.layout.fragment_login, container, false);

        email = view.findViewById(R.id.login_email);
        password = view.findViewById(R.id.login_password);
        Button loginBtn = view.findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        auth = FirebaseAuth.getInstance();


        return  view;
    }

    private void login() {

        final AlertDialog dialog = new SpotsDialog(getContext(),R.style.Custom);
        dialog.setCanceledOnTouchOutside(false);

        String em = email.getText().toString();
        String pass = password.getText().toString();

        if(TextUtils.isEmpty(em) || TextUtils.isEmpty(pass)){

            TSnackbar snackbar = TSnackbar.make(view.findViewById(R.id.content_login),R.string.error, 5000);
            snackbar.setActionTextColor(Color.WHITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(getResources().getColor(R.color.red));
            TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackbar.show();
        }else {
            if(pass.length()<6){
                TSnackbar snackbar = TSnackbar.make(view.findViewById(R.id.content_login),R.string.shortpass, 5000);
                snackbar.setActionTextColor(Color.WHITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.red));
                TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            }else {
                dialog.dismiss();

                auth.signInWithEmailAndPassword(em, pass).addOnSuccessListener(getActivity(), new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if(auth !=null && getActivity() != null)
                            if (authResult.getUser().isEmailVerified()) {

                                TSnackbar snackbar = TSnackbar.make(view.findViewById(R.id.content_login), String.format("Welcome %s", auth.getCurrentUser().getEmail()), 5000);
                                snackbar.setActionTextColor(Color.WHITE);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(getResources().getColor(R.color.green_bg));
                                TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                                textView.setTextColor(Color.WHITE);
                                snackbar.show();
                                dialog.dismiss();
                                Intent intent = new Intent(getContext(),MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                getActivity().finish();
                                getActivity().startActivity(intent);

                            } else {
                                TSnackbar snackbar = TSnackbar.make(view.findViewById(R.id.content_login), R.string.verify, 5000);
                                snackbar.setActionTextColor(Color.WHITE);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(getResources().getColor(R.color.green_bg));
                                TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                                textView.setTextColor(Color.WHITE);
                                snackbar.show();
                                dialog.dismiss();
                                auth.signOut();
                            }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        TSnackbar snackbar = TSnackbar.make(view.findViewById(R.id.content_login),e.getMessage(), 5000);
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
    }

}
