package com.example.desicuisine.User.Adapters;

import android.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.desicuisine.Models.User;
import com.example.desicuisine.R;
import com.example.desicuisine.Utils.InternetHelper;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class KitchenViewUserAdapter extends RecyclerView.Adapter<KitchenViewUserAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<User> kitchenList;
    private String mySp = "MYSP";
    private SharedPreferences sp;


    private String latitudeUser, longitudeUser;
    private String latitudeKit, longitudeKit;
    private OnkitchenViewClickListener oListener;

    public KitchenViewUserAdapter(Context mContext, ArrayList<User> expertList) {
        this.mContext = mContext;
        this.kitchenList = expertList;
        sp = mContext.getSharedPreferences(mySp, MODE_PRIVATE);
        latitudeUser = sp.getString("Lat", null);
        longitudeUser = sp.getString("Long", null);

    }

    @NonNull
    @Override
    public KitchenViewUserAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.kichen_view_layout, viewGroup, false);
        return new MyViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull KitchenViewUserAdapter.MyViewHolder myViewHolder, int i) {

        final User obj = kitchenList.get(i);
        myViewHolder.cookNameTv.setText(obj.firstName + " " + obj.lastName);
        myViewHolder.kitchenEmailTv.setText(obj.emailID);
        myViewHolder.kitchenRatingTv.setText("Rating " + obj.rating + " based on " + obj.noOfRating + " Reveiws");
        myViewHolder.kitchenPhoneTv.setText(obj.phone);
        myViewHolder.statusTv.setText(obj.status);

        Uri imageUri = Uri.parse(obj.photoUrl);
        Glide.with(mContext)
                .load(imageUri)
                .apply(new RequestOptions().placeholder(R.drawable.loading_image))
                .into(myViewHolder.kitchenImage);


        if (obj.category.equals("Customer")) {
            myViewHolder.kitchenRatingTv.setVisibility(View.GONE);
        }
        myViewHolder.locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                latitudeKit = obj.latitude;
                longitudeKit = obj.longitude;

                if (internetNotConnected(mContext)) {
                    return;
                }
                showMap(mContext, latitudeKit, longitudeKit, latitudeUser, longitudeUser);

            }
        });

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
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        Button okBtn;
        TextView alertTv;
        View alertView = LayoutInflater.from(mContext).inflate(R.layout.informational_alert_layout_access, null);
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


    @Override
    public int getItemCount() {
        return kitchenList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView kitchenImage;
        public TextView kitchenEmailTv, cookNameTv, kitchenPhoneTv, kitchenRatingTv, statusTv;

        public Button locationBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            kitchenImage = itemView.findViewById(R.id.kitchenImageL1);
            cookNameTv = itemView.findViewById(R.id.cookNameL1);
            kitchenEmailTv = itemView.findViewById(R.id.kitchenEmailL1);
            kitchenRatingTv = itemView.findViewById(R.id.ratingDomainL1);
            kitchenPhoneTv = itemView.findViewById(R.id.kitchenPhoneL1);
            locationBtn = itemView.findViewById(R.id.locationButtonViewKitchen);
            statusTv = itemView.findViewById(R.id.statusL1);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (oListener != null) {
                int posiion = getAdapterPosition();
                if (posiion != RecyclerView.NO_POSITION) {
                    oListener.OnKitchenViewCardClick(posiion);
                }
            }
        }
    }

    public interface OnkitchenViewClickListener {
        void OnKitchenViewCardClick(int position);
    }

    public void setOnKitchenViewClickListener(OnkitchenViewClickListener abc) {
        oListener = abc;
    }
}

