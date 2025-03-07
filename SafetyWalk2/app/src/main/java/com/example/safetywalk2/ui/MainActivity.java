package com.example.safetywalk2.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Person;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.LinearLayout;
import android.widget.Button;

import com.example.safetywalk2.R;
import com.example.safetywalk2.service.WalkDetectionService;
import com.example.safetywalk2.util.BatteryOptimizationUtils;
import com.example.safetywalk2.util.Config;
import com.example.safetywalk2.util.PermissionManager;
import com.example.safetywalk2.util.SystemUtil;
import com.example.safetywalk2.util.ThemeManager;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String CHANNEL_ID = "motion_guard_channel";
    private static final int NOTIFICATION_ID = 1;
    
    private FrameLayout statusCircle;
    private ImageView shieldIcon;
    private TextView statusText;
    private SeekBar sensitivitySeekBar;
    private SwitchMaterial notificationSwitch, soundSwitch, vibrationSwitch, launchSwitch;
    private boolean isServiceOn = false;
    private SharedPreferences preferences;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private NotificationManager notificationManager;

    private boolean isChangingProgrammatically = false; // 添加一个标志，处理switch重复处理。

    private Button helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        processPermission();

        isServiceOn = isWalkDetectionServiceRunning();
        // 初始化系统服务
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        // 初始化音频播放器
//        mediaPlayer = MediaPlayer.create(this, R.raw.service_start);
        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);


        // 初始化 SharedPreferences
        preferences = getSharedPreferences(Config.SHAREFILE_NAME, MODE_PRIVATE);

        initializeViews();
        loadSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        statusCircle = findViewById(R.id.status_circle);
        shieldIcon = findViewById(R.id.shield_icon);
        statusText = findViewById(R.id.status_text);
        sensitivitySeekBar = findViewById(R.id.sensitivity_seekbar);
        notificationSwitch = findViewById(R.id.notification_switch);
        soundSwitch = findViewById(R.id.sound_switch);
        vibrationSwitch = findViewById(R.id.vibration_switch);
        launchSwitch = findViewById(R.id.launch_switch);

        // 初始化 helpButton
        helpButton = findViewById(R.id.help_button);
        helpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        });
    }

    private void loadSettings() {
        // 加载保存的设置
        updateServiceState(isServiceOn, false); // 不执行动画

        sensitivitySeekBar.setProgress(preferences.getInt(Config.SENSITIVITY, 100));
        notificationSwitch.setChecked(preferences.getBoolean(Config.NOTIFICATION, true));
        soundSwitch.setChecked(preferences.getBoolean(Config.SOUND, true));
        vibrationSwitch.setChecked(preferences.getBoolean(Config.VIBRATION, true));
        launchSwitch.setChecked(preferences.getBoolean(Config.LAUNCH_ICON, false));
    }

    private void setupClickListeners() {
        // 状态圆圈点击事件
        statusCircle.setOnClickListener(v -> toggleService());

        // 灵敏度调节
        sensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println("onChange");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                System.out.println("onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("onStopTrackingTouch");
                int finalProgress = seekBar.getProgress();
                Log.d(TAG, "onStopTrackingTouch: " + finalProgress);
                preferences.edit().putInt(Config.SENSITIVITY, finalProgress).apply();

            }
        });

        // 开关事件监听
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(Config.NOTIFICATION, isChecked).apply();
        });

        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(Config.SOUND, isChecked).apply();
        });

        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(Config.VIBRATION, isChecked).apply();
        });

        launchSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->{
            if (isChangingProgrammatically)
                return;
//             显示确认弹窗
            if (isChecked){
                showConfirmationDialog(isChecked);
            } else {
                preferences.edit().putBoolean(Config.LAUNCH_ICON, isChecked).apply();
                SystemUtil.toggleLauncherIcon(this, "com.example.safetywalk2.ui.MainActivityAlias", true);
            }
        });


        LinearLayout logSettings = findViewById(R.id.log_setting);
        logSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LogActivity.class);
            startActivity(intent);
        });


        // 主题设置点击事件
        LinearLayout themeSettings = findViewById(R.id.theme_setting);
        themeSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ThemeSettingsActivity.class);
            startActivity(intent);
        });

    }


    private void showConfirmationDialog(boolean isChecked) {
        String title = isChecked ? "确认启用" : "确认禁用";
        String message = isChecked ? "你确定要启用这个功能吗？" : "你确定要禁用这个功能吗？";


        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", (dialog, which) -> {
                    // 用户确认后保存状态
                    preferences.edit().putBoolean(Config.LAUNCH_ICON, isChecked).apply();
                    Toast.makeText(this, isChecked ? "功能已启用" : "功能已禁用", Toast.LENGTH_SHORT).show();
                    SystemUtil.toggleLauncherIcon(this, "com.example.safetywalk2.ui.MainActivityAlias", false);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    // 用户取消后恢复之前的开关状态
                    isChangingProgrammatically = true;
                    launchSwitch.setChecked(!isChecked);
                    isChangingProgrammatically = false;
                })
                .setOnCancelListener(dialog -> {
                    // 用户点击返回键或对话框外部时也恢复状态
                    isChangingProgrammatically = true;
                    launchSwitch.setChecked(!isChecked);
                    isChangingProgrammatically = false;
                })
                .setCancelable(true)  // 允许点击对话框外部取消
                .show();
    }

    private void toggleService() {
        // 禁用点击事件，防止重复点击
        statusCircle.setEnabled(false);
        
        // 创建旋转动画
        RotateAnimation rotateAnimation = new RotateAnimation(
            0, 360,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(1000);
        shieldIcon.startAnimation(rotateAnimation);

        // 1秒后更新状态
        new Handler().postDelayed(() -> {
            isServiceOn = !isServiceOn;
            updateServiceState(isServiceOn, true);
            
            // 如果服务开启，执行相应的操作
            if (isServiceOn) {
//                performServiceStartActions();
                startWalkDetectService();
            } else {
                stopWalkDetectService();
            }
            // 重新启用点击事件
            statusCircle.setEnabled(true);
        }, 1000);
    }

    private void updateServiceState(boolean isOn, boolean animate) {
        // 更新UI状态
        if (isOn) {
            statusCircle.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_background));
            shieldIcon.setImageResource(R.drawable.ic_shield_check);
            statusText.setText("服务已开启");
        } else {
            statusCircle.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_background_light));
            shieldIcon.setImageResource(R.drawable.ic_shield_outline);
            statusText.setText("服务已关闭");
        }

        // 如果需要动画效果
        if (animate) {
            // 添加缩放动画
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(statusCircle, "scaleX", 0.9f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(statusCircle, "scaleY", 0.9f, 1f);
            scaleX.setDuration(200);
            scaleY.setDuration(200);
            scaleX.start();
            scaleY.start();
        }
    }

    private void performServiceStartActions() {
        // 检查并执行震动
        if (vibrationSwitch.isChecked()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1000);
            }
        }

        // 检查并播放声音
        if (soundSwitch.isChecked()) {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(0); // 重置到开始位置
                mediaPlayer.start();
            }
        }

        // 检查并发送通知
        if (notificationSwitch.isChecked()) {
            showServiceNotification();
        }
    }

    private void showServiceNotification() {
        // 获取默认通知音效
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        // 设置震动模式
        long[] vibrationPattern = {0, 1000, 500, 1000}; // 延迟0ms，震动1000ms，暂停500ms，震动1000ms

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield_check)
            .setContentTitle("MotionGuard 服务已开启")
            .setContentText("防盗保护已启动，设备受到监控")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            // 添加声音
            .setSound(soundUri)
            // 添加震动
            .setVibrate(vibrationPattern)
            // 设置通知的重要性
            .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MotionGuard Service";
            String description = "Notifications for MotionGuard service status";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();

        isServiceOn = isWalkDetectionServiceRunning();
        updateServiceState(isServiceOn, false); // 不执行动画
    }

    private void setupVerificationScreen() {
        // This method would set up the verification screen as shown in Image 2
    }


    public void startWalkDetectService() {
        if (isWalkDetectionServiceRunning())
            return;

        // 启动服务
        Intent serviceIntent = new Intent(this, WalkDetectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

    }

    private boolean isWalkDetectionServiceRunning (){
        boolean isServiceRunning = false;
        WalkDetectionService service = WalkDetectionService.getInstance();
        if (service == null || service.isDestroying())
            isServiceRunning = false;
        else
            isServiceRunning = true;
        return isServiceRunning;
    }

    public void stopWalkDetectService() {
        boolean isServiceRunning = isWalkDetectionServiceRunning();
        if (!isServiceRunning)
            return;

        // 发送停止服务的 Intent
        Intent stopIntent = new Intent(this, WalkDetectionService.class);
        stopIntent.setAction(WalkDetectionService.ACTION_STOP_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent);
        } else {
            startService(stopIntent);
        }
    }






    private static final String[] REQUIRED_PERMISSIONS = {
            android.Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.POST_NOTIFICATIONS,
//            android.Manifest.permission.RECEIVE_BOOT_COMPLETED //静态的用动态的方式去处理也可以。但是只要在文件声明就能granted。如果在文件中没声明，那么申请静态权限时会申请不到。
                                                                //实际上动态方式去申请静态的，没有意义。没在Manifest中声明，代码中是无法获取到的。而只要在文件中声明了，自然就获取到，没必要动态。
    };

    //提示，为了用户更友好。
    private Map<String, String> PERMISSION_DES = Map.of(
            android.Manifest.permission.ACTIVITY_RECOGNITION,"需要身体运动权限，为了检测运动状态",
            android.Manifest.permission.RECEIVE_BOOT_COMPLETED, "开启自启动权限可以防止被系统杀死",
            Manifest.permission.POST_NOTIFICATIONS, "开启通知权限，可以在通知栏获取服务执行状态",
            android.Manifest.permission.READ_CONTACTS,"需要日历读取权限，为了XXX",
            android.Manifest.permission.READ_CALENDAR, "需要联系人权限，为了XXX"
    );

    private Map<String, Boolean> PERMISSION_MUST = Map.of(
            android.Manifest.permission.ACTIVITY_RECOGNITION, true,
            Manifest.permission.POST_NOTIFICATIONS, false
    );

    private static final int PERMISSION_REQUEST_CODE = 100;

    private static final int OVERLAY_REQUEST_CODE = 101;


    private void processPermission() {
        //该应用需要的权限：
        // 运动健康、后台弹出界面，显示悬浮窗，锁屏显示，自启动，通知，省电策略（不限制），使用情况访问权限。

        //两大类：
        // 1.常规的动态申请。（系统提供标准API进行申请。）
        // 2.跳转设置页面的申请。 (在某些权限无法从常规的动态获取或者被拒绝后无法再从常规的获取，那么就要用上2)
        //能尽量1的就1，1的流程比较标准。

        //1.
        //申请权限。 常规权限的动态申请。 弹出权限让其选择。
        List<String> needPermissions = PermissionManager.needPermissions(this, REQUIRED_PERMISSIONS);
        if (!needPermissions.isEmpty()) {
            PermissionManager.requestPermissions(this,  needPermissions.toArray(new String[0]) , PERMISSION_REQUEST_CODE);
        }

        //2.
        //某些系统的 动态申请，跳转到对应的设置页面，无法跳出权限让其其选择。
        //这种情况下，一般是先判断是否有了该权限，如果没有跳转到具体的设置页面


        //申请 悬浮窗口。
        if (!PermissionManager.hasOverlayPermission(this)) {
            Log.e(TAG, "No overlay permission");
//            new AlertDialog.Builder(this)
//                    .setTitle("悬浮窗权限未开启")
//                    .setMessage("请开启悬浮窗权限")
//                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            PermissionManager.requestOverlayPermission(MainActivity.this, OVERLAY_REQUEST_CODE);
//                        }
//                    })
//                    .setNegativeButton("取消", null)
//                    .show();

            PermissionManager.requestOverlayPermission(MainActivity.this, OVERLAY_REQUEST_CODE);
        }

        //申请后台弹出界面 权限
        if (!PermissionManager.isBackgroundStartAllowed(this)) {
            PermissionManager.requestBackgroundStartPerssion(this);

        }

        // 申请锁屏显示
        if (!PermissionManager.isShowLockViewGranted(this)) {
            PermissionManager.requestShowLockViewPermission(this);

        }

        // 申请自启动权限。
        if (!PermissionManager.isAutoStartPermissionGranted(this)) {
            PermissionManager.requestAutoStartPermission(this);
        }

//
//        // 省电策略
//        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//        startActivity(intent);

//        Log.d(TAG, "processPermission: " + BatteryOptimizationUtils.getMiuiBatteryOptimization(this));
//        Log.d(TAG, "processPermission: " + BatteryOptimizationUtils.isIgnoringBatteryOptimizations(this));
        if (!BatteryOptimizationUtils.isIgnoringBatteryOptimizations(this)) {
//            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//            startActivity(intent);
            Log.d(TAG, "isIgnoringBatteryOptimizations: false");
            PermissionManager.requestBatteryNolimit(this);
        }


        //申请 使用情况访问权限
        PermissionManager.checkUsageStatsPermission(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        List<String> needPermissions = new ArrayList<String>();
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0 ; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    needPermissions.add(permissions[i]);
                    Log.d(TAG, "onRequestPermissionsResult:needPermissions " + permissions[i]);
                }
            }

            if (needPermissions.isEmpty()){
                Toast.makeText(this, "全部权限已申请",
                        Toast.LENGTH_LONG).show();
            } else {
                boolean needLast = true;
                for (String permission : needPermissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        //对未授权的权限再次全部申请。
                        PermissionManager.requestPermissions(this, needPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
                        needLast = false;
                        break;
                        //更详细，具体的，可以弹出对话框来告知原因
//                        new AlertDialog.Builder(this)
//                                .setTitle("权限申请")
//                                .setMessage("我们需要这些权限来提供更好的服务，请授予权限。" + permission)
//                                .setPositiveButton("确定", (dialog, which) -> {
////                                ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
//                                    PermissionManager.requestPermissions(this, needPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
//                                })
//                                .setNegativeButton("取消", null)
//                                .create()
//                                .show();
                    }
                }
                if (needLast) {
                    for (String per: needPermissions) {
                        if (PERMISSION_MUST.get(per)) {
                            //确实需要该权限的话，引导到设置页面。 否则，啥都不做也行。
                            Toast.makeText(this, "需要所有权限才能运行应用",
                                    Toast.LENGTH_LONG).show();
                            new AlertDialog.Builder(this)
                                    .setTitle("权限请求")
                                    .setMessage("我们需要这些权限来提供更好的服务，请授予权限。" + per)
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
                }
            }
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

}