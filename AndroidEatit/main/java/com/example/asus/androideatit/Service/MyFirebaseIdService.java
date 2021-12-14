package com.example.asus.androideatit.Service;

import com.example.asus.androideatit.Common.Common;
import com.example.asus.androideatit.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService{


    String tokenRefreshed;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        tokenRefreshed = FirebaseInstanceId.getInstance().getToken();
        if (Common.currentUser != null)
            updateTokenToFirebase();
    }

    private void updateTokenToFirebase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tokens = database.getReference("Tokens");
        Token token = new Token(tokenRefreshed , false);  //false becuz this token send from Client App
        tokens.child(Common.currentUser.getPhone()).setValue(token);
    }
}
