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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.desicuisine.Kitchen.Activities.ProductDetailsActivity;
import com.example.desicuisine.Models.Order;
import com.example.desicuisine.Models.Product;
import com.example.desicuisine.R;
import com.example.desicuisine.Utils.InternetHelper;
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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private Context mContex;
    private ArrayList<Product> productList;
    private ProgressDialog progressDialog;

    private DatabaseReference mOrderRef;
    private SharedPreferences sp;
    private String mySp = "MYSP";
    private FirebaseUser user;

    private String latitudeUser, longitudeUser;
    private String latitudeProduct, longitudeProduct;
    private OnProductCardItemClickListener oListener;
    private Boolean isAlreadyOrdered;

    public ProductAdapter(Context mContex, ArrayList<Product> productList) {
        this.mContex = mContex;
        this.productList = productList;
        sp = mContex.getSharedPreferences(mySp, MODE_PRIVATE);
        latitudeUser = sp.getString("Lat", null);
        longitudeUser = sp.getString("Long", null);
        user = FirebaseAuth.getInstance().getCurrentUser();
        isAlreadyOrdered = false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContex).inflate(R.layout.products_layout_user, viewGroup, false);
        return new ProductAdapter.ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final Product obj = productList.get(i);
        Uri imageUri = Uri.parse(obj.photoUrl);


        Glide.with(mContex)
                .load(imageUri)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.loading_image))
                .into(viewHolder.imageView);


        viewHolder.nameTv.setText(obj.productName);
        viewHolder.priceTv.setText("Rs " + obj.productPrice);
        viewHolder.cookNameTv.setText("Added by " + obj.cookName);
        viewHolder.quantityTV.setText("Quantity Per Serving " + obj.productQuantity);
        viewHolder.dateTv.setText("Added on " + obj.date);

        if (obj.kitchenID.equals(user.getUid())) {
            viewHolder.layoutAA.setVisibility(View.GONE);
            viewHolder.layoutKK.setVisibility(View.VISIBLE);
            viewHolder.cookNameTv.setVisibility(View.GONE);
        }
        viewHolder.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString("id", obj.id);
                bundle.putString("productName", obj.productName);
                bundle.putString("productPrice", obj.productPrice);
                bundle.putString("productQuantity", obj.productQuantity);
                bundle.putString("productCategory", obj.productCategory);
                bundle.putString("status", obj.status);
                bundle.putString("cookName", obj.cookName);
                bundle.putString("kichenEmail", obj.kichenEmail);
                bundle.putString("kitchenID", obj.kitchenID);
                bundle.putString("date", obj.date);
                bundle.putString("photoUrl", obj.photoUrl);
                bundle.putString("latitude", obj.latitudeP);
                bundle.putString("longitude", obj.longitudeP);

                Intent intent = new Intent(mContex, ProductDetailsActivity.class);
                intent.putExtras(bundle);
                mContex.startActivity(intent);
                Animatoo.animateCard(mContex);
                ((Activity) mContex).finish();

            }
        });

        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetNotConnected(mContex)) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                builder.setMessage("Are You Sure You Want to Delete?")
                        .setCancelable(false)
                        .setIcon(R.drawable.logo_app)
                        .setTitle("Please Confirm");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createProgressDialog();
                        progressDialog.show();


                        FirebaseDatabase.getInstance().getReference().child("Products").child(obj.id).removeValue()
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(mContex, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(mContex, "Deleted Sucessfully", Toast.LENGTH_SHORT).show();
                                ((Activity) mContex).recreate();
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
        viewHolder.orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAlreadyOrdered = false;
                if (internetNotConnected(mContex)) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                builder.setMessage("Are You Sure You Want to Order?")
                        .setCancelable(false)
                        .setIcon(R.drawable.logo_app)
                        .setTitle("Please Confirm");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createProgressDialog();
                        progressDialog.show();

                        FirebaseDatabase.getInstance().getReference().child("Orders").orderByChild("userID").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                Order order = dataSnapshot1.getValue(Order.class);
                                                if (order.productID.equals(obj.id) && order.orderStatus.equals("Pending")) {
                                                    isAlreadyOrdered = true;
                                                    progressDialog.dismiss();
                                                    showSucessAlert("Your Order of " + obj.productName + " is already Pending");
                                                    return;
                                                }
                                            }
                                        }

                                        if (!isAlreadyOrdered) {

                                            mOrderRef = FirebaseDatabase.getInstance().getReference().child("Orders");
                                            String orderID = mOrderRef.push().getKey();

                                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
                                            String currentDate = sdf.format(new Date());


                                            Order order = new Order(orderID, FirebaseAuth.getInstance().getCurrentUser().getUid(), obj.id, obj.productName, obj.productPrice,
                                                    obj.productCategory, obj.kitchenID, obj.kichenEmail,
                                                    obj.cookName, "Pending", currentDate, "Normal Order",
                                                    latitudeUser, longitudeUser, obj.latitudeP, obj.longitudeP);

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
                                                    showSucessAlert("Your Order is Placed");
                                                }
                                            });


                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        progressDialog.dismiss();
                                        Toast.makeText(mContex, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                latitudeProduct = obj.latitudeP;
                longitudeProduct = obj.longitudeP;

                if (internetNotConnected(mContex)) {
                    return;
                }
                showMap(mContex, obj.latitudeP, obj.longitudeP, latitudeUser, longitudeUser);

            }
        });


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

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView nameTv, dateTv, cookNameTv, priceTv, quantityTV, emailTv;
        public ImageView imageView;
        public Button orderBtn, locationBtn, deleteBtn, updateBtn;
        public LinearLayout layoutAA, layoutKK;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.proNameViewHolder);
            imageView = itemView.findViewById(R.id.imgViewHolder);
            priceTv = itemView.findViewById(R.id.proPriceViewHolder);
            cookNameTv = itemView.findViewById(R.id.cookNameViewHolder);
            dateTv = itemView.findViewById(R.id.dateProViewHolder);
            orderBtn = itemView.findViewById(R.id.orderBtnViewHolder);
            locationBtn = itemView.findViewById(R.id.viewLocationAlertBtn);
            deleteBtn = itemView.findViewById(R.id.deleteProductViewHolder);
            updateBtn = itemView.findViewById(R.id.updateAlertBtnViewHolder);
            layoutAA = itemView.findViewById(R.id.layoutCC);
            layoutKK = itemView.findViewById(R.id.layoutKK);
            quantityTV = itemView.findViewById(R.id.quantityViewHolder);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (oListener != null) {
                int posiion = getAdapterPosition();
                if (posiion != RecyclerView.NO_POSITION) {
                    oListener.setProductCardClickListener(posiion);
                }
            }
        }
    }

    public interface OnProductCardItemClickListener {
        void setProductCardClickListener(int position);
    }

    public void setOnProductCardItemClickListener(OnProductCardItemClickListener abc) {
        oListener = abc;
    }

}
