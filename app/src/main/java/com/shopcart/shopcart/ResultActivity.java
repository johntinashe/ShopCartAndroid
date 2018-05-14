package com.shopcart.shopcart;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.shopcart.shopcart.models.Product;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ResultActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        List<BarcodeFormat> formatList = new ArrayList<>();
        formatList.add(BarcodeFormat.EAN_13);
        mScannerView.setFormats(formatList);
        mScannerView.setFlash(true);
        setContentView(mScannerView);

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result result) {
        if(result != null){
            if(result.getBarcodeFormat() == BarcodeFormat.EAN_13){
                setContentView(R.layout.activity_result);
                TextView tvResult = findViewById(R.id.productCode);
                tvResult.setText(result.getText());
                getProduct(result.getText());
                mScannerView.stopCamera();
            }
        }
    }

    public void getProduct(String code){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final TextView noResult = findViewById(R.id.noResult);
        final TextView productName = findViewById(R.id.productNameResult);

        db.collection("products").whereEqualTo("product_id_sku",code).addSnapshotListener(ResultActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(!documentSnapshots.isEmpty() && e == null){
                    for(final DocumentChange documentChange : documentSnapshots.getDocumentChanges()){
                        Product product = documentChange.getDocument().toObject(Product.class);
                        productName.setText(String.format("%s %s", product.getProduct_name(), new DecimalFormat("#.##").format(product.getProduct_price())));
                        productName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ResultActivity.this,ViewProductActivity.class);
                                intent.putExtra("product_id",documentChange.getDocument().getId());
                                startActivity(intent);
                            }
                        });
                    }
                } else if(documentSnapshots.isEmpty() && e == null){
                    noResult.setVisibility(View.VISIBLE);
                }else if(e!= null){
                    noResult.setText(e.getMessage());
                    noResult.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
