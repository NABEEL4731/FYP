package com.example.desicuisine.User;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.example.desicuisine.Kitchen.Activities.KitchenDashboardActivity;
import com.example.desicuisine.Models.User;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Activities.UserDashboardActivity;
import com.example.desicuisine.Utils.InternetHelper;
import com.example.desicuisine.Utils.PackageUtil;
import com.example.desicuisine.Utils.Validations;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class SignUpActivity extends Activity implements View.OnClickListener {
    private EditText firstNameEt, lastNameEt, phoneEt, emailEt, passEt, conPassEt;
    private TextView alreadyHaveAc;

    private String firstName, lastName, email, pass, conPass, phone, category = null;

    private ToggleButton passTb, conPassTb;
    private CheckBox kitchenCb;

    private AlertDialog.Builder builder;
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private final static int Pick_IMAGE_REQUEST_CODE = 200;
    private Uri photoUri = null;
    private ImageView profileImageIV = null;
    private ImageView pickProfileImageIV = null;

    private static final int permission_code = 300;

    private Button signUpBtn;

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private DatabaseReference mRealTimeRef;
    private String id, downLoadURL;

    private ProgressDialog searchDialog, locationDialog, saveDialog;
    private UploadTask uploadTask;

    private String mysp = "MYSP";
    private SharedPreferences sp;


    private Boolean found = false;
    private User userObj;
    private Query query;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude, longitude;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures").child("Uploads");
        // Write a message to the database
        mRealTimeRef = FirebaseDatabase.getInstance().getReference();

        connectivity();
        pickProfileImageIV.setOnClickListener(this);
        passTb.setOnClickListener(this);
        conPassTb.setOnClickListener(this);
        signUpBtn.setOnClickListener(this);
        kitchenCb.setOnClickListener(this);
        alreadyHaveAc.setOnClickListener(this);
        kitchenCb.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.pickProfilePhotoIv:
                setUpProfileImageAlert();
                break;

            case R.id.tbPassSu:
                showPassword();
                break;

            case R.id.tbConPassSu:
                showConPassword();
                break;

            case R.id.cbIsKitchenSu:
                checkKitchenOrUser();
                break;

            case R.id.signUpButtonSu:
                setUpSignUpMethod();
                break;

            case R.id.alreadyTvSu:
                intentToLogin();
                break;

        }
    }


    private void intentToLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        Animatoo.animateSwipeLeft(SignUpActivity.this);
        finish();
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


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        Animatoo.animateSwipeLeft(SignUpActivity.this);
        finish();
    }


    private void showConPassword() {
        conPassTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (conPassEt.getText().length() > 0) {
                    if (isChecked) {
                        conPassEt.setSelection(conPassEt.length());
                        conPassEt.setTransformationMethod(null);
                        conPassTb.setBackgroundResource(R.drawable.visible_pass);
                    } else {
                        conPassEt.setSelection(conPassEt.length());
                        conPassEt.setTransformationMethod(new PasswordTransformationMethod());
                        conPassTb.setBackgroundResource(R.drawable.invisible_pass);
                    }
                    conPassEt.setSelection(conPassEt.length());
                }
            }
        });
    }


    private void showPassword() {
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


    private void setUpProfileImageAlert() {
        builder = new AlertDialog.Builder(SignUpActivity.this);

        builder.setMessage("Complete This Action Using");
        builder.setTitle("Please Confirm");
        builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startGallaryPicRequest();

            }
        }).setNegativeButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                startTakePictureIntent();

            }
        }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog1 = builder.create();
        dialog1.setCanceledOnTouchOutside(false);
        dialog1.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        dialog1.show();

    }


    private void setUpSignUpMethod() {

        found = false;
        firstName = firstNameEt.getText().toString().trim();
        lastName = lastNameEt.getText().toString().trim();
        checkKitchenOrUser();
        email = emailEt.getText().toString().trim();
        pass = passEt.getText().toString().trim();
        conPass = conPassEt.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();


        if (allNotValid()) {
            return;
        }

        if (checkGPS()) {
            return;
        }

        if (internetNotConnected(SignUpActivity.this)) {
            return;
        }

        searchDialog = createProcessDialog("Please Wait...");
        searchDialog.show();
//              check if already exist
        query = FirebaseDatabase.getInstance().getReference()
                .child(category)
                .orderByChild("emailID")
                .equalTo(email);

        query.addListenerForSingleValueEvent(userValueListener);
    }


    private void showAlreadyExistMsg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Button okBtn;
        TextView alertTv;
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        alertTv = alertView.findViewById(R.id.alertTv);
        alertTv.setText("Sorry! This email is already exists");
        builder.setView(alertView);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }


    ValueEventListener userValueListener = new ValueEventListener() {


        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


            String key = "emailID";
            if (dataSnapshot.exists()) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    String result = dataSnapshot1.child(key).getValue(String.class);
                    if (result.equals(email)) {
                        searchDialog.dismiss();
                        found = true;
                        showAlreadyExistMsg();
                        break;
                    }
                }

            }

            if (found == false) {
                searchDialog.dismiss();
                getLocation();

            }


        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            searchDialog.dismiss();
            Toast.makeText(SignUpActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };


    private void getLocation() {

        locationDialog = createProcessDialog("Please Wait Getting Location...");
        locationDialog.show();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                locationDialog.dismiss();
                createAndSave(latitude, longitude);

                if (locationManager != null) {
                    locationManager.removeUpdates(this);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(i, 700);
            }

        };
        abc();

    }


    private void createAndSave(final double latitude, final double longitude) {

        saveDialog = createProcessDialog("Saving Data Please Wait...");
        saveDialog.show();
        mAuth.createUserWithEmailAndPassword(email, pass).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                saveDialog.dismiss();
                Toast.makeText(SignUpActivity.this, "Account " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        })
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        id = mAuth.getCurrentUser().getUid();

                        uploadTask = mStorageRef.child(id).putFile(photoUri);

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return mStorageRef.child(id).getDownloadUrl();
                            }
                        })
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downLoadURL = uri.toString();
                                        userObj = new User(id, firstName, lastName, email, phone, downLoadURL, category, "5.0", "1",
                                                String.valueOf(latitude), String.valueOf(longitude),"Online");
                                        mRealTimeRef.child(category).child(id).setValue(userObj).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                saveDialog.dismiss();
                                                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                saveDialog.dismiss();

