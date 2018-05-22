package com.shopcart.shopcart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.shopcart.shopcart.Utils.Utils;
import com.shopcart.shopcart.models.Category;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class CategoriesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private String androidId;
    private ImageView goToCart;
    private TextView numberProducts,infoTv;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView toolbarTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        androidId = auth.getCurrentUser().getUid();

        final LayoutInflater inflater =(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View action_bar_view = inflater.inflate(R.layout.custom_tool ,null);
        assert actionBar != null;
        actionBar.setCustomView(action_bar_view);
        goToCart = action_bar_view.findViewById(R.id.toolbar_cart);
        toolbarTitle = action_bar_view.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("All Products");
        ImageView drawerToggle = action_bar_view.findViewById(R.id.drawerToggle);
        numberProducts = action_bar_view.findViewById(R.id.tv_number_of_products);
        goToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),CartActivity.class));
            }
        });

        infoTv = findViewById(R.id.infoTv);
        recyclerView = findViewById(R.id.categoriesRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
//        int spanCount = 2;
//        int spacing = 20;
//        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));

        NavigationView navigationView = findViewById(R.id.navigationMenu);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        LinearLayout linearLayout = navigationView.findViewById(R.id.logout);

        Utils.setNavigationView(
                navigationView,
                drawerLayout,
                auth,
                drawerToggle,
                CategoriesActivity.this,
                linearLayout
        );
    }


    public void getCategories(){
        Query query = FirebaseFirestore.getInstance()
                .collection("categories")
                .whereEqualTo("active", true);

        final FirestoreRecyclerOptions<Category> options = new FirestoreRecyclerOptions.Builder<Category>()
                .setQuery(query, Category.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Category ,CategoryViewHolder>(options){

            @Override
            public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.single_category_item,parent ,false);
                return new CategoryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final CategoryViewHolder holder, int position, final Category model) {
                holder.name.setText(model.getName());
               // holder.nbProducts.setText(String.format("%d", model.getNumber_of_product()));
                if(!model.getCatimage().equalsIgnoreCase("default") && model.getCatimage() != null) {
                    Picasso.with(getApplicationContext()).load(model.getCatimage()).networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.categoryImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getApplicationContext()).load(model.getCatimage())
                                            .into(holder.categoryImage);
                                }
                            });
                }
                final String id = options.getSnapshots().getSnapshot(position).getId();
                db.collection("products").whereEqualTo("product_category_id",id)
                  .addSnapshotListener(CategoriesActivity.this, new EventListener<QuerySnapshot>() {
                      @Override
                      public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                       if(!documentSnapshots.isEmpty()){
                           holder.nbProducts.setText(String.format("%d", documentSnapshots.size()));
                       }else{
                           holder.nbProducts.setText("0");
                       }
                      }
                  });

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CategoryDialog bottomSheetFragment = new CategoryDialog();
                        bottomSheetFragment.setId(id,androidId);
                        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                    }
                });


            }
        };

        recyclerView.setAdapter(adapter);

    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder{
       TextView name, nbProducts;
       ImageView categoryImage;
        View view;
        CategoryViewHolder(View itemView) {
            super(itemView);
            view=itemView;

            name = view.findViewById(R.id.category_name);
            nbProducts = view.findViewById(R.id.category_number_of_products);
            categoryImage = view.findViewById(R.id.category_image);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.getCart(androidId,this,numberProducts);
        getCategories();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
