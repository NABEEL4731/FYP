package com.example.desicuisine.User;

import android.Manifest;
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
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.desicuisine.Kitchen.Activities.KitchenDashboardActivity;
import com.example.desicuisine.Models.Product;
import com.example.desicuisine.Models.User;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Activities.UserDashboardActivity;
import com.example.desicuisine.Utils.InternetHelper;
import com.example.desicuisine.Utils.Validations;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends Activity implements View.OnClickListener {

    private TextView createTv;
    private EditText passEt;
    private CheckBox kitchenCb;
    private EditText emailEt;
    private ToggleButton passTb;
    private String email, password;
    private FirebaseAuth mAuth;
    private String mysp = "MYSP";
    private SharedPreferences sp;
    private ProgressDialog pd;
    private User user;
    private Boolean found = false;
    private EditText emailResetEt;
    private String resetEmail;
    private Button loginBtn;
    private TextView forgetPassTv;
    private Boolean kitchenFlag = false, userFlag = true;
    private Query query;
    private String category;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        connectivity();
        requestPermissionIfNotGranted();

        createTv.setOnClickListener(this);
        passTb.setOnClickListener(this);
        forgetPassTv.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        kitchenCb.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.laCreateNewAcTv:
                intentToRegistration();
                break;

//                visibility button
            case R.id.laToggleButton:
                showHidePassword();
                break;

//                forget Password
            case R.id.lAforgetPassTv:
                startForgotPassMethod();
                break;
//                restaurant
            case R.id.isKitchenCbLA:
                checkKitchenOrUser();
                break;
//                login
            case R.id.laLoginButton:
                loginMethod();
                break;

        }

    }

    private void loginMethod() {

        found = false;
//        validaions
        if (allNotValid()) {
            return;
        }

        email = emailEt.getText().toString().trim();
        password = passEt.getText().toString().trim();
//      inernet connectivity
        if (internetNotConnected(LoginActivity.this)) {
            return;
        }
        createProgressDialog();
        pd.show();

        query = FirebaseDatabase.getInstance().getReference()
                .child(category)
                .orderByChild("emailID").equalTo(email);
        query.addListenerForSingleValueEvent(userValueListener);


    }


    //    fetch data for user
    ValueEventListener userValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String result = dataSnapshot1.child("emailID").getValue(String.class);
                    if (result.equals(email)) {
                        found = true;
                        user = dataSnapshot1.getValue(User.class);
                        break;
                    }
                }
            }
//            user exists
            if (found) {

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {


                                String statusKey = "status";
                                Map<String, Object> statusUpdate = new HashMap<String, Object>();
                                statusUpdate.put(statusKey, "Online");

                                FirebaseDatabase.getInstance().getReference().child(category).child(mAuth.getCurrentUser().getUid())
                                        .updateChildren(statusUpdate).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {


//                                save in Shared Preferences
                                                sp = getSharedPreferences(mysp, MODE_PRIVATE);
                                                SharedPreferences.Editor ed = sp.edit();
                                                ed.clear();
                                                ed.commit();

                                                ed.putString("id", mAuth.getCurrentUser().getUid());
                                                ed.putString("FName", user.firstName);
                                                ed.putString("LName", user.lastName);
                                                ed.putString("Email", user.emailID);
                                                ed.putString("phone", user.phone);
                                                ed.putString("category", category);
                                                ed.putString("ProfileUrl", user.photoUrl);
                                                ed.putString("Lat", user.latitude);
                                                ed.putString("Long", user.longitude);

                                                ed.commit();
                                                if (category.equals("Customer")) {

                                                    Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("Fragment", "A");
                                                    i.putExtras(bundle);
                                                    startActivity(i);
                                                    Animatoo.animateCard(LoginActivity.this);
                                                    finish();
                                                } else {


                                                    Query m = FirebaseDatabase.getInstance().getReference().child("Products").orderByChild("kitchenID").equalTo(mAuth.getCurrentUser().getUid());
                                                    m.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                String statusKey = "status";
                                                                Map<String, Object> statusUpdate = new HashMap<String, Object>();
                                                                statusUpdate.put(statusKey, "Available");
                                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                                    String productID = ds.getValue(Product.class).id;
                                                                    FirebaseDatabase.getInstance().getReference().child("Products").child(productID).updateChildren(statusUpdate);
                                                                }

                                                            }

                                                            Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                                                            i.putExtra("Fragment", "A");
                                                            startActivity(i);
                                                            Animatoo.animateCard(LoginActivity.this);
                                                            finish();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            Toast.makeText(LoginActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });


                                                }
                                            }
                                        });


                            }
                        });

            }
