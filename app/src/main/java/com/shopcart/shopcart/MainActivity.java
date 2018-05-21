package com.shopcart.shopcart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.lid.lib.LabelImageView;
import com.like.LikeButton;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPickerListener;
import com.shawnlin.numberpicker.NumberPicker;
import com.shopcart.shopcart.Utils.GridSpacingItemDecoration;
import com.shopcart.shopcart.Utils.Utils;
import com.shopcart.shopcart.models.Product;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ss.com.bannerslider.banners.Banner;
import ss.com.bannerslider.banners.DrawableBanner;
import ss.com.bannerslider.events.OnBannerClickListener;
import ss.com.bannerslider.views.BannerSlider;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    FirestoreRecyclerAdapter adapter;
    private TextView numberProducts;
    private FirebaseFirestore database;
    private String androidId;
    private ArrayList<Product> products;
    private ProgressBar progressBar;
    private ImageView goToCart;
    private ArcProgress arcProgress;
    //starting for the first time
    public static final String GettingStarted = "GettingStarted";

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;
    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");

        check();
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        database = FirebaseFirestore.getInstance();
        if (auth.getCurrentUser() != null)
            androidId = auth.getCurrentUser().getUid();
        products = new ArrayList<>();

        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View action_bar_view = inflater.inflate(R.layout.custom_tool, null);
        assert actionBar != null;
        actionBar.setCustomView(action_bar_view);
        goToCart = action_bar_view.findViewById(R.id.toolbar_cart);
        ImageView drawerToggle = action_bar_view.findViewById(R.id.drawerToggle);
        progressBar = action_bar_view.findViewById(R.id.progressBar2);
        numberProducts = action_bar_view.findViewById(R.id.tv_number_of_products);
        goToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
            }
        });
        slider();

        //Main screen recyclerView
        recyclerView = findViewById(R.id.mainRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        int spanCount = 2;
        int spacing = 10;
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        getProducts();

        NavigationView navigationView = findViewById(R.id.navigationMenu);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        LinearLayout linearLayout = navigationView.findViewById(R.id.logout);

        if (auth.getCurrentUser() != null) {
            Utils.setNavigationView(
                    navigationView,
                    drawerLayout,
                    auth,
                    drawerToggle,
                    MainActivity.this,
                    linearLayout
            );

            if (Utils.header != null) {
                ImageView img = Utils.header.findViewById(R.id.userProfileImgHeader);
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chooseImage(MainActivity.this);
                    }
                });

                arcProgress = Utils.header.findViewById(R.id.arc_progress);
            }
        }


    }

    public void slider() {
        BannerSlider bannerSlider = (BannerSlider) findViewById(R.id.banner_slider1);
        List<Banner> banners = new ArrayList<>();
        //add banner using image url
        //  banners.add(new RemoteBanner("Put banner image url here ..."));
        //add banner using resource drawable
        bannerSlider.setOnBannerClickListener(new OnBannerClickListener() {
            @Override
            public void onClick(int position) {

            }
        });
        banners.add(new DrawableBanner(R.drawable.im));
        banners.add(new DrawableBanner(R.drawable.fruits));
        banners.add(new DrawableBanner(R.drawable.grocery));
        banners.add(new DrawableBanner(R.drawable.supermarket));
        bannerSlider.setBanners(banners);
    }


    public void getProducts() {

        Query query = FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("product_status", true)
                .limit(50);

        final FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Product, ProductHolder>(options) {

            @Override
            public ProductHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorites_single_item, null, false);
                return new ProductHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ProductHolder holder, final int position, final Product model) {
                final int[] n = {1};

                holder.price.setText(String.format("$%s", model.getProduct_price()));
                holder.name.setText(model.getProduct_name());

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, ViewProductActivity.class);
//                        Pair[] pairs = new Pair[3];
//                        pairs[0] = new Pair<View,String>(holder.product_image,"productImage");
//                        pairs[1] = new Pair<View,String>(holder.name,"productName");
//                        pairs[2] = new Pair<View,String>(holder.price,"productPrice");
//
//                        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,pairs);
                        intent.putExtra("product_id", options.getSnapshots().getSnapshot(position).getId());
                        startActivity(intent);
                    }
                });

                holder.numberPicker.setMaxValue(model.getProduct_quantity());

                holder.numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        if (newVal == holder.numberPicker.getMaxValue()) {
                            Toast.makeText(MainActivity.this, getString(R.string.msg_toast_max_value), Toast.LENGTH_LONG).show();
                        } else {
                            n[0] = newVal;
                        }
                    }
                });

                if(model.getProduct_quantity()==0){
                    holder.addToCart.setEnabled(false);
                    holder.addToCart.setBackgroundResource(R.drawable.gray_add_to_cart_bg);
                    holder.numberPicker.setVisibility(View.INVISIBLE);
                   // holder.labelImageView.setLabelText("Out of Stock");
                  //  holder.labelImageView.setLabelBackgroundColor(Color.RED);
                  //  holder.labelImageView.setLabelTextColor(Color.WHITE);
                }else{
                    holder.addToCart.setEnabled(true);
                    holder.addToCart.setBackgroundResource(R.drawable.blue_bg);
                    holder.numberPicker.setVisibility(View.VISIBLE);
                 //   holder.labelImageView.setLabelText("");
                 //   holder.labelImageView.setLabelBackgroundColor(Color.WHITE);
                 //   holder.labelImageView.setLabelTextColor(Color.WHITE);
                }

                if (model.getProduct_image() != null) {
                    Picasso.with(getApplicationContext()).load(model.getProduct_thumb_image()).networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.product_image, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getApplicationContext()).load(model.getProduct_thumb_image()).into(holder.product_image);
                                }
                            });
                }

                holder.addToCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        model.setId(options.getSnapshots().getSnapshot(position).getId());
                        Utils.addToCart(model.getId(), n[0], model.getProduct_name(), numberProducts, goToCart, progressBar, androidId, MainActivity.this);
                    }
                });
                String id = options.getSnapshots().getSnapshot(position).getId();
                Utils.favorites(database,auth,id,MainActivity.this,holder.fav_button);

            }
        };

        recyclerView.setAdapter(adapter);
    }

    private static class ProductHolder extends RecyclerView.ViewHolder {
        TextView price, name;
        Button addToCart;
        View view;
        LikeButton fav_button;
        LabelImageView product_image;
        NumberPicker numberPicker;

        ProductHolder(View itemView) {
            super(itemView);
            view = itemView;

            price = view.findViewById(R.id.product_price);
            name = view.findViewById(R.id.product_name);
            addToCart = view.findViewById(R.id.add_to_cart);
            numberPicker = view.findViewById(R.id.number_picker);
            product_image = view.findViewById(R.id.product_image);
            fav_button = view.findViewById(R.id.fav_button_heart);
            numberPicker.setMinValue(1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        user = auth.getCurrentUser();
        if (user != null) {
            adapter.startListening();
            Utils.getCart(androidId, this, numberProducts);
        } else {
            String value = settings.getString("key", "");
            if (value.equalsIgnoreCase("true")) {
                Intent intent = new Intent(this, RegisterAndLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, GetStartedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void check() {
        SharedPreferences shared = getSharedPreferences(GettingStarted, Context.MODE_PRIVATE);
        String check = (shared.getString("finalFinished", ""));

        if (check.equalsIgnoreCase("true")) {
            Toast.makeText(this, check, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, GetStartedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            startActivity(intent);
        }

    }

    public void chooseImage(Activity activity) {
        Intent gallery_intent = new Intent();
        gallery_intent.setType("image/*");
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        if (gallery_intent.resolveActivity(getPackageManager()) != null) {
            activity.startActivityForResult(Intent.createChooser(gallery_intent, "Select Image"), 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.uploadImage(data, requestCode, resultCode, this, auth, arcProgress);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // save RecyclerView state
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = recyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // restore RecyclerView state
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

}
