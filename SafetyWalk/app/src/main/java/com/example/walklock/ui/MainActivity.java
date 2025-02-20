package com.example.walklock.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.safetywalk.R;
import com.example.walklock.service.WalkDetectionService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.walklock.util.PermissionManager;
import com.example.walklock.util.SystemUtil;
import com.example.walklock.util.LogManager;


/**
 *
 * 功能：
 *     - 检测运动状态，是否在走路等。 传感器检测。
 *          - 如果在走路，而且在看手机，检测到后，会进入锁屏。
 *          - 锁住屏幕下，要么等待时间，要么计算问题。
 *     - 开屏检测，锁屏关闭检测。
 *     - 如果在锁住屏幕下，关机重启，会记住锁住屏幕状态，重启后依然会进入锁住屏幕状态。开机会延迟2分钟进入service。
 *     - 增加了日志保存和查看。
 *     - 增加了移除桌面图标的功能。（减少通过应用图标进入应用信息页进行强制停止， 以及 进行卸载）
 *     - 增加了小组件的功能（在删除桌面图标后可以进入应用）。
 *     - todo： 增加配置应用白名单功能。在白名单的应用在使用过程，不会进行运动检测。
 *     - todo:  增加，在锁屏屏幕下，可以打开某些应用的功能。
 *     - todo:  做成配置形式，可调整检测的阈值。
 *
 * 技术点：
 *      全局锁住屏幕的实现。禁止其他操作。
 *      走路的检测。
 *      应用的保活 以及 应用的重启。
 *      service，广播，intent， pendingIntent。
 *
 *
 * 需要在手机手动开启的权限：
 *      运动与健康
 *      悬浮窗
 *      后台弹出界面
 *      锁屏显示。这样在锁屏状态下也能弹出锁住界面。
 *              熄屏状态应该不检测，避免在熄屏时进入锁住界面。导致需要开屏使用手机时无法使用。
 *              两种方式：1.简单方式，不设置锁屏显示(这样在熄屏下不会进入锁住界面。但是可能会有问题，在开屏的时候可能过一会就进入锁住界面)
 *                      2.监听屏幕的亮和熄。 亮的时候开始检测，熄的时候停止服务。
 *      自启动。 （service杀死后可以自启动。开机时也可以自启动）。 主要是Service 重启时会受到限制。
 *              自启动影响Service重启。 START_STICK, onTaskRemoved 等。
 *      省电策略。设置后台无限制。（避免限制传感器停止检测）（如果传感器有变动，一般会10s检测一次）。
 *          主要是传感器会收到限制，会导致传感器不是即时，而是堆积到打开应用后一下子调用。
 *          也会对定时器有一些影响，导致无法按时发出。 省电策略影响的是传感器，定时器alarm等，不会影响广播接收器等。
 *          所以，省电策略改成无限制，不要智能限制。
 *
 *      闹钟权限。
 *      应用使用情况权限。
 *
 *  不要强制停止应用。会导致应用完全不能工作。
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private int detectWalkMode = 0; // 0 Activity Recognition API; 1 sensor
    //1.如果支持GMS，直接申请Activity Recognition API全新啊。
    //2.如果不支持GMS，使用传感器。


    //FOREGROUND_SERVICE是普通权限，直接在Manifest中声明就好，不用动态申请。
    private static final String[] REQUIRED_PERMISSIONS = {
        android.Manifest.permission.ACTIVITY_RECOGNITION
//            Manifest.permission.READ_CONTACTS,
//            Manifest.permission.READ_CALENDAR
//        android.Manifest.permission.FOREGROUND_SERVICE,
//            Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE

    };

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int OVERLAY_REQUEST_CODE = 110;
    private static final int AUTO_BOOT_CODE = 100;



    //提示，为了用户更友好。
    private Map<String, String> desMap = Map.of(
            android.Manifest.permission.ACTIVITY_RECOGNITION,"需要身体运动权限，为了检测运动状态",
            android.Manifest.permission.READ_CONTACTS,"需要日历读取权限，为了XXX",
            android.Manifest.permission.READ_CALENDAR, "需要联系人权限，为了XXX"
    );

    private Switch serviceSwitch;

    private Button btnViewlog;

    private Button btnHideDesktop;


    private Button btnTestAlarm;


    private Switch stwLog;


//    private Intent serviceIntent;

//    private WalkDetectionService myService;
//    private boolean isBound = false;
//
//    private ServiceConnection connection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            WalkDetectionService.LocalBinder binder = (WalkDetectionService.LocalBinder) service;
//            myService = binder.getService();
//            isBound = true;
//            myService.setHandleWalkListener(MainActivity.this);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            isBound = false;
//        }
//    };


    private void checkAlarmPer() {
        // 检查是否具有 SCHEDULE_EXACT_ALARM 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!((AlarmManager) getSystemService(Context.ALARM_SERVICE)).canScheduleExactAlarms()) {
                // 权限未开启，提示用户开启
                Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(settingsIntent);
            }
        }
    }

    private void processPermission() {

        if (!areMiuiNotificationsEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("通知权限未开启")
                    .setMessage("请开启通知权限，以确保正常接收服务通知")
                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openNotificationSettings();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }

        // 检查 Google Play Services 是否可用
        if (!PermissionManager.checkPlayServices(this) && !PermissionManager.isGooglePlayServicesAvailable(this)) {
            Toast.makeText(this, "不支持 Google Play Services", Toast.LENGTH_LONG).show();
            Log.d(TAG, "checkAndRequestPermissions: 不支持 Google Play Services");
//                finish();
//                return;
            detectWalkMode = 0;
        } else {
            detectWalkMode = 1;
        }

//        if (isMiui()) {
//            checkMiuiPermissions();
//        }

        PermissionManager.requestOverlayPermission(this, OVERLAY_REQUEST_CODE);


        //申请权限
        List<String> needPermissions = PermissionManager.needPermissions(this, REQUIRED_PERMISSIONS);
        if (!needPermissions.isEmpty()) {
            PermissionManager.requestPermissions(this,  needPermissions.toArray(new String[0]) , PERMISSION_REQUEST_CODE);
        }

        checkUsageStatsPermission();


        // 检查自启动权限
        checkAutoStartPermission();


        checkAlarmPer();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        processPermission();
        //不管权限给不给，app应该还能用。只是在真的使用权限时再提示其需要权限。
        initializeApp();
    }




//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (isBound) {
//            unbindService(connection);
//            isBound = false;
//        }
//    }


    public void startService() {

        if (SystemUtil.isServiceRunning(this, WalkDetectionService.class))
            return;

        // 启动服务
        Intent serviceIntent = new Intent(this, WalkDetectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

//        Intent guardServiceIntent = new Intent(this, GuardService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(guardServiceIntent);
//        } else {
//            startService(guardServiceIntent);
//        }


        // 可以关闭 MainActivity
//        finish();
    }

    public void stopService() {
        //如果服务没有启动，关闭时会异常。
        boolean isServiceRunning = SystemUtil.isServiceRunning(this, WalkDetectionService.class);
        if (!isServiceRunning)
            return;

        // //关闭服务
        // if (isBound) {
        //     unbindService(connection);
        //     isBound = false;
        // }
        // stopService(serviceIntent);

        // 发送停止服务的 Intent
        Intent stopIntent = new Intent(this, WalkDetectionService.class);
        stopIntent.setAction(WalkDetectionService.ACTION_STOP_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent);
        } else {
            startService(stopIntent);
        }

//        Intent stopGuardIntent = new Intent(this, GuardService.class);
//        stopIntent.setAction(WalkDetectionService.ACTION_STOP_SERVICE);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(stopIntent);
//        } else {
//            startService(stopIntent);
//        }

    }


    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        List<String> needPermissions = new ArrayList<String>();
        if (requestCode == PERMISSION_REQUEST_CODE) {
//            boolean allGranted = true;
            for (int i = 0 ; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                    allGranted = false;
                    needPermissions.add(permissions[i]);
                }
            }

            if (needPermissions.isEmpty()){
                Toast.makeText(this, "全部权限已申请",
                        Toast.LENGTH_LONG).show();
//                initializeApp();
                // 还要申请悬浮窗权限
//                PermissionManager.requestOverlayPermission(this, OVERLAY_REQUEST_CODE);
            }else {
                String des = "";

                for (String per : needPermissions) {
                    des += desMap.get(per);
                }

                boolean shouldExplain = false;
                for (String permission : needPermissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        shouldExplain = true;
                        break;
                    }
                }
                if (shouldExplain) {
                    new AlertDialog.Builder(this)
                            .setTitle("权限申请")
                            .setMessage("我们需要这些权限来提供更好的服务，请授予权限。" + des)
                            .setPositiveButton("确定", (dialog, which) -> {
//                                ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
                                PermissionManager.requestPermissions(this, needPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
                            })
                            .setNegativeButton("取消", null)
                            .create()
                            .show();

                } else {
                    //确实需要该权限的话，引导到设置页面。 否则，啥都不做也行。
                    Toast.makeText(this, "需要所有权限才能运行应用",
                            Toast.LENGTH_LONG).show();
                    new AlertDialog.Builder(this)
                            .setTitle("权限请求")
                            .setMessage("我们需要这些权限来提供更好的服务，请授予权限。" + des)
                            .setPositiveButton("去设置", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .setNegativeButton("取消", (dialog, which) -> {
                                // 用户取消操作
                            })
                            .show();
                }
            }
        } else if (requestCode == AUTO_BOOT_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                if (Build.MANUFACTURER.toLowerCase().contains("xiaomi") ||
                        Build.MANUFACTURER.toLowerCase().contains("oppo") ||
                        Build.MANUFACTURER.toLowerCase().contains("vivo") ||
                        Build.MANUFACTURER.toLowerCase().contains("huawei") ||
                        Build.MANUFACTURER.toLowerCase().contains("honor")) {

                    new AlertDialog.Builder(this)
                            .setTitle("需要自启动权限")
                            .setMessage("请在设置中允许应用自启动，以确保功能正常运行")
                            .setPositiveButton("去设置", (dialog, which) -> {
                                try {
                                    switch (Build.MANUFACTURER.toLowerCase()) {
                                        case "xiaomi":
                                            openXiaomiAutoStartSettings();
                                            break;
                                        case "oppo":
                                            openOppoAutoStartSettings();
                                            break;
                                        case "vivo":
                                            openVivoAutoStartSettings();
                                            break;
                                        case "huawei":
                                        case "honor":
                                            openHuaweiAutoStartSettings();
                                            break;
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(this, "请手动允许应用自启动", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            }



        }
    }

    
    private void initializeApp() {
        Log.d(TAG, "initializeApp: ");

        serviceSwitch = findViewById(R.id.serviceSwitch);
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Log.d(TAG, "onCheckedChanged: " + checked);
                if (checked) {
                    startService();
                } else {
                    stopService();
                }
            }
        });


        stwLog = findViewById(R.id.logSwitch);

        stwLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LogManager.setIfWrite2file(b);
                SharedPreferences.Editor editor = getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit();
                editor.putBoolean("write_file", b);
                editor.apply();
            }
        });

        // 初始化日志按钮
        btnViewlog = findViewById(R.id.btnViewLogs);
        btnViewlog.setOnClickListener(v -> showLogDialog());




        btnHideDesktop = findViewById(R.id.btnHideDesktop);

        btnHideDesktop.setOnClickListener(v ->{

            // 获取 PackageManager 实例
            PackageManager packageManager = getPackageManager();

            // 定义需要检查的组件（例如一个 Activity 或 Service）
            ComponentName componentName = new ComponentName(this, "com.example.walklock.ui.MainActivityAlias");
            int componentState = packageManager.getComponentEnabledSetting(componentName);

            Log.d(TAG, "initializeApp: " + componentState);

            if (componentState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || componentState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
                SystemUtil.toggleLauncherIcon(this, false);
            else if (componentState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED || componentState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                SystemUtil.toggleLauncherIcon(this, true);
            }
        });

        btnTestAlarm = findViewById(R.id.btnTestAlarm);
        btnTestAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                // 创建一个 Intent，指定要触发的组件（BroadcastReceiver）
//                Intent intent = new Intent(TestActivity.this, AlarmBroadcastReceiver.class);
//
//                // 创建 PendingIntent
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(TestActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//
//                // 获取 AlarmManager 实例
//                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//
//
//                //重复性。
//                if (alarmManager != null) {
//                    // 设置重复触发任务
//                    long intervalMillis = 3 * 1000; // 每隔 5 秒触发一次
//                    long triggerAtMillis = System.currentTimeMillis() + intervalMillis; // 第一次触发时间
//                    //并不是很精准
//                    alarmManager.setRepeating(AlarmManager.RTC, triggerAtMillis, intervalMillis, pendingIntent);
//                }

            }
        });

    }





    @Override
    protected void onResume() {
        super.onResume();

        // 检查服务是否正在运行并设置开关状态
        // 每次回到前台时检查服务状态
        if (serviceSwitch != null) {
            boolean isServiceRunning = SystemUtil.isServiceRunning(this, WalkDetectionService.class);
            Log.d(TAG, "onResume: isServiceRunning " + isServiceRunning);
            serviceSwitch.setChecked(isServiceRunning);
        }


        if (stwLog != null) {
            stwLog.setChecked(LogManager.getIfWrite2file());
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
//                initializeApp();
            } else {
                Toast.makeText(this, "需要悬浮窗权限才能运行应用", 
                    Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }



    private boolean areMiuiNotificationsEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                return manager.areNotificationsEnabled();
            }
        }
        return true; // 默认返回 true，避免低版本 Android 出现问题
    }



    private void openNotificationSettings() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
        }
        startActivity(intent);
    }

    private void checkMiuiPermissions() {
        if (isMiui()) {
            try {
                // 检查 MIUI 的后台弹出权限
                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra("extra_pkgname", getPackageName());
                startActivity(intent);
            } catch (Exception e) {
                // 如果上面的方式失败，尝试其他方式
                try {
                    Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                    intent.setClassName("com.miui.securitycenter",
                        "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                    intent.putExtra("extra_pkgname", getPackageName());
                    startActivity(intent);
                } catch (Exception e1) {
                    Toast.makeText(this, "请手动授予后台弹出权限", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean isMiui() {
        String manufacturer = Build.MANUFACTURER;
        return "xiaomi".equalsIgnoreCase(manufacturer);
    }

    private void checkUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            if (usageStatsManager == null) return;

            long currentTime = System.currentTimeMillis();
            List<UsageStats> stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, currentTime - 1000, currentTime);

            if (stats == null || stats.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("需要使用情况访问权限")
                        .setMessage("请在设置中允许访问使用情况，以便应用能在导航时暂停检测")
                        .setPositiveButton("去设置", (dialog, which) -> {
                            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        }
    }


    private void checkAutoStartPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}, AUTO_BOOT_CODE);
            } else {
                Log.d(TAG, "checkAutoStartPermission: is true");
            }
        }
    }

    private void openXiaomiAutoStartSettings() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"));
            startActivity(intent);
        }
    }

    private void openOppoAutoStartSettings() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.oppo.safe",
                        "com.oppo.safe.permission.startup.StartupAppListActivity"));
                startActivity(intent);
            } catch (Exception ex) {
                try {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.coloros.safecenter",
                            "com.coloros.safecenter.startupapp.StartupAppListActivity"));
                    startActivity(intent);
                } catch (Exception exx) {
                    // 打开应用详情页面
                    openAppSettings();
                }
            }
        }
    }

    private void openVivoAutoStartSettings() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            startActivity(intent);
        } catch (Exception e) {
            openAppSettings();
        }
    }

    private void openHuaweiAutoStartSettings() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            startActivity(intent);
        } catch (Exception e) {
            openAppSettings();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

//    private void checkServiceStatus() {
//        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
//        boolean wasServiceRunning = prefs.getBoolean("service_was_running", false);
//
//        if (wasServiceRunning && !SystemUtil.isServiceRunning(this, WalkDetectionService.class)) {
//            // 服务之前在运行但现在停止了，可能是被强制停止
//            new AlertDialog.Builder(this)
//                .setTitle("服务已停止")
//                .setMessage("检测到服务已停止运行，是否重新启动？")
//                .setPositiveButton("启动", (dialog, which) -> {
//                    startWalkDetectionService();
//                })
//                .setNegativeButton("取消", null)
//                .show();
//        }
//    }

//    private void startWalkDetectionService() {
//        Intent intent = new Intent(this, WalkDetectionService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent);
//        } else {
//            startService(intent);
//        }
//
//        // 记录服务状态
//        SharedPreferences.Editor editor = getSharedPreferences("app_settings", MODE_PRIVATE).edit();
//        editor.putBoolean("service_was_running", true);
//        editor.apply();
//    }

    private boolean isAutoStartAllowed() {
        // 根据不同厂商检查自启动权限
        // 这个需要根据具体机型来实现
        return true;
    }

    private void openAutoStartSettings() {
        try {
            Intent intent = new Intent();
            String manufacturer = Build.MANUFACTURER.toLowerCase();
            switch (manufacturer) {
                case "xiaomi":
                    intent.setComponent(new ComponentName("com.miui.securitycenter", 
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                    break;
                case "oppo":
                    intent.setComponent(new ComponentName("com.coloros.safecenter", 
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
                    break;
                // 添加其他厂商的设置页面
                default:
                    intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    break;
            }
            startActivity(intent);
        } catch (Exception e) {
            // 打开应用详情页面
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private void showLogDialog() {
        File[] logFiles = LogManager.getLogFiles();
        if (logFiles == null || logFiles.length == 0) {
            Toast.makeText(this, "没有日志文件", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建日志文件列表，添加时间信息
        String[] fileNames = new String[logFiles.length];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        for (int i = 0; i < logFiles.length; i++) {
            String date = sdf.format(new Date(logFiles[i].lastModified()));
            fileNames[i] = date + " - " + logFiles[i].getName();
        }

        // 按时间降序排序
        Arrays.sort(logFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        Arrays.sort(fileNames, Collections.reverseOrder());

//        // 创建自定义布局的对话框
//        View dialogView = getLayoutInflater().inflate(R.layout.dialog_log_list, null);
//        ListView listView = dialogView.findViewById(R.id.logListView);
//
//        // 设置适配器
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_list_item_1, fileNames);
//        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("日志文件")
            .setItems(fileNames, (d, which) -> showLogContent(logFiles[which]))
//            .setView(dialogView) // 使用items 和 自定义的dialogView都可以。
            .setPositiveButton("分享全部", (d, which) -> shareAllLogs(logFiles))
            .setNeutralButton("清除日志", (d, which) -> {
                new AlertDialog.Builder(this)
                    .setTitle("确认清除")
                    .setMessage("确定要清除所有日志吗？")
                    .setPositiveButton("确定", (innerDialog, innerWhich) -> {
                        LogManager.clearLogs();
                        Toast.makeText(this, "日志已清除", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            })
            .setNegativeButton("关闭", null)
            .create();

//        // 设置列表项点击事件
//        listView.setOnItemClickListener((parent, view, position, id) -> {
//            showLogContent(logFiles[position]);
//            dialog.dismiss();
//        });

        dialog.show();
    }

    private void showLogContent(File logFile) {
        try {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_log_content, null);
            TextView logContentView = dialogView.findViewById(R.id.logContentView);
            logContentView.setText(content.toString());

            new AlertDialog.Builder(this)
                .setTitle(logFile.getName())
                .setView(dialogView)
                .setPositiveButton("分享", (dialog, which) -> shareLogFile(logFile))
                .setNegativeButton("关闭", null)
                .show();
        } catch (IOException e) {
            Toast.makeText(this, "无法读取日志文件", Toast.LENGTH_SHORT).show();
            LogManager.e(TAG, "Error reading log file", e);
        }
    }

    private void shareLogFile(File logFile) {
        try {
            Uri uri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider",
                logFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "应用日志");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "分享日志文件"));
        } catch (Exception e) {
            Toast.makeText(this, "分享日志失败", Toast.LENGTH_SHORT).show();
            LogManager.e(TAG, "Error sharing log file", e);
        }
    }

    private void shareAllLogs(File[] logFiles) {
        try {
            ArrayList<Uri> uris = new ArrayList<>();
            for (File file : logFiles) {
                Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    file);
                uris.add(uri);
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "应用日志");
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "分享所有日志文件"));
        } catch (Exception e) {
            Toast.makeText(this, "分享日志失败", Toast.LENGTH_SHORT).show();
            LogManager.e(TAG, "Error sharing all log files", e);
        }
    }
}