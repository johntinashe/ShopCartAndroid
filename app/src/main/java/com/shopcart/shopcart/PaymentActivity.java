package com.shopcart.shopcart;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.cooltechworks.creditcarddesign.CreditCardView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.shopcart.shopcart.Utils.Validator;
import com.shopcart.shopcart.models.CartProduct;
import com.shopcart.shopcart.models.Payment;
import com.shopcart.shopcart.models.Product;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Token;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

public class PaymentActivity extends AppCompatActivity {

    private com.stripe.android.model.Card cardNew;
    private int am;
    private EditText cardNumber , cardCVC;
    private String androidId;
    private Button payBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private FirebaseUser user;
    private EditText cardMYET,cardName;
    private ImageView errorIcon, errorIconDate,errorIconCVC,errorIconName;
    public static final String PUBLISHABLE_KEY = "pk_test_8jdJpoyZRltzC0kxOkSylFMz";
    private TextView toolbarTitle;
    private ImageView cart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        androidId  = auth.getCurrentUser().getUid();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");


        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View action_bar_view = inflater.inflate(R.layout.custom_tool, null);
        assert actionBar != null;
        actionBar.setCustomView(action_bar_view);

        toolbarTitle = action_bar_view.findViewById(R.id.toolbarTitle);

        ImageView drawerToggle = action_bar_view.findViewById(R.id.drawerToggle);
        cart = action_bar_view.findViewById(R.id.toolbar_cart);
        cart.setVisibility(View.INVISIBLE);
        cart.setClickable(false);
        toolbarTitle.setText("Payment");
        TextView tv = action_bar_view.findViewById(R.id.tv_number_of_products);
        tv.setVisibility(View.INVISIBLE);
        drawerToggle.setImageResource(R.drawable.ic_action_back);
        drawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (auth != null) {
            user = auth.getCurrentUser();
        }
        final CreditCardView cardView = findViewById(R.id.card_5);

        cardCVC = findViewById(R.id.editTextCVC);
        cardMYET = findViewById(R.id.cardMYET);
        cardName = findViewById(R.id.cardNameET);
        cardNumber = findViewById(R.id.cardNumberET);
        errorIcon = findViewById(R.id.errorIconCardNumber);
        errorIconDate = findViewById(R.id.errorIconDate);
        errorIconCVC = findViewById(R.id.errorIconCVC);
        errorIconName = findViewById(R.id.errorIconName);


