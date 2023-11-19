package com.example.desicuisine.Kitchen.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.desicuisine.Models.Product;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Activities.UserDashboardActivity;
import com.example.desicuisine.Utils.InternetHelper;
import com.example.desicuisine.Utils.Validations;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProductDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressDialog progressDialog;

    private EditText productNameEt, productPriceEt, quantityEt;
    private ImageView profilePhotoIV, editBtn;
    private ImageView captureIv;


    Button updateBtn;
    TextView categoryTv;

    private String
            id,
            productName,
            productPrice,
            productQuantity,
            productCategory,
            status,
            cookName,
            kichenEmail,
            kitchenID,
            date,
            photoUrl;
    private String latitude,
            longitude;

    private AlertDialog.Builder builder;
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private final static int Pick_IMAGE_REQUEST_CODE = 200;
    private Uri photoUriNew;

    private DatabaseReference mProductRef;
    private StorageReference mProductPhotoRef;
    private UploadTask uploadTask;
    private String productImageUrlNew;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        connectivity();

        captureIv.setOnClickListener(this);
        updateBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);
        final Uri picUriOld = Uri.parse(photoUrl);

        Glide.with(ProductDetailsActivity.this)
                .load(picUriOld)
                .apply(new RequestOptions().placeholder(R.drawable.loading_image))
                .into(profilePhotoIV);

        productNameEt.setText(productName);
        productNameEt.setSelection(productNameEt.getText().toString().length());
        categoryTv.setText(productCategory);
        quantityEt.setText(productQuantity);
        quantityEt.setSelection(quantityEt.getText().toString().length());
        productPriceEt.setText(productPrice);
        productPriceEt.setSelection(productPriceEt.getText().toString().length());

    }

    private void connectivity() {
        photoUriNew = null;
        setTitle("Update Product");
        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        productName = bundle.getString("productName");
        productPrice = bundle.getString("productPrice");
        productQuantity = bundle.getString("productQuantity");
        productCategory = bundle.getString("productCategory");
        status = bundle.getString("status");
        cookName = bundle.getString("cookName");
        kichenEmail = bundle.getString("kichenEmail");
        kitchenID = bundle.getString("kitchenID");
        date = bundle.getString("date");
        photoUrl = bundle.getString("photoUrl");
        latitude = bundle.getString("latitude");
        longitude = bundle.getString("longitude");

        profilePhotoIV = findViewById(R.id.productPhotoEditAlertIv);
        captureIv = findViewById(R.id.pickProductPhotoEditAlertBtnIv);
        categoryTv = findViewById(R.id.categoryEditProductAlertTv);
        productNameEt = findViewById(R.id.etPNameEdit);
        productPriceEt = findViewById(R.id.etPriceEdit);
        quantityEt = findViewById(R.id.etQuantityEdit);
        updateBtn = findViewById(R.id.updateAlertBtnEdit);
        updateBtn.setEnabled(false);
        captureIv.setEnabled(false);
        editBtn = findViewById(R.id.editBtnP);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.pickProductPhotoEditAlertBtnIv:
                setUpProfileImageAlert();
                break;

            case R.id.updateAlertBtnEdit:
                updateMethod();
                break;

            case R.id.editBtnP:
                makeEditableMethod();
                break;

        }
    }

    private void makeEditableMethod() {
        captureIv.setEnabled(true);
        updateBtn.setEnabled(true);

        productNameEt.setFocusable(true);
        productNameEt.setClickable(true);
        productNameEt.setCursorVisible(true);
        productNameEt.setSelection(productNameEt.getText().toString().length());
        productNameEt.setFocusableInTouchMode(true);
        productNameEt.requestFocus();

        productPriceEt.setFocusable(true);
        productPriceEt.setClickable(true);
        productPriceEt.setCursorVisible(true);
        productPriceEt.setSelection(productPriceEt.getText().toString().length());
        productPriceEt.setFocusableInTouchMode(true);

        quantityEt.setFocusable(true);
        quantityEt.setClickable(true);
        quantityEt.setCursorVisible(true);
        quantityEt.setSelection(quantityEt.getText().toString().length());
        quantityEt.setFocusableInTouchMode(true);

        Toast.makeText(this, "You can update Product Now", Toast.LENGTH_SHORT).show();

    }

    private void updateMethod() {
        productName = productNameEt.getText().toString().trim();
        productPrice = productPriceEt.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        if (allNotValid()) {
            return;
        }

        createProgressDialog();
        progressDialog.show();

        if (internetNotConnected(ProductDetailsActivity.this)) {
            return;
        }


        mProductRef = FirebaseDatabase.getInstance().getReference().child("Products");
        mProductPhotoRef = FirebaseStorage.getInstance().getReference().child("Product Pictures").child(kitchenID);

        if (photoUriNew == null) {

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
            String currentDate = sdf.format(new Date());

            Product obj = new Product(id, productName, productPrice, productQuantity, productCategory, "Available",
                    cookName, kichenEmail, kitchenID, currentDate, photoUrl, latitude, longitude);

            mProductRef.child(id).setValue(obj).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(ProductDetailsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                            intent.putExtra("Fragment", "B");
                            startActivity(intent);
                            Animatoo.animateCard(ProductDetailsActivity.this);
                            finish();
                        }
                    });

        } else {

            uploadTask = mProductPhotoRef.child(id).putFile(photoUriNew);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return mProductPhotoRef.child(id).getDownloadUrl();
                }
            }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    productImageUrlNew = uri.toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
                    String currentDate = sdf.format(new Date());

                    Product obj = new Product(id, productName, productPrice, productQuantity, productCategory, "Available",
                            cookName, kichenEmail, kitchenID, currentDate, productImageUrlNew, latitude, longitude);

                    mProductRef.child(id).setValue(obj).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ProductDetailsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
                                    intent.putExtra("Fragment", "B");
                                    startActivity(intent);
                                    Animatoo.animateCard(ProductDetailsActivity.this);
                                    finish();
                                }
                            });

                }
            });
        }


    }

    private void setUpProfileImageAlert() {
        builder = new AlertDialog.Builder(ProductDetailsActivity.this);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap;
                Bundle b = data.getExtras();
                bitmap = (Bitmap) b.get("data");
                photoUriNew = saveInGallary(getApplicationContext(), bitmap);


                Glide.with(getApplicationContext())
                        .load(photoUriNew)
                        .into(profilePhotoIV);


            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == Pick_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            photoUriNew = data.getData();
            Glide.with(getApplicationContext())
                    .load(photoUriNew)
                    .into(profilePhotoIV);
        }
    }

    private Uri saveInGallary(Context applicationContext, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(applicationContext.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
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

    private boolean allNotValid() {
        Validations objVal = new Validations();
        if (objVal.isValidString(productNameEt) || objVal.isValidAmount(productPriceEt) ||
                objVal.isValidAmount(quantityEt)) {
            return true;
        } else {
            return false;
        }
    }


    private void createProgressDialog() {
        progressDialog = new ProgressDialog(ProductDetailsActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Please Wait....");
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

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), KitchenDashboardActivity.class);
        i.putExtra("Fragment", "B");
        startActivity(i);
        Animatoo.animateSwipeLeft(ProductDetailsActivity.this);
        finish();

    }

    private void showNoInternetMsg(final Context mContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        Button okBtn;
        TextView alertTv;
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        alertTv = alertView.findViewById(R.id.alertTv);
        alertTv.setText("Sory! No Internet Connection");
        builder.setView(alertView);
        builder.setCancelable(false);

        final android.app.AlertDialog noInternetAlert = builder.create();
        noInternetAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetAlert.dismiss();

                Intent i = new Intent(mContext, UserDashboardActivity.class);
                i.putExtra("Fragment", "A");
                startActivity(i);
                Animatoo.animateSwipeLeft(mContext);
                finish();
            }
        });
        noInternetAlert.show();
    }


}