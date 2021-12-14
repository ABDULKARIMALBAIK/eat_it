package com.example.asus.androideatitshipper;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.asus.androideatitshipper.Common.Common;
import com.example.asus.androideatitshipper.Model.Shipper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class SignIn extends AppCompatActivity {

    MaterialEditText edt_phone , edt_password;
    Button btn_sign_in;

    FirebaseDatabase database;
    DatabaseReference shippers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edt_phone = (MaterialEditText)findViewById(R.id.edtPhone);
        edt_password = (MaterialEditText)findViewById(R.id.edtPassword);
        btn_sign_in = (Button)findViewById(R.id.btnSignIn);

        //Init FireBase
        database = FirebaseDatabase.getInstance();
        shippers = database.getReference(Common.SHIPPERS_TABLE);

        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!edt_phone.getText().toString().isEmpty() && !edt_password.getText().toString().isEmpty()){

                    if (Common.isConnectionToInternet(getApplicationContext()))
                        login(edt_phone.getText().toString() , edt_password.getText().toString());

                    else
                        Toast.makeText(SignIn.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(SignIn.this, "Please all information fields ", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void login(String phone, final String password) {

        shippers.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        
                        if (dataSnapshot.exists()){

                            Shipper shipper = dataSnapshot.getValue(Shipper.class);
                            if (shipper.getPassword().equals(password)){

                                //Login
                                startActivity(new Intent(SignIn.this , HomeActivity.class));
                                Common.currentShipper = shipper;
                                finish();
                            }
                            else
                                Toast.makeText(SignIn.this, "Password is wrong !", Toast.LENGTH_SHORT).show();
                        }
                        
                        else {
                            Toast.makeText(SignIn.this, "The shipper isn't exists in Database", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
}
