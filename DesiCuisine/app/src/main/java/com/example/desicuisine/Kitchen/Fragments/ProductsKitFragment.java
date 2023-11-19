package com.example.desicuisine.Kitchen.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.example.desicuisine.Kitchen.Activities.ViewProductsKActivity;
import com.example.desicuisine.Kitchen.Activities.ViewUserActivity;
import com.example.desicuisine.Models.Product;
import com.example.desicuisine.R;
import com.example.desicuisine.Utils.InternetHelper;
import com.example.desicuisine.Utils.Validations;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductsKitFragment extends Fragment implements View.OnClickListener {
    private CardView addProductCV, viewProductCv, viewAvailableCV, viewCustomerCv;
    private View view;

    private SharedPreferences sp;
    private String latiInStr, longiINStr;
    private String mySp = "MYSP";
    private String userID, managerFName, profileImageUrl;
    private String managerLastName, productImageUrl, kitchenEmail;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private DatabaseReference mProductRef;
    private StorageReference mProductPhotoRef;
    private UploadTask uploadTask;
    private String pName, pPrice, category, quantity;
    private String[] categoryArray;
    private Spinner categorySpn;

    private AlertDialog.Builder builderCamera;

    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE1 = 100;
    private final static int Pick_IMAGE_REQUEST_CODE2 = 200;
    private Uri photoUri = null;
    private ImageView profileImageIV = null;
    private ImageView pickProfileImageIV = null;

    private EditText pNameEt, pPriceEt, pQuantityEt;
    private Boolean found;


    public ProductsKitFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_products_kit, container, false);

        connectivity();

        addProductCV.setOnClickListener(this);
        viewProductCv.setOnClickListener(this);
        viewAvailableCV.setOnClickListener(this);
        viewCustomerCv.setOnClickListener(this);

        return view;
    }


    private void connectivity() {

        getActivity().setTitle("Products");
        addProductCV = view.findViewById(R.id.addProductCV);
        viewProductCv = view.findViewById(R.id.viewProductCv);
        viewAvailableCV = view.findViewById(R.id.viewAvailableCV);
        viewCustomerCv = view.findViewById(R.id.viewCustomerCv);
        sp = getActivity().getSharedPreferences(mySp, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        category = "Select Product Category";
        photoUri = null;
        categoryArray = new String[]{"Select Product Category", "Gosht", "Chawal", "Daal",
                "Sabzi", "Traditional", "Seasonal", "Chinees", "Other"};


        userID = mAuth.getCurrentUser().getUid();
        managerFName = sp.getString("FName", null);
        managerLastName = sp.getString("LName", null);
        kitchenEmail = sp.getString("Email", null);
        profileImageUrl = sp.getString("ProfileUrl", null);

        sp = getActivity().getSharedPreferences("MYSP", MODE_PRIVATE);
        latiInStr = sp.getString("Lat", null);
        longiINStr = sp.getString("Long", null);


    }

    @Override
    public void onClick(View v) {
        Bundle bundle;
        Intent intent;
        int id = v.getId();
        switch (id) {
            case R.id.addProductCV:
                addNewProduct();
                break;
            case R.id.viewProductCv:
                bundle = new Bundle();
                bundle.putString("CHOISE", "All Products");
                intent = new Intent(getActivity(), ViewProductsKActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateCard(getActivity());
                getActivity().finish();

                break;
            case R.id.viewAvailableCV:
                bundle = new Bundle();
                bundle.putString("CHOISE", "Available Products");
                intent = new Intent(getActivity(), ViewProductsKActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                Animatoo.animateCard(getActivity());
                getActivity().finish();

                break;
            case R.id.viewCustomerCv:
                Intent intent2 = new Intent(getActivity(), ViewUserActivity.class);
                intent2.putExtra("Fragment", "B");
                startActivity(intent2);
                Animatoo.animateCard(getActivity());
                getActivity().finish();
                break;
        }
    }

    private void addNewProduct() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        Button cencelBtn, addBtn;

        View alertView = getLayoutInflater().inflate(R.layout.add_product_layout, null);
        cencelBtn = alertView.findViewById(R.id.cencleButtonADD);
        addBtn = alertView.findViewById(R.id.addButtonADD);

        pNameEt = alertView.findViewById(R.id.etPNameADD);
        pPriceEt = alertView.findViewById(R.id.etPriceADD);
        pQuantityEt = alertView.findViewById(R.id.etQuantityADD);
        profileImageIV = alertView.findViewById(R.id.showProdImgADDIV);
        pickProfileImageIV = alertView.findViewById(R.id.pickProductPhotoADDIv);

        categorySpn = alertView.findViewById(R.id.categorySpinnerADD);
        setUpDomailSpinner();
        builder.setView(alertView);


        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);


        pickProfileImageIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpProductImageAlert();
            }
        });
        cencelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pName = pNameEt.getText().toString().trim();
                pPrice = pPriceEt.getText().toString().trim();
                quantity = pQuantityEt.getText().toString().trim();

                found = false;
                if (allNotValid()) {
                    return;
                }
                if (internetNotConnected(getContext())) {
                    return;
                }

                createProsessDialog();
                progressDialog.show();

                mProductRef = FirebaseDatabase.getInstance().getReference().child("Products");
                mProductPhotoRef = FirebaseStorage.getInstance().getReference().child("Product Pictures").child(userID);
                final String productID = mProductRef.push().getKey();

                uploadTask = mProductPhotoRef.child(productID).putFile(photoUri);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return mProductPhotoRef.child(productID).getDownloadUrl();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        productImageUrl = uri.toString();


                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy  hh:mm a");
                        String currentDate = sdf.format(new Date());

                        Product obj = new Product(productID, pName, pPrice, quantity, category, "Available",
                                managerFName + " " + managerLastName, kitchenEmail, userID,
                                currentDate, productImageUrl, latiInStr, longiINStr);

                        mProductRef.child(productID).setValue(obj).addOnFailureListener(new OnFailureListener() {
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
                                        photoUri = null;
                                        showSucessAlert("Product Is Added Sucessfully");
                                    }
                                });
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        Button okBtn;
        TextView alertTv;
        View alertView = getLayoutInflater().inflate(R.layout.informational_alert_layout_access, null);
        okBtn = alertView.findViewById(R.id.noInternetAlertBtn);
        alertTv = alertView.findViewById(R.id.alertTv);