//            no user found
            else {
                pd.dismiss();
                showNoUserFound();
            }

        }


        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            pd.dismiss();
            Toast.makeText(LoginActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };


    private void showNoUserFound() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Button okBtn;
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null, false);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        TextView textView = alertView.findViewById(R.id.alertTv);

        if (category.equals("Kitchen")) {
            textView.setText("No Kitchen Found");
        } else {
            textView.setText("No Customer Found");
        }

        builder.setView(alertView);

        final AlertDialog susessAlert = builder.create();
        susessAlert.setCanceledOnTouchOutside(false);
        susessAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                susessAlert.dismiss();
            }
        });
        susessAlert.show();


    }

    private void checkKitchenOrUser() {

        if (kitchenCb.isChecked()) {
            category = "Kitchen";
        } else {
            category = "Customer";

        }
        kitchenCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    category = "Kitchen";
                } else {
                    category = "Customer";
                }
            }
        });

    }


    private void createProgressDialog() {
        pd = new ProgressDialog(LoginActivity.this);
        pd.setCancelable(false);
        pd.setMessage("Please Wait....");
    }



    private boolean allNotValid() {
        Validations obj = new Validations();
        if (obj.isValidEmailId(emailEt) || obj.isValidPass(passEt)) {
            return true;
        } else {
            return false;
        }
    }


    private void showHidePassword() {
        passTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (passEt.getText().length() > 0) {
                    if (isChecked) {
                        passEt.setSelection(passEt.length());
                        passEt.setTransformationMethod(null);
                        passTb.setBackgroundResource(R.drawable.visible_pass);
                    } else {
                        passEt.setSelection(passEt.length());
                        passEt.setTransformationMethod(new PasswordTransformationMethod());
                        passTb.setBackgroundResource(R.drawable.invisible_pass);
                    }
                    passEt.setSelection(passEt.length());
                }
            }
        });

    }

    public void startForgotPassMethod() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Button resetBtn;
        View alertView = getLayoutInflater().inflate(R.layout.reset_pass_alert, null);
        resetBtn = alertView.findViewById(R.id.resetAlertBtn);
        emailResetEt = alertView.findViewById(R.id.resetPassAlertEt);
        builder.setView(alertView);

        final AlertDialog susessAlert = builder.create();
        susessAlert.setCanceledOnTouchOutside(false);
        susessAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                validtions
                resetEmail = emailResetEt.getText().toString().trim();
                Validations obj = new Validations();
                if (obj.isValidEmailId(emailResetEt)) {
                    return;
                }

//                internet connection
                if (internetNotConnected(LoginActivity.this)) {
                    return;
                }
                mAuth.sendPasswordResetEmail(resetEmail);
                showSucessSendResetPassword();
                susessAlert.dismiss();
            }
        });
        susessAlert.show();
    }

    private void showSucessSendResetPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Button okBtn;
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        TextView textView = alertView.findViewById(R.id.alertTv);
        textView.setText("Password reset link has been sent to your email address, check your email and follow instructions to reset your password ");
        builder.setView(alertView);

        final AlertDialog susessAlert = builder.create();
        susessAlert.setCanceledOnTouchOutside(false);
        susessAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                susessAlert.dismiss();

            }
        });
        susessAlert.show();
    }


    private boolean internetNotConnected(Context mContext) {
        InternetHelper obj = new InternetHelper();
        if (obj.isInternetConnected(mContext)) {
//            "Connected"
            return false;
        } else {
            showNoInternetMsg();
//            "Not Connected"
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
        noInternetAlert.setCanceledOnTouchOutside(false);
        noInternetAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetAlert.dismiss();
            }
        });
        noInternetAlert.show();
    }


    private void intentToRegistration() {
        Intent i = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(i);
        Animatoo.animateSlideLeft(LoginActivity.this);
        finish();
    }

    private void connectivity() {
        passEt = findViewById(R.id.lApassLoginEt);
        emailEt = findViewById(R.id.lAemailLoginEt);
        passTb = findViewById(R.id.laToggleButton);
        kitchenCb = findViewById(R.id.isKitchenCbLA);

        forgetPassTv = findViewById(R.id.lAforgetPassTv);
        createTv = findViewById(R.id.laCreateNewAcTv);
        loginBtn = findViewById(R.id.laLoginButton);
        userFlag = true;
        kitchenFlag = false;
        category = "Customer";
        query = null;
        mAuth = FirebaseAuth.getInstance();


    }

    private void requestPermissionIfNotGranted() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted

                        if (!report.areAllPermissionsGranted()) {
                            showSettingsDialog();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

}
