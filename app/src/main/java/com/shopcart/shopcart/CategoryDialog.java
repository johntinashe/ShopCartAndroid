package com.shopcart.shopcart;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.lid.lib.LabelImageView;
import com.shawnlin.numberpicker.NumberPicker;
import com.shopcart.shopcart.Utils.GridSpacingItemDecoration;
import com.shopcart.shopcart.models.CartProduct;
import com.shopcart.shopcart.models.Category;
import com.shopcart.shopcart.models.Product;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    String id;
    CardView cardView;
    ImageView show;
    FirebaseFirestore db;
    RecyclerView prod_recycler;
    FirestoreRecyclerAdapter adapter;
    TextView catName;
    ImageView cateImg ,sortButton;
    String androidId;
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Val = "priceKey";
    public static final String Name = "nameKey";
    SharedPreferences sharedpreferences;
    boolean sort_price , sort_name;
    EditText prd_name;

    public CategoryDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();
        final View view = inflater.inflate(R.layout.category_products_dialog, container, false);
        prd_name = view.findViewById(R.id.search_product);
        cardView = view.findViewById(R.id.searchProductCardView);
        show = view.findViewById(R.id.showSearch);
        show.setClickable(true);
        show.setOnClickListener(this);
        catName = view.findViewById(R.id.categoryName);
        cateImg = view.findViewById(R.id.cateImage);
        sortButton = view.findViewById(R.id.sortButton);


        prd_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    if(getActivity() !=null){
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        assert imm != null;
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            } });

        sharedpreferences = getActivity().getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        final boolean val = sharedpreferences.getBoolean(Val, false);
        final String nm = sharedpreferences.getString(Name, "");


        if(val){
            if(!nm.equalsIgnoreCase("") && nm.equalsIgnoreCase("PRICE")){
                sort_price = true;
            }else if(!nm.equalsIgnoreCase("") && nm.equalsIgnoreCase("NAME")){
                sort_name = true;
            }
        }else {
            sort_price=false;
            sort_name=false;
        }

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity().getApplicationContext(),2);
        int spanCount = 2;
        int spacing = 10;
        prod_recycler = view.findViewById(R.id.cateProductsRecycler);
        prod_recycler.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        prod_recycler.setHasFixedSize(true);
        prod_recycler.setLayoutManager(layoutManager);
        getQtyandName(id);
        showMenu();
        return  view;
    }


    // TODO : show no products available
    public void getQtyandName(final String i){
        final Activity activity =getActivity();
        if(activity != null){
            db.collection("categories").document(i).addSnapshotListener(activity, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if(documentSnapshot.exists()){
                        final Category category = documentSnapshot.toObject(Category.class);
                        catName.setText(category.getName()+ "(0)");
                        Picasso.with(getContext()).load(category.getCatimage()).networkPolicy(NetworkPolicy.OFFLINE).into(cateImg, new Callback() {
                            @Override
                            public void onSuccess() {
                                Picasso.with(getActivity().getApplicationContext()).load(category.getCatimage()).into(cateImg);
                            }

                            @Override
                            public void onError() {

                            }
                        });
                        db.collection("products").whereEqualTo("product_category_id",i)
                                   .whereEqualTo("product_status",true)
                                   .addSnapshotListener(activity, new EventListener<QuerySnapshot>() {
                                       @Override
                                       public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                           if(!documentSnapshots.isEmpty()){
                                               catName.setText(category.getName()+ " (" + documentSnapshots.size() +")");
                                           }
                                       }
                                   });
                    }
                }
            });
        }
    }

    private void performSearch() {
        if (prd_name.getText() != null && prd_name.getText().toString().length() > 0) {
            String name = prd_name.getText().toString();
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            db.collection("products").orderBy("product_name").startAt(name).endAt(name + "\uf8ff")
                    .addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                            if (documentSnapshots != null) {
                                if (!documentSnapshots.isEmpty()) {
                                    for (DocumentSnapshot document : documentSnapshots) {
                                        Product product = document.toObject(Product.class);
                                        Log.d("Product", product.getProduct_name());
                                    }
                                }
                            }
                        }
                    });
        }
    }

    public void addToCart(final String id, final int nbItems ,final String name) {

        final FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("products").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Product product = documentSnapshot.toObject(Product.class);
                database.collection("carts").document(androidId).collection("products")
                        .document(id)
                        .set(new CartProduct(nbItems, product.getProduct_price() * nbItems))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), name + " has been added", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Sorry something happened!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    public void getProducts(){

        sharedpreferences = getActivity().getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final PopupMenu popup = new PopupMenu(getContext(), sortButton);
        popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());

        Query query;
        if(sort_price){
             query = FirebaseFirestore.getInstance()
                    .collection("products")
                    .whereEqualTo("product_status", true)
                    .whereEqualTo("product_category_id",this.id)
                    .orderBy("product_price", Query.Direction.ASCENDING);
        }else if(sort_name){
             query = FirebaseFirestore.getInstance()
                    .collection("products")
                    .whereEqualTo("product_status", true)
                    .whereEqualTo("product_category_id",this.id)
                    .orderBy("product_name", Query.Direction.ASCENDING);
        }else {
             query = FirebaseFirestore.getInstance()
                    .collection("products")
                    .whereEqualTo("product_status", true)
                    .whereEqualTo("product_category_id",this.id);
        }

        final FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Product, ProductHolder>(options) {
            @Override
            public ProductHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorites_single_item,null,false);
                return  new ProductHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ProductHolder holder, final int position, final Product model) {
                holder.name.setText(model.getProduct_name());
                holder.price.setText(model.getProduct_price() +" DT");
                holder.quantity.setMaxValue(model.getProduct_quantity());
                final String id = options.getSnapshots().getSnapshot(position).getId();
                final int[] nb = {1};
                holder.quantity.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        nb[0] = newVal;
                    }
                });

                if (model.getProduct_quantity() == 0) {
                    holder.addToCart.setEnabled(false);
                    holder.addToCart.setBackgroundResource(R.drawable.gray_add_to_cart_bg);
                    holder.quantity.setVisibility(View.INVISIBLE);
                    // holder.labelImageView.setLabelText("Out of Stock");
                    //  holder.labelImageView.setLabelBackgroundColor(Color.RED);
                    //  holder.labelImageView.setLabelTextColor(Color.WHITE);
                } else {
                    holder.addToCart.setEnabled(true);
                    holder.addToCart.setBackgroundResource(R.drawable.blue_bg);
                    holder.quantity.setVisibility(View.VISIBLE);
                    //   holder.labelImageView.setLabelText("");
                    //   holder.labelImageView.setLabelBackgroundColor(Color.WHITE);
                    //   holder.labelImageView.setLabelTextColor(Color.WHITE);
                }

                holder.addToCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       addToCart(id, nb[0],model.getProduct_name());
                    }
                });

                holder.product_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ViewProductActivity.class);
                        intent.putExtra("product_id", options.getSnapshots().getSnapshot(position).getId());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });

                Picasso.with(getContext()).load(model.getProduct_thumb_image()).networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.product_image, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(getActivity().getApplicationContext()).load(model.getProduct_thumb_image()).into(holder.product_image);
                            }
                        });
            }
        };

        prod_recycler.setAdapter(adapter);
    }

    public void setId(String d, String androidId) {
        this.id= d;
        this.androidId = androidId;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.showSearch: {
                cardView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.SlideInRight)
                        .duration(700)
                        .playOn(cardView);
                YoYo.with(Techniques.FadeOut)
                        .duration(800)
                        .playOn(show);
                //show.setVisibility(View.INVISIBLE);
                show.setClickable(false);
            }
        }
    }

    private static class ProductHolder extends RecyclerView.ViewHolder {
        TextView price,name;
        Button addToCart;
        View view;
        LabelImageView product_image;
        NumberPicker quantity;

        ProductHolder(View itemView) {
            super(itemView);
            view = itemView;

            price = view.findViewById(R.id.product_price);
            name = view.findViewById(R.id.product_name);
            addToCart = view.findViewById(R.id.add_to_cart);
            product_image = view.findViewById(R.id.product_image);
            quantity = view.findViewById(R.id.number_picker);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getProducts();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void showMenu(){

        sharedpreferences = getActivity().getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = sharedpreferences.edit();

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu popup = new PopupMenu(getContext(), sortButton);
                popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());

                final boolean val = sharedpreferences.getBoolean(Val, false);
                final String nm = sharedpreferences.getString(Name, "");
                MenuItem item1 = popup.getMenu().getItem(0);
                MenuItem item2 = popup.getMenu().getItem(1);

                if(val){
                    if(!nm.equalsIgnoreCase("") && nm.equalsIgnoreCase("PRICE")){
                        item1.setChecked(val);
                        sort_price = true;
                    }else if(!nm.equalsIgnoreCase("") && nm.equalsIgnoreCase("NAME")){
                        item2.setChecked(val);
                        sort_name = true;
                    }
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.sort_by_price : {
                                if(item.isChecked()){
                                    item.setChecked(false);
                                    edit.putBoolean(Val, false);
                                    edit.putString(Name,"");
                                    sort_price = false;
                                    getProducts();
                                    adapter.startListening();
                                    adapter.notifyDataSetChanged();
                                    edit.apply();
                                }else{
                                    item.setChecked(true);
                                    edit.putBoolean(Val, true);
                                    edit.putString(Name,"PRICE");
                                    sort_price = true;
                                    sort_name=false;
                                    getProducts();
                                    adapter.startListening();
                                    adapter.notifyDataSetChanged();
                                    edit.apply();
                                }
                                return true;
                            }
                            case R.id.sort_by_name : {
                                if(item.isChecked()){
                                    item.setChecked(false);
                                    edit.putBoolean(Val, false);
                                    edit.putString(Name,"");
                                    sort_name = false;
                                    getProducts();
                                    adapter.startListening();
                                    adapter.notifyDataSetChanged();
                                    edit.apply();
                                }else{
                                    item.setChecked(true);
                                    edit.putBoolean(Val, true);
                                    sort_name = true;
                                    sort_price=false;
                                    getProducts();
                                    adapter.startListening();
                                    adapter.notifyDataSetChanged();
                                    edit.putString(Name, "NAME");
                                    edit.apply();
                                }
                                return true;
                            }
                            default: return false;

                        }

                    }
                });
                popup.show();
            }
        });
    }

}
