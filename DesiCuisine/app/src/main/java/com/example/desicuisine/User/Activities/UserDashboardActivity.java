package com.example.desicuisine.User.Activities;

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
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.desicuisine.Kitchen.Activities.ProfileActivity;
import com.example.desicuisine.Models.Feedback;
import com.example.desicuisine.Models.Order;
import com.example.desicuisine.Models.User;
import com.example.desicuisine.Notification.Sending.Token;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Fragments.OrderFragment;
import com.example.desicuisine.User.Fragments.UserHomeFragment;
import com.example.desicuisine.User.Fragments.WeeklySubscriptionUserFragment;
import com.example.desicuisine.User.LoginActivity;
import com.example.desicuisine.Utils.CustomSwipeAdapter;
import com.example.desicuisine.Utils.InternetHelper;
import com.example.desicuisine.Utils.Validations;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class UserDashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences sp;

    private Toolbar toolbar;
    public CollapsingToolbarLayout colapseToolbar;

    private ViewPager mViewPager;
    private String mySp = "MYSP";
    private String userID, userFirstName, userEmail;
    private String userLastName, domain, profileImageUrl;
    private ImageView navProfileIV;
    private FragmentManager fm;
    private FirebaseDatabase firebaseDatabase = null;
    private FirebaseAuth mAuth;
    private String feedback, userCategory;
    private EditText feedbackEt;
    private ProgressDialog progressDialog;
    private DrawerLayout drawer;
    private CircleIndicator indicator = null;
    private CustomSwipeAdapter mCustomSwipeAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);
        sp = getSharedPreferences("MYSP", MODE_PRIVATE);


        connectivity();
        setSupportActionBar(toolbar);
        fm = getSupportFragmentManager();
//        show pager viewer

        mCustomSwipeAdapter = new CustomSwipeAdapter(this);
        mViewPager.setAdapter(mCustomSwipeAdapter);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new MyTimerTask(), 3000, 4000);

        indicator.setViewPager(mViewPager);
        mCustomSwipeAdapter.registerDataSetObserver(indicator.getDataSetObserver());

//        setting header
        NavigationView navigationView = findViewById(R.id.nav_viewUsr);
        View headerView = navigationView.getHeaderView(0);
        TextView tvEmailNav = headerView.findViewById(R.id.navKitEmailTv);
        TextView tvManNAmeNav = headerView.findViewById(R.id.navUserNameTv);
        navProfileIV = headerView.findViewById(R.id.navProfile);

        tvManNAmeNav.setText(userFirstName + " " + userLastName);
        tvEmailNav.setText(userEmail);

