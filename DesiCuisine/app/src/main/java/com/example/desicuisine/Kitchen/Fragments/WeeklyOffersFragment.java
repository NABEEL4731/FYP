package com.example.desicuisine.Kitchen.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.desicuisine.Kitchen.Activities.ViewUserActivity;
import com.example.desicuisine.Kitchen.Activities.ViewWeeklyOfferKitchenActivity;
import com.example.desicuisine.Kitchen.Activities.WeeklySubscriptionKitchenActivity;
import com.example.desicuisine.Models.WeeklyOffer;
import com.example.desicuisine.R;
import com.example.desicuisine.Utils.InternetHelper;
import com.example.desicuisine.Utils.Validations;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeeklyOffersFragment extends Fragment implements View.OnClickListener {
    private View view, alertView;

    private CardView penddingReqCV, viewWkOfferCv, subOferUserCV, addWeeklyOfferCV, acceptedCv, allCv;
    private String day1D, day1L, day1B, day2D, day2L, day2B, day3D, day3L, day3B, day4D, day4L, day4B,
            day5D, day5L, day5B, day6D, day6L, day6B, day7D, day7L, day7B;
    private String offerName, offerPrice;

    private EditText day1DEt, day1LEt, day1BEt, day2DEt, day2LEt, day2BEt, day3DEt, day3LEt, day3BEt, day4DEt, day4LEt, day4BEt,
            day5DEt, day5LEt, day5BEt, day6DEt, day6LEt, day6BEt, day7DEt, day7LEt, day7BEt;
    private EditText offerNameEt, offerPriceEt;
    private Button cencelBtn, addBtn;
    private ProgressDialog progressDialog;
    private DatabaseReference mOfferRef;
    private String mySp = "MYSP";
    private String kitchenID, managerFName;
    private String managerLastName, kitchenEmail;
    private FirebaseAuth mAuth;
    private SharedPreferences sp;
    private String latiInStr, longiINStr;


    public WeeklyOffersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_weekly_offers, container, false);

        connectivity();

        penddingReqCV.setOnClickListener(this);
        viewWkOfferCv.setOnClickListener(this);
        subOferUserCV.setOnClickListener(this);
        addWeeklyOfferCV.setOnClickListener(this);
        allCv.setOnClickListener(this);
        acceptedCv.setOnClickListener(this);

        return view;
    }


    private void connectivity() {

        getActivity().setTitle("Weekly Menu");
        acceptedCv = view.findViewById(R.id.viewAcceptedOfersCv);
        allCv = view.findViewById(R.id.viewAllOffersCv);
        penddingReqCV = view.findViewById(R.id.pendingWkOfersCv);
        subOferUserCV = view.findViewById(R.id.viewSubUserWkOfersCv);
        addWeeklyOfferCV = view.findViewById(R.id.addWkOfferCV);
        viewWkOfferCv = view.findViewById(R.id.viewWkOfersCv);
        sp = getActivity().getSharedPreferences(mySp, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        kitchenID = mAuth.getCurrentUser().getUid();
        managerFName = sp.getString("FName", null);
        managerLastName = sp.getString("LName", null);
        kitchenEmail = sp.getString("Email", null);
        latiInStr = sp.getString("Lat", null);
        longiINStr = sp.getString("Long", null);

    }

    @Override
    public void onClick(View v) {
        Intent intent;

        int id = v.getId();
        switch (id) {
            case R.id.pendingWkOfersCv:
                intent = new Intent(getActivity(), WeeklySubscriptionKitchenActivity.class);
                intent.putExtra("CHOISE", "Pending Offers");
                startActivity(intent);
                Animatoo.animateCard(getActivity());
                getActivity().finish();
                break;
            case R.id.viewAcceptedOfersCv:
                intent = new Intent(getActivity(), WeeklySubscriptionKitchenActivity.class);
                intent.putExtra("CHOISE", "Accepted Offers");
                startActivity(intent);
                Animatoo.animateCard(getActivity());
                getActivity().finish();
                break;
            case R.id.viewAllOffersCv:
                intent = new Intent(getActivity(), WeeklySubscriptionKitchenActivity.class);
                intent.putExtra("CHOISE", "All Offers");
                startActivity(intent);
                Animatoo.animateCard(getActivity());
                getActivity().finish();
                break;
            case R.id.viewWkOfersCv:
                viewWeeklyMethod();
                break;
            case R.id.viewSubUserWkOfersCv:
                Intent intent2 = new Intent(getActivity(), ViewUserActivity.class);
                intent2.putExtra("Fragment", "C");
                startActivity(intent2);
                Animatoo.animateCard(getActivity());
                getActivity().finish();
                break;
            case R.id.addWkOfferCV:
                addWeeklyOfferMethod();
                break;
        }
    }

    private void viewWeeklyMethod() {
        Intent intent = new Intent(getActivity(), ViewWeeklyOfferKitchenActivity.class);
        startActivity(intent);
        Animatoo.animateCard(getActivity());
        getActivity().finish();

    }

    private void addWeeklyOfferMethod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        alertView = getLayoutInflater().inflate(R.layout.add_weekly_offer_layout, null);
        connectivityAddWeeklyAlert();
        builder.setView(alertView);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);


        cencelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offerName = offerNameEt.getText().toString().trim();
                offerPrice = offerPriceEt.getText().toString().trim();
                getDataFromView();
                if (allNotValid()) {
                    return;
                }
                if (internetNotConnected(getContext())) {
                    return;
                }
                createProsessDialog();
                progressDialog.show();

                mOfferRef = FirebaseDatabase.getInstance().getReference().child("Weekly Offers");
                final String offerID = mOfferRef.push().getKey();

                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
                String currentDate = sdf.format(new Date());

                WeeklyOffer obj = new WeeklyOffer(offerID, offerName, offerPrice,
                        day1D, day1L, day1B, day2D, day2L, day2B, day3D, day3L, day3B, day4D, day4L, day4B,
                        day5D, day5L, day5B, day6D, day6L, day6B, day7D, day7L, day7B,
                        managerFName + " " + managerLastName, kitchenEmail, kitchenID,
                        currentDate, latiInStr, longiINStr);
                mOfferRef.child(offerID).setValue(obj).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                alertDialog.dismiss();
                                showSucessAlert("Menu Is Added Sucessfully");
                            }
                        });

            }
        });
        alertDialog.show();

    }

    private void createProsessDialog() {

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait....");

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetAlert.dismiss();
            }
        });
        noInternetAlert.show();
    }

    private void showSucessAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                susessAlert.dismiss();

            }
        });
        susessAlert.show();


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

    private void getDataFromView() {

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
        cencelBtn = alertView.findViewById(R.id.cencleButtonW);
        addBtn = alertView.findViewById(R.id.addButtonW);
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
}
