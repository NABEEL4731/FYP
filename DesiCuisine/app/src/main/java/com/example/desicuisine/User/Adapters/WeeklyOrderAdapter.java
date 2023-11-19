package com.example.desicuisine.User.Adapters;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.desicuisine.Models.Order;
import com.example.desicuisine.Models.WeeklyOffer;
import com.example.desicuisine.R;
import com.example.desicuisine.Utils.InternetHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class WeeklyOrderAdapter extends RecyclerView.Adapter<WeeklyOrderAdapter.ViewHolder> {
    private Context mContex;
    private ArrayList<Order> orderList;
    private String userCurrent;
    private ProgressDialog progressDialog;
    private String latitudeKit, longitudeKit;
    private SharedPreferences sp;
    private String user;


    private TextView day1DTv, day1LTv, day1BTv, day2DTv, day2LTv, day2BTv, day3DTv, day3LTv, day3BTv, day4DTv, day4LTv, day4BTv,
            day5DTv, day5LTv, day5BTv, day6DTv, day6LTv, day6BTv, day7DTv, day7LTv, day7BTv;
    public TextView offerNameTv, offerDateTv, cookNameTv, offerPriceTv;
    private ProgressBar proBar;

    public WeeklyOrderAdapter(Context mContex, ArrayList<Order> orderList, String user) {
        this.mContex = mContex;
        this.orderList = orderList;
        this.user = user;
        userCurrent = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sp = mContex.getSharedPreferences("MYSP", MODE_PRIVATE);

        latitudeKit = sp.getString("Lat", null);
        longitudeKit = sp.getString("Long", null);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContex).inflate(R.layout.weekly_orders_layout_user, viewGroup, false);
        return new WeeklyOrderAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Order obj = orderList.get(i);

        viewHolder.proNameTv.setText(obj.productName);
        viewHolder.proPriceTv.setText("Rs " + obj.productPrice);
        viewHolder.orderStatusTv.setText(obj.orderStatus);
        viewHolder.orderCategoryTV.setText(obj.productCategory);
        viewHolder.kitchenEmailTv.setText(obj.kitchenEmail);
        viewHolder.descTv.setText(obj.orderDescription);
        viewHolder.dateTv.setText(obj.orderDate);


        if (userCurrent.equals(obj.kitchenID) && obj.orderStatus.equals("Pending")) {
            viewHolder.layoutLL.setVisibility(View.GONE);
            viewHolder.kitLayuout.setVisibility(View.VISIBLE);

        }
        if (userCurrent.equals(obj.kitchenID) && obj.orderStatus.equals("Accepted")) {

            viewHolder.kitLayuout.setVisibility(View.GONE);

        }


        if (userCurrent.equals(obj.kitchenID)) {

            viewHolder.kitchenEmailTv.setVisibility(View.GONE);
            viewHolder.emailTv.setVisibility(View.GONE);


        }

        viewHolder.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetNotConnected(mContex)) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                builder.setMessage("Are You Sure You Want to Accept Order?")
                        .setCancelable(false)
                        .setIcon(R.drawable.logo_app)
                        .setTitle("Please Confirm");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateStatus(obj.orderId, "Accepted");
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
        viewHolder.rejectOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetNotConnected(mContex)) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                builder.setMessage("Are You Sure You Want to Reject Order?")
                        .setCancelable(false)
                        .setIcon(R.drawable.logo_app)
                        .setTitle("Please Confirm");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateStatus(obj.orderId, "Rejected");
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
                if (internetNotConnected(mContex)) {
                    return;
                }

                if (user.equals("Customer")) {
                    showMap(mContex, obj.productLatitude, obj.productLongitude, latitudeKit, longitudeKit);
                } else
                    showMap(mContex, obj.userLatitude, obj.userLongitude, latitudeKit, longitudeKit);

            }
        });

        viewHolder.viewMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetNotConnected(mContex)) {
                    return;
                }
                FirebaseDatabase.getInstance().getReference().child("Weekly Offers").child(obj.productID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        WeeklyOffer offer = dataSnapshot.getValue(WeeklyOffer.class);

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                        View itemView = LayoutInflater.from(mContex).inflate(R.layout.offer_menu_layout_view, null);
                        proBar = itemView.findViewById(R.id.viewPb);

                        offerNameTv = itemView.findViewById(R.id.OfferNameViewHolder);
                        offerPriceTv = itemView.findViewById(R.id.OfferPriceViewHolder);
                        offerDateTv = itemView.findViewById(R.id.dateOfferViewHolder);
                        cookNameTv = itemView.findViewById(R.id.cookNameOfferViewHolder);

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


                        offerNameTv.setText(offer.offerName);
                        offerPriceTv.setText("Rs " + offer.offerPrice);
                        cookNameTv.setText("Added by " + offer.cookName);
                        offerDateTv.setText("Added on " + offer.date);

                        day1BTv.setText(offer.day1B);
                        day1LTv.setText(offer.day1L);
                        day1DTv.setText(offer.day1D);

                        day2BTv.setText(offer.day2B);
                        day2LTv.setText(offer.day2L);
                        day2DTv.setText(offer.day2D);

                        day3BTv.setText(offer.day3B);
                        day3LTv.setText(offer.day3L);
                        day3DTv.setText(offer.day3D);

                        day4BTv.setText(offer.day4B);
                        day4LTv.setText(offer.day4L);
                        day4DTv.setText(offer.day4D);

                        day5BTv.setText(offer.day5B);
                        day5LTv.setText(offer.day5L);
                        day5DTv.setText(offer.day5D);

                        day6BTv.setText(offer.day6B);
                        day6LTv.setText(offer.day6L);
                        day6DTv.setText(offer.day6D);

                        day7BTv.setText(offer.day7B);
                        day7LTv.setText(offer.day7L);
                        day7DTv.setText(offer.day7D);

                        proBar.setVisibility(View.GONE);
                        builder.setView(itemView);

                        final AlertDialog alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    private void updateStatus(String orderID, final String newStatus) {
        String statusKey = "orderStatus";
        createProgressDialog();
        progressDialog.show();

        Map<String, Object> statusUpdate = new HashMap<String, Object>();
        statusUpdate.put(statusKey, newStatus);

        FirebaseDatabase.getInstance().getReference().child("Weekly Offer Orders").child(orderID).updateChildren(statusUpdate).addOnFailureListener(new OnFailureListener() {
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
                        if (newStatus.equals("Accepted")) {
                            Toast.makeText(mContex, "You have accepted order sucessfully\nNow you can start working on this order", Toast.LENGTH_SHORT).show();
                        } else if (newStatus.equals("Rejected")) {
                            Toast.makeText(mContex, "You have rejectd order sucessfully", Toast.LENGTH_SHORT).show();
                        }

                        ((Activity) mContex).recreate();
                    }
                });

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

    @Override
    public int getItemCount() {
        return orderList.size();
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView proNameTv, proPriceTv, kitchenEmailTv, dateTv, orderStatusTv, orderCategoryTV, emailTv, descTv;
        public Button acceptBtn, locationBtn, rejectOrderBtn, viewMenu;
        public LinearLayout kitLayuout, descriptionLayout, layoutLL;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            proNameTv = itemView.findViewById(R.id.proNameOrderViewHolder);
            proPriceTv = itemView.findViewById(R.id.proPriceOrderViewHolder);
            orderStatusTv = itemView.findViewById(R.id.orderStatusViewHolder);
            kitchenEmailTv = itemView.findViewById(R.id.kitchenEmailViewHolder);
            dateTv = itemView.findViewById(R.id.dateOrderViewHolder);
            orderCategoryTV = itemView.findViewById(R.id.orderCategoryViewHolder);
            acceptBtn = itemView.findViewById(R.id.acceptOrderViewHolder);
            viewMenu = itemView.findViewById(R.id.viewMenuViewHolder);
            rejectOrderBtn = itemView.findViewById(R.id.rejectOrderViewHolder);
            emailTv = itemView.findViewById(R.id.emalViewHolderTv);
            descTv = itemView.findViewById(R.id.orderDescriptionViewHolder);
            descriptionLayout = itemView.findViewById(R.id.layoutVV);
            locationBtn = itemView.findViewById(R.id.locationOrderViewHolder);
            kitLayuout = itemView.findViewById(R.id.orderKitLayoutViewHolder);
            layoutLL = itemView.findViewById(R.id.layoutLL);
        }

    }


}