//        show picture
        displayProfilePic();


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        firebaseDatabase = FirebaseDatabase.getInstance();
        updateToken(FirebaseInstanceId.getInstance().getToken());
        String replaceFragment = getIntent().getExtras().getString("Fragment", "A");

        if (replaceFragment.equals("B")) {
            fm.beginTransaction().replace(R.id.userMain, new OrderFragment()).commit();
        } else if (replaceFragment.equals("C")) {
            fm.beginTransaction().replace(R.id.userMain, new WeeklySubscriptionUserFragment()).commit();
        } else if (replaceFragment.equals("A")) {
            fm.beginTransaction().replace(R.id.userMain, new UserHomeFragment()).commit();
        } else if (replaceFragment.equals("Notification")) {
            fm.beginTransaction().replace(R.id.userMain, new UserHomeFragment()).commit();
            showNotificationView(getIntent().getExtras().getString("orderData"));

        }
    }

    private void showNotificationView(String orderData) {
        final Order obj = new Gson().fromJson(orderData, Order.class);

        AlertDialog.Builder builderM = new AlertDialog.Builder(UserDashboardActivity.this);
        TextView proNameTv, proPriceTv, dateTv, orderStatusTv, orderCategoryTV, descTv;
        Button showRateBtn;

        View alertViewMain = getLayoutInflater().inflate(R.layout.show_status_layout, null);

        proNameTv = alertViewMain.findViewById(R.id.proNameOrderViewHolder);
        proPriceTv = alertViewMain.findViewById(R.id.proPriceOrderViewHolder);
        orderStatusTv = alertViewMain.findViewById(R.id.orderStatusViewHolder);
        dateTv = alertViewMain.findViewById(R.id.dateOrderViewHolder);
        orderCategoryTV = alertViewMain.findViewById(R.id.orderCategoryViewHolder);
        descTv = alertViewMain.findViewById(R.id.orderDescriptionViewHolder);
        showRateBtn = alertViewMain.findViewById(R.id.rateKitchenBtn);

        if (obj.orderStatus.equals("In Process")) {
            showRateBtn.setVisibility(View.GONE);
        }

        proNameTv.setText(obj.productName);
        proPriceTv.setText("Rs " + obj.productPrice);
        orderStatusTv.setText(obj.orderStatus);
        orderCategoryTV.setText(obj.productCategory);
        descTv.setText(obj.orderDescription);
        dateTv.setText(obj.orderDate);

        builderM.setView(alertViewMain);

        final AlertDialog mainAlert = builderM.create();
        mainAlert.setCanceledOnTouchOutside(false);

        showRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showRattingAlert(obj.kitchenID, mainAlert);
            }
        });

        mainAlert.show();

    }

    private void showRattingAlert(final String kitchenID, final AlertDialog mainAlert) {


        final AlertDialog.Builder builder = new AlertDialog.Builder(UserDashboardActivity.this);
        Button rateBtn;

        final RatingBar ratingBar;
        View alertView = getLayoutInflater().inflate(R.layout.ratting_bar_layout, null);
        rateBtn = alertView.findViewById(R.id.rateButtonR);
        ratingBar = alertView.findViewById(R.id.rBar);
        builder.setView(alertView);

        final AlertDialog ratingAlert = builder.create();
        ratingAlert.setCanceledOnTouchOutside(false);

        rateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetNotConnected(UserDashboardActivity.this)) {
                    return;
                }
                createProsessDialog();
                progressDialog.show();
                FirebaseDatabase.getInstance().getReference().child("Kitchen").child(kitchenID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User u = dataSnapshot.getValue(User.class);

                            double avr = Double.parseDouble(u.rating);
                            int tNum = Integer.parseInt(u.noOfRating);
                            int newTnum = tNum + 1;
                            float currRate = ratingBar.getRating();
                            double newAvg = (((avr * tNum) + currRate) / (newTnum * 5)) * 5;
                            Double num = new Double(newAvg);
                            Double tD = new BigDecimal(num).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//

                            String newAvgRat = String.valueOf(tD);


                            String ratingKey = "rating";
                            String tKey = "noOfRating";
                            Map<String, Object> ratUpdate = new HashMap<String, Object>();
                            ratUpdate.put(ratingKey, newAvgRat);
                            ratUpdate.put(tKey, String.valueOf(newTnum));

                            FirebaseDatabase.getInstance().getReference().child("Kitchen").child(kitchenID).updateChildren(ratUpdate).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(UserDashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            })
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            ratingAlert.dismiss();
                                            mainAlert.dismiss();
                                            Toast.makeText(UserDashboardActivity.this, "Your Rating Submitted Sucessfully", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(UserDashboardActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        ratingAlert.show();

    }


    private void updateToken(String token) {
        Token token1 = new Token(token);
        firebaseDatabase.getReference("Tokens").child(mAuth.getCurrentUser().getUid()).setValue(token1);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (fm.getBackStackEntryCount() != 0) {
                fm.popBackStack();
            } else {
                showCloseAlert();
            }

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_about) {
            showAboutView();
            return true;
        }
        if (id == R.id.action_privacy_policy) {

            showPrivacyView();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_homeUser) {
//            replace home fragment

            fm.beginTransaction().replace(R.id.userMain, new UserHomeFragment()).addToBackStack(null).commit();

//             replace instance home fragment
        } else if (id == R.id.nav_weeklySubUser) {

            fm.beginTransaction().replace(R.id.userMain, new WeeklySubscriptionUserFragment()).addToBackStack(null).commit();

        } else if (id == R.id.nav_ProfileUser) {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
            Animatoo.animateCard(UserDashboardActivity.this);
            finish();
        }

//        order
        else if (id == R.id.nav_orderUser) {

            fm.beginTransaction().replace(R.id.userMain, new OrderFragment()).addToBackStack(null).commit();

        }
//        logout
        else if (id == R.id.logoutUser) {

            showLogOutAlert();
        }

//        feedback
        else if (id == R.id.nav_feedU) {

            showFeedbackAlert();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showLogOutAlert() {
        final AlertDialog.Builder ADB = new AlertDialog.Builder(UserDashboardActivity.this);
        ADB.setMessage("Are You Sure You Want to Logout?")
                .setCancelable(false)
                .setIcon(R.drawable.logo_app)
                .setTitle("Please Confirm");
        ADB.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (internetNotConnected(UserDashboardActivity.this)) {
                    return;
                }
                createProsessDialog();
                progressDialog.show();

                String statusKey = "status";
                Map<String, Object> statusUpdate = new HashMap<String, Object>();
                statusUpdate.put(statusKey, "Offline");

                FirebaseDatabase.getInstance().getReference().child("Customer").child(mAuth.getCurrentUser().getUid())
                        .updateChildren(statusUpdate).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(UserDashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                SharedPreferences.Editor editor = sp.edit();
                                FirebaseAuth.getInstance().signOut();
                                editor.clear();
                                editor.commit();
                                progressDialog.dismiss();
                                startActivity(new Intent(UserDashboardActivity.this, LoginActivity.class));
                                Animatoo.animateSwipeLeft(UserDashboardActivity.this);
                                finish();
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


    public void showCloseAlert() {

        AlertDialog.Builder ADB = new AlertDialog.Builder(UserDashboardActivity.this);
        ADB.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setIcon(R.drawable.logo_app)
                .setTitle("Please Confirm");
        ADB.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        ADB.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        AlertDialog AD = ADB.create();
        AD.show();
    }

    private void showAboutView() {

        View alertView;
        Button closeButton;

        AlertDialog.Builder builder = new AlertDialog.Builder(UserDashboardActivity.this);
        alertView = getLayoutInflater().inflate(R.layout.about_alert_layout, null);
        closeButton = alertView.findViewById(R.id.closeAboutAlertBtn);
        builder.setView(alertView);
        final AlertDialog aboutAlert = builder.create();
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutAlert.dismiss();
            }
        });
        aboutAlert.setCanceledOnTouchOutside(false);
        aboutAlert.show();


    }

    private void showPrivacyView() {

        View alertView;
        Button closeButton;

        AlertDialog.Builder builder = new AlertDialog.Builder(UserDashboardActivity.this);
        alertView = getLayoutInflater().inflate(R.layout.privacy_alert_layout, null);
        closeButton = alertView.findViewById(R.id.closeAboutAlertBtn);
        builder.setView(alertView);
        final AlertDialog aboutAlert = builder.create();
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutAlert.dismiss();
            }
        });
        aboutAlert.setCanceledOnTouchOutside(false);
        aboutAlert.show();


    }

    private void displayProfilePic() {
        Uri picUriOld = Uri.parse(profileImageUrl);

        Glide.with(getApplicationContext())
                .load(picUriOld)
                .apply(new RequestOptions().placeholder(R.drawable.loading_image))
                .into(navProfileIV);

    }

    private void showFeedbackAlert() {

        View feedbackView;
        Button sendBtn;

        AlertDialog.Builder builder = new AlertDialog.Builder(UserDashboardActivity.this);
        feedbackView = getLayoutInflater().inflate(R.layout.feedback_alert_layout, null);
        sendBtn = feedbackView.findViewById(R.id.sendFeedbackBtn);
        feedbackEt = feedbackView.findViewById(R.id.feedbackEt);
        builder.setView(feedbackView);
        final AlertDialog feedBackAlert = builder.create();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feedbackIsNotValid()) {
                    return;
                }

                feedback = feedbackEt.getText().toString().trim();
                if (internetNotConnected(UserDashboardActivity.this)) {
                    return;
                }
                createProsessDialog();
                progressDialog.show();
                DatabaseReference mFeedbackRef = FirebaseDatabase.getInstance().getReference().child("Feedbacks");

                String id = mFeedbackRef.push().getKey();
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
                String currentDate = sdf.format(new Date());

                Feedback obj = new Feedback(id, userID, userFirstName + " " + userLastName, feedback, userCategory, currentDate);

                mFeedbackRef.child(id).setValue(obj).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(UserDashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        feedBackAlert.dismiss();
                        progressDialog.dismiss();
                        showSucessAlert();
                    }
                });
            }
        });
        feedBackAlert.setCanceledOnTouchOutside(false);
        feedBackAlert.show();

    }

    private void createProsessDialog() {

        progressDialog = new ProgressDialog(UserDashboardActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait....");

    }

    private boolean feedbackIsNotValid() {
        Validations objVal = new Validations();
        if (objVal.isValidString(feedbackEt)) {
            return true;
        } else {
            return false;
        }
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

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetAlert.dismiss();
            }
        });
        noInternetAlert.show();
    }

    private void showSucessAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Button okBtn;
        TextView alertTv;
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        alertTv = alertView.findViewById(R.id.alertTv);
        alertTv.setText("Your feedback has been sent sucessfully");
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


    private void connectivity() {
        mAuth = FirebaseAuth.getInstance();
        mViewPager = findViewById(R.id.viewPager);
        sp = getSharedPreferences(mySp, MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        colapseToolbar = findViewById(R.id.colapseToolbarUser);
        indicator = (CircleIndicator) findViewById(R.id.indicatorKn);

        profileImageUrl = sp.getString("ProfileUrl", null);

        userID = sp.getString("id", null);
        userFirstName = sp.getString("FName", null);
        userCategory = sp.getString("category", null);
        userLastName = sp.getString("LName", null);
        userEmail = sp.getString("Email", null);


    }


    public class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            UserDashboardActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    if (mViewPager.getCurrentItem() == 0) {
                        mViewPager.setCurrentItem(1);

                    } else if (mViewPager.getCurrentItem() == 1) {
                        mViewPager.setCurrentItem(2);
                    } else if (mViewPager.getCurrentItem() == 2) {
                        mViewPager.setCurrentItem(3);
                    } else if (mViewPager.getCurrentItem() == 3) {
                        mViewPager.setCurrentItem(4);
                    } else if (mViewPager.getCurrentItem() == 4) {
                        mViewPager.setCurrentItem(5);
                    } else if (mViewPager.getCurrentItem() == 5) {
                        mViewPager.setCurrentItem(6);
                    } else if (mViewPager.getCurrentItem() == 6) {
                        mViewPager.setCurrentItem(7);
                    } else if (mViewPager.getCurrentItem() == 7) {
                        mViewPager.setCurrentItem(8);
                    } else {
                        mViewPager.setCurrentItem(0);
                    }
                }
            });
        }

    }
}
