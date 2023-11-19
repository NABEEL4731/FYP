package com.example.desicuisine.User.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.desicuisine.Models.Order;
import com.example.desicuisine.Models.User;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Adapters.KitchenViewUserAdapter;
import com.example.desicuisine.Utils.InternetHelper;
import com.example.desicuisine.Utils.Validations;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UserSpecialOrderActivity extends AppCompatActivity implements View.OnClickListener, KitchenViewUserAdapter.OnkitchenViewClickListener {
    private EditText titleEt, descriptionEt, maxPriceEt;

    private String titleOrder, descriptionOrder, priceMaxOrder;
    private Button sendButton;
    private FirebaseAuth mAuth;
    private SharedPreferences sp;
    private String mySp = "MYSP";

    private String latitudeUser, longitudeUser;
    private ProgressDialog progressDialog;
    private FirebaseUser user;

    private DatabaseReference mOrderRef;

    private ArrayList<User> kitchenList;
    private AlertDialog alert;
    private String orderToKitchenID;
    private ProgressBar progressBar;
    private Button cencleBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_special_order);
        connectivity();
        sendButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
//            instance expert
            case R.id.pickButtonSp:
                setUpSendBtnMethod();
                break;
        }
    }

    private void setUpSendBtnMethod() {

        if (allNotValid()) {
            return;
        }
        if (internetNotConnected(UserSpecialOrderActivity.this)) {
            return;
        }
        titleOrder = titleEt.getText().toString().trim();
        descriptionOrder = descriptionEt.getText().toString().trim();
        priceMaxOrder = maxPriceEt.getText().toString().trim();

        kitchenList = new ArrayList<>();

        final KitchenViewUserAdapter adapter;
        RecyclerView revKitchen;
        AlertDialog.Builder builder = new AlertDialog.Builder(UserSpecialOrderActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.kitchen_view_layout_alert, null);
        revKitchen = alertView.findViewById(R.id.kitchenAlertRv);
        progressBar = alertView.findViewById(R.id.kitchenAlertProgressBar);
        cencleBtn = alertView.findViewById(R.id.cencleAlertexpertAllHuBtn);

        builder.setView(alertView);
        alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                alert.dismiss();
            }
        });
        cencleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        if (internetNotConnected(UserSpecialOrderActivity.this)) {
            alert.dismiss();
            return;
        }


        adapter = new KitchenViewUserAdapter(UserSpecialOrderActivity.this, kitchenList);
        revKitchen.setHasFixedSize(true);
        revKitchen.setLayoutManager(new LinearLayoutManager(UserSpecialOrderActivity.this));
        adapter.setOnKitchenViewClickListener(UserSpecialOrderActivity.this);
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("Kitchen");

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
                Toast.makeText(UserSpecialOrderActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        revKitchen.setAdapter(adapter);
        alert.show();

    }


    private void showNotFound() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Button okBtn;
        TextView alertTv;
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        alertTv = alertView.findViewById(R.id.alertTv);
        alertTv.setText("Sorry! No Kitchen Exist");

        builder.setView(alertView);

        final AlertDialog soryAlert = builder.create();
        soryAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        soryAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                soryAlert.dismiss();
                alert.dismiss();
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soryAlert.dismiss();
                alert.dismiss();
            }
        });
        soryAlert.setCanceledOnTouchOutside(false);
        soryAlert.show();


    }


    private boolean internetNotConnected(Context mContext) {
        InternetHelper obj = new InternetHelper();

        if (obj.isInternetConnected(mContext)) {
            return false;
        } else {
            showNoInternetMsg(mContext);
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
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetAlert.dismiss();
            }
        });
        noInternetAlert.show();
    }

    private void showSucessAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Button okBtn;
        TextView alertTv;
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        alertTv = alertView.findViewById(R.id.alertTv);
        alertTv.setText(msg);
        builder.setView(alertView);

        final AlertDialog susessAlert = builder.create();
        susessAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        susessAlert.setCanceledOnTouchOutside(false);
        susessAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                susessAlert.dismiss();
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                susessAlert.dismiss();
            }
        });
        susessAlert.show();


    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Fragment", "B");
        i.putExtras(bundle);
        startActivity(i);
        Animatoo.animateSwipeLeft(UserSpecialOrderActivity.this);
        finish();

    }

    private boolean allNotValid() {
        Validations objVal = new Validations();
        if (objVal.isValidString(titleEt) || objVal.isValidString(descriptionEt) || objVal.isValidAmount(descriptionEt)) {
            return true;
        } else {
            return false;
        }
    }


    private void connectivity() {
        mAuth = FirebaseAuth.getInstance();
        setTitle("Specail Order");
        titleEt = findViewById(R.id.etOrderNameSp);
        maxPriceEt = findViewById(R.id.etOrderPriceSp);
        descriptionEt = findViewById(R.id.etOrderDescrPtioneSp);
        sendButton = findViewById(R.id.pickButtonSp);
        user = FirebaseAuth.getInstance().getCurrentUser();
        sp = getSharedPreferences(mySp, MODE_PRIVATE);
        latitudeUser = sp.getString("Lat", null);
        longitudeUser = sp.getString("Long", null);
    }

    @Override
    public void OnKitchenViewCardClick(int position) {
        alert.dismiss();
        final User kitchenObjCurrent = kitchenList.get(position);
        orderToKitchenID = kitchenObjCurrent.id;
        if (internetNotConnected(UserSpecialOrderActivity.this)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(UserSpecialOrderActivity.this);
        builder.setMessage("Are You Sure You Want to Order?")
                .setCancelable(false)
                .setIcon(R.drawable.logo_app)
                .setTitle("Please Confirm");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                createProcessDialog();
                progressDialog.show();

                mOrderRef = FirebaseDatabase.getInstance().getReference().child("Orders");
                String orderID = mOrderRef.push().getKey();

                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
                String currentDate = sdf.format(new Date());


                Order order = new Order(orderID, user.getUid(), "Special", titleOrder, priceMaxOrder,
                        "Special Order", kitchenObjCurrent.id, kitchenObjCurrent.emailID,
                        kitchenObjCurrent.firstName + " " + kitchenObjCurrent.lastName, "Pending",
                        currentDate, descriptionOrder, latitudeUser, longitudeUser, kitchenObjCurrent.latitude, kitchenObjCurrent.longitude);

                mOrderRef.child(orderID).setValue(order).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        alert.dismiss();
                        Toast.makeText(UserSpecialOrderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        alert.dismiss();
                        clearAll();
                        showSucessAlert("Your Order is Placed");
                    }
                });

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog aD = builder.create();
        aD.show();


    }

    private void clearAll() {
        titleEt.setText("");
        descriptionEt.setText("");
        maxPriceEt.setText("");

    }


    private void createProcessDialog() {

        progressDialog = new ProgressDialog(UserSpecialOrderActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait....");

    }

}
