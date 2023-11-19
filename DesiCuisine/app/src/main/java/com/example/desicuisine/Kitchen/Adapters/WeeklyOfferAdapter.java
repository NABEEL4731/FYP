package com.example.desicuisine.Kitchen.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.desicuisine.Models.Order;
import com.example.desicuisine.Models.WeeklyOffer;
import com.example.desicuisine.R;
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
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class WeeklyOfferAdapter extends RecyclerView.Adapter<WeeklyOfferAdapter.ViewHolder> {
    private Context mContex;
    private ArrayList<WeeklyOffer> offerList;
    private ProgressDialog progressDialog;

    private DatabaseReference mOrderRef;
    private SharedPreferences sp;
    private String mySp = "MYSP";
    private FirebaseUser user;

    private String latitudeUser, longitudeUser;
    private String latitudeProduct, longitudeProduct;
    private boolean found = false;


    private View view, alertView;

    private String day1D, day1L, day1B, day2D, day2L, day2B, day3D, day3L, day3B, day4D, day4L, day4B,
            day5D, day5L, day5B, day6D, day6L, day6B, day7D, day7L, day7B;
    private String offerName, offerPrice;

    private EditText day1DEt, day1LEt, day1BEt, day2DEt, day2LEt, day2BEt, day3DEt, day3LEt, day3BEt, day4DEt, day4LEt, day4BEt,
            day5DEt, day5LEt, day5BEt, day6DEt, day6LEt, day6BEt, day7DEt, day7LEt, day7BEt;
    private EditText offerNameEt, offerPriceEt;
    private Button cencelBtn, updateBtn;
    private ImageView editBtn;

    private DatabaseReference mOfferRef;
    private String kitchenID, managerFName;
    private String managerLastName, kitchenEmail;
    private String userS;


    public WeeklyOfferAdapter(Context mContex, ArrayList<WeeklyOffer> offerList, String user) {
        this.mContex = mContex;
        this.offerList = offerList;
        this.userS = user;
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        sp = mContex.getSharedPreferences(mySp, MODE_PRIVATE);
        latitudeUser = sp.getString("Lat", null);
        longitudeUser = sp.getString("Long", null);
        kitchenID = sp.getString("id", null);
        managerFName = sp.getString("FName", null);
        managerLastName = sp.getString("LName", null);
        kitchenEmail = sp.getString("Email", null);


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContex).inflate(R.layout.offer_layout_view, viewGroup, false);
        return new WeeklyOfferAdapter.ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        final WeeklyOffer obj = offerList.get(i);
        viewHolder.offerNameTv.setText(obj.offerName);
        viewHolder.offerPriceTv.setText("Rs " + obj.offerPrice);
        viewHolder.cookNameTv.setText("Added by " + obj.cookName);
        viewHolder.offerDateTv.setText("Added on " + obj.date);

        viewHolder.day1BTv.setText(obj.day1B);
        viewHolder.day1LTv.setText(obj.day1L);
        viewHolder.day1DTv.setText(obj.day1D);

        viewHolder.day2BTv.setText(obj.day2B);
        viewHolder.day2LTv.setText(obj.day2L);
        viewHolder.day2DTv.setText(obj.day2D);

        viewHolder.day3BTv.setText(obj.day3B);
        viewHolder.day3LTv.setText(obj.day3L);
        viewHolder.day3DTv.setText(obj.day3D);

        viewHolder.day4BTv.setText(obj.day4B);
        viewHolder.day4LTv.setText(obj.day4L);
        viewHolder.day4DTv.setText(obj.day4D);

        viewHolder.day5BTv.setText(obj.day5B);
        viewHolder.day5LTv.setText(obj.day5L);
        viewHolder.day5DTv.setText(obj.day5D);

        viewHolder.day6BTv.setText(obj.day6B);
        viewHolder.day6LTv.setText(obj.day6L);
        viewHolder.day6DTv.setText(obj.day6D);

        viewHolder.day7BTv.setText(obj.day7B);
        viewHolder.day7LTv.setText(obj.day7L);
        viewHolder.day7DTv.setText(obj.day7D);

        if (obj.kitchenID.equals(user.getUid())) {
            viewHolder.layoutCC.setVisibility(View.GONE);
            viewHolder.layoutKK.setVisibility(View.VISIBLE);
            viewHolder.cookNameTv.setVisibility(View.GONE);
        }
        viewHolder.subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                found = false;
                if (internetNotConnected(mContex)) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                builder.setMessage("Are You Sure You Want to Subscribe?")
                        .setCancelable(false)
                        .setIcon(R.drawable.logo_app)
                        .setTitle("Please Confirm");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createProgressDialog();
                        progressDialog.show();

                        mOrderRef = FirebaseDatabase.getInstance().getReference().child("Weekly Offer Orders");
                        mOrderRef.orderByChild(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                        Order o = dataSnapshot1.getValue(Order.class);
                                        if (o.kitchenID.equals(obj.kitchenID) && o.orderStatus.equals("Pending") && o.productID.equals(obj.offerId)) {
                                            found = true;
                                            progressDialog.dismiss();
                                            showSucessAlert("Your request is already exists");
                                        }

                                    }
                                }

                                if (found == false) {
                                    String orderID = mOrderRef.push().getKey();

                                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
                                    String currentDate = sdf.format(new Date());


                                    Order order = new Order(orderID, user.getUid(), obj.offerId, obj.offerName, obj.offerPrice,
                                            "Weekly Menu", obj.kitchenID, obj.kichenEmail,
                                            obj.cookName, "Pending", currentDate, "Normal",
                                            latitudeUser, longitudeUser, obj.latitudeKitchen, obj.longitudeKitchen);

                                    mOrderRef.child(orderID).setValue(order).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(mContex, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            showSucessAlert("Your Request is Sent Sucessfully");
                                        }
                                    });

                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

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

        });
        viewHolder.locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                latitudeProduct = obj.latitudeKitchen;
                longitudeProduct = obj.longitudeKitchen;

                if (internetNotConnected(mContex)) {
                    return;
                }
                if (userS.equals("Customer")) {
                    showMap(mContex, latitudeProduct, longitudeProduct, latitudeUser, longitudeUser);
                } else
                    showMap(mContex, latitudeUser, longitudeUser, latitudeProduct, longitudeProduct);

            }
        });

        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ADB = new AlertDialog.Builder(mContex);
                ADB.setMessage("Are You Sure You Want to Delete?")
                        .setCancelable(false)
                        .setIcon(R.drawable.logo_app)
                        .setTitle("Please Confirm");
                ADB.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createProgressDialog();
                        progressDialog.show();
                        FirebaseDatabase.getInstance().getReference().child("Weekly Offers").child(obj.offerId).removeValue()
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(mContex, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        showSucessAlert("Deleted Sucessfully");
                                        ((Activity) mContex).recreate();
                                    }
                                });
                    }
                });
                ADB.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });
                AlertDialog AD = ADB.create();
                AD.show();
            }
        });

        viewHolder.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                alertView = LayoutInflater.from(mContex).inflate(R.layout.update_weekly_menu_layout, null);
                connectivityAddWeeklyAlert();
                builder.setView(alertView);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                setDataOnAlert(obj);
                editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeEditAbleAll();
                    }
                });
                cencelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        getDataFromView();

                        if (allNotValid()) {
                            return;
                        }
                        if (internetNotConnected(mContex)) {
                            return;
                        }
                        createProgressDialog();
                        progressDialog.show();

                        mOfferRef = FirebaseDatabase.getInstance().getReference().child("Weekly Offers");
                        final String offerID = obj.offerId;

                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
                        String currentDate = sdf.format(new Date());

                        WeeklyOffer obj = new WeeklyOffer(offerID, offerName, offerPrice,
                                day1D, day1L, day1B, day2D, day2L, day2B, day3D, day3L, day3B, day4D, day4L, day4B,
                                day5D, day5L, day5B, day6D, day6L, day6B, day7D, day7L, day7B,
                                managerFName + " " + managerLastName, kitchenEmail, kitchenID,
                                currentDate, latitudeUser, longitudeUser);
                        mOfferRef.child(offerID).setValue(obj).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(mContex, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        alertDialog.dismiss();
                                        showSucessAlert("Menu Is Updated Sucessfully");
                                        ((Activity) mContex).recreate();
                                    }
                                });

                    }
                });
                alertDialog.show();

            }
        });

    }

    private void setDataOnAlert(WeeklyOffer obj) {

        offerNameEt.setText(obj.offerName);
        offerPriceEt.setText(obj.offerPrice);

        day1BEt.setText(obj.day1B);
        day1LEt.setText(obj.day1L);
        day1DEt.setText(obj.day1D);

        day2BEt.setText(obj.day2B);
        day2LEt.setText(obj.day2L);
        day2DEt.setText(obj.day2D);

        day3BEt.setText(obj.day3B);
        day3LEt.setText(obj.day3L);
        day3DEt.setText(obj.day3D);

        day4BEt.setText(obj.day4B);
        day4LEt.setText(obj.day4L);
        day4DEt.setText(obj.day4D);

        day5BEt.setText(obj.day5B);
        day5LEt.setText(obj.day5L);
        day5DEt.setText(obj.day5D);

        day6BEt.setText(obj.day6B);
        day6LEt.setText(obj.day6L);
        day6DEt.setText(obj.day6D);

        day7BEt.setText(obj.day7B);
        day7LEt.setText(obj.day7L);
        day7DEt.setText(obj.day7D);
        updateBtn.setEnabled(false);
    }

    private void makeEditAbleAll() {

        offerNameEt.setFocusable(true);
        offerNameEt.setClickable(true);
        offerNameEt.setCursorVisible(true);
        offerNameEt.setSelection(offerNameEt.getText().toString().length());
        offerNameEt.setFocusableInTouchMode(true);
        offerNameEt.requestFocus();

        offerPriceEt.setFocusable(true);
        offerPriceEt.setClickable(true);
        offerPriceEt.setCursorVisible(true);
        offerPriceEt.setSelection(offerPriceEt.getText().toString().length());
        offerPriceEt.setFocusableInTouchMode(true);

        day1BEt.setFocusable(true);
        day1BEt.setClickable(true);
        day1BEt.setCursorVisible(true);
        day1BEt.setSelection(day1BEt.getText().toString().length());
        day1BEt.setFocusableInTouchMode(true);

        day1LEt.setFocusable(true);
        day1LEt.setClickable(true);
        day1LEt.setCursorVisible(true);
        day1LEt.setSelection(day1LEt.getText().toString().length());
        day1LEt.setFocusableInTouchMode(true);

        day1DEt.setFocusable(true);
        day1DEt.setClickable(true);
        day1DEt.setCursorVisible(true);
        day1DEt.setSelection(day1DEt.getText().toString().length());
        day1DEt.setFocusableInTouchMode(true);

        day2BEt.setFocusable(true);
        day2BEt.setClickable(true);
        day2BEt.setCursorVisible(true);
        day2BEt.setSelection(day2BEt.getText().toString().length());
        day2BEt.setFocusableInTouchMode(true);

        day2LEt.setFocusable(true);
        day2LEt.setClickable(true);
        day2LEt.setCursorVisible(true);
        day2LEt.setSelection(day2LEt.getText().toString().length());
        day2LEt.setFocusableInTouchMode(true);

        day2DEt.setFocusable(true);
        day2DEt.setClickable(true);
        day2DEt.setCursorVisible(true);
        day2DEt.setSelection(day2DEt.getText().toString().length());
        day2DEt.setFocusableInTouchMode(true);

        day3BEt.setFocusable(true);
        day3BEt.setClickable(true);
        day3BEt.setCursorVisible(true);
        day3BEt.setSelection(day3BEt.getText().toString().length());
        day3BEt.setFocusableInTouchMode(true);

        day3LEt.setFocusable(true);
        day3LEt.setClickable(true);
        day3LEt.setCursorVisible(true);
        day3LEt.setSelection(day3LEt.getText().toString().length());
        day3LEt.setFocusableInTouchMode(true);

        day3DEt.setFocusable(true);
        day3DEt.setClickable(true);
        day3DEt.setCursorVisible(true);
        day3DEt.setSelection(day3DEt.getText().toString().length());
        day3DEt.setFocusableInTouchMode(true);


        day4BEt.setFocusable(true);
        day4BEt.setClickable(true);
        day4BEt.setCursorVisible(true);
        day4BEt.setSelection(day4BEt.getText().toString().length());
        day4BEt.setFocusableInTouchMode(true);

        day4LEt.setFocusable(true);
        day4LEt.setClickable(true);
        day4LEt.setCursorVisible(true);
        day4LEt.setSelection(day4LEt.getText().toString().length());
        day4LEt.setFocusableInTouchMode(true);

        day4DEt.setFocusable(true);
        day4DEt.setClickable(true);
        day4DEt.setCursorVisible(true);
        day4DEt.setSelection(day4DEt.getText().toString().length());
        day4DEt.setFocusableInTouchMode(true);


        day5BEt.setFocusable(true);
        day5BEt.setClickable(true);
        day5BEt.setCursorVisible(true);
        day5BEt.setSelection(day5BEt.getText().toString().length());
        day5BEt.setFocusableInTouchMode(true);

        day5LEt.setFocusable(true);
        day5LEt.setClickable(true);
        day5LEt.setCursorVisible(true);
        day5LEt.setSelection(day5LEt.getText().toString().length());
        day5LEt.setFocusableInTouchMode(true);

        day5DEt.setFocusable(true);
        day5DEt.setClickable(true);
        day5DEt.setCursorVisible(true);
        day5DEt.setSelection(day5DEt.getText().toString().length());
        day5DEt.setFocusableInTouchMode(true);


        day6BEt.setFocusable(true);
        day6BEt.setClickable(true);
        day6BEt.setCursorVisible(true);
        day6BEt.setSelection(day6BEt.getText().toString().length());
        day6BEt.setFocusableInTouchMode(true);

        day6LEt.setFocusable(true);
        day6LEt.setClickable(true);
        day6LEt.setCursorVisible(true);
        day6LEt.setSelection(day6LEt.getText().toString().length());
        day6LEt.setFocusableInTouchMode(true);

        day6DEt.setFocusable(true);
        day6DEt.setClickable(true);
        day6DEt.setCursorVisible(true);
        day6DEt.setSelection(day6DEt.getText().toString().length());
        day6DEt.setFocusableInTouchMode(true);


        day7BEt.setFocusable(true);
        day7BEt.setClickable(true);
        day7BEt.setCursorVisible(true);
        day7BEt.setSelection(day7BEt.getText().toString().length());
        day7BEt.setFocusableInTouchMode(true);

        day7LEt.setFocusable(true);
        day7LEt.setClickable(true);
        day7LEt.setCursorVisible(true);
        day7LEt.setSelection(day7LEt.getText().toString().length());
        day7LEt.setFocusableInTouchMode(true);

        day7DEt.setFocusable(true);
        day7DEt.setClickable(true);
        day7DEt.setCursorVisible(true);
        day7DEt.setSelection(day7DEt.getText().toString().length());
        day7DEt.setFocusableInTouchMode(true);


        updateBtn.setEnabled(true);

        Toast.makeText(mContex, "You can Update Now", Toast.LENGTH_SHORT).show();

    }

    private void getDataFromView() {

        offerName = offerNameEt.getText().toString().trim();
        offerPrice = offerPriceEt.getText().toString().trim();

        day1B = day1BEt.getText().toString().trim();
        day1L = day1LEt.getText().toString().trim();
        day1D = day1DEt.getText().toString().trim();

        day2B = day2BEt.getText().toString().trim();
        day2L = day2LEt.getText().toString().trim();
        day2D = day2DEt.getText().toString().trim();

        day3B = day3BEt.getText().toString().trim();
        day3L = day3LEt.getText().toString().trim();
        day3D = day3DEt.getText().toString().trim();

        day4B = day4BEt.getText().toString().trim();
        day4L = day4LEt.getText().toString().trim();
        day4D = day4DEt.getText().toString().trim();

        day5B = day5BEt.getText().toString().trim();
        day5L = day5LEt.getText().toString().trim();
        day5D = day5DEt.getText().toString().trim();

        day6B = day6BEt.getText().toString().trim();
        day6L = day6LEt.getText().toString().trim();
        day6D = day6DEt.getText().toString().trim();

        day7B = day7BEt.getText().toString().trim();
        day7L = day7LEt.getText().toString().trim();
        day7D = day7DEt.getText().toString().trim();

    }

    private void connectivityAddWeeklyAlert() {
        editBtn = alertView.findViewById(R.id.editBtnW);
        cencelBtn = alertView.findViewById(R.id.cencleButtonW);
        updateBtn = alertView.findViewById(R.id.addButtonW);
        offerNameEt = alertView.findViewById(R.id.etOnameW);
        offerPriceEt = alertView.findViewById(R.id.etPriceW);

        day1BEt = alertView.findViewById(R.id.day1B);
        day1LEt = alertView.findViewById(R.id.day1L);
        day1DEt = alertView.findViewById(R.id.day1D);

        day2BEt = alertView.findViewById(R.id.day2B);
        day2LEt = alertView.findViewById(R.id.day2L);
        day2DEt = alertView.findViewById(R.id.day2D);

        day3BEt = alertView.findViewById(R.id.day3B);
        day3LEt = alertView.findViewById(R.id.day3L);
        day3DEt = alertView.findViewById(R.id.day3D);

        day4BEt = alertView.findViewById(R.id.day4B);
        day4LEt = alertView.findViewById(R.id.day4L);
        day4DEt = alertView.findViewById(R.id.day4D);

        day5BEt = alertView.findViewById(R.id.day5B);
        day5LEt = alertView.findViewById(R.id.day5L);
        day5DEt = alertView.findViewById(R.id.day5D);

        day6BEt = alertView.findViewById(R.id.day6B);
        day6LEt = alertView.findViewById(R.id.day6L);
        day6DEt = alertView.findViewById(R.id.day6D);

        day7BEt = alertView.findViewById(R.id.day7B);
        day7LEt = alertView.findViewById(R.id.day7L);
        day7DEt = alertView.findViewById(R.id.day7D);

    }

    private boolean allNotValid() {
        Validations objVal = new Validations();
        if (objVal.isValidString(offerNameEt) || objVal.isValidAmount(offerPriceEt) ||
                objVal.isValidString(day1BEt) || objVal.isValidString(day1LEt) || objVal.isValidString(day1DEt) ||
                objVal.isValidString(day2BEt) || objVal.isValidString(day2LEt) || objVal.isValidString(day2DEt) ||
                objVal.isValidString(day3BEt) || objVal.isValidString(day3LEt) || objVal.isValidString(day3DEt) ||
                objVal.isValidString(day4BEt) || objVal.isValidString(day4LEt) || objVal.isValidString(day4DEt) ||
                objVal.isValidString(day5BEt) || objVal.isValidString(day5LEt) || objVal.isValidString(day5DEt) ||
                objVal.isValidString(day6BEt) || objVal.isValidString(day6LEt) || objVal.isValidString(day6DEt) ||
                objVal.isValidString(day7BEt) || objVal.isValidString(day7LEt) || objVal.isValidString(day7DEt)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return offerList.size();
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

    private void showSucessAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
        Button okBtn;
        TextView alertTv;
        View alertView = LayoutInflater.from(mContex).inflate(R.layout.informational_alert_layout_access, null);
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

    private void createProgressDialog() {
        progressDialog = new ProgressDialog(mContex);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait...");

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
        AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
        Button okBtn;
        TextView alertTv;
        View alertView = LayoutInflater.from(mContex).inflate(R.layout.informational_alert_layout_access, null);
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView offerNameTv, offerDateTv, cookNameTv, offerPriceTv;
        public Button subscribeBtn, locationBtn, deleteBtn, updateBtn;
        public LinearLayout layoutCC, layoutKK;
        private TextView day1DTv, day1LTv, day1BTv, day2DTv, day2LTv, day2BTv, day3DTv, day3LTv, day3BTv, day4DTv, day4LTv, day4BTv,
                day5DTv, day5LTv, day5BTv, day6DTv, day6LTv, day6BTv, day7DTv, day7LTv, day7BTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            offerNameTv = itemView.findViewById(R.id.OfferNameViewHolder);
            offerPriceTv = itemView.findViewById(R.id.OfferPriceViewHolder);
            offerDateTv = itemView.findViewById(R.id.dateOfferViewHolder);
            cookNameTv = itemView.findViewById(R.id.cookNameOfferViewHolder);


            updateBtn = itemView.findViewById(R.id.updateOfferBtnViewHolder);
            deleteBtn = itemView.findViewById(R.id.deleteOfferViewHolder);
            locationBtn = itemView.findViewById(R.id.viewLocationOffertBtn);
            subscribeBtn = itemView.findViewById(R.id.subscribeBtnViewHolder);

            layoutCC = itemView.findViewById(R.id.layoutOfferCC);
            layoutKK = itemView.findViewById(R.id.layoutOfferKK);
            day1BTv = itemView.findViewById(R.id.day1BTv);
            day1LTv = itemView.findViewById(R.id.day1LTv);
            day1DTv = itemView.findViewById(R.id.day1DTv);

            day2BTv = itemView.findViewById(R.id.day2BTv);
            day2LTv = itemView.findViewById(R.id.day2LTv);
            day2DTv = itemView.findViewById(R.id.day2DTv);

            day3BTv = itemView.findViewById(R.id.day3BTv);
            day3LTv = itemView.findViewById(R.id.day3LTv);
            day3DTv = itemView.findViewById(R.id.day3DTv);

            day4BTv = itemView.findViewById(R.id.day4BTv);
            day4LTv = itemView.findViewById(R.id.day4LTv);
            day4DTv = itemView.findViewById(R.id.day4DTv);

            day5BTv = itemView.findViewById(R.id.day5BTv);
            day5LTv = itemView.findViewById(R.id.day5LTv);
            day5DTv = itemView.findViewById(R.id.day5DTv);

            day6BTv = itemView.findViewById(R.id.day6BTv);
            day6LTv = itemView.findViewById(R.id.day6LTv);
            day6DTv = itemView.findViewById(R.id.day6DTv);

            day7BTv = itemView.findViewById(R.id.day7BTv);
            day7LTv = itemView.findViewById(R.id.day7LTv);
            day7DTv = itemView.findViewById(R.id.day7DTv);

        }
    }

}
