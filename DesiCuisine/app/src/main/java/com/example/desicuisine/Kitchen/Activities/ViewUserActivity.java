package com.example.desicuisine.Kitchen.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.example.desicuisine.Models.User;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Adapters.KitchenViewUserAdapter;
import com.example.desicuisine.Utils.InternetHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewUserActivity extends AppCompatActivity {
    private KitchenViewUserAdapter adapter;
    private RecyclerView revKitchen;
    private ProgressBar progressBar;
    private ArrayList<User> kitchenList;
    private String fr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user);
        connectivity();
        if (internetNotConnected(ViewUserActivity.this)) {
            return;
        }


        adapter = new KitchenViewUserAdapter(ViewUserActivity.this, kitchenList);
        revKitchen.setHasFixedSize(true);
        revKitchen.setLayoutManager(new LinearLayoutManager(ViewUserActivity.this));
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("Customer");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                kitchenList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        User kitchen = dataSnapshot1.getValue(User.class);
                        kitchenList.add(kitchen);
                    }
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                } else {
                    showNotFound();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ViewUserActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        revKitchen.setAdapter(adapter);


    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
        i.putExtra("Fragment", fr);
        startActivity(i);
        Animatoo.animateSwipeLeft(ViewUserActivity.this);
        finish();

    }


    private void connectivity() {
        setTitle("Users");
        fr = getIntent().getStringExtra("Fragment");
        revKitchen = findViewById(R.id.kitchenViewRv);
        progressBar = findViewById(R.id.kitchenViewProgressBar);
        kitchenList = new ArrayList<>();

    }

    private void showNotFound() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Button okBtn;
        TextView alertTv;
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        alertTv = alertView.findViewById(R.id.alertTv);
        alertTv.setText("Sorry! No User Exist");

        builder.setView(alertView);

        final AlertDialog soryAlert = builder.create();
        soryAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        soryAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                soryAlert.dismiss();
                Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                i.putExtra("Fragment", fr);
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewUserActivity.this);
                finish();


            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soryAlert.dismiss();
                Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                i.putExtra("Fragment", fr);
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewUserActivity.this);
                finish();

            }
        });
        soryAlert.setCanceledOnTouchOutside(false);
        soryAlert.show();


    }


    private boolean internetNotConnected(Context mContext) {
        InternetHelper obj = new InternetHelper();

        if (obj.isInternetConnected(mContext)) {
//            Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            showNoInternetMsg(mContext);
//            Toast.makeText(mContext, "Not Connected", Toast.LENGTH_SHORT).show();
            return true;
        }

    }

    private void showNoInternetMsg(final Context mContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
                noInternetAlert.dismiss();
                Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                i.putExtra("Fragment", fr);
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewUserActivity.this);
                finish();

            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetAlert.dismiss();
            }
        });
        noInternetAlert.show();
        Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
        i.putExtra("Fragment", fr);
        startActivity(i);
        Animatoo.animateSwipeLeft(ViewUserActivity.this);
        finish();

    }

}
