package com.example.asus.androideatitserver.Service;

import com.example.asus.androideatitserver.Common.Common;
import com.example.asus.androideatitserver.Model.Token;
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

        if (Common.currentUser != null){

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference tokens = database.getReference("Tokens");
            Token token = new Token(refreshedToken , true);  //true becuz this token send from Server Side
            tokens.child(Common.currentUser.getPhone()).setValue(token);
        }

    }
}
