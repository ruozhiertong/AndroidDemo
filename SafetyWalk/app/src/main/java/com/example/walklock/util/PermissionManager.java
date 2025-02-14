package com.example.walklock.util;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {



    public static List<String> needPermissions(Context ctx , String[] permissions){
        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(ctx, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }
        return permissionsNeeded;
    }


    public static void requestPermission(Activity activity, String permission, int requestCode){
        //申请权限。
        requestPermissions(activity, new String[] {permission}, requestCode);
    }



    public static void requestPermissions(Activity activity, String[] permissions, int requestCode){
        //申请权限。
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }


    public static boolean checkPlayServices(Context ctx) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

        int resultCode = apiAvailability.isGooglePlayServicesAvailable(ctx);
        Log.d("checkPlayServices", "" + resultCode);
//        return resultCode == ConnectionResult.SUCCESS;
        if (resultCode != ConnectionResult.SUCCESS) {
            // Google Play Services 不可用
            if (apiAvailability.isUserResolvableError(resultCode)) {
                // 显示对话框提示用户解决问题
                apiAvailability.getErrorDialog((Activity) ctx, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                // 设备不支持 Google Play Services
                Log.e("GooglePlayServices", "设备不支持 Google Play Services");
            }
            return false;
        }
        return true;
    }

    public static boolean isGooglePlayServicesAvailable(Context ctx) {
        try {
            ctx.getPackageManager().getPackageInfo("com.google.android.gms", 0);
            Log.d("isGooglePlayServicesAvailable", "true" );
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("isGooglePlayServicesAvailable", "false" );
            return false;
        }
    }

    public static void requestOverlayPermission(Activity activity, int requestCode) {
        // 检查 SYSTEM_ALERT_WINDOW 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
            // 申请悬浮窗权限
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            activity.startActivityForResult(intent, requestCode);
        }
    }




}
