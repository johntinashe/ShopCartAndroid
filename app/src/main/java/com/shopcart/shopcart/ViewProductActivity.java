package com.shopcart.shopcart;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.lid.lib.LabelImageView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPickerListener;
import com.shopcart.shopcart.Utils.GetLastSeen;
import com.shopcart.shopcart.Utils.Utils;
import com.shopcart.shopcart.models.Like;
import com.shopcart.shopcart.models.Product;
import com.shopcart.shopcart.models.Review;
import com.shopcart.shopcart.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.messages.MessageInput;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ViewProductActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView goToCart,increase,decrease;
    private TextView numberProducts,price,description,rating,name,toolbarTitle;
    private String androidId;
    private FirebaseFirestore database;
    private MaterialRatingBar ratingBar;
    private RecyclerView recyclerViewReviews, recyclerViewRelatedProducts;
    private EditText value;
    private ProgressBar progressBar;
    private Intent intentProduct;
    private Button addToCartBtn;
    private MessageInput messageInput;
    private String prodId;
    private FirebaseAuth auth;
    private FirestoreRecyclerAdapter adapter,adapterRelatedProducts;
    private LikeButton fav_button;
    private ArcProgress arcProgress;
    private LinearLayout ingredients,facts;
    private TextView ingredientsTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");


        database = FirebaseFirestore.getInstance();
        recyclerViewReviews = findViewById(R.id.viewProductRecycletView);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setHasFixedSize(false);
        recyclerViewReviews.setNestedScrollingEnabled(false);

        final LayoutInflater inflater =(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View action_bar_view = inflater.inflate(R.layout.custom_tool ,null);
        assert actionBar != null;
        actionBar.setCustomView(action_bar_view);
        goToCart = action_bar_view.findViewById(R.id.toolbar_cart);
        ImageView drawerToggle = action_bar_view.findViewById(R.id.drawerToggle);
        numberProducts = action_bar_view.findViewById(R.id.tv_number_of_products);
        progressBar = action_bar_view.findViewById(R.id.progressBar2);
        toolbarTitle = action_bar_view.findViewById(R.id.toolbarTitle);
        goToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),CartActivity.class));
            }
        });


