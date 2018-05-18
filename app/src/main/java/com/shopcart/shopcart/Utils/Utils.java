package com.shopcart.shopcart.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shopcart.shopcart.CartActivity;
import com.shopcart.shopcart.CategoriesActivity;
import com.shopcart.shopcart.FavoritesActivity;
import com.shopcart.shopcart.HomeActivity;
import com.shopcart.shopcart.MainActivity;
import com.shopcart.shopcart.R;
import com.shopcart.shopcart.RegisterAndLoginActivity;
import com.shopcart.shopcart.ResultActivity;
import com.shopcart.shopcart.models.CartProduct;
import com.shopcart.shopcart.models.Product;
import com.shopcart.shopcart.models.User;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;

/**
 * Created by John on 24/1/2018.
 */

public class Utils {

    public static View header;
    public static void getCart(String androidId, final Context context, final TextView numberProducts) {
        Query query = FirebaseFirestore.getInstance()
                .collection("carts")
                .document(androidId)
                .collection("products");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                int n = 0;
                // Convert query snapshot to a list of chats
                assert snapshot != null;
                List<CartProduct> products = snapshot.toObjects(CartProduct.class);
                for (int i = 0; i < products.size(); i++) {
                    n = n + products.get(i).getNumber();
                }
                numberProducts.setText(String.format(context.getString(R.string.numberOfPrd), n));
            }
        });

    }

    public static void setNavigationView(
            NavigationView navigationView,
            final DrawerLayout drawerLayout,
            final FirebaseAuth auth,
            ImageView drawerToggle,
            final Activity activity,
            LinearLayout linearLayout) {

        final View navHeaderView = navigationView.inflateHeaderView(R.layout.navigation_header);
        final TextView name = navHeaderView.findViewById(R.id.userNameHeader);
        final TextView status = navHeaderView.findViewById(R.id.userStatusHeader);
        final ImageView imageView = navHeaderView.findViewById(R.id.userProfileImgHeader);
        header = navHeaderView;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (auth.getCurrentUser() == null)
            return;
        DocumentReference documentReference = db.collection("users").document(auth.getCurrentUser().getUid());
        documentReference.addSnapshotListener(activity,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
               if(documentSnapshot != null) {
                   if(documentSnapshot.exists()){
                       User user = documentSnapshot.toObject(User.class);
                       name.setText(String.format("%s %s", user.getName(), user.getSurname()));
                       status.setText(user.getMembership());

                       if(!user.getThumb_image().equals("default")){
                            Picasso.with(activity.getApplicationContext()).load(user.getThumb_image())
                                   .placeholder(R.drawable.avatar).into(imageView);
                       }
                   }
               }
            }
        });

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (auth.getCurrentUser() != null) {
                    auth.signOut();
                    Intent intent = new Intent(activity.getApplicationContext(), RegisterAndLoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.getApplicationContext().startActivity(intent);
                    activity.finish();
                }
            }
        });

        drawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.categories: {
                        Intent intent = new Intent(activity.getApplicationContext(), CategoriesActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.getApplicationContext().startActivity(intent);
                        drawerLayout.closeDrawer(Gravity.START);
                        return true;
                    }
                    case R.id.contactUs: {
                        Intent intent = new Intent(activity.getApplicationContext(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("fromWhere", "fromContactUs");
                        activity.getApplicationContext().startActivity(intent);
                        drawerLayout.closeDrawer(Gravity.START);
                        return true;
                    }
                    case R.id.myProfile: {
                        Intent intent = new Intent(activity.getApplicationContext(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("fromWhere", "fromProfile");
                        activity.getApplicationContext().startActivity(intent);
                        drawerLayout.closeDrawer(Gravity.START);
                        return true;
                    }
                    case R.id.search: {
                        Intent intent = new Intent(activity.getApplicationContext(), ResultActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.getApplicationContext().startActivity(intent);
                        drawerLayout.closeDrawer(Gravity.START);
                        return true;
                    }
                    case R.id.favorites_nav_drawer: {
                        Intent intent = new Intent(activity.getApplicationContext(), FavoritesActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.getApplicationContext().startActivity(intent);
                        drawerLayout.closeDrawer(Gravity.START);
                        return true;
                    }
                    case R.id.navigationCart: {
                        Intent intent = new Intent(activity.getApplicationContext(), CartActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.getApplicationContext().startActivity(intent);
                        drawerLayout.closeDrawer(Gravity.START);
                        return true;
                    }
                    case R.id.navigationHome : {
                        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.getApplicationContext().startActivity(intent);
                        drawerLayout.closeDrawer(Gravity.START);
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });
    }

    public static void addToCart(final String id, final int nbItems, final String name, final TextView numberProducts
            , final ImageView goToCart, final ProgressBar progressBar, final String androidId, final Context context) {

        final FirebaseFirestore database = FirebaseFirestore.getInstance();
        numberProducts.setVisibility(View.INVISIBLE);
        goToCart.setVisibility(View.INVISIBLE);
        goToCart.setClickable(false);
        progressBar.setVisibility(View.VISIBLE);
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
                                progressBar.setVisibility(View.INVISIBLE);
                                goToCart.setClickable(true);
                                goToCart.setVisibility(View.VISIBLE);
                                numberProducts.setVisibility(View.VISIBLE);
                                Toast.makeText(context, name + " has been added", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                goToCart.setClickable(true);
                                goToCart.setVisibility(View.VISIBLE);
                                numberProducts.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });

    }


    public static boolean testForConnection(Context context) {
        boolean connection;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert conectivtyManager != null;
        connection = conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected();
        return connection;
    }


//    private static void addToCart(final String id, final int nbItems, final String name , final Context context, final String androidId,
//                           final TextView numberProducts, final ImageView goToCart, final ProgressBar progressBar ) {
//
//        final FirebaseFirestore database = FirebaseFirestore.getInstance();
//        numberProducts.setVisibility(View.INVISIBLE);
//        goToCart.setVisibility(View.INVISIBLE);
//        goToCart.setClickable(false);
//        progressBar.setVisibility(View.VISIBLE);
//        database.collection("products").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                Product product = documentSnapshot.toObject(Product.class);
//                database.collection("carts").document(androidId).collection("products")
//                        .document(id)
//                        .set(new CartProduct(nbItems,product.getProduct_price()*nbItems))
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                progressBar.setVisibility(View.INVISIBLE);
//                                goToCart.setClickable(true);
//                                goToCart.setVisibility(View.VISIBLE);
//                                numberProducts.setVisibility(View.VISIBLE);
//                                Toast.makeText(context, name+" has been added", Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                progressBar.setVisibility(View.INVISIBLE);
//                                goToCart.setClickable(true);
//                                goToCart.setVisibility(View.VISIBLE);
//                                numberProducts.setVisibility(View.VISIBLE);
//                                Toast.makeText(context, "Sorry something happened", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//            }
//        });
//
//    }

    //upload image method

    public static void uploadImage(Intent data, int requestCode, int resultCode, final Activity activity, FirebaseAuth auth ,@Nullable final ArcProgress arcProgress) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        StorageReference userProfile = FirebaseStorage.getInstance().getReference();
        Uri imageURI;
        if (data != null) {
            imageURI = data.getData();
        } else {
            return;
        }

        if (requestCode == 1 && resultCode == RESULT_OK) {
            CropImage.activity(imageURI)
                    .setMinCropWindowSize(500, 500)
                    .setAspectRatio(1, 1)
                    .start(activity);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();
                final String uid = auth.getCurrentUser().getUid();

                File thumbFile = new File(resultUri.getPath());
                Bitmap thumb_bitmap = new Compressor(activity.getApplicationContext())
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(75)
                        .compressToBitmap(thumbFile);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                final StorageReference thumbfile = userProfile.child("profile_pics").child("thumbs").child(uid + ".jpg");
                StorageReference filePath = userProfile.child("profile_pics").child(uid + ".jpg");
                if (arcProgress != null)
                    arcProgress.setVisibility(View.VISIBLE);
                UploadTask uploadTask = filePath.putFile(resultUri);

                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            @SuppressWarnings("VisibleForTests") final String download_url = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumbfile.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbtask) {
                                    @SuppressWarnings("VisibleForTests") String url = thumbtask.getResult().getDownloadUrl().toString();

                                    if (thumbtask.isSuccessful()) {
                                        Map<String, Object> imageHashMap = new HashMap();
                                        imageHashMap.put("image", download_url);
                                        imageHashMap.put("thumb_image", url);

                                        db.collection("users").document(uid).set(imageHashMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if (task.isSuccessful()) {
                                                    if (arcProgress != null)
                                                        arcProgress.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(activity.getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(activity.getApplicationContext(), " Sorry something went wrong while merging", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                    } else {
                                        Toast.makeText(activity.getApplicationContext(), "Sorry something went wrong in uploading this thumbnail", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(activity.getApplicationContext(), "Sorry something went wrong", Toast.LENGTH_LONG).show();
                            //progressDialog.dismiss();
                        }
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        if (arcProgress != null)
                            arcProgress.setProgress((int) progress);
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }



}
