package com.shopcart.shopcart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.lid.lib.LabelImageView;
import com.like.LikeButton;
import com.shawnlin.numberpicker.NumberPicker;
import com.shopcart.shopcart.Utils.GridSpacingItemDecoration;
import com.shopcart.shopcart.Utils.Utils;
import com.shopcart.shopcart.models.Product;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class AllProductsActivity extends AppCompatActivity {

    public static final String MyPREFERENCES = "MyPrefsAll";
    public static final String Val = "priceKey";
    public static final String Name = "nameKey";
    SharedPreferences sharedpreferences;
    boolean sort_price, sort_name;
    ImageView sortButton;
    private RecyclerView recyclerView;
    private String androidId;
    private ImageView goToCart;
    private TextView numberProducts, toolbarTitle;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private View action_bar_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        androidId = auth.getCurrentUser().getUid();

        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        action_bar_view = inflater.inflate(R.layout.custom_tool, null);
        assert actionBar != null;
        actionBar.setCustomView(action_bar_view);
        goToCart = action_bar_view.findViewById(R.id.toolbar_cart);
        ImageView drawerToggle = action_bar_view.findViewById(R.id.drawerToggle);
        progressBar = action_bar_view.findViewById(R.id.progressBar2);
        sortButton = action_bar_view.findViewById(R.id.all_products_sort);
        sortButton.setVisibility(View.VISIBLE);
        sortButton.setClickable(true);
        numberProducts = action_bar_view.findViewById(R.id.tv_number_of_products);
        goToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        int spanCount = 2;
        int spacing = 10;
        recyclerView = findViewById(R.id.all_products_RecyclerView);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);


        NavigationView navigationView = findViewById(R.id.navigationMenu);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        LinearLayout linearLayout = navigationView.findViewById(R.id.logout);

        Utils.setNavigationView(
                navigationView,
                drawerLayout,
                auth,
                drawerToggle,
                AllProductsActivity.this,
                linearLayout
        );

        sharedpreferences = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        final boolean val = sharedpreferences.getBoolean(Val, false);
        final String nm = sharedpreferences.getString(Name, "");


        if (val) {
            if (!nm.equalsIgnoreCase("") && nm.equalsIgnoreCase("PRICE")) {
                sort_price = true;
            } else if (!nm.equalsIgnoreCase("") && nm.equalsIgnoreCase("NAME")) {
                sort_name = true;
            }
        } else {
            sort_price = false;
            sort_name = false;
        }

        showMenu();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.getCart(androidId, this, numberProducts);
        getProducts();
        toolbarTitle = action_bar_view.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("All Products");
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void getProducts() {

        sharedpreferences = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final PopupMenu popup = new PopupMenu(this, sortButton);
        popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());

        Query query;
        if (sort_price) {
            query = FirebaseFirestore.getInstance()
                    .collection("products")
                    .whereEqualTo("product_status", true)
                    .orderBy("product_price", Query.Direction.ASCENDING);
        } else if (sort_name) {
            query = FirebaseFirestore.getInstance()
                    .collection("products")
                    .whereEqualTo("product_status", true)
                    .orderBy("product_name", Query.Direction.ASCENDING);
        } else {
            query = FirebaseFirestore.getInstance()
                    .collection("products")
                    .whereEqualTo("product_status", true);
        }
        final FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Product, AllProductsActivity.ProductHolder>(options) {

            @Override
            public AllProductsActivity.ProductHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorites_single_item, null, false);
                return new AllProductsActivity.ProductHolder(view);
            }

            @Override
            protected void onBindViewHolder(final AllProductsActivity.ProductHolder holder, final int position, final Product model) {
                final int[] n = {1};

                holder.price.setText(String.format("$%s", model.getProduct_price()));
                holder.name.setText(model.getProduct_name());

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(AllProductsActivity.this, ViewProductActivity.class);
                        intent.putExtra("product_id", options.getSnapshots().getSnapshot(position).getId());
                        startActivity(intent);
                    }
                });

                holder.numberPicker.setMaxValue(model.getProduct_quantity());

                holder.numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        n[0] = newVal;
                    }
                });

                if (model.getProduct_quantity() == 0) {
                    holder.addToCart.setEnabled(false);
                    holder.addToCart.setBackgroundResource(R.drawable.gray_add_to_cart_bg);
                    holder.numberPicker.setVisibility(View.INVISIBLE);
                    // holder.labelImageView.setLabelText("Out of Stock");
                    //  holder.labelImageView.setLabelBackgroundColor(Color.RED);
                    //  holder.labelImageView.setLabelTextColor(Color.WHITE);
                } else {
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
                        Utils.addToCart(model.getId(), n[0], model.getProduct_name(), numberProducts, goToCart, progressBar, androidId, AllProductsActivity.this);
                    }
                });
                String id = options.getSnapshots().getSnapshot(position).getId();
                Utils.favorites(db, auth, id, AllProductsActivity.this, holder.fav_button);

            }
        };

        recyclerView.setAdapter(adapter);
    }

    public void showMenu() {

        sharedpreferences = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = sharedpreferences.edit();

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu popup = new PopupMenu(AllProductsActivity.this, sortButton);
                popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());

                final boolean val = sharedpreferences.getBoolean(Val, false);
                final String nm = sharedpreferences.getString(Name, "");
                MenuItem item1 = popup.getMenu().getItem(0);
                MenuItem item2 = popup.getMenu().getItem(1);

                if (val) {
                    if (!nm.equalsIgnoreCase("") && nm.equalsIgnoreCase("PRICE")) {
                        item1.setChecked(val);
                        sort_price = true;
                    } else if (!nm.equalsIgnoreCase("") && nm.equalsIgnoreCase("NAME")) {
                        item2.setChecked(val);
                        sort_name = true;
                    }
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.sort_by_price: {
                                if (item.isChecked()) {
                                    item.setChecked(false);
                                    edit.putBoolean(Val, false);
                                    edit.putString(Name, "");
                                    sort_price = false;
                                    getProducts();
                                    adapter.startListening();
                                    adapter.notifyDataSetChanged();
                                    edit.apply();
                                } else {
                                    item.setChecked(true);
                                    edit.putBoolean(Val, true);
                                    edit.putString(Name, "PRICE");
                                    sort_price = true;
                                    sort_name = false;
                                    getProducts();
                                    adapter.startListening();
                                    adapter.notifyDataSetChanged();
                                    edit.apply();
                                }
                                return true;
                            }
                            case R.id.sort_by_name: {
                                if (item.isChecked()) {
                                    item.setChecked(false);
                                    edit.putBoolean(Val, false);
                                    edit.putString(Name, "");
                                    sort_name = false;
                                    getProducts();
                                    adapter.startListening();
                                    adapter.notifyDataSetChanged();
                                    edit.apply();
                                } else {
                                    item.setChecked(true);
                                    edit.putBoolean(Val, true);
                                    sort_name = true;
                                    sort_price = false;
                                    getProducts();
                                    adapter.startListening();
                                    adapter.notifyDataSetChanged();
                                    edit.putString(Name, "NAME");
                                    edit.apply();
                                }
                                return true;
                            }
                            default:
                                return false;

                        }

                    }
                });
                popup.show();
            }
        });
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
}
