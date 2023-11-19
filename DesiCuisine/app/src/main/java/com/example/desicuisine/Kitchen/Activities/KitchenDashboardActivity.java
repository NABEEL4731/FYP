package com.example.desicuisine.Kitchen.Activities;

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
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.desicuisine.Kitchen.Fragments.HomeFragment;
import com.example.desicuisine.Kitchen.Fragments.ProductsKitFragment;
import com.example.desicuisine.Kitchen.Fragments.WeeklyOffersFragment;
import com.example.desicuisine.Models.Feedback;
import com.example.desicuisine.Models.Product;
import com.example.desicuisine.Notification.Sending.Token;
import com.example.desicuisine.R;
import com.example.desicuisine.User.LoginActivity;
import com.example.desicuisine.Utils.InternetHelper;
import com.example.desicuisine.Utils.Validations;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class KitchenDashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences sp;
    public Toolbar toolbar;
    //    private CustomSwipeAdapter mCustomSwipeAdapter;

    private String mySp = "MYSP";
    private String managerFName, profileImageUrl;
    private String managerLastName, kitchenEmail;
    private ImageView navProfileIV;
    private FragmentManager fm;
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase = null;
    private String feedback, userCategory, kitID;
    private EditText feedbackEt;
    private ProgressDialog progressDialog;
    private DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen_dashboard);

        connectivity();
        setSupportActionBar(toolbar);
        fm = getSupportFragmentManager();

//        setting header
        NavigationView navigationView = findViewById(R.id.nav_viewKit);
        View headerView = navigationView.getHeaderView(0);
        TextView tvEmailNav = headerView.findViewById(R.id.navKitEmailTv);
        TextView tvManNAmeNav = headerView.findViewById(R.id.navUserNameTv);
        navProfileIV = headerView.findViewById(R.id.navProfile);


        tvManNAmeNav.setText(managerFName + " " + managerLastName);
        tvEmailNav.setText(kitchenEmail);

//        show picture
        displayProfilePic();


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        firebaseDatabase = FirebaseDatabase.getInstance();
        updateToken(FirebaseInstanceId.getInstance().getToken());


        String replacePivateFragment = getIntent().getStringExtra("Fragment");

        if (replacePivateFragment.equals("C")) {
            fm.beginTransaction().replace(R.id.kitchenMain, new WeeklyOffersFragment()).commit();
        } else if (replacePivateFragment.equals("B")) {
            fm.beginTransaction().replace(R.id.kitchenMain, new ProductsKitFragment()).commit();
        } else {
            fm.beginTransaction().replace(R.id.kitchenMain, new HomeFragment()).commit();
        }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layoutKit);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (fm.getBackStackEntryCount() != 0) {
                fm.popBackStack();
            } else
                showCloseAlert();
        }
    }


    private void updateToken(String token) {
        Token token1 = new Token(token);
        firebaseDatabase.getReference("Tokens").child(mAuth.getCurrentUser().getUid()).setValue(token1);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_homeKit) {

//            replace home fragment
            fm.beginTransaction().replace(R.id.kitchenMain, new HomeFragment()).addToBackStack(null).commit();

//             replace instance home fragment
        } else if (id == R.id.productKitNav) {

            fm.beginTransaction().replace(R.id.kitchenMain, new ProductsKitFragment()).addToBackStack(null).commit();

        } else if (id == R.id.weeklyOfferKit) {

            fm.beginTransaction().replace(R.id.kitchenMain, new WeeklyOffersFragment()).addToBackStack(null).commit();

        } else if (id == R.id.profileKit) {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.putExtra("Fragment", "A");
            startActivity(intent);
            Animatoo.animateCard(KitchenDashboardActivity.this);
            finish();
        }

//        logout
        else if (id == R.id.logoutKit) {

            showLogOutAlert();
        }

//        feedback
        else if (id == R.id.nav_feedK) {

            showFeedbackAlert();
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layoutKit);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showLogOutAlert() {
        AlertDialog.Builder ADB = new AlertDialog.Builder(KitchenDashboardActivity.this);
        ADB.setMessage("Are You Sure You Want to Logout?")
                .setCancelable(false)
                .setIcon(R.drawable.logo_app)
                .setTitle("Please Confirm");
        ADB.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (internetNotConnected(KitchenDashboardActivity.this)) {
                    return;
                }
                createProsessDialog();
                progressDialog.show();
                Query m = FirebaseDatabase.getInstance().getReference().child("Products").orderByChild("kitchenID").equalTo(kitID);
                m.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String statusKey = "status";
                            Map<String, Object> statusUpdate = new HashMap<String, Object>();
                            statusUpdate.put(statusKey, "Unavailable");
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                String productID = ds.getValue(Product.class).id;
                                FirebaseDatabase.getInstance().getReference().child("Products").child(productID).updateChildren(statusUpdate);
                            }

                        }

                        String statusKeyK = "status";
                        Map<String, Object> statusUpdateK = new HashMap<String, Object>();
                        statusUpdateK.put(statusKeyK, "Offline");

                        FirebaseDatabase.getInstance().getReference().child("Kitchen").child(mAuth.getCurrentUser().getUid())
                                .updateChildren(statusUpdateK).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(KitchenDashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                        startActivity(new Intent(KitchenDashboardActivity.this, LoginActivity.class));
                                        Animatoo.animateSwipeLeft(KitchenDashboardActivity.this);
                                        finish();

                                    }
                                });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(KitchenDashboardActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

        AlertDialog.Builder ADB = new AlertDialog.Builder(KitchenDashboardActivity.this);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(KitchenDashboardActivity.this);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(KitchenDashboardActivity.this);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(KitchenDashboardActivity.this);
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
                if (internetNotConnected(KitchenDashboardActivity.this)) {
                    return;
                }
                createProsessDialog();
                progressDialog.show();
                DatabaseReference mFeedbackRef = FirebaseDatabase.getInstance().getReference().child("Feedbacks");

                String id = mFeedbackRef.push().getKey();
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
                String currentDate = sdf.format(new Date());

                Feedback obj = new Feedback(id, kitID, managerFName + " " + managerLastName, feedback, userCategory, currentDate);

                mFeedbackRef.child(id).setValue(obj).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(KitchenDashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        feedBackAlert.dismiss();
                        progressDialog.dismiss();
                        showSucessAlert("Your feedback has been sent sucessfully");
                    }
                });
            }
        });
        feedBackAlert.setCanceledOnTouchOutside(false);
        feedBackAlert.show();

    }


    private void createProsessDialog() {

        progressDialog = new ProgressDialog(KitchenDashboardActivity.this);
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
//            Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            showNoInternetMsg(mContext);
//            Toast.makeText(mContext, "Not Connected", Toast.LENGTH_SHORT).show();
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
//        builder.setCancelable(false);

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

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                susessAlert.dismiss();

            }
        });
        susessAlert.show();


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


    private void connectivity() {

        sp = getSharedPreferences(mySp, MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbarKit);
        drawer = findViewById(R.id.drawer_layoutKit);

        mAuth = FirebaseAuth.getInstance();
        managerFName = sp.getString("FName", null);
        kitID = sp.getString("id", null);
        managerLastName = sp.getString("LName", null);
        kitchenEmail = sp.getString("Email", null);
        profileImageUrl = sp.getString("ProfileUrl", null);
        userCategory = sp.getString("category", null);
        setTitle("Main Menu");

    }

}
