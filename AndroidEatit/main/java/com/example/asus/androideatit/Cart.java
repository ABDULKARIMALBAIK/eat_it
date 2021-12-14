package com.example.asus.androideatit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.androideatit.Common.Common;
import com.example.asus.androideatit.Common.Config;
import com.example.asus.androideatit.Database.Database;
import com.example.asus.androideatit.Helper.RecyclerItemTouchHelper;
import com.example.asus.androideatit.Interface.RecyclerItemTouchHelperListener;
import com.example.asus.androideatit.Model.DataMessage;
import com.example.asus.androideatit.Model.MyResponse;
import com.example.asus.androideatit.Model.Order;
import com.example.asus.androideatit.Model.Request;
import com.example.asus.androideatit.Model.Token;
import com.example.asus.androideatit.Model.User;
import com.example.asus.androideatit.Remote.APIService;
import com.example.asus.androideatit.Remote.IGoogleService;
import com.example.asus.androideatit.ViewHolder.CartAdapter;
import com.example.asus.androideatit.ViewHolder.CartViewHolder;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    private static final int PAYPAL_REQUEST_CODE = 9999;
    private static final int CODE_REQUEST_PERMISSION_lOCATION = 1000;
    RecyclerView recyclerView;
    Button btnPlace;
    public TextView txtTotalPrice;

    RelativeLayout rootLayout;

    FirebaseDatabase database;
    DatabaseReference requests;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    APIService mService;

    //Google API
    Place shippingAddress;

    //Location
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    LocationManager manager;
    Location mLastLocation;

    //Declare Google Map API Retrofit
    IGoogleService mGoogleMapService;

    static PayPalConfiguration config;

    String address, comment;


    //Press Ctrl + O
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Note: add this code before setContentView method
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_cart);

        //PayPal payment
         config = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) //use SendBox because we test , change it late if you going to production
                .clientId(Config.PAYPAL_CLIENT_ID);

        //Init Google Map API
        mGoogleMapService = Common.getGoogleMapAPI();

        //init rootLayout
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        //Init PayPal (we can't used in syria , so i commented last line)
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        //startActivity(intent);

        //Init Service
        mService = Common.getFCMService();


        //FireBase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        btnPlace = (Button) findViewById(R.id.btnPlaceOrder);
        txtTotalPrice = (TextView) findViewById(R.id.total);

        recyclerView = (RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Swipe to delete item
        //Very important put code ItemTouchHelper....  AFTER init RecyclerView
        ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new RecyclerItemTouchHelper(0,
                ItemTouchHelper.LEFT,
                this);
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(recyclerView);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your cart is empty !!!", Toast.LENGTH_SHORT).show();

            }
        });

        loadListFood();

        buildLocationRequest();
        buildLocationCallBack();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void showAlertDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle("One more step!")
                .setMessage("Enter your address: ");

        View order_address_comment = getLayoutInflater().inflate(R.layout.order_address_comment, null);


        //final MaterialEditText edtAddress = (MaterialEditText)order_address_comment.findViewById(R.id.edtAddress);
        final SupportPlaceAutocompleteFragment edtAddress = (SupportPlaceAutocompleteFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);//Hide search icon before fragment
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        //Set Hint for Autocomplete Edit Text
        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Enter your address...");
        //set Text size
        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);
        //Get Address from Place Autocomplete
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress = place;
            }

            @Override
            public void onError(Status status) {
                Log.e("ERROR", status.getStatusMessage());
            }
        });


        final MaterialEditText edtComment = (MaterialEditText) order_address_comment.findViewById(R.id.edtComment);
        final RadioButton rdiShipToAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiHomeAddress);

        final RadioButton rdiCOD = (RadioButton) order_address_comment.findViewById(R.id.rdiCOD);
        final RadioButton rdiPaypal = (RadioButton) order_address_comment.findViewById(R.id.rdiPaypal);
        final RadioButton rdiBalance = (RadioButton) order_address_comment.findViewById(R.id.rdiEatItBalance);


        //Event Home address Radio Button
        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){

                    if (TextUtils.isEmpty(Common.currentUser.getHomeAddress()) ||
                            Common.currentUser.getHomeAddress() == null)
                        Toast.makeText(Cart.this, "Please update your Home Address", Toast.LENGTH_SHORT).show();

                    else{
                        address = Common.currentUser.getHomeAddress();
                        //Then set this address to edtAddress
                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                .setText(address);
                    }
                }
            }
        });

        //Event Ship to this address Radio Button
        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Ship to this address feature
                if (isChecked) {  //isChecked == true

                    //Init my Location , here Fix error , we commented this line
                    //initMyLocation();
                    // solved by add two  buildLocationCallBack() & buildLocationRequest(
                    buildLocationRequest();
                    buildLocationCallBack();

                    if (mLastLocation != null){

                        mGoogleMapService.getAddressName(
                                //Copy this link and put LatLng and paste it in Google search , you will see JSON file
                                String.format(Locale.US, "............................",
                                        mLastLocation.getLatitude(),
                                        mLastLocation.getLongitude()) ,

                                Common.MAP_API_KEY

                        ).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {

                                //If fetch API ok
                                try {

                                    JSONObject jsonObject = new JSONObject(response.body().toString());
                                    JSONArray resultArray = jsonObject.getJSONArray("results");
                                    JSONObject firstObject = resultArray.getJSONObject(0);

                                    if (firstObject != null)
                                        address = firstObject.getString("formatted_address");
                                    else
                                        address = "";

                                    //Then set this address to edtAddress
                                    ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                            .setText(address);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(Cart.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Toast.makeText(Cart.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        address = "";
                        Toast.makeText(Cart.this, "Location is NULL", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        alertDialog.setIcon(R.mipmap.ic_shop_cart_black);
        alertDialog.setView(order_address_comment);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                comment = edtComment.getText().toString();

                //Check Payment
                if (!rdiCOD.isChecked() && !rdiPaypal.isChecked() && !rdiBalance.isChecked()){  //If both COD and paypal and Balance is not checked

                    Toast.makeText(Cart.this, "Please select Payment options", Toast.LENGTH_SHORT).show();
                    //Remove Fragment (Google Places API)
                    getSupportFragmentManager()
                            .beginTransaction()
                            .remove(getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();

                    return;
                }
                else if (rdiPaypal.isChecked()){

                    //If Paypal run successfully , we must CUT code in (CODE 1) and paste in onActivityResult()
                    usingPaypalForPayment();

                }
                else if (rdiCOD.isChecked()){

                    usingCODForPayment();
                    //for we don't complete this method , where we will see add two request in Firebase's database
                    //If Paypal run successfully , and cut and paste in usingPaypalForPayment() , must remove this (return)
                    return;
                }
                else if (rdiBalance.isChecked()){

                    usingEatItBalanceForPayment();
                    //for we don't complete this method , where we will see add two request in Firebase's database
                    //If Paypal run successfully , and cut and paste in usingPaypalForPayment() , must remove this (return)
                    return;

                }

                //Add check Delivery condition here
                //If user select address from Place fragment , just use it
                //If use Ship to this address , get Address from location and use it
                //If use select Home address , get Home address from Retrofit and use it
                if (!rdiHomeAddress.isChecked() && !rdiShipToAddress.isChecked()){

                    //if both radio is not selected ->
                    if (shippingAddress != null && !shippingAddress.getAddress().toString().isEmpty()){

                        address = shippingAddress.getAddress().toString();
                    }
                    else {
                        Toast.makeText(Cart.this, "Please enter address or select options address " +
                                "OR your placed isn't supported by Google Places", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                //else if (rdiShipToThis Address) coded above


                if (TextUtils.isEmpty(address)){  //this mean redioButton Ship to this address OR (Enter your address) was clicked and result is null or empty

                    address = "";
                }

                /////////////////////////////////////Create new Request  (copy start CODE 1)
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        address,
                        txtTotalPrice.getText().toString(),
                        "0",
                        comment,
                        "paypal",
                        "approved",   //we assume the client pay the order , so save "approved"
                        //by using paypal SDK, add this line instance of "approved"
                        //jsonObject.getJSONObject("response").getString("state")
                        (mLastLocation != null)?
                                String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()) : "36.192984,37.117703",
                        cart
                );
                //Submit to FireBase
                //We will using  System.currentMilli  to Key
                String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number)
                        .setValue(request);
                //Delete Cart
                new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                sendNotificationOrder(order_number);

                Toast.makeText(Cart.this, "Thank you , order placed", Toast.LENGTH_SHORT).show();
                finish();

                //Remove Fragment (Google Places API)
                getSupportFragmentManager()
                        .beginTransaction()
                        .remove(getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
                //////////////////////////////////////////Create new Request  (copy end CODE 1)

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();


                //Remove Fragment (Google Places API)
                //Basically you're calling hide(), remove(), etc., with a null value
                //So we add this condition to be sure that (shippingAddress) is't null
                // if (shippingAddress != null)
                getSupportFragmentManager()
                        .beginTransaction()
                        .remove(getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
            }
        });

        alertDialog.setCancelable(false); //Fix crush inFlate fragment

        alertDialog.show();
    }

    private void buildLocationCallBack() {

        locationCallback = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {

                mLastLocation = locationResult.getLastLocation();
            }

        };
    }

    @SuppressLint("RestrictedApi")
    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
    }

    private void usingEatItBalanceForPayment() {

        double amount = 0;

        //First , we will get total price from txtTotalPrice
        try {
            amount = Common.formatCurrent(txtTotalPrice.getText().toString() , Locale.US).doubleValue();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        //After receive total price of this order , just compare with user balance
        if (Double.parseDouble(Common.currentUser.getBalance().toString())>= amount){

            //here we must paste Code that save order in FireBase's database (i mean CODE 1)
            //with change paymentMethod & paymentState & LatLng
            Request request = new Request(
                    Common.currentUser.getPhone(),
                    Common.currentUser.getName(),
                    address,
                    txtTotalPrice.getText().toString(),
                    "0",
                    comment,
                    "EatIt Balance",
                    "Paid",
                    (mLastLocation != null)?
                            String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()) : "36.192984,37.117703",  //Coordinates when user order
                    cart
            );
            //Submit to FireBase
            //We will using  System.currentMilli  to Key
            final String order_number = String.valueOf(System.currentTimeMillis());
            requests.child(order_number)
                    .setValue(request);
            //Delete Cart
            new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());


            //update balance
            double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) - amount;
            Map<String , Object> update_balance = new HashMap<>();
            update_balance.put("balance" , balance);

            FirebaseDatabase.getInstance().getReference("User")
                    .child(Common.currentUser.getPhone())
                    .updateChildren(update_balance)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                //Refresh user
                                FirebaseDatabase.getInstance().getReference("User")
                                        .child(Common.currentUser.getPhone())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                Common.currentUser = dataSnapshot.getValue(User.class);
                                                //Send Order to server
                                                sendNotificationOrder(order_number);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }
                    });


            Toast.makeText(Cart.this, "Thank you , order placed", Toast.LENGTH_SHORT).show();
            finish();

            //Remove Fragment (Google Places API)
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                    .commit();

        }
        else {

            Toast.makeText(Cart.this, "Your balance not enough , Please choose other payment", Toast.LENGTH_SHORT).show();
        }

    }

    private void usingCODForPayment() {


        Request request = new Request(
                Common.currentUser.getPhone(),
                Common.currentUser.getName(),
                address,
                txtTotalPrice.getText().toString(),
                "0",
                comment,
                "COD",
                "Unpaid",
                (mLastLocation != null)?
                        String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()) : "36.192984,37.117703",  //Coordinates when user order
                cart
        );
        //Submit to FireBase
        //We will using  System.currentMilli to Key
        String order_number = String.valueOf(System.currentTimeMillis());
        requests.child(order_number)
                .setValue(request);
        //Delete Cart
        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

        sendNotificationOrder(order_number);

        Toast.makeText(Cart.this, "Thank you , order placed", Toast.LENGTH_SHORT).show();
        finish();

        //Remove Fragment (Google Places API)
        getSupportFragmentManager()
                .beginTransaction()
                .remove(getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                .commit();
    }

    private void usingPaypalForPayment() {

        //Show PayPal to payment
        String formatAmount = txtTotalPrice.getText().toString()
                .replace("$", "")
                .replace(",", "");

        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount),
                "USD",
                "Eat It App Order",
                PayPalPayment.PAYMENT_INTENT_SALE);

        //First , get address and comment from Alert Dialog (we can't use paypal in syria , so i commented last line)
        Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        //startActivityForResult(intent , PAYPAL_REQUEST_CODE);
    }

    private void initMyLocation() {

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= 23)
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, CODE_REQUEST_PERMISSION_lOCATION);

            else{
                mLastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (mLastLocation == null)
                    mLastLocation = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
        }


    }

    private void sendNotificationOrder(final String order_number) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("serverToken").equalTo(true);  //get all node with isServerToken is true
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()){

                    Token serverToken = postSnapShot.getValue(Token.class);

                    //Create raw payload to send
                    Map<String , String> dataSend = new HashMap<>();
                    dataSend.put("title" , "ABD");
                    dataSend.put("message" , "You have new order " + order_number);
                    DataMessage dataMessage = new DataMessage(serverToken.getToken() , dataSend);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    //Only true when get result
                                    if (response.code() == 200){
                                        if (response.body().success == 1){

                                            //Toast.makeText(Cart.this, "Thank you , order place", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                        else
                                            Toast.makeText(Cart.this, "Failed !!!", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadListFood() {

        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart , this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate total price
        int total = 0;

        for (Order order : cart)
            total += (Integer.parseInt(order.getPrice()) * Integer.parseInt(order.getQuantity()));

        Locale locale = new Locale("en" , "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));
    }

    private void deleteCart(int position) {

        //We will remove item at List<Order> by position
        cart.remove(position);
        //After that , we will delete all old data from SQLite
        new Database(this).cleanCart(Common.currentUser.getPhone());
        //And final , we will update new data from List<Order> to SQLite
        for (Order item : cart)
            new Database(this).addToCart(item);

        //Refresh
        loadListFood();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PAYPAL_REQUEST_CODE){

            if (resultCode == RESULT_OK){

                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null){

                    try {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);

                        //here we must paste Code that save order in FireBase's database (i mean CODE 1)
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (resultCode == RESULT_CANCELED)
                Toast.makeText(this, "Payment cancel", Toast.LENGTH_SHORT).show();

            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(this, "Invalid payment", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){

            case CODE_REQUEST_PERMISSION_lOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    initMyLocation();
                else
                    Toast.makeText(this, "You can't use the location service", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof CartViewHolder){

            String name = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());

            final int deleteIndex = viewHolder.getAdapterPosition();
            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId() , Common.currentUser.getPhone());

            //Update txtTotalPrice
            //Calculate total price
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());

            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice()) * Integer.parseInt(item.getQuantity()));

            Locale locale = new Locale("en" , "US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

            txtTotalPrice.setText(fmt.format(total));

            //Make Snackbar
            Snackbar snackbar = Snackbar.make(rootLayout , name + " is removed from cart !" , Snackbar.LENGTH_LONG );
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    adapter.restoreItem(deleteItem ,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    //Update txtTotalPrice
                    //Calculate total price
                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());

                    for (Order item : orders)
                        total += (Integer.parseInt(item.getPrice()) * Integer.parseInt(item.getQuantity()));

                    Locale locale = new Locale("en" , "US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                    txtTotalPrice.setText(fmt.format(total));

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
