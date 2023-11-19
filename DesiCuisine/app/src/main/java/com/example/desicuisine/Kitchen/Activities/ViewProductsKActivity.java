package com.example.desicuisine.Kitchen.Activities;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.desicuisine.Models.Product;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Adapters.ProductAdapter;
import com.example.desicuisine.Utils.InternetHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewProductsKActivity extends AppCompatActivity {

    private RecyclerView allProRV;
    private ArrayList<Product> products;
    private Boolean foundShowNot = true;
    private ProgressBar progressBar;
    private ProductAdapter adapter;
    private String mysp = "MYSP", category;
    private SharedPreferences sp;
    private LinearLayoutManager mLayoutManager;

    private FirebaseAuth mAuth;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_products_k);
        connectivity();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (internetNotConnected(ViewProductsKActivity.this)) {
                    return;
                }
            }
        }, 1000);

        products = new ArrayList<>();
        query = FirebaseDatabase.getInstance().getReference()
                .child("Products").orderByChild("kitchenID").equalTo(mAuth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(valueListener);

        adapter = new ProductAdapter(this, products);
        sp = getSharedPreferences(mysp, MODE_PRIVATE);
        mLayoutManager = new LinearLayoutManager(this);
        // this will load the items from bottom means newest first
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        allProRV.setLayoutManager(mLayoutManager);
        allProRV.setHasFixedSize(true);
        allProRV.setAdapter(adapter);

    }

    ValueEventListener valueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            products.clear();
            if (dataSnapshot.exists()) {

                if (category.equals("Available Products")) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Product rfs = dataSnapshot1.getValue(Product.class);
                        if (rfs.status.equals("Available")) {
                            products.add(rfs);
                            foundShowNot = false;
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                } else {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Product rfs = dataSnapshot1.getValue(Product.class);
                        products.add(rfs);
                        foundShowNot = false;

                    }
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }

            }
            if (foundShowNot) {
                showNotFound();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Toast.makeText(ViewProductsKActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };


    private void connectivity() {
        progressBar = findViewById(R.id.allProProgressBarK);
        allProRV = findViewById(R.id.allProductKitRv);
        sp = getSharedPreferences(mysp, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        //        initilize default
        Bundle bundle = getIntent().getExtras();
        category = bundle.getString("CHOISE", "Not Selected");
        setTitle(category);
        foundShowNot = true;
        products = new ArrayList<>();


    }

    private boolean internetNotConnected(Context mContext) {
        InternetHelper obj = new InternetHelper();

        if (obj.isInternetConnected(mContext)) {
            return false;
        } else {
            showNoInternetMsg();
            return true;
        }

    }

    private void showNoInternetMsg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Button okBtn;
        TextView alertTv;
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        alertTv = alertView.findViewById(R.id.alertTv);
        alertTv.setText("Sorry! No Internet Connection");
        builder.setView(alertView);

        final AlertDialog noInternetAlert = builder.create();
        noInternetAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        noInternetAlert.setCanceledOnTouchOutside(false);
        noInternetAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                i.putExtra("Fragment", "B");
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewProductsKActivity.this);
                finish();

            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetAlert.dismiss();

                Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                i.putExtra("Fragment", "B");
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewProductsKActivity.this);
                finish();
            }
        });
        noInternetAlert.show();
    }


    private void showNotFound() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Button okBtn;
        TextView alertTv;
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        alertTv = alertView.findViewById(R.id.alertTv);
        alertTv.setText("Sorry! No Record Found");
        builder.setView(alertView);

        final AlertDialog soryAlert = builder.create();
        soryAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        soryAlert.setCanceledOnTouchOutside(false);
        soryAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                i.putExtra("Fragment", "B");
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewProductsKActivity.this);
                finish();
            }
        });


        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soryAlert.dismiss();

                Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                i.putExtra("Fragment", "B");
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewProductsKActivity.this);
                finish();
            }
        });
        soryAlert.show();

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
        i.putExtra("Fragment", "B");
        startActivity(i);
        Animatoo.animateSwipeLeft(ViewProductsKActivity.this);
        finish();

    }
}
