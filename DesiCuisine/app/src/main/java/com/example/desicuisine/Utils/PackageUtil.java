package com.example.desicuisine.Utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class PackageUtil {

    public static boolean checkPermission(Context context, String accessFineLocation) {

        int res = context.checkCallingOrSelfPermission(accessFineLocation);
        return (res == PackageManager.PERMISSION_GRANTED);

    }

}