package com.example.desicuisine.User.Activities;

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
import com.example.desicuisine.Models.Order;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Adapters.WeeklyOrderAdapter;
import com.example.desicuisine.Utils.InternetHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewWeeklyOrderUserActivity extends AppCompatActivity {
    private RecyclerView allProRV;
    private ArrayList<Order> orderList;
    private Boolean foundShowNot = true;
    private ProgressBar progressBar;
    private WeeklyOrderAdapter adapter;
    private String mysp = "MYSP", choise;
    private SharedPreferences sp;
    private LinearLayoutManager mLayoutManager;

    private FirebaseAuth mAuth;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_weekly_order_user);
        connectivity();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (internetNotConnected(ViewWeeklyOrderUserActivity.this)) {
                    return;
                }
            }
        }, 1000);

        query = FirebaseDatabase.getInstance().getReference()
                .child("Weekly Offer Orders").orderByChild("userID").equalTo(mAuth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(valueListener);

        adapter = new WeeklyOrderAdapter(this, orderList, sp.getString("category","Customer"));
        mLayoutManager = new LinearLayoutManager(this);

        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        allProRV.setLayoutManager(mLayoutManager);
        allProRV.setHasFixedSize(true);
        allProRV.setAdapter(adapter);

    }

    ValueEventListener valueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            orderList.clear();
            if (dataSnapshot.exists()) {
                if (choise.equals("Pending Offers")) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Order o = dataSnapshot1.getValue(Order.class);
                        if (o.orderStatus.equals("Pending")) {
                            orderList.add(o);
                            foundShowNot = false;
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
                if (choise.equals("Accepted")) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Order o = dataSnapshot1.getValue(Order.class);
                        if (o.orderStatus.equals("Accepted")) {
                            orderList.add(o);
                            foundShowNot = false;
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
                if (choise.equals("Rejected")) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Order o = dataSnapshot1.getValue(Order.class);
                        if (o.orderStatus.equals("Rejected")) {
                            orderList.add(o);
                            foundShowNot = false;
                        }
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
            Toast.makeText(ViewWeeklyOrderUserActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };


    private void connectivity() {
        progressBar = findViewById(R.id.weeklyOrderKitPB);
        allProRV = findViewById(R.id.weeklyOrderKitRv);
        sp = getSharedPreferences(mysp, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        foundShowNot = true;
        orderList = new ArrayList<>();
        choise = getIntent().getStringExtra("CHOISE");
        if (choise.equals("Pending Offers")) {
            setTitle("My Pending Menu");
        } else if (choise.equals("Accepted")) {
            setTitle("My Subcribed Menu");
        } else if (choise.equals("Rejected")) {
            setTitle("My Rejected Menu");
        }


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
                Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Fragment", "C");
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewWeeklyOrderUserActivity.this);
                finish();

            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetAlert.dismiss();
                Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Fragment", "C");
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewWeeklyOrderUserActivity.this);
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
                Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Fragment", "C");
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewWeeklyOrderUserActivity.this);
                finish();
            }
        });


        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soryAlert.dismiss();
                Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Fragment", "C");
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewWeeklyOrderUserActivity.this);
                finish();
            }
        });
        soryAlert.show();

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Fragment", "C");
        i.putExtras(bundle);
        startActivity(i);
        Animatoo.animateSwipeLeft(ViewWeeklyOrderUserActivity.this);
        finish();

    }
}

