package com.example.desicuisine;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.desicuisine.Kitchen.Activities.KitchenDashboardActivity;
import com.example.desicuisine.User.Activities.UserDashboardActivity;
import com.example.desicuisine.User.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;



public class MainActivity extends Activity {
    private FirebaseAuth mAuth;

    private String mysp = "MYSP";
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        final Thread t = new Thread() {
            public void run() {

                try {
//                    sleep
                    sleep(1000);
                } catch (Exception e) {
                    e.getMessage();
                }

                mAuth = FirebaseAuth.getInstance();
                sp = getSharedPreferences(mysp, MODE_PRIVATE);
                String userCategory = sp.getString("category", null);

//                if someone is already login
                if (mAuth.getCurrentUser() != null && userCategory != null) {

//                    User
                    if (userCategory.equals("Customer")) {
                        Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("Fragment", "A");
                        i.putExtras(bundle);
                        startActivity(i);
                        Animatoo.animateSlideUp(MainActivity.this);
                        finish();
                    }
//                    Kitchen
                    else {
                        Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                        i.putExtra("Fragment", "A");
                        Animatoo.animateSlideUp(MainActivity.this);
                        startActivity(i);
                        finish();

                    }


                }
//                login screen
                else {
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);
                    Animatoo.animateSlideUp(MainActivity.this);
                    finish();
                }

            }
        };
        t.start();

    }

}
