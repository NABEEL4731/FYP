package com.example.desicuisine.User.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.desicuisine.Models.Order;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Adapters.OrderAdapter;
import com.example.desicuisine.Utils.InternetHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewOrderUserActivity extends AppCompatActivity implements OrderAdapter.OnOrderCardItemClickListener {
    private RecyclerView orderRV;
    private ArrayList<Order> orderList;
    private Boolean foundShowNot = true;
    private ProgressBar progressBar;
    private OrderAdapter adapter;


    private String mysp = "MYSP", category;
    private SharedPreferences sp;
    private ProgressDialog progressDialog;
    private LinearLayoutManager mLayoutManager;

    private FirebaseAuth mAuth;
    private Query query;

    private String latitudeUser, longitudeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order_user);

        connectivity();


        orderList = new ArrayList<>();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (internetNotConnected(ViewOrderUserActivity.this)) {
                    return;
                }
            }
        }, 1000);

        query = FirebaseDatabase.getInstance().getReference()
                .child("Orders").orderByChild("userID").equalTo(mAuth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(valueListener);


        adapter = new OrderAdapter(this, orderList);
        sp = getSharedPreferences(mysp, MODE_PRIVATE);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        orderRV.setLayoutManager(mLayoutManager);
        orderRV.setHasFixedSize(true);
        orderRV.setAdapter(adapter);
        adapter.setOnOrderCardItemClickListener(this);

    }


    ValueEventListener valueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            orderList.clear();
            if (dataSnapshot.exists()) {
                if (category.equals("My All Orders")) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Order o = dataSnapshot1.getValue(Order.class);
                        orderList.add(o);
                        foundShowNot = false;

                    }
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                } else if (category.equals("My Pending Orders")) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Order o = dataSnapshot1.getValue(Order.class);
                        if (o.orderStatus.equals("Pending") && o.orderDescription.equals("Normal Order")) {
                            orderList.add(o);
                            foundShowNot = false;
                        }

                    }
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                } else if (category.equals("Special Orders")) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Order o = dataSnapshot1.getValue(Order.class);
                        if (o.productID.equals("Special") && o.orderStatus.equals("In Process")) {
                            orderList.add(o);
                            foundShowNot = false;
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                } else {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Order o = dataSnapshot1.getValue(Order.class);
                        if (o.orderStatus.equals("In Process") && o.orderDescription.equals("Normal Order")) {
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
            Toast.makeText(ViewOrderUserActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Fragment", "B");
        i.putExtras(bundle);
        startActivity(i);
        Animatoo.animateSwipeLeft(ViewOrderUserActivity.this);
        finish();

    }

    private void connectivity() {
        foundShowNot = true;
        progressBar = findViewById(R.id.OrderProBarUserPB);
        orderRV = findViewById(R.id.orderUserRv);
        sp = getSharedPreferences(mysp, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        //        initilize default
        Bundle bundle = getIntent().getExtras();
        category = bundle.getString("CHOISE", "Not Selected");

        setTitle(category);


        latitudeUser = sp.getString("Lat", null);
        longitudeUser = sp.getString("Long", null);


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
                bundle.putString("Fragment", "B");
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewOrderUserActivity.this);
                finish();

            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetAlert.dismiss();
                Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Fragment", "B");
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewOrderUserActivity.this);
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
                bundle.putString("Fragment", "B");
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewOrderUserActivity.this);
                finish();

            }
        });


        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soryAlert.dismiss();
                Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Fragment", "B");
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateSwipeLeft(ViewOrderUserActivity.this);
                finish();
            }
        });
        soryAlert.show();

    }


    @Override
    public void setOrderCardClickListener(int position) {
        final Order obj = orderList.get(position);
        TextView proNameTv, proPriceTv, cookNameTv, dateTv, orderStatusTv, orderCategoryTV, emailTv, descTv;
        Button cencelBtn, locationBtn;
        LinearLayout layout;
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewOrderUserActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.alert_order_view_layout, null);
        emailTv = alertView.findViewById(R.id.viewOrderKitchenEmailAlertTv);
        proNameTv = alertView.findViewById(R.id.proNameOrderViewHolder);
        proPriceTv = alertView.findViewById(R.id.proPriceOrderViewHolder);
        orderStatusTv = alertView.findViewById(R.id.orderStatusViewHolder);
        cookNameTv = alertView.findViewById(R.id.cookNameOrderViewHolder);
        dateTv = alertView.findViewById(R.id.dateOrderViewHolder);
        descTv = alertView.findViewById(R.id.orderDescriptionViewHolder);
        layout = alertView.findViewById(R.id.layoutVV);
        orderCategoryTV = alertView.findViewById(R.id.orderCategoryViewHolder);
        cencelBtn = alertView.findViewById(R.id.cencelOrderBtn);
        locationBtn = alertView.findViewById(R.id.locationOrderBtn);
        orderCategoryTV.setText(obj.productCategory);
        proNameTv.setText(obj.productName);
        proPriceTv.setText(obj.productPrice);
        emailTv.setText(obj.kitchenEmail);
        cookNameTv.setText(obj.cookName);
        orderStatusTv.setText(obj.orderStatus);
        descTv.setText(obj.orderDescription);
        dateTv.setText("Date " + obj.orderDate);

        if (obj.productID.equals("Special")) {
            layout.setVisibility(View.VISIBLE);
        }
        builder.setView(alertView);
        final AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);

        if (obj.orderStatus.equals("Pending")) {
            cencelBtn.setVisibility(View.VISIBLE);
        }

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetNotConnected(ViewOrderUserActivity.this)) {
                    return;
                }

                showMap(ViewOrderUserActivity.this,
                        obj.productLatitude, obj.productLongitude, latitudeUser, longitudeUser);
            }
        });
        cencelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetNotConnected(ViewOrderUserActivity.this)) {
                    return;
                }

                AlertDialog.Builder adB = new AlertDialog.Builder(ViewOrderUserActivity.this);
                adB.setMessage("Are you sure you want to cencel order?")
                        .setCancelable(false)
                        .setIcon(R.drawable.logo_app)
                        .setTitle("Please Confirm");

                adB.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        createProgressDialog();
                        progressDialog.show();

                        String statusKey = "orderStatus";
                        String newStatus = "Cenceled";

                        Map<String, Object> statusUpdate = new HashMap<String, Object>();
                        statusUpdate.put(statusKey, newStatus);

                        FirebaseDatabase.getInstance().getReference().child("Orders").child(obj.orderId).updateChildren(statusUpdate).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(ViewOrderUserActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        })
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        alert.dismiss();
                                        Toast.makeText(ViewOrderUserActivity.this, "Sucessfully Cenceled", Toast.LENGTH_SHORT).show();
                                        recreate();
                                    }
                                });
                    }
                });
                adB.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                adB.setNeutralButton("Cencel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                adB.show();

            }
        });


        alert.show();
    }

    private void showMap(Context context, String kitLati, String kitLon, String latitudeUser, String longitudeUser) {
        Intent intent = null;
        if (latitudeUser != null && latitudeUser != null) {
            intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr=" + latitudeUser + "," + longitudeUser + "&daddr=" + kitLati + "," + kitLon));
        } else {
            intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=" + kitLati + "," + kitLon));
        }
        context.startActivity(intent);
    }

    private void createProgressDialog() {
        progressDialog = new ProgressDialog(ViewOrderUserActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Cenceling Order...");

    }
}