//                                save in Shared Preferences
                                                sp = getSharedPreferences(mysp, MODE_PRIVATE);
                                                SharedPreferences.Editor ed = sp.edit();
                                                ed.clear();
                                                ed.commit();
                                                ed.putString("id", mAuth.getCurrentUser().getUid());
                                                ed.putString("FName", userObj.firstName);
                                                ed.putString("LName", userObj.lastName);
                                                ed.putString("Email", userObj.emailID);
                                                ed.putString("phone", userObj.phone);
                                                ed.putString("category", category);
                                                ed.putString("ProfileUrl", userObj.photoUrl);
                                                ed.putString("Lat", userObj.latitude);
                                                ed.putString("Long", userObj.longitude);
                                                ed.commit();
                                                Toast.makeText(SignUpActivity.this, "Created Account", Toast.LENGTH_SHORT).show();

                                                if (category.equals("Customer")) {
                                                    Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("Fragment", "A");
                                                    i.putExtras(bundle);

                                                    Animatoo.animateCard(SignUpActivity.this);
                                                    startActivity(i);
                                                    finish();
                                                } else if (category.equals("Kitchen")) {
                                                    Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("Fragment", "A");
                                                    i.putExtras(bundle);
                                                    Animatoo.animateCard(SignUpActivity.this);
                                                    startActivity(i);
                                                    finish();
                                                }

                                            }
                                        });

                                    }

                                });
                    }
                });
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
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null, false);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        alertTv = alertView.findViewById(R.id.alertTv);
        alertTv.setText("Sorry No Internet Conection");
        builder.setView(alertView);
        final AlertDialog noInternetAlert = builder.create();
        noInternetAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetAlert.dismiss();
            }
        });
        noInternetAlert.show();
    }


    private boolean allNotValid() {
        Validations objVal = new Validations();
        if (objVal.isValidName(firstNameEt) || objVal.isValidName(lastNameEt) ||
                objVal.isValidEmailId(emailEt) || objVal.isValidPass(passEt) || objVal.isValidConPass(conPassEt, passEt) ||
                objVal.isValidPhone(phoneEt) || isUserCategorySelected() ||
                isProfileImageChoosen()) {
            return true;
        } else {
            return false;
        }
    }


    private boolean isProfileImageChoosen() {
        if (photoUri == null) {
            Toast.makeText(this, "Please Choose Your Profile Photo", Toast.LENGTH_SHORT).show();
            return true;
        } else {


            return false;
        }
    }


    private boolean isUserCategorySelected() {
        if (category.equals(null)) {
            return true;
        } else
            return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap;
                Bundle b = data.getExtras();
                bitmap = (Bitmap) b.get("data");
                photoUri = saveInGallary(getApplicationContext(), bitmap);


                Glide.with(getApplicationContext())
                        .load(photoUri)
                        .into(profileImageIV);


            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == Pick_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            photoUri = data.getData();
            Glide.with(getApplicationContext())
                    .load(photoUri)
                    .into(profileImageIV);
        }
    }


    private void opencamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

    }


    private void startTakePictureIntent() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

    }


    private void startGallaryPicRequest() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i.createChooser(i, "Select an Image"), Pick_IMAGE_REQUEST_CODE);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 700:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abc();
                }
                break;

            case permission_code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    opencamera();
                }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }


    private void connectivity() {

        firstNameEt = findViewById(R.id.etFNameSu);

        lastNameEt = findViewById(R.id.etlNameLA);
        profileImageIV = findViewById(R.id.suProfileImg);
        pickProfileImageIV = findViewById(R.id.pickProfilePhotoIv);

        passEt = findViewById(R.id.etPassSu);
        passTb = findViewById(R.id.tbPassSu);
        kitchenCb = findViewById(R.id.cbIsKitchenSu);
        kitchenCb.setChecked(false);
        category = "Customer";

        conPassEt = findViewById(R.id.etConPassSu);
        conPassTb = findViewById(R.id.tbConPassSu);

        emailEt = findViewById(R.id.etEmailSu);

        phoneEt = findViewById(R.id.etPhoneSu);
        alreadyHaveAc = findViewById(R.id.alreadyTvSu);


        signUpBtn = findViewById(R.id.signUpButtonSu);

        sp = getSharedPreferences(mysp, MODE_PRIVATE);


    }


    private void abc() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 700);
            }
            return;
        }

        locationManager.requestLocationUpdates("gps", 3000, 0, locationListener);
    }


    private Uri saveInGallary(Context applicationContext, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(applicationContext.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }


    private ProgressDialog createProcessDialog(String msg) {
        ProgressDialog processDialog = new ProgressDialog(SignUpActivity.this);
        processDialog.setCancelable(false);
        processDialog.setIndeterminate(false);
        processDialog.setMessage(msg);

        return processDialog;
    }


    private boolean checkGPS() {
        if (!isGpsEnabled(SignUpActivity.this)) {
            Toast.makeText(this, "Please Enable Your GPS", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }


    public static boolean isGpsEnabled(Context context) {

        if (PackageUtil.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String providers = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (TextUtils.isEmpty(providers)) {
                return false;
            }
            return providers.contains(LocationManager.GPS_PROVIDER);
        } else {
            final int locationMode;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            switch (locationMode) {
                case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                    return true;
                case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                case Settings.Secure.LOCATION_MODE_OFF:
                default:
                    return false;
            }
        }
    }


}

