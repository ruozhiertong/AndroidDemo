package com.example.safetywalk2.util;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safetywalk2.ui.MainActivity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;


public class PermissionManager {


    public static final String TAG = "PermissionManager";



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


//    public static boolean checkPlayServices(Context ctx) {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
//
//        int resultCode = apiAvailability.isGooglePlayServicesAvailable(ctx);
//        Log.d("checkPlayServices", "" + resultCode);
////        return resultCode == ConnectionResult.SUCCESS;
//        if (resultCode != ConnectionResult.SUCCESS) {
//            // Google Play Services 不可用
//            if (apiAvailability.isUserResolvableError(resultCode)) {
//                // 显示对话框提示用户解决问题
//                apiAvailability.getErrorDialog((Activity) ctx, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
//            } else {
//                // 设备不支持 Google Play Services
//                Log.e("GooglePlayServices", "设备不支持 Google Play Services");
//            }
//            return false;
//        }
//        return true;
//    }

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



    //申请 使用情况访问权限
    public static void checkUsageStatsPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usageStatsManager == null) return;

            long currentTime = System.currentTimeMillis();
            List<UsageStats> stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, currentTime - 1000, currentTime);

            if (stats == null || stats.isEmpty()) {
                new AlertDialog.Builder(context)
                        .setTitle("需要使用情况访问权限")
                        .setMessage("请在设置中允许访问使用情况，以便应用能在导航时暂停检测")
                        .setPositiveButton("去设置", (dialog, which) -> {
                            context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        }
    }

    //判断是否有悬浮窗权限
    public static boolean hasOverlayPermission(Context context) {
        return Settings.canDrawOverlays(context);
    }

    //申请悬浮窗权限。
    public static void requestOverlayPermission(Activity activity, int requestCode) {
        // 检查 SYSTEM_ALERT_WINDOW 权限
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
//            // 申请悬浮窗权限
//            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//            activity.startActivityForResult(intent, requestCode);
//        }
        //如果需要回调处理，用上面的。

        //原生系统
//        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

        //MIUI系统
        Intent intent = getMiuiPermissionIntent(activity, Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        requestMIUIPermission(activity,"悬浮窗权限",  intent);
    }


    //后台打开
    public static boolean isBackgroundStartAllowed(Context context) {
        return isMIUIPermissionGranted(context, 10021);
    }

    public static void requestBackgroundStartPerssion(Context context) {
        Intent intent = getMiuiPermissionIntent(context, "android.permission.BACKGROUND_START_ACTIVITY");
        requestMIUIPermission(context, "后台弹出界面权限", intent);
    }



    //锁屏显示
    public static boolean isShowLockViewGranted(Context context) {
        return isMIUIPermissionGranted(context, 10020);
    }

    public static void requestShowLockViewPermission(Context context) {
        // 申请锁屏显示 权限。
//        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        intent.setData(Uri.fromParts("package", context.getPackageName(), null));

        Intent intent = getMiuiPermissionIntent(context, "miui.permission.LOCK_SCREEN_DISPLAY");
        requestMIUIPermission(context, "锁屏显示权限", intent);
    }



    // 自启动
    public static boolean isAutoStartPermissionGranted(Context context) {
        return isMIUIPermissionGranted(context, 10008);
    }

    //自启动
    public static void requestAutoStartPermission(Activity activity) {
//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        Intent intent = getMiuiIntent(activity, "android.permission.AUTO_START");

        //去详情页
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        requestMIUIPermission(activity,"自启动",  intent);

    }



    public static void requestBatteryNolimit(Activity activity) {
        //去详情页
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        requestMIUIPermission(activity,"省电策略无限制",  intent);
    }


    private static boolean isMIUIPermissionGranted(Context context , int opCode) {
        try {
            AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int op = opCode; //10007; // MIUI自启动权限的OP代码
            Method method = ops.getClass().getMethod("checkOpNoThrow", int.class, int.class, String.class);
            int result = (int) method.invoke(ops, op, Process.myUid(), context.getPackageName());
            Log.d("isMIUIPermissionGranted", "isMIUIPermissionGranted: " + result);
            return result == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            Log.e("PermissionCheck", "Failed to check auto-start permission", e);
        }
        return false;
    }


    private static void requestMIUIPermission(Context context, String per, Intent intent) {
        new AlertDialog.Builder(context)
                .setTitle(per + "未开启")
                .setMessage("请开启" + per)
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            // 检查 Intent 是否可用
                            PackageManager pm = context.getPackageManager();
                            if (intent.resolveActivity(pm) != null) {
                                context.startActivity(intent);
                            } else {
                                // 如果 MIUI 权限页面不可用，跳转到通用的应用详情页面
                                Log.d(TAG, "onClick: not use");
                                openAppDetailSetting(context);
                            }
                        }catch (Exception e){
                            Log.d(TAG, "onClick: Exception");
                            // 如果跳转失败，跳转到通用的应用详情页面
                            openAppDetailSetting(context);

                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }


    private static Intent getMiuiPermissionIntent(Context context, String permissionName) {

        // 创建 Intent
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setClassName("com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity"); // 注意 Activity 名称 PermissionsEditorActivity, AppPermissionsEditorActivity
//        com.miui.permcenter.permissions.PermissionsEditorActivity
//        com.miui.permcenter.settings.OtherPermissionsActivity
        intent.putExtra("extra_pkgname", context.getPackageName());
        intent.putExtra("extra_permission_name", permissionName); // 传递权限名称. 好像不起作用了

        return intent;
    }

    /**
     * 跳转到应用详情页面
     */
    private static void openAppDetailSetting(Context context) {
        Log.d(TAG, "openAppDetailSetting: ");
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(intent);
    }


}
