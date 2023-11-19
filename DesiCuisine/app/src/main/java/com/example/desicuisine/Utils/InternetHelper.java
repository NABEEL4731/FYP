package com.example.desicuisine.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class InternetHelper {

    public InternetHelper() {
    }

    public boolean isInternetConnected(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE) {

            return true;
        } else {
            return false;
        }

    }
}

