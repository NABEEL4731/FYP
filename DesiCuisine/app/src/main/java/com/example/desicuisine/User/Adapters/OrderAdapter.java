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
import android.widget.TextView;
import android.widget.Toast;

import com.example.desicuisine.Models.Order;
import com.example.desicuisine.Notification.APIService;
import com.example.desicuisine.Notification.Sending.Client;
import com.example.desicuisine.Notification.Sending.Data;
import com.example.desicuisine.Notification.Sending.MyResponse;
import com.example.desicuisine.Notification.Sending.Sender;
import com.example.desicuisine.Notification.Sending.Token;
import com.example.desicuisine.R;
import com.example.desicuisine.Utils.InternetHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private Context mContex;
    private ArrayList<Order> orderList;
    private OnOrderCardItemClickListener oListener;
    private String userCurrent;
    private ProgressDialog progressDialog;
    private String latitudeKit, longitudeKit;
    private SharedPreferences sp;
    private FirebaseDatabase firebaseDatabase;
    private APIService apiService = null;


    public OrderAdapter(Context mContex, ArrayList<Order> orderList) {
        this.mContex = mContex;
        this.orderList = orderList;
        userCurrent = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sp = mContex.getSharedPreferences("MYSP", MODE_PRIVATE);

        latitudeKit = sp.getString("Lat", null);
        longitudeKit = sp.getString("Long", null);

        apiService = Client.getClient("https://fcm.googleapis.com/")
                .create(APIService.class);


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContex).inflate(R.layout.orders_layout_user, viewGroup, false);
        return new OrderAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        final Order obj = orderList.get(i);

        viewHolder.proNameTv.setText(obj.productName);
        viewHolder.proPriceTv.setText("Rs " + obj.productPrice);
        viewHolder.orderStatusTv.setText(obj.orderStatus);
        viewHolder.orderCategoryTV.setText(obj.productCategory);
        viewHolder.kitchenEmailTv.setText(obj.kitchenEmail);
        viewHolder.descTv.setText(obj.orderDescription);
        viewHolder.dateTv.setText(obj.orderDate);



        if (userCurrent.equals(obj.kitchenID) && obj.orderStatus.equals("Pending")) {

            viewHolder.kitLayuout.setVisibility(View.VISIBLE);
            viewHolder.approveBtn.setVisibility(View.VISIBLE);

        }

        if (obj.productID.equals("Special")) {
            viewHolder.descriptionLayout.setVisibility(View.VISIBLE);
        }

        if (userCurrent.equals(obj.kitchenID)) {

            viewHolder.kitchenEmailTv.setVisibility(View.GONE);
            viewHolder.emailTv.setVisibility(View.GONE);


        }
        if (userCurrent.equals(obj.kitchenID) && obj.orderStatus.equals("In Process")) {
            viewHolder.kitLayuout.setVisibility(View.VISIBLE);
            viewHolder.completeOrderBtn.setVisibility(View.VISIBLE);
        }
        if (userCurrent.equals(obj.kitchenID) && (obj.orderStatus.equals("Completed") || obj.orderStatus.equals("Cenceled"))) {
            viewHolder.kitLayuout.setVisibility(View.VISIBLE);
            viewHolder.locationBtn.setVisibility(View.VISIBLE);
        }


        viewHolder.completeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetNotConnected(mContex)) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                builder.setMessage("Are You Sure You Want to Complete Order?")
                        .setCancelable(false)
                        .setIcon(R.drawable.logo_app)
                        .setTitle("Please Confirm");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        updateStatus(obj.orderId, "Completed", orderList.get(i));
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
        viewHolder.approveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetNotConnected(mContex)) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                builder.setMessage("Are You Sure You Want to Approve Order?")
                        .setCancelable(false)
                        .setIcon(R.drawable.logo_app)
                        .setTitle("Please Confirm");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateStatus(obj.orderId, "In Process", orderList.get(i));
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

                showMap(mContex,  obj.userLatitude, obj.userLongitude, latitudeKit, longitudeKit);

            }
        });

    }

    public void sendNotification(final String receiverId, final Order obj) {

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference("Tokens")
                .child(receiverId)
                .child("token")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            String sToken = dataSnapshot.getValue(String.class);
                            Token token = new Token(sToken);
                            String body;
                            if (obj.orderStatus.equals("In Process")) {
                                body = "Your order of " + obj.productName + " is In Process now";
                            } else {
                                body = "Your order of " + obj.productName + " is Completed";
                            }

                            Data data = new Data(FirebaseAuth.getInstance().getCurrentUser().getUid(), R.drawable.logo_app,
                                    mContex.getResources().getString(R.string.app_name),
                                    body, new Gson().toJson(obj),
                                    receiverId);
                            Sender sender = new Sender(data, token.getToken());

                            apiService.sendNotification(sender)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if (response.code() == 200) {
                                                assert response.body() != null;
                                                if (response.body().success != 1) {
                                                    Toast.makeText(mContex, "Notification not sent", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(mContex, "Notification Sent", Toast.LENGTH_SHORT).show();

                                                }

                                            }
                                            ((Activity) mContex).recreate();

                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<MyResponse> call, Throwable t) {
                                            ((Activity) mContex).recreate();
                                            Toast.makeText(mContex, t.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void updateStatus(String orderID, final String newStatus, final Order obj) {
        String statusKey = "orderStatus";
        createProgressDialog();
        progressDialog.show();

        Map<String, Object> statusUpdate = new HashMap<String, Object>();
        statusUpdate.put(statusKey, newStatus);

        FirebaseDatabase.getInstance().getReference().child("Orders").child(orderID).updateChildren(statusUpdate).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(mContex, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        obj.setOrderStatus(newStatus);
                        progressDialog.dismiss();
                        if (newStatus.equals("In Process")) {
                            Toast.makeText(mContex, "You have approved order\nNow you can start working on this order", Toast.LENGTH_SHORT).show();
                        } else if (newStatus.equals("Completed")) {
                            Toast.makeText(mContex, "You have sucessfully completed your order", Toast.LENGTH_SHORT).show();
                        }

                        sendNotification(obj.userID, obj);
//
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView proNameTv, proPriceTv, kitchenEmailTv, dateTv, orderStatusTv, orderCategoryTV, emailTv, descTv;
        public Button approveBtn, locationBtn, completeOrderBtn;
        public LinearLayout kitLayuout, descriptionLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            proNameTv = itemView.findViewById(R.id.proNameOrderViewHolder);
            proPriceTv = itemView.findViewById(R.id.proPriceOrderViewHolder);
            orderStatusTv = itemView.findViewById(R.id.orderStatusViewHolder);
            kitchenEmailTv = itemView.findViewById(R.id.kitchenEmailViewHolder);
            dateTv = itemView.findViewById(R.id.dateOrderViewHolder);
            orderCategoryTV = itemView.findViewById(R.id.orderCategoryViewHolder);
            approveBtn = itemView.findViewById(R.id.approveOrderViewHolder);
            completeOrderBtn = itemView.findViewById(R.id.completedOrderViewHolder);
            emailTv = itemView.findViewById(R.id.emalViewHolderTv);
            descTv = itemView.findViewById(R.id.orderDescriptionViewHolder);
            descriptionLayout = itemView.findViewById(R.id.layoutVV);
            locationBtn = itemView.findViewById(R.id.locationOrderViewHolder);
            kitLayuout = itemView.findViewById(R.id.orderKitLatoutViewHolder);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (oListener != null) {
                int posiion = getAdapterPosition();
                if (posiion != RecyclerView.NO_POSITION) {
                    oListener.setOrderCardClickListener(posiion);
                }
            }
        }
    }

    public interface OnOrderCardItemClickListener {
        void setOrderCardClickListener(int position);
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

    public void setOnOrderCardItemClickListener(OnOrderCardItemClickListener abc) {
        oListener = abc;
    }


}