//        String more = "More";
//        String viewAll = "ViewALl";
//        SpannableString contentMore = new SpannableString(more);
//        SpannableString contentAll = new SpannableString(viewAll);
//        contentAll.setSpan(new UnderlineSpan(),0,viewAll.length(),0);
//        contentMore.setSpan(new UnderlineSpan(), 0, more.length(), 0);
//        morePrds.setText(contentMore);
//        viewAllPrds.setText(contentAll);

        auth = FirebaseAuth.getInstance();
        androidId = auth.getCurrentUser().getUid();
        name = findViewById(R.id.view_product_name);
        description = findViewById(R.id.view_product_description);
        ingredientsTv = findViewById(R.id.ingredients_tv);
        price = findViewById(R.id.view_product_price);
        rating = findViewById(R.id.view_product_rating);
        ratingBar = findViewById(R.id.materialRatingBar);
        intentProduct = getIntent();
        prodId = intentProduct.getStringExtra("product_id");
        messageInput = findViewById(R.id.input);
        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                if(input.toString().length()> 0){
                    comment(input.toString());
                    return  true;
                }
                return false;
            }
        });


        //favorite button
        fav_button = findViewById(R.id.fav_button);

        Utils.favorites(database,auth,prodId,this,fav_button);


        //increase decrease buttons and edit text
        increase = findViewById(R.id.plus_btn);
        decrease = findViewById(R.id.minus_btn);
        value = findViewById(R.id.qty_value);

        increase.setOnClickListener(this);
        decrease.setOnClickListener(this);

        //Add to cart button
        addToCartBtn = findViewById(R.id.add_to_btn);
        addToCartBtn.setOnClickListener(this);
        getProductReview();

        NavigationView navigationView = findViewById(R.id.navigationMenu);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        LinearLayout linearLayout = navigationView.findViewById(R.id.logout);

        Utils.setNavigationView(
                navigationView,
                drawerLayout,
                auth,
                drawerToggle,
                ViewProductActivity.this,
                linearLayout
        );

        Utils.getCart(androidId,this,numberProducts);

        if( Utils.header != null){
            ImageView img =  Utils.header.findViewById(R.id.userProfileImgHeader);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setImage(ViewProductActivity.this);
                }
            });

            arcProgress = Utils.header.findViewById(R.id.arc_progress);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        showProduct(intentProduct.getStringExtra("product_id"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        adapterRelatedProducts.stopListening();
    }

    public void showProduct(String id){
        final LabelImageView prdImg = findViewById(R.id.productImage);
        ingredients = findViewById(R.id.ingredients);
        facts = findViewById(R.id.facts);
        database.collection("products").document(id)
                .addSnapshotListener(ViewProductActivity.this,new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if(documentSnapshot.exists()){

                            final Product product = documentSnapshot.toObject(Product.class);

                            if (product.getProduct_nutritional_facts() != null) {
                                facts.setVisibility(View.VISIBLE);
                            }

                            if (product.getProduct_ingredients() != null) {
                                ingredients.setVisibility(View.VISIBLE);
                                ingredientsTv.setText(product.getProduct_ingredients());
                            }

                            name.setText(product.getProduct_name());
                            description.setText(product.getProduct_description());
                            toolbarTitle.setText(product.getProduct_name());
                            price.setText("$" +product.getProduct_price());
                            rating.setText(product.getProduct_rating()+"");
                            ratingBar.setRating(product.getProduct_rating());
                            prdImg.setLabelText(product.getProduct_quantity()+" In Stock");
                            if(product.getProduct_quantity()==0) {
                                prdImg.setLabelBackgroundColor(Color.RED);
                            }else{
                                prdImg.setLabelBackgroundColor(getResources().getColor(R.color.green_bg));
                            }
                            getRelatedProducts(product.getProduct_category_id());
                            adapterRelatedProducts.startListening();
                            Picasso.with(getApplicationContext()).load(product.getProduct_image()).networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(prdImg, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getApplicationContext()).load(product.getProduct_image()).into(prdImg);
                                        }
                                    });
                            getNotification(product.getProduct_quantity());
                        }
                    }
                });
    }

    public void getProductReview() {
        final TextView numberOfReviews = findViewById(R.id.numberOfReviews);
        Query query = FirebaseFirestore.getInstance()
                .collection("reviews")
                .whereEqualTo("prodId",prodId);

        database.collection("reviews").whereEqualTo("prodId",prodId).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(documentSnapshots == null) {
                    numberOfReviews.setText("Reviews 0");
                }else {
                    numberOfReviews.setText(String.format("Reviews (%d)", documentSnapshots.size()));
                }
            }
        });


        final FirestoreRecyclerOptions<Review> options = new FirestoreRecyclerOptions.Builder<Review>()
                .setQuery(query, Review.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Review, ReviewHolder>(options){

            @Override
            public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_layout,null,false);
                return new ViewProductActivity.ReviewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ReviewHolder holder, final int position, Review model) {
                holder.name.setText(model.getUser());
                holder.review.setText(model.getReview());
                holder.ratingBar.setRating(model.getRating());
                holder.ratingBar.setClickable(false);
                holder.ratingBar.setActivated(false);
                holder.time.setText(GetLastSeen.getTimeAgo(model.getTime(),ViewProductActivity.this));

                database.collection("users").document(model.getUser_id()).addSnapshotListener(ViewProductActivity.this,new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                       if(documentSnapshot.exists()){
                          User user = documentSnapshot.toObject(User.class);
                           holder.setProfileImg(user.getThumb_image(),ViewProductActivity.this);
                       }
                    }
                });


                if(model.getUser_id().equals(auth.getCurrentUser().getUid())){
                    holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            final Dialog dialog = new Dialog(ViewProductActivity.this);
                            Rect displayRectangle = new Rect();
                            Window window = getWindow();
                            window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
                            Button yes ,no;
                            LayoutInflater inflater = getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.alert_dialog_custom, null);
                            dialogView.setMinimumWidth((int)(displayRectangle.width() * 0.9f));
                            dialog.setContentView(dialogView);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.setCanceledOnTouchOutside(true);
                            dialog.show();

                            yes = dialogView.findViewById(R.id.yesBtn);
                            no = dialogView.findViewById(R.id.noBtn);

                            yes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                    database.collection("reviews").document(options.getSnapshots().getSnapshot(position).getId())
                                            .delete()
                                            .addOnCompleteListener(ViewProductActivity.this,new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                   if(task.isSuccessful()){
                                                       adapter.notifyDataSetChanged();
                                                       Toast.makeText(ViewProductActivity.this, "Review has been deleted", Toast.LENGTH_SHORT).show();
                                                   }else{
                                                       Toast.makeText(ViewProductActivity.this, "Sorry something happened", Toast.LENGTH_SHORT).show();
                                                   }
                                                }
                                            });
                                }
                            });

                            no.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                            return true;
                        }
                    });
                }

                database.collection("reviews").document(options.getSnapshots().getSnapshot(position).getId())
                        .collection("likes")
                        .document(auth.getCurrentUser().getUid())
                        .addSnapshotListener(ViewProductActivity.this,new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                if(e!=null){
                                    Toast.makeText(ViewProductActivity.this,"Sorry Something happened",Toast.LENGTH_SHORT).show();
                                }else {
                                    if(documentSnapshot.exists()){
                                        holder.likeButton.setLiked(true);
                                    }else{
                                        holder.likeButton.setLiked(false);
                                    }
                                }
                            }
                        });

                holder.likeButton.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        Like like = new Like(auth.getCurrentUser().getUid());
                        database.collection("reviews").document(options.getSnapshots().getSnapshot(position).getId())
                                .collection("likes")
                                .document(auth.getCurrentUser().getUid())
                                .set(like);
                    }
                    @Override
                    public void unLiked(LikeButton likeButton) {
                        database.collection("reviews").document(options.getSnapshots().getSnapshot(position).getId())
                                .collection("likes")
                                .document(auth.getCurrentUser().getUid())
                                .delete();
                    }
                });

                database.collection("reviews").document(options.getSnapshots().getSnapshot(position).getId())
                        .collection("likes")
                        .addSnapshotListener(ViewProductActivity.this,new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                if(!documentSnapshots.isEmpty()){
                                    holder.setNumberOfLikes(documentSnapshots.size());
                                }else{
                                    holder.setNumberOfLikes(0);
                                }
                            }
                        });
            }
        };
        recyclerViewReviews.setAdapter(adapter);
    }

    private static class ReviewHolder extends RecyclerView.ViewHolder {
        TextView review,name,time;
        MaterialRatingBar ratingBar;
        View view;
        CircleImageView profileImg;
        LikeButton likeButton;
        TextView numberOfLikes;

        ReviewHolder(View itemView) {
            super(itemView);
            view = itemView;

            review = view.findViewById(R.id.review_message_tv);
            name = view.findViewById(R.id.review_user);
            time = view.findViewById(R.id.review_time);
            ratingBar = view.findViewById(R.id.materialRatingBar);
            profileImg = view.findViewById(R.id.user_image);
            likeButton = view.findViewById(R.id.fav_button);
            numberOfLikes = view.findViewById(R.id.numberOfLikes);
        }

         void setProfileImg(final String url, final Context context){
            if(!url.equalsIgnoreCase("default")){
                Picasso.with(context).load(url).placeholder(R.drawable.avatar).networkPolicy(NetworkPolicy.OFFLINE).into(profileImg, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(url).placeholder(R.drawable.avatar).into(profileImg);
                    }
                });
            }
        }

        public void setNumberOfLikes(int number){
            numberOfLikes.setText(number+"");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.plus_btn:{
                String n = value.getText().toString();
                if(!TextUtils.isEmpty(n)){
                  int i = Integer.parseInt(n);
                  i++;
                  value.setText(String.format("%d", i));
                }
                break;
            }
            case R.id.minus_btn:{
                String n = value.getText().toString();
                if(!TextUtils.isEmpty(n)){
                    int i = Integer.parseInt(n);
                    if(i>1){
                        i--;
                    }
                    value.setText(String.format("%d", i));
                }
                break;
            }
            case R.id.add_to_btn:{
                String n = value.getText().toString();
                if(!TextUtils.isEmpty(n)){
                    int i = Integer.parseInt(n);
                    if(i>=1){
                      String id =intentProduct.getStringExtra("product_id");
                      Utils.addToCart(
                              id, i,name.getText().toString(),numberProducts,goToCart,progressBar,androidId,this
                      );
                    }
                }
                break;
            }
            default:break;
        }
    }

    public void comment(final String msg){
        final Review review = new Review();
        database.collection("users").document(Objects.requireNonNull(auth.getUid())).get().addOnCompleteListener(ViewProductActivity.this,new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot snapshot = task.getResult();
                User user = snapshot.toObject(User.class);
                review.setUser(user.getName()+ " "+ user.getSurname());
                review.setProdId(prodId);
                review.setRating(4);
                review.setUser_id(auth.getCurrentUser().getUid());
                review.setReview(msg);
                review.setTime(System.currentTimeMillis());
                database.collection("reviews").add(review)
                        .addOnSuccessListener(ViewProductActivity.this,new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(ViewProductActivity.this,"Thank you for your comment",Toast.LENGTH_LONG).show();
                                adapter.notifyDataSetChanged();
                            }
                        });
            }
        });

    }

    public void getRelatedProducts(String catId){

        Query query = FirebaseFirestore.getInstance().collection("products")
                .whereEqualTo("product_category_id",catId)
                // .whereGreaterThan("product_quantity",0)
                .limit(10);

        final FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

         adapterRelatedProducts = new FirestoreRecyclerAdapter<Product,PrdViewHolder>(options){

            @Override
            public PrdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_screen,null,false);
                return new PrdViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final PrdViewHolder holder, final int position, final Product model) {
                final int[] n = {1};
                holder.productName.setText(model.getProduct_name());
                holder.productPrice.setText(model.getProduct_price()+ "DT");
                holder.number_picker_horizontal.setMinValue(1);
                holder.number_picker_horizontal.setListener(new ScrollableNumberPickerListener() {
                    @Override
                    public void onNumberPicked(int value) {
                        if(value == holder.number_picker_horizontal.getMaxValue()) {
                            Toast.makeText(ViewProductActivity.this, getString(R.string.msg_toast_max_value), Toast.LENGTH_LONG).show();
                        }else {
                            n[0] =value;
                        }
                    }
                });


                holder.addtoCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String id = options.getSnapshots().getSnapshot(position).getId();
                        Utils.addToCart(id,n[0],model.getProduct_name(),numberProducts,goToCart,progressBar,androidId,ViewProductActivity.this);
                    }
                });

                holder.productImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ViewProductActivity.this,ViewProductActivity.class);
                        intent.putExtra("product_id",options.getSnapshots().getSnapshot(position).getId());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
                Picasso.with(getApplicationContext()).load(model.getProduct_thumb_image()).networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.productImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext()).load(model.getProduct_thumb_image()).into(holder.productImage);
                            }
                        });
            }
        };

        recyclerViewRelatedProducts = findViewById(R.id.relatedProductsRecyclerView);
        recyclerViewRelatedProducts.setHasFixedSize(true);
        recyclerViewRelatedProducts.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerViewRelatedProducts.setAdapter(adapterRelatedProducts);
    }

    public static class PrdViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView productName,productPrice;
        ImageView productImage;
        Button addtoCart;
        ScrollableNumberPicker number_picker_horizontal;
        PrdViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            productName = view.findViewById(R.id.product_name);
            productPrice = view.findViewById(R.id.product_price);
            productImage = view.findViewById(R.id.product_image);
            addtoCart = view.findViewById(R.id.add_to_cart);
            number_picker_horizontal = view.findViewById(R.id.number_picker_horizontal);
        }

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


    public void getNotification(final int n) {
        final ImageView bell = findViewById(R.id.noti_bell);

        if(n==0){
            database.collection("notifications").document(prodId).collection("subscribers")
                    .whereEqualTo("user_id",auth.getCurrentUser().getUid())
                    .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                            if(documentSnapshots != null && !documentSnapshots.isEmpty()){
                                bell.setImageResource(R.drawable.ic_action_remind_active);
                                bell.setVisibility(View.VISIBLE);
                                bell.setClickable(true);

                                bell.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        database.collection("notifications").document(prodId).collection("subscribers")
                                                .document(auth.getCurrentUser().getUid())
                                                .delete();
                                        bell.setImageResource(R.drawable.ic_action_remind_off);
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(prodId);
                                    }
                                });
                            } else {
                                bell.setVisibility(View.VISIBLE);
                                bell.setClickable(true);
                                bell.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Map<String,String> map = new HashMap<>();
                                        map.put("user_id",auth.getCurrentUser().getUid());
                                        database.collection("notifications").document(prodId).collection("subscribers")
                                                .document(auth.getCurrentUser().getUid())
                                                .set(map)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            FirebaseMessaging.getInstance().subscribeToTopic(prodId);
                                                            bell.setImageResource(R.drawable.ic_action_remind_active);
                                                        }
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
        } else {
            bell.setVisibility(View.INVISIBLE);
            bell.setClickable(false);
        }
    }

}
