package com.example.asus.androideatitshipper.Service;

import com.example.asus.androideatitshipper.Common.Common;
import com.example.asus.androideatitshipper.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        updateToService(refreshedToken);
    }

    private void updateToService(String refreshedToken) {

        if (Common.currentShipper != null){

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference tokens = database.getReference("Tokens");
            Token token = new Token(refreshedToken , true);  //true becuz this token send from Server Side
            tokens.child(Common.currentShipper.getPhone()).setValue(token);
        }

    }
}
