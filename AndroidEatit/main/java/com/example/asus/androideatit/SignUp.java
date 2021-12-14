 package com.example.asus.androideatit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.asus.androideatit.Common.Common;
import com.example.asus.androideatit.Model.User;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


 public class SignUp extends AppCompatActivity {

     private static final int REQUEST_CODE = 7171 ;

     MaterialEditText edtPhone , edtName , edtPassword , edtSecureCode;
     Button btnSignUp;
     LinearLayout facebookSignUp;

     AlertDialog waitingDialog;

     FirebaseDatabase database;
     DatabaseReference table_user;

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

        setContentView(R.layout.activity_sign_up);

        edtPhone = (MaterialEditText)findViewById(R.id.edtPhone);
        edtName = (MaterialEditText)findViewById(R.id.edtName);
        edtPassword = (MaterialEditText)findViewById(R.id.edtPassword);
        edtSecureCode = (MaterialEditText)findViewById(R.id.edtSecureCode);
        btnSignUp = (Button)findViewById(R.id.btnSignUp);
        facebookSignUp = (LinearLayout)findViewById(R.id.SignUpFaceBook);

        //Fix showing characters in MaterialEditText password
        Typeface typeface = Typeface.DEFAULT;
        edtPassword.setTypeface(typeface);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectionToInternet(SignUp.this)) {

                    final AlertDialog dialog = new SpotsDialog(SignUp.this);
                    dialog.setMessage("Please waiting...");
                    dialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            //check if already user phone
                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()){

                                dialog.dismiss();
                                Toast.makeText(SignUp.this, "Phone Number already register", Toast.LENGTH_SHORT).show();
                            }
                            else {

                                dialog.dismiss();

                                User user = new User(edtName.getText().toString() ,
                                        edtPassword.getText().toString(),
                                        edtSecureCode.getText().toString());
                                user.setBalance(0.0);
                                table_user.child(edtPhone.getText().toString()).setValue(user);

                                Intent intentHome = new Intent(SignUp.this , Home.class);
                                Common.currentUser = user;
                                startActivity(intentHome);
                                finish();

                                Toast.makeText(SignUp.this, "Sign Up Successfully !", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(SignUp.this, "Please check your connection !!!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });


        facebookSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectionToInternet(getApplicationContext())){

                    if (AccountKit.getCurrentAccessToken() != null){   //he have an account in Facebook Account kit
                        Toast.makeText(SignUp.this, "You already have an account ,just sign in by FaceBook account", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else { //he doesn't have an account

                        Intent intent = new Intent(SignUp.this , AccountKitActivity.class);
                        AccountKitConfiguration.AccountKitConfigurationBuilder builder =
                                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                                        AccountKitActivity.ResponseType.TOKEN);
                        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION , builder.build());
                        startActivityForResult(intent , REQUEST_CODE);
                    }

                }
                else {
                    Toast.makeText(SignUp.this, "Please check your connection !!!", Toast.LENGTH_SHORT).show();
                    return;
                }



            }
        });

    }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         if (requestCode == REQUEST_CODE){

             AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

             if (result.getError() != null){
                 Toast.makeText(this, result.getError().getErrorType().toString(), Toast.LENGTH_SHORT).show();
                 return;
             }
             else if (result.wasCancelled()){
                 Toast.makeText(this, "Canceled !!!", Toast.LENGTH_SHORT).show();
                 return;
             }
             else {

                 checkUserAccount(result);

             }

         }
     }

     private void checkUserAccount(final AccountKitLoginResult result) {

         if (result.getAccessToken() != null){  //Sign up is completed Successfully

             //Show Dialog
             waitingDialog = new SpotsDialog(this);
             waitingDialog.show();
             waitingDialog.setMessage("Please waiting...");
             waitingDialog.setCancelable(false);

             //Get current Phone
             AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                 @Override
                 public void onSuccess(Account account) {

                     final String userPhone = account.getPhoneNumber().toString();

                     //Check if the user exists in FireBase database' table of User
                     table_user.orderByKey().equalTo(userPhone)
                             .addValueEventListener(new ValueEventListener() {
                                 @Override
                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                     if (!dataSnapshot.child(userPhone).exists()){  //If not exists

                                         //We will create new user and login
                                         addNewUser(userPhone);
                                     }
                                     else {  //If exists

                                         waitingDialog.dismiss();
                                         Toast.makeText(SignUp.this, "You have already account ,just sign in by FaceBook account", Toast.LENGTH_SHORT).show();
                                         return;
                                     }
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError databaseError) {

                                 }
                             });
                 }

                 @Override
                 public void onError(AccountKitError accountKitError) {
                     waitingDialog.dismiss();
                     Toast.makeText(SignUp.this, accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                 }
             });
         }
     }

     private void addNewUser(final String phone) {

         String name = "___" , password = "___" , secureCode = "___";

         User newUser = new User();
         newUser.setName(name);
         newUser.setPassword(password);
         newUser.setSecureCode(secureCode);
         newUser.setIsStaff("false");
         newUser.setBalance(0.0);

         //Add to FireBase database
         table_user.child(phone)
                 .setValue(newUser)
                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {

                         if (task.isSuccessful())
                             Toast.makeText(SignUp.this, "User register successfully !!!", Toast.LENGTH_SHORT).show();

                         loginHome(phone);
                     }
                 })
                 .addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {
                         Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                     }
                 });
     }

     private void loginHome(final String phone) {

         table_user.child(phone)
                 .addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                         User localUser = dataSnapshot.getValue(User.class);

                         Intent homeIntent = new Intent(SignUp.this, Home.class);
                         Common.currentUser = localUser;
                         Common.currentUser.setPhone(phone);
                         startActivity(homeIntent);

                         waitingDialog.dismiss();
                         finish();

                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });

     }
 }
