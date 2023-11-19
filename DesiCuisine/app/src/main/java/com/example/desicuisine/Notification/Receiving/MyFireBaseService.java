package com.example.desicuisine.Notification.Receiving;

import com.example.desicuisine.Notification.Sending.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFireBaseService extends FirebaseMessagingService {

    private FirebaseDatabase firebaseDatabase = null;
    private String userId = null;

    //instance id service
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();

        String newToken = FirebaseInstanceId.getInstance().getToken();

        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            updateToken(newToken);
        }
    }

    private void updateToken(String newToken) {
        Token token1 = new Token(newToken);
        firebaseDatabase.getReference("Tokens").child(userId).setValue(token1);
    }
}