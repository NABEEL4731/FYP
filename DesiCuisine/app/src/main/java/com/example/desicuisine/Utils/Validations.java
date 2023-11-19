package com.example.desicuisine.Utils;

import android.util.Patterns;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validations {

    public boolean isValidString(EditText editText) {
        if (editText.getText().toString().isEmpty()) {
            editText.setError("This Field Cannot Be Empty");
            editText.requestFocus();
            return true;
        } else {
            return false;

        }
    }

    public boolean isValidAmount(EditText editText) {
        String amtS = editText.getText().toString().trim();
        if (editText.getText().toString().isEmpty()) {
            editText.setError("This Field Cannot Be Empty");
            editText.requestFocus();
            return true;
        }
        if (amtS.equals("0")) {
            editText.setError("This Field Is Not Valid");
            editText.requestFocus();
            return true;
        } else {
            return false;

        }
    }

    public boolean isValidName(EditText editText) {
        if (editText.getText().toString().trim().isEmpty()) {
            editText.setError("This field is Empty");
            editText.requestFocus();
            return true;
        } else if (!isValidNM(editText.getText().toString().trim())) {
            editText.setError("Wrong Name Pattern");
            editText.requestFocus();
            return true;
        } else {
            return false;
        }
    }

    private boolean isValidNM(String p) {
        final String name = "^[a-zA-Z ]{3,30}$";
        Pattern pattern = Pattern.compile(name);
        Matcher matcher = pattern.matcher(p);
        return matcher.matches();

    }


    public boolean isValidEmailId(EditText editText) {
        if (editText.getText().toString().trim().isEmpty()) {
            editText.setError("Please Provide Your Email Address");
            editText.requestFocus();
            return true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString().trim()).matches()) {
            editText.setError("Wrong Email Pattern");
            editText.requestFocus();
            return true;
        } else {
            return false;

        }
    }

    public boolean isValidPhone(EditText editText) {
        if (editText.getText().toString().trim().isEmpty()) {
            editText.setError("Please Provide Your Contact Number");
            editText.requestFocus();
            return true;
        } else if (!Patterns.PHONE.matcher(editText.getText().toString().trim()).matches()) {
            editText.setError("Wrong Phone Pattern");
            editText.requestFocus();
            return true;
        } else if (editText.getText().toString().trim().length() < 12) {
            editText.setError("Wrong Phone Number");
            editText.requestFocus();
            return true;
        } else {
            return false;

        }
    }


    public boolean isValidPass(EditText editText) {
        if (editText.getText().toString().trim().isEmpty()) {
            editText.setError("Please Provide Your Password", null);
            editText.requestFocus();
            return true;
        } else if (editText.getText().length() < 6) {
            editText.setError("Password should be greater then 6 character", null);
            editText.requestFocus();
            return true;
        } else {
            return false;

        }
    }

    public boolean isValidConPass(EditText conPassEt, EditText passEt) {
        String pass = passEt.getText().toString().trim();
        String conPass = conPassEt.getText().toString().trim();

        if (conPassEt.getText().toString().trim().isEmpty()) {
            conPassEt.setError("Please Provide Your Password", null);
            conPassEt.requestFocus();
            return true;
        } else if (conPassEt.getText().length() < 6) {
            conPassEt.setError("Password should be greater then 6 character", null);
            conPassEt.requestFocus();
            return true;
        } else if (!conPass.equals(pass)) {
            conPassEt.setError("Password Not Matched", null);
            conPassEt.requestFocus();
            return true;
        } else {
            return false;

        }
    }

}