//        alertTv.setText("Your feedback has been sent sucessfully");
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
        if (objVal.isValidString(pNameEt) || objVal.isValidAmount(pPriceEt) || objVal.isValidAmount(pQuantityEt) || isProductCategorySelected() ||
                isProductImageChoosen()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isProductCategorySelected() {
        if (category.equals("Select Product Category")) {
            Toast.makeText(getContext(), "Please Select Product Category", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private boolean isProductImageChoosen() {
        if (photoUri == null) {
            Toast.makeText(getContext(), "Please Choose Your Product Photo", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    private void setUpProductImageAlert() {
        builderCamera = new AlertDialog.Builder(getContext());

        builderCamera.setMessage("Complete This Action Using");
        builderCamera.setTitle("Please Confirm");
        builderCamera.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
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
        AlertDialog dialog1 = builderCamera.create();
        dialog1.setCanceledOnTouchOutside(false);
        dialog1.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        dialog1.show();

    }

    private void setUpDomailSpinner() {
//        android.R.layout.simple_spinner_dropdown_item
        ArrayAdapter spnAdapterDomain = new ArrayAdapter(getContext(), R.layout.white_spinner_dropdown_item,
                categoryArray);
        categorySpn.setAdapter(spnAdapterDomain);
        categorySpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                category = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void startTakePictureIntent() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE1);

    }

    private void startGallaryPicRequest() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i.createChooser(i, "Select an Image"), Pick_IMAGE_REQUEST_CODE2);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE1) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap;
                Bundle b = data.getExtras();
                bitmap = (Bitmap) b.get("data");
                photoUri = saveInGallary(getContext(), bitmap);


                Glide.with(getContext())
                        .load(photoUri)
                        .into(profileImageIV);


            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == Pick_IMAGE_REQUEST_CODE2 && resultCode == RESULT_OK && data != null) {

            photoUri = data.getData();
            Glide.with(getContext())
                    .load(photoUri)
                    .into(profileImageIV);
        }
    }

    private Uri saveInGallary(Context applicationContext, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(applicationContext.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }

}
