package com.shopcart.shopcart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
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

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.lid.lib.LabelImageView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.shopcart.shopcart.Utils.GridSpacingItemDecoration;
import com.shopcart.shopcart.Utils.Utils;
import com.shopcart.shopcart.models.Favorite;
import com.shopcart.shopcart.models.Product;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView fav_recycler_view;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirestoreRecyclerAdapter adapter;

    //empty widgets
    private TextView em1, em2;
    private CircleImageView img;
    private Button empBtn;
    private ImageView goToCart;
    private TextView toolbarTitle;

    //navigation drawer setup
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LinearLayout linearLayout;
    private ArcProgress arcProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);


        db = FirebaseFirestore.getInstance();
        auth =FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");

        fav_recycler_view = findViewById(R.id.favorites_recycler_view);
        fav_recycler_view.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        int spanCount = 2;
        int spacing = 10;
        fav_recycler_view.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        fav_recycler_view.setLayoutManager(layoutManager);

        //empty widgets
        em1 = findViewById(R.id.emptyTv1);
        em2 = findViewById(R.id.emptyTV2);
        img = findViewById(R.id.emptyImg);
        empBtn = findViewById(R.id.empty_back_to_home);

        getFavorites();
        final LayoutInflater inflater =(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View action_bar_view = inflater.inflate(R.layout.custom_tool ,null);
        assert actionBar != null;
        actionBar.setCustomView(action_bar_view);
        goToCart = action_bar_view.findViewById(R.id.toolbar_cart);
        ImageView drawerToggle = action_bar_view.findViewById(R.id.drawerToggle);
        TextView numberProducts = action_bar_view.findViewById(R.id.tv_number_of_products);
        ProgressBar progressBar = action_bar_view.findViewById(R.id.progressBar2);
        toolbarTitle = action_bar_view.findViewById(R.id.toolbarTitle);
        goToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),CartActivity.class));
            }
        });
        String androidId  = auth.getCurrentUser().getUid();

        Utils.getCart(androidId,this,numberProducts);

        db.collection("users").document(auth.getCurrentUser().getUid()).collection("favorites")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if(documentSnapshots.isEmpty()){
                            toolbarTitle.setText("Favorites (0)");
                        }else{
                            if(documentSnapshots.size()>1){
                                toolbarTitle.setText("Favorites ("+ documentSnapshots.size()+")");
                            }else{
                                toolbarTitle.setText("Favorite ("+ documentSnapshots.size()+")");
                            }
                        }
                    }
                });

        //setting up navigation drawer
        navigationView = findViewById(R.id.navigationMenu);
        drawerLayout = findViewById(R.id.fav_drawer_layout);
        linearLayout = findViewById(R.id.logout);
        Utils.setNavigationView(navigationView,drawerLayout,auth,drawerToggle,FavoritesActivity.this,linearLayout);

        if( Utils.header != null){
            ImageView img =  Utils.header.findViewById(R.id.userProfileImgHeader);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setImage(FavoritesActivity.this);
                }
            });

            arcProgress = Utils.header.findViewById(R.id.arc_progress);
        }
    }

    public void getFavorites(){

        Query query = db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("favorites");

        final FirestoreRecyclerOptions<Favorite> options = new FirestoreRecyclerOptions.Builder<Favorite>()
                .setQuery(query, Favorite.class)
                .build();

        db.collection("users").document(auth.getCurrentUser().getUid()).collection("favorites")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if(documentSnapshots.isEmpty()){
                            em1.setVisibility(View.VISIBLE);
                            em2.setVisibility(View.VISIBLE);
                            img.setVisibility(View.VISIBLE);
                            empBtn.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.FadeIn)
                                    .duration(700)
                                    .playOn(em1);
                            YoYo.with(Techniques.FadeIn)
                                    .duration(1000)
                                    .playOn(em2);
                            YoYo.with(Techniques.FadeIn)
                                    .duration(1200)
                                    .playOn(img);
                            YoYo.with(Techniques.FadeIn)
                                    .duration(1500)
                                    .playOn(empBtn);
                        }else{
                            em1.setVisibility(View.INVISIBLE);
                            em2.setVisibility(View.INVISIBLE);
                            img.setVisibility(View.INVISIBLE);
                            empBtn.setVisibility(View.INVISIBLE);
                        }
                    }
                });

        adapter = new FirestoreRecyclerAdapter<Favorite,FavoritesViewHolder>(options){

            @Override
            public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorites_single_item,null,false);
                return new FavoritesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final FavoritesViewHolder holder, int position, final Favorite model) {
                //favorite button
                db.collection("users").document(auth.getCurrentUser().getUid()).collection("favorites")
                        .document(model.getProduct_id())
                        .addSnapshotListener(FavoritesActivity.this, new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                if(documentSnapshot.exists() && e ==null){
                                    holder.fav_button.setLiked(true);
                                }else{
                                    holder.fav_button.setLiked(false);
                                }
                            }
                        });

                db.collection("products").document(options.getSnapshots().getSnapshot(position).getId())
                        .addSnapshotListener(FavoritesActivity.this, new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                if(documentSnapshot.exists()){
                                    final Product product = documentSnapshot.toObject(Product.class);
                                    holder.setName(product.getProduct_name());
                                    holder.setProduct_price(product.getProduct_price());

                                    if(product.getProduct_quantity()==0){
                                        holder.addToCart.setEnabled(false);
                                        holder.addToCart.setBackgroundResource(R.drawable.gray_add_to_cart_bg);
                                        holder.labelImageView.setLabelText("Out of Stock");
                                        holder.labelImageView.setLabelBackgroundColor(Color.RED);
                                        holder.labelImageView.setLabelTextColor(Color.WHITE);
                                    }else{
                                        holder.addToCart.setEnabled(true);
                                        holder.addToCart.setBackgroundResource(R.drawable.blue_bg);
                                        holder.labelImageView.setLabelText("");
                                        holder.labelImageView.setLabelBackgroundColor(Color.WHITE);
                                        holder.labelImageView.setLabelTextColor(Color.WHITE);
                                    }
                                    Picasso.with(getApplicationContext()).load(product.getProduct_thumb_image()).networkPolicy(NetworkPolicy.OFFLINE )
                                            .into(holder.labelImageView, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(getApplicationContext()).load(product.getProduct_thumb_image()).into(holder.labelImageView);
                                                }
                                            });
                                }
                            }
                        });

                holder.fav_button.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        db.collection("users").document(auth.getCurrentUser().getUid()).collection("favorites")
                                .document(model.getProduct_id())
                                .delete()
                                .addOnCompleteListener( new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            adapter.notifyDataSetChanged();
                                            // Toast.makeText(FavoritesActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                                        }else{
                                            if(task.getException() != null)
                                                Toast.makeText(FavoritesActivity.this, "Sorry something happened "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
            }

        };
        fav_recycler_view.setAdapter(adapter);

    }

    public static class FavoritesViewHolder extends RecyclerView.ViewHolder{

        TextView name,product_price;
        View view;
        LikeButton fav_button;
        Button addToCart;
        LabelImageView labelImageView;
        FavoritesViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            name = view.findViewById(R.id.product_name);
            fav_button = view.findViewById(R.id.fav_button_heart);
            product_price = view.findViewById(R.id.product_price);
            addToCart = view.findViewById(R.id.add_to_cart);
            labelImageView = view.findViewById(R.id.product_image);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setProduct_price(float product_price) {
            this.product_price.setText(product_price+" DT");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void setImage(Activity activity){
        Intent gallery_intent = new Intent();
        gallery_intent.setType("image/*");
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(gallery_intent,"Select Image"),1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.uploadImage(data,requestCode,resultCode,this,auth,arcProgress);
    }
}