        cardCVC.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    cardView.showBack();
                } else {
                    cardView.showFront();
                }
            }
        });

        cardCVC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cardView.setCVV(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        cardName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cardView.setCardHolderName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        cardMYET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cardView.setCardExpiry(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//        cardMYET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                SimpleTooltip tooltipDate  = new SimpleTooltip.Builder(getApplicationContext())
//                        .anchorView(v)
//                        .text("Invalid Date")
//                        .gravity(Gravity.START)
//                        .animated(true)
//                        .backgroundColor(Color.RED)
//                        .textColor(Color.WHITE)
//                        .transparentOverlay(false)
//                        .build();
//
//                if(!hasFocus){
//                    if(cardMYET.getText().toString().length()>0 && cardMYET.getText().toString().length()==5 && cardMYET.getText().toString().contains("/")){
//                        String date[] = cardMYET.getText().toString().split("/");
//                        Validator.setExpMonth(Integer.parseInt(date[0]));
//                        Validator.setExpYear(Integer.parseInt(date[1]));
//                        Toast.makeText(getApplicationContext(),"month "+ date[0]+ " year "+ date[1],Toast.LENGTH_SHORT).show();
//                        if(Validator.validateExpiryDate()){
//                            tooltipDate.dismiss();
//                            errorIconDate.setVisibility(View.INVISIBLE);
//                        }else {
//                            errorIconDate.setVisibility(View.VISIBLE);
//                            tooltipDate.show();
//                        }
//                    }else{
//                        errorIconDate.setVisibility(View.VISIBLE);
//                        tooltipDate.show();
//                    }
//                }
//            }
//        });
        cardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String sf = s.toString();
                cardView.setCardNumber(sf);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

//        cardNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                SimpleTooltip tooltipCard  = new SimpleTooltip.Builder(getApplicationContext())
//                        .anchorView(v)
//                        .backgroundColor(Color.RED)
//                        .textColor(Color.WHITE)
//                        .text("Invalid Card Number")
//                        .gravity(Gravity.END)
//                        .animated(true)
//                        .transparentOverlay(false)
//                        .build();
//                if(!hasFocus){
//                    String sf = cardNumber.getText().toString();
//                    if(sf.length()>0){
//                        Validator.setNumber(sf);
//                        if(!Validator.validateNumber()){
//                            tooltipCard.show();
//                            errorIcon.setVisibility(View.VISIBLE);
//                        }else {
//                            tooltipCard.dismiss();
//                            errorIcon.setVisibility(View.INVISIBLE);
//                        }
//                    }
//                }
//            }
//        });

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        float amount = Float.parseFloat(extras.getString("price"));
        String name = extras.getString("purchase_name");
        am = (int)amount + 10 +1;

        payBtn =findViewById(R.id.payBtn);
        payBtn.setText("PAY $"+am);

            payBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (validateCard(cardNumber) && validateCVC(cardCVC) && validateDate(cardMYET)) {
                        if (Validator.validateExpiryDate() && Validator.validateCVC() && Validator.validateNumber()) {
                            if (user == null) {
                                snack("Please Login first");
                            } else {
                                cardNew = new com.stripe.android.model.Card(
                                        Validator.getNumber(),
                                        Validator.getExpMonth(),
                                        Validator.getExpYear(),
                                        Validator.getCvc()
                                );
                                buy();
                            }
                        }
                    } else {
                        snack("Please Verify card info first!");
                    }
                }
            });


    }

    public boolean validateCard(View v) {
        SimpleTooltip tooltipCard = new SimpleTooltip.Builder(getApplicationContext())
                .anchorView(v)
                .backgroundColor(Color.RED)
                .textColor(Color.WHITE)
                .text("Invalid Card Number")
                .gravity(Gravity.END)
                .animated(true)
                .transparentOverlay(false)
                .build();
        String sf = cardNumber.getText().toString();
        if (sf.length() > 0) {
            Validator.setNumber(sf);
            if (!Validator.validateNumber()) {
                tooltipCard.show();
                errorIcon.setVisibility(View.VISIBLE);
                return false;
            } else {
                tooltipCard.dismiss();
                errorIcon.setVisibility(View.INVISIBLE);
                return true;
            }
        } else {
            tooltipCard.show();
            errorIcon.setVisibility(View.VISIBLE);
            return false;
        }
    }

    public boolean validateCVC(View v) {
        SimpleTooltip tooltip = new SimpleTooltip.Builder(getApplicationContext())
                .anchorView(v)
                .text("Invalid CVC")
                .gravity(Gravity.START)
                .animated(true)
                .backgroundColor(Color.RED)
                .textColor(Color.WHITE)
                .transparentOverlay(false)
                .build();

        Validator.setCvc(cardCVC.getText().toString());
        if (!Validator.validateCVC()) {
            errorIconCVC.setVisibility(View.VISIBLE);
            tooltip.show();
            return false;
        } else {
            tooltip.dismiss();
            errorIconCVC.setVisibility(View.INVISIBLE);
            return true;
        }

    }

    public boolean validateDate(View v) {
        SimpleTooltip tooltipDate = new SimpleTooltip.Builder(getApplicationContext())
                .anchorView(v)
                .text("Invalid Date")
                .gravity(Gravity.START)
                .animated(true)
                .backgroundColor(Color.RED)
                .textColor(Color.WHITE)
                .transparentOverlay(false)
                .build();

        if (cardMYET.getText().toString().length() > 0 && cardMYET.getText().toString().length() == 5 && cardMYET.getText().toString().contains("/")) {
            String date[] = cardMYET.getText().toString().split("/");
            try {
                Validator.setExpMonth(Integer.parseInt(date[0]));
                Validator.setExpYear(Integer.parseInt(date[1]));
            } catch (Exception e) {
                errorIconDate.setVisibility(View.VISIBLE);
                tooltipDate.show();
                return false;
            }
            if (Validator.validateExpiryDate()) {
                tooltipDate.dismiss();
                errorIconDate.setVisibility(View.INVISIBLE);
                return true;
            } else {
                errorIconDate.setVisibility(View.VISIBLE);
                tooltipDate.show();
                return false;
            }
        } else {
            errorIconDate.setVisibility(View.VISIBLE);
            tooltipDate.show();
            return false;
        }
    }

    private void buy() {

        boolean validation = cardNew.validateCard();
        if (validation) {
            startProgress("Validating Credit Card");

            try {
                new Stripe(PUBLISHABLE_KEY).createToken(cardNew, new TokenCallback() {
                    @Override
                    public void onError(Exception error) {
                        Log.d("StripeError", error.toString());
                        finishProgress();
                    }

                    @Override
                    public void onSuccess(final Token token) {

                        final Payment payment = new Payment();
                        payment.setPrice(am);
                        payment.setTime(System.currentTimeMillis());
                        payment.setToken(token);
                        database.collection("users")
                                .document(user.getUid())
                                .collection("payments")
                                .add(payment)
                                .addOnSuccessListener(PaymentActivity.this, new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(final DocumentReference documentReference) {
                                        TSnackbar snackbar = TSnackbar.make(findViewById(R.id.payment), R.string.payment, 3000);
                                        snackbar.setActionTextColor(Color.WHITE);
                                        View snackbarView = snackbar.getView();
                                        snackbarView.setBackgroundColor(getResources().getColor(R.color.green_bg));
                                        TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                                        textView.setTextColor(Color.WHITE);
                                        snackbar.show();
                                        Validator.setCvc("");
                                        Validator.setNumber("");
                                        Validator.setExpYear(0);
                                        Validator.setExpMonth(0);
                                        Charge ch;
                                    Thread thread =  new Thread(new Runnable() {
                                            @Override
                                            public void run() {

                                                Map<String, Object> chargeParams = new HashMap<>();
                                                chargeParams.put("amount", am * 100);
                                                chargeParams.put("currency", "usd");
                                                chargeParams.put("source", token.getId());
                                                chargeParams.put("description", "First Purchase");
                                                chargeParams.put("receipt_email", auth.getCurrentUser().getEmail());
                                                com.stripe.Stripe.apiKey = "sk_test_bfZoZubZ9rdnvKS5BMhSQqxn";
                                                RequestOptions requestOptions = RequestOptions.builder()
                                                        .setIdempotencyKey(documentReference.getId())
                                                        .build();
                                                try {
                                                    Charge ch = Charge.create(chargeParams, requestOptions);
                                                    if (ch.getPaid()) {
                                                        final Map<String, Object> map = new HashMap<>();

                                                        Map<String, Map<String, Object>> charge = new HashMap<>();
                                                        map.put("description", ch.getDescription());
                                                        map.put("paid", ch.getPaid());
                                                        map.put("amount", ch.getAmount());
                                                        map.put("email", auth.getCurrentUser().getEmail());
                                                        map.put("created", ch.getCreated());
                                                        map.put("brand",Validator.getBrand());
                                                        map.put("id", ch.getId());

                                                        charge.put("charge", map);
                                                        Validator.setBrand("");
                                                        database.collection("users").document(auth.getCurrentUser().getUid())
                                                                .collection("payments")
                                                                .document(documentReference.getId())
                                                                .set(charge, SetOptions.merge())
                                                                .addOnSuccessListener(PaymentActivity.this, new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Map<String,Object> order = new HashMap<>();
                                                                        order.put("placed_at",System.currentTimeMillis());
                                                                        order.put("status","new");
                                                                        order.put("user_id",auth.getCurrentUser().getUid());
                                                                        database.collection("orders").document(documentReference.getId())
                                                                                .set(order)
                                                                                .addOnCompleteListener(PaymentActivity.this, new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        database.collection("carts").document(androidId).collection("products")
                                                                                                .get()
                                                                                                .addOnSuccessListener(PaymentActivity.this ,new OnSuccessListener<QuerySnapshot>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(QuerySnapshot documentSnapshots) {
                                                                                                        if(!documentSnapshots.isEmpty()){
                                                                                                            for (final DocumentSnapshot documentSnap : documentSnapshots) {

                                                                                                                final CartProduct cartProduct = documentSnap.toObject(CartProduct.class);

                                                                                                                database.collection("products").document(documentSnap.getId()).get()
                                                                                                                        .addOnCompleteListener(PaymentActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                                                if (task.isSuccessful()) {
                                                                                                                                    Product product = task.getResult().toObject(Product.class);

                                                                                                                                    product.setProduct_quantity(product.getProduct_quantity() - cartProduct.getNumber());

                                                                                                                                    database.collection("products").document(documentSnap.getId()).set(product, SetOptions.merge())
                                                                                                                                            .addOnCompleteListener(PaymentActivity.this, new OnCompleteListener<Void>() {
                                                                                                                                                @Override
                                                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                                    if (task.isSuccessful()) {
                                                                                                                                                        database.collection("orders")
                                                                                                                                                                .document(documentReference.getId())
                                                                                                                                                                .collection(documentReference.getId())
                                                                                                                                                                .add(documentSnap)
                                                                                                                                                                .addOnSuccessListener(PaymentActivity.this, new OnSuccessListener<DocumentReference>() {
                                                                                                                                                                    @Override
                                                                                                                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                                                                                                                        database.collection("carts").document(androidId).collection("products")
                                                                                                                                                                                .document(documentSnap.getId())
                                                                                                                                                                                .delete();
                                                                                                                                                                    }
                                                                                                                                                                });
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            });
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });

                                                                                                            }
                                                                                                            finishProgress();
                                                                                                            TSnackbar snackbar = TSnackbar.make(findViewById(R.id.payment), R.string.paymentSuccessful, 5000);
                                                                                                            snackbar.setActionTextColor(Color.WHITE);
                                                                                                            View snackbarView = snackbar.getView();
                                                                                                            snackbarView.setBackgroundColor(getResources().getColor(R.color.green_bg));
                                                                                                            TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                                                                                                            textView.setTextColor(Color.WHITE);
                                                                                                            snackbar.show();

//                                                                                                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                                                                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                                                                            intent.putExtra("fromWhere", "fromPayments");
//                                                                                                            startActivity(intent);
//                                                                                                            finish();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                });

                                                                    }
                                                                });
                                                    }

                                                } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
                                                    e.printStackTrace();
                                                    Map<String, Object> map = new HashMap<>();

                                                    Map<String, Map<String, Object>> charge = new HashMap<>();

                                                    map.put("description", e.getMessage());
                                                    map.put("paid", false);
                                                    map.put("amount", am *100);
                                                    map.put("fee",null);
                                                    map.put("created", System.currentTimeMillis());

                                                    charge.put("charge", map);
                                                    database.collection("users").document(auth.getCurrentUser().getUid())
                                                            .collection("payments")
                                                            .document(documentReference.getId())
                                                            .set(charge, SetOptions.merge())
                                                            .addOnSuccessListener(PaymentActivity.this, new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    TSnackbar snackbar = TSnackbar.make(findViewById(R.id.payment), R.string.paymentError,5000);
                                                                    snackbar.setActionTextColor(Color.WHITE);
                                                                    View snackbarView = snackbar.getView();
                                                                    snackbarView.setBackgroundColor(getResources().getColor(R.color.red));
                                                                    TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                                                                    textView.setTextColor(Color.WHITE);
                                                                    snackbar.show();
                                                                }
                                                            });
                                                }
                                            }
                                        });
                                    thread.start();
                                    }
                                });
                    }
                });
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }


        } else if (!cardNew.validateNumber()) {
            snack("The card number that you entered is invalid");
        } else if (!cardNew.validateExpiryDate()) {
            snack("The expiration date that you entered is invalid");
        } else if (!cardNew.validateCVC()) {
            snack("The expiration date that you entered is invalid");
        } else {
            snack("The card details that you entered are invalid");
        }


    }



    AlertDialog dialog;

    private void startProgress(String title) {
        dialog = new SpotsDialog(this, R.style.Custom);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(title);
        dialog.show();
    }

    private void finishProgress() {
        dialog.dismiss();
    }


    public void snack(String message) {
        TSnackbar snackbar = TSnackbar.make(findViewById(R.id.payment), message, 5000);
        snackbar.setActionTextColor(Color.WHITE);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.red));
        TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

}
