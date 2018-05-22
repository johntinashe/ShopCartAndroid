package com.shopcart.shopcart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.shopcart.shopcart.Utils.BottomNavigationViewHelper;
import com.shopcart.shopcart.Utils.Utils;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private PaymentsFragment paymentsFragment;
    private ProfileFragment profileFragment;
    private OrdersFragment ordersFragment;
    private ContactUsFragment contactUsFragment;
    private BottomNavigationView bottomNavigationView;
    private ImageView cart;

    private FirebaseAuth mAuth;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

        toolbarTitle = action_bar_view.findViewById(R.id.toolbarTitle);

        ImageView drawerToggle = action_bar_view.findViewById(R.id.drawerToggle);
        cart = action_bar_view.findViewById(R.id.toolbar_cart);
        cart.setVisibility(View.INVISIBLE);
        cart.setClickable(false);
        TextView tv = action_bar_view.findViewById(R.id.tv_number_of_products);
        tv.setVisibility(View.INVISIBLE);
        drawerToggle.setImageResource(R.drawable.ic_action_back);
        drawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        contactUsFragment = new ContactUsFragment();
        paymentsFragment = new PaymentsFragment();
        ordersFragment = new OrdersFragment();
        profileFragment = new ProfileFragment();

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent i = getIntent();
        if (i != null) {
            String string = i.getStringExtra("fromWhere");
            if (string != null) {
                if (string.equals("fromProfile")) {
                    setfragment(profileFragment);
                    bottomNavigationView.setSelectedItemId(R.id.profile);
                } else if (string.equals("fromContactUs")) {
                    setfragment(contactUsFragment);
                    bottomNavigationView.setSelectedItemId(R.id.contact_us);
                } else if (string.equals("fromPayments")) {
                    setfragment(paymentsFragment);
                    bottomNavigationView.setSelectedItemId(R.id.payments);
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.payments: {
                setfragment(paymentsFragment);
                toolbarTitle.setText("Payments");
                cart.setVisibility(View.INVISIBLE);
                return true;
            }
            case R.id.orders: {
                setfragment(ordersFragment);
                toolbarTitle.setText("Orders");
                cart.setVisibility(View.INVISIBLE);
                return true;
            }
            case R.id.profile: {
                setfragment(profileFragment);
                toolbarTitle.setText("My Profile");
                cart.setVisibility(View.VISIBLE);
                cart.setImageResource(R.drawable.ic_action_pen);
                cart.setClickable(true);
                cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(),EditProfileActivity.class));
                    }
                });
                return true;
            }
            case R.id.contact_us: {
                setfragment(contactUsFragment);
                toolbarTitle.setText("Services");
                cart.setVisibility(View.INVISIBLE);
                return true;
            }
            default:
                return false;
        }
    }


    public void setfragment(android.support.v4.app.Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.framelayout, fragment).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.uploadImage(data,requestCode,resultCode,HomeActivity.this,mAuth,null);
    }
}
