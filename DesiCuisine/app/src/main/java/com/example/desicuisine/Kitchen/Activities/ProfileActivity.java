package com.example.desicuisine.Kitchen.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.desicuisine.Models.Order;
import com.example.desicuisine.Models.Product;
import com.example.desicuisine.Models.WeeklyOffer;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Activities.UserDashboardActivity;
import com.example.desicuisine.Utils.InternetHelper;
import com.example.desicuisine.Utils.PackageUtil;
import com.example.desicuisine.Utils.Validations;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText firstNameEt, lastNameEt, phoneEt;
    private String firstName, lastName, phone, categoryOld = null;
    private TextView categoryTV, emailTv;

    private AlertDialog.Builder builder;
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private final static int Pick_IMAGE_REQUEST_CODE = 200;
    private Uri photoUriNew = null;
    private ImageView profileImageIV = null;
    private ImageView pickProfileImageIV = null;
    private ImageView editBtnIV = null;

    private Button updateBtn, changePassBtn, locationBtn;
    private StorageReference mStorageRef;
    private String downLoadURL;

    private ProgressDialog locationDialog, saveDialog;

    private String mysp = "MYSP";
    private SharedPreferences sp;


    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude, longitude;

    private String userIDOld;
    private String firstNameOld;
    private String lastNameOld;
    private String emailOld;
    private String phoneOld;
    private String photoDownloadLinkOld;

    private String oldPass, newPass, conPass;
    private ToggleButton oldPassTgleBtn, newPassTgleBtn, conPassTgleBtn;
    private EditText oldPassEt, newPassEt, conPassEt;
    private ProgressDialog changePassProcessDialog;

    private long child, i;
    private boolean move1, move2, move3, move4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        connectivity();
        setValuesOnAlert();

        editBtnIV.setOnClickListener(this);
        pickProfileImageIV.setOnClickListener(this);
        changePassBtn.setOnClickListener(this);
        updateBtn.setOnClickListener(this);
        locationBtn.setOnClickListener(this);

    }

    private void makeEditableAll() {

        pickProfileImageIV.setEnabled(true);
        firstNameEt.setFocusable(true);
        firstNameEt.setClickable(true);
        firstNameEt.setCursorVisible(true);
        firstNameEt.setSelection(firstNameEt.getText().toString().length());
        firstNameEt.setFocusableInTouchMode(true);
        firstNameEt.requestFocus();

        lastNameEt.setFocusable(true);
        lastNameEt.setClickable(true);
        lastNameEt.setCursorVisible(true);
        lastNameEt.setSelection(lastNameEt.getText().toString().length());
        lastNameEt.setFocusableInTouchMode(true);

        phoneEt.setFocusable(true);
        phoneEt.setClickable(true);
        phoneEt.setCursorVisible(true);
        phoneEt.setSelection(phoneEt.getText().toString().length());
        phoneEt.setFocusableInTouchMode(true);
        updateBtn.setEnabled(true);

        Toast.makeText(getApplicationContext(), "You can Update your Profile", Toast.LENGTH_SHORT).show();
    }

    private void setValuesOnAlert() {
        emailTv.setText(emailOld);
        categoryTV.setText(categoryOld);
        firstNameEt.setText(firstNameOld);
        lastNameEt.setText(lastNameOld);
        phoneEt.setText(phoneOld);

        displayProfilePic();
    }

    private void displayProfilePic() {
        final Uri picUriOld = Uri.parse(photoDownloadLinkOld);

        Glide.with(getApplicationContext())
                .load(picUriOld)
                .apply(new RequestOptions().placeholder(R.drawable.loading_image))
                .into(profileImageIV);

    }

    private void connectivity() {

        setTitle("Profile");
        firstNameEt = findViewById(R.id.etFNameP);
        lastNameEt = findViewById(R.id.etlNameP);
        phoneEt = findViewById(R.id.etPhoneP);

        profileImageIV = findViewById(R.id.profilePhotoProfileAlertIv);
        pickProfileImageIV = findViewById(R.id.pickProfilePhotoProfileAlertBtnIv);
        pickProfileImageIV.setEnabled(false);

        editBtnIV = findViewById(R.id.editBtn);
        emailTv = findViewById(R.id.emailProfileAlertTv);
        categoryTV = findViewById(R.id.categoryUserProfileAlertTv);

        updateBtn = findViewById(R.id.profileAlertUpdateBtn);
        updateBtn.setEnabled(false);
        changePassBtn = findViewById(R.id.profileAlertChangePasswordBtn);
        locationBtn = findViewById(R.id.profileLocationBtn);

        sp = getSharedPreferences(mysp, MODE_PRIVATE);
        categoryOld = sp.getString("category", null);


        userIDOld = sp.getString("id", null);

        firstNameOld = sp.getString("FName", null);
        lastNameOld = sp.getString("LName", null);
        emailOld = sp.getString("Email", null);
        phoneOld = sp.getString("phone", null);
        photoDownloadLinkOld = sp.getString("ProfileUrl", null);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.editBtn:
                makeEditableAll();
                break;

            case R.id.pickProfilePhotoProfileAlertBtnIv:
                setUpProfileImageAlert();
                break;

            case R.id.profileAlertChangePasswordBtn:
                showResetPasswordAlert();
                break;

            case R.id.profileAlertUpdateBtn:
                updateProfileMethod();
                break;

            case R.id.profileLocationBtn:
                setLocationMethod();
                break;
        }
    }

    private void updateProfileMethod() {

        firstName = firstNameEt.getText().toString().trim();
        lastName = lastNameEt.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();

        if (allNotValid()) {
            return;
        }
        if (internetNotConnected(ProfileActivity.this)) {
            return;
        }

        UploadTask uploadTask = null;
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures").child("Uploads");
        // Write a message to the database


        saveDialog = createProcessDialog("Updating Please Wait...");
        saveDialog.show();

        if (photoUriNew == null) {
            final String fNameKey = "firstName", lastNameKey = "lastName", phoneKey = "phone", photoUrlKey = "photoUrl";

            Map<String, Object> profileUpdate = new HashMap<String, Object>();
            profileUpdate.put(fNameKey, firstName);
            profileUpdate.put(lastNameKey, lastName);
            profileUpdate.put(phoneKey, phone);


            FirebaseDatabase.getInstance().getReference().child(categoryOld).child(userIDOld).updateChildren(profileUpdate).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    saveDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            sp = getSharedPreferences(mysp, MODE_PRIVATE);
                            SharedPreferences.Editor ed = sp.edit();
                            ed.putString("FName", firstName);
                            ed.putString("LName", lastName);
                            ed.putString("phone", phone);

                            ed.commit();
                            saveDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Your Profile is Updated Sucessfully", Toast.LENGTH_SHORT).show();
                            recreate();
                        }
                    });

        } else {
            uploadTask = mStorageRef.child(userIDOld).putFile(photoUriNew);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return mStorageRef.child(userIDOld).getDownloadUrl();
                }
            })
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downLoadURL = uri.toString();

                            final String fNameKey = "firstName", lastNameKey = "lastName", phoneKey = "phone", photoUrlKey = "photoUrl";

                            Map<String, Object> profileUpdate = new HashMap<String, Object>();
                            profileUpdate.put(fNameKey, firstName);
                            profileUpdate.put(lastNameKey, lastName);
                            profileUpdate.put(phoneKey, phone);
                            profileUpdate.put(photoUrlKey, downLoadURL);

                            FirebaseDatabase.getInstance().getReference().child(categoryOld).child(userIDOld).updateChildren(profileUpdate).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    saveDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            })
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            sp = getSharedPreferences(mysp, MODE_PRIVATE);
                                            SharedPreferences.Editor ed = sp.edit();
                                            ed.putString("FName", firstName);
                                            ed.putString("LName", lastName);
                                            ed.putString("phone", phone);
                                            ed.putString("ProfileUrl", downLoadURL);
                                            ed.commit();
                                            saveDialog.dismiss();
                                            Toast.makeText(ProfileActivity.this, "Your Profile is Updated Sucessfully", Toast.LENGTH_SHORT).show();
                                            recreate();
                                        }
                                    });


                        }
                    });

        }
    }

    private boolean allNotValid() {
        Validations objVal = new Validations();
        if (objVal.isValidName(firstNameEt) || objVal.isValidName(lastNameEt) ||
                objVal.isValidPhone(phoneEt)) {
            return true;
        } else {
            return false;
        }
    }

    private void setLocationMethod() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ProfileActivity.this);

        builder1.setMessage("Are You Sure You Want To Update Location ");
        builder1.setTitle("Please Confirm");
        builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getLocation();

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog1 = builder1.create();
        dialog1.setCanceledOnTouchOutside(false);
        dialog1.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        dialog1.show();

    }

    private void getLocation() {

        if (checkGPS()) {
            return;
        }
        if (internetNotConnected(ProfileActivity.this)) {
            return;
        }

        locationDialog = createProcessDialog("Please Wait Getting Location...");
        locationDialog.show();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                locationDialog.dismiss();
                updateLocaion(latitude, longitude);

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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case 700:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abc();
                }
                break;


            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

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


    private ProgressDialog createProcessDialog(String msg) {
        ProgressDialog processDialog = new ProgressDialog(ProfileActivity.this);
        processDialog.setCancelable(false);
        processDialog.setIndeterminate(false);
        processDialog.setMessage(msg);

        return processDialog;
    }


    private boolean checkGPS() {
        if (!isGpsEnabled(ProfileActivity.this)) {
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


    private void showResetPasswordAlert() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(ProfileActivity.this);
        final AlertDialog changePassDialog;

        Button updatePassBtn, cancelPassBtn;
        View changePassView;


        LayoutInflater inflater = getLayoutInflater();
        changePassView = inflater.inflate(R.layout.change_password_layout, null);
        builder1.setView(changePassView);


        changePassDialog = builder1.create();

        changePassDialog.setCanceledOnTouchOutside(false);

        oldPassEt = changePassView.findViewById(R.id.oldPassAlertEt);
        newPassEt = changePassView.findViewById(R.id.newPassAlertEt);
        conPassEt = changePassView.findViewById(R.id.conPassAlertEt);

        oldPassTgleBtn = changePassView.findViewById(R.id.oldPassToggleButton);
        newPassTgleBtn = changePassView.findViewById(R.id.newPassToggleButton);
        conPassTgleBtn = changePassView.findViewById(R.id.conPassToggleButton);

        updatePassBtn = changePassView.findViewById(R.id.changePassAlertUpdateBtn);
        cancelPassBtn = changePassView.findViewById(R.id.changePassAlertCancelBtn);

        showHidePassword();

        updatePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (allNotValidChangePassAlrt()) {
                    return;
                }

                oldPass = oldPassEt.getText().toString().trim();
                newPass = newPassEt.getText().toString().trim();
                conPass = conPassEt.getText().toString().trim();

                if (internetNotConnected(ProfileActivity.this)) {
                    changePassDialog.dismiss();
                    return;
                }

                changePassProcessDialog = new ProgressDialog(ProfileActivity.this);
                changePassProcessDialog.setMessage("Updating...");
                changePassProcessDialog.setCancelable(false);
                changePassProcessDialog.setIndeterminate(false);
                changePassProcessDialog.setCanceledOnTouchOutside(false);

                changePassProcessDialog.show();


                final FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();

                assert mAuth != null;
                String sUserEmail = mAuth.getEmail();

                assert sUserEmail != null;
                AuthCredential credential = EmailAuthProvider
                        .getCredential(sUserEmail, oldPass);


                mAuth.reauthenticate(credential).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        changePassProcessDialog.dismiss();
//                        changePassDialog.dismiss();
                    }
                })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mAuth.updatePassword(newPass).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ProfileActivity.this, "Password is Updated", Toast.LENGTH_SHORT).show();
                                        changePassProcessDialog.dismiss();
                                        changePassDialog.dismiss();
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                changePassProcessDialog.dismiss();
                                                changePassDialog.dismiss();
                                            }
                                        });
                            }
                        });

            }

            private boolean allNotValidChangePassAlrt() {

                Validations objVal = new Validations();
                if (objVal.isValidPass(oldPassEt) || objVal.isValidPass(newPassEt) ||
                        objVal.isValidConPass(conPassEt, newPassEt)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        cancelPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassDialog.dismiss();
            }
        });

        changePassDialog.show();

    }


    private void showHidePassword() {
        oldPassTgleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (oldPassEt.getText().length() > 0) {
                    if (isChecked) {
                        oldPassEt.setSelection(oldPassEt.length());
                        oldPassEt.setTransformationMethod(null);
                        oldPassTgleBtn.setBackgroundResource(R.drawable.visible_pass);
                    } else {
                        oldPassEt.setSelection(oldPassEt.length());
                        oldPassEt.setTransformationMethod(new PasswordTransformationMethod());
                        oldPassTgleBtn.setBackgroundResource(R.drawable.invisible_pass);
                    }
                    oldPassEt.setSelection(oldPassEt.length());
                }
            }
        });
        newPassTgleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (newPassEt.getText().length() > 0) {
                    if (isChecked) {
                        newPassEt.setSelection(newPassEt.length());
                        newPassEt.setTransformationMethod(null);
                        newPassTgleBtn.setBackgroundResource(R.drawable.visible_pass);
                    } else {
                        newPassEt.setSelection(newPassEt.length());
                        newPassEt.setTransformationMethod(new PasswordTransformationMethod());
                        newPassTgleBtn.setBackgroundResource(R.drawable.invisible_pass);
                    }
                    newPassEt.setSelection(newPassEt.length());
                }
            }
        });

        conPassTgleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (conPassEt.getText().length() > 0) {
                    if (isChecked) {
                        conPassEt.setSelection(conPassEt.length());
                        conPassEt.setTransformationMethod(null);
                        conPassTgleBtn.setBackgroundResource(R.drawable.visible_pass);
                    } else {
                        conPassEt.setSelection(conPassEt.length());
                        conPassEt.setTransformationMethod(new PasswordTransformationMethod());
                        conPassTgleBtn.setBackgroundResource(R.drawable.invisible_pass);
                    }
                    conPassEt.setSelection(conPassEt.length());
                }
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
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        alertTv = alertView.findViewById(R.id.alertTv);
        alertTv.setText("Sorry! No Internet Connection");
        builder.setView(alertView);
        final android.app.AlertDialog noInternetAlert = builder.create();
        noInternetAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetAlert.dismiss();

            }
        });
        noInternetAlert.show();
    }


    private void setUpProfileImageAlert() {
        builder = new AlertDialog.Builder(ProfileActivity.this);

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
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap;
                Bundle b = data.getExtras();
                bitmap = (Bitmap) b.get("data");
                photoUriNew = saveInGallary(getApplicationContext(), bitmap);


                Glide.with(getApplicationContext())
                        .load(photoUriNew)
                        .into(profileImageIV);


            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == Pick_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            photoUriNew = data.getData();
            Glide.with(getApplicationContext())
                    .load(photoUriNew)
                    .into(profileImageIV);
        }
    }

    private Uri saveInGallary(Context applicationContext, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(applicationContext.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onBackPressed() {
        if (categoryOld.equals("Kitchen")) {
            Intent intent = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
            intent.putExtra("Fragment", "A");
            startActivity(intent);
            Animatoo.animateSwipeLeft(ProfileActivity.this);
            finish();
        } else {
            Intent i = new Intent(getApplicationContext(), UserDashboardActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("Fragment", "A");
            i.putExtras(bundle);
            startActivity(i);
            Animatoo.animateSwipeLeft(ProfileActivity.this);
            finish();
        }

    }


    private void updateLocaion(double latitude, double longitude) {
        move1 = false;
        move2 = false;
        move3 = false;
        move4 = false;
        final String latKey = "latitude", latiInStr, longiInStr;
        String longKey = "longitude";
        latiInStr = String.valueOf(latitude);
        longiInStr = String.valueOf(longitude);

        locationDialog.dismiss();
        saveDialog = createProcessDialog("Updating Please Wait...");
        saveDialog.show();

        Map<String, Object> locatiomUpdate = new HashMap<String, Object>();
        locatiomUpdate.put(latKey, latiInStr);
        locatiomUpdate.put(longKey, longiInStr);

        FirebaseDatabase.getInstance().getReference().child(categoryOld).child(userIDOld).updateChildren(locatiomUpdate).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                saveDialog.dismiss();
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (categoryOld.equals("Kitchen")) {

                            final Query mProductRef = FirebaseDatabase.getInstance().getReference().child("Products")
                                    .orderByChild("kitchenID").equalTo(userIDOld);
                            mProductRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String latiKey = "latitudeP";
                                        String longiKey = "longitudeP";
                                        Map<String, Object> locUpdate = new HashMap<String, Object>();
                                        locUpdate.put(latiKey, latiInStr);
                                        locUpdate.put(longiKey, longiInStr);
                                        i = 0;
                                        child = dataSnapshot.getChildrenCount();
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String productID = ds.getValue(Product.class).id;
                                            FirebaseDatabase.getInstance().getReference().child("Products").child(productID).updateChildren(locUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    i++;
                                                    if (i == child) {
                                                        updateOrdKit();
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        updateOrdKit();
                                    }
                                }

                                private void updateOrdKit() {
                                    Query orderRef = FirebaseDatabase.getInstance().getReference().child("Orders")
                                            .orderByChild("kitchenID").equalTo(userIDOld);
                                    orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String latiKey3 = "productLatitude";
                                                String longiKey3 = "productLongitude";
                                                Map<String, Object> locUpdate3 = new HashMap<String, Object>();
                                                locUpdate3.put(latiKey3, latiInStr);
                                                locUpdate3.put(longiKey3, longiInStr);
                                                i = 0;
                                                child = dataSnapshot.getChildrenCount();
                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                    String orderKID = ds.getValue(Order.class).orderId;
                                                    FirebaseDatabase.getInstance().getReference().child("Orders").child(orderKID).updateChildren(locUpdate3).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            i++;
                                                            if (i == child) {
                                                                updateWorder();

                                                            }
                                                        }
                                                    });
                                                }

                                            } else {
                                                updateWorder();
                                            }
                                        }

                                        private void updateWorder() {
                                            Query orderRefW = FirebaseDatabase.getInstance().getReference().child("Weekly Offer Orders")
                                                    .orderByChild("kitchenID").equalTo(userIDOld);
                                            orderRefW.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        String latiKey4 = "productLatitude";
                                                        String longiKey4 = "productLongitude";
                                                        Map<String, Object> locUpdate4 = new HashMap<String, Object>();
                                                        locUpdate4.put(latiKey4, latiInStr);
                                                        locUpdate4.put(longiKey4, longiInStr);
                                                        i = 0;
                                                        child = dataSnapshot.getChildrenCount();
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            String orderIdW = ds.getValue(Order.class).orderId;
                                                            FirebaseDatabase.getInstance().getReference().child("Weekly Offer Orders").child(orderIdW).updateChildren(locUpdate4).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    i++;
                                                                    if (i == child) {
                                                                        updateWoffer();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    } else {
                                                        updateWoffer();
                                                    }
                                                }

                                                private void updateWoffer() {

                                                    Query query = FirebaseDatabase.getInstance().getReference()
                                                            .child("Weekly Offers").orderByChild("kitchenID").equalTo(userIDOld);
                                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                String latiKey1 = "latitudeKitchen";
                                                                String longiKey1 = "longitudeKitchen";
                                                                Map<String, Object> locUpdate1 = new HashMap<String, Object>();
                                                                locUpdate1.put(latiKey1, latiInStr);
                                                                locUpdate1.put(longiKey1, longiInStr);
                                                                i = 0;
                                                                child = dataSnapshot.getChildrenCount();
                                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                                    String productID = ds.getValue(WeeklyOffer.class).offerId;
                                                                    FirebaseDatabase.getInstance().getReference().child("Weekly Offers").child(productID).updateChildren(locUpdate1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            i++;
                                                                            if (i == child) {
                                                                                showSS();
                                                                            }
                                                                        }
                                                                    });


                                                                }
                                                            } else {
                                                                showSS();
                                                            }
                                                        }

                                                        private void showSS() {
                                                            sp = getSharedPreferences(mysp, MODE_PRIVATE);
                                                            SharedPreferences.Editor ed = sp.edit();
                                                            ed.putString("Lat", latiInStr);
                                                            ed.putString("Long", longiInStr);
                                                            ed.commit();
                                                            saveDialog.dismiss();
                                                            Toast.makeText(ProfileActivity.this, "Your Location is Updated Sucessfully", Toast.LENGTH_SHORT).show();

                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT);
                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT);
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT);
                                }
                            });


                        } else if (categoryOld.equals("Customer")) {

                            Query orderRef = FirebaseDatabase.getInstance().getReference().child("Orders")
                                    .orderByChild("userID").equalTo(userIDOld);
                            orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String latiKey = "userLatitude";
                                        String longiKey = "userLongitude";
                                        Map<String, Object> locUpdate2 = new HashMap<String, Object>();
                                        locUpdate2.put(latiKey, latiInStr);
                                        locUpdate2.put(longiKey, longiInStr);
                                        i = 0;
                                        child = dataSnapshot.getChildrenCount();
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                            String orderID = ds.getValue(Order.class).orderId;
                                            FirebaseDatabase.getInstance().getReference().child("Orders").child(orderID).updateChildren(locUpdate2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    i++;
                                                    if (i == child) {
                                                        updateWorderC();
                                                    }
                                                }
                                            });

                                        }
                                    } else {
                                        updateWorderC();
                                    }
                                }

                                private void updateWorderC() {

                                    Query query1 = FirebaseDatabase.getInstance().getReference()
                                            .child("Weekly Offer Orders").orderByChild("userID").equalTo(userIDOld);
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String latiKey2 = "userLatitude";
                                                String longiKey2 = "userLongitude";
                                                Map<String, Object> locUpdate2 = new HashMap<String, Object>();
                                                locUpdate2.put(latiKey2, latiInStr);
                                                locUpdate2.put(longiKey2, longiInStr);
                                                i = 0;
                                                child = dataSnapshot.getChildrenCount();
                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                    String ordID = ds.getValue(Order.class).orderId;
                                                    FirebaseDatabase.getInstance().getReference().child("Weekly Offer Orders").child(ordID).updateChildren(locUpdate2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            i++;
                                                            if (i == child) {
                                                                showSS();
                                                            }
                                                        }
                                                    });
                                                }
                                            } else {
                                                showSS();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }

                                private void showSS() {

                                    sp = getSharedPreferences(mysp, MODE_PRIVATE);
                                    SharedPreferences.Editor ed = sp.edit();
                                    ed.putString("Lat", latiInStr);
                                    ed.putString("Long", longiInStr);
                                    ed.commit();
                                    saveDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Your Location is Updated Sucessfully", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                        }
                    }
                });

    }

}
//31.485133, 74.324835
//31.488015, 74.323204
//31.494296, 74.315482
//31.504650, 74.287824
//31.519980, 74.291746

//        31.518169, 74.324512



