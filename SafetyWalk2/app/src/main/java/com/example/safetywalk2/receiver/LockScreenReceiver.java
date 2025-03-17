package com.example.safetywalk2.receiver;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.example.safetywalk2.service.WalkDetectionService;
import com.example.safetywalk2.util.Config;
import com.example.safetywalk2.util.LogManager;

/**
 * 系统锁屏和解锁的广播接收器。
 *
 *
 *
 *
 *

 bug: 当开启LockScreenActivity,关闭Service的时候，还是会让Service onCreate。

 分析：当关闭的时候，这时候可能lockScreenReceiver刚好又收到开锁的广播通知， 又发起了startService，导致Service又被重建。 关闭之前刚好广播又接收到了。
 这种情况是可能发生的。在 onDestroy 执行过程中，如果 lockScreenReceiver 收到解锁广播，确实可能导致服务重新启动。这是一个典型的竞态条件问题。


 WalkDetectionService: startLockScreenActivity...
 2025-02-20 19:43:50.858 D/LockScreenActivity: onCreate: Starting LockScreenActivity
 2025-02-20 19:43:51.048 D/LockScreenActivity: recordLock:true
 2025-02-20 19:43:51.790 D/WalkDetectionService: onDestroy:
 2025-02-20 19:56:22.185 D/WalkDetectionService: onCreate
 2025-02-20 19:56:22.209 D/WalkDetectionService: onStartCommand: Intent { act=com.example.walklock.START_SENSOR cmp=com.example.safetywalk/com.example.walklock.service.WalkDetectionService }
 2025-02-20 19:56:22.216 D/WalkDetectionService: ACTION_START_SENSOR
 2025-02-20 19:56:22.218 D/WalkDetectionService: startDetector:
 2025-02-20 19:56:22.219 D/WalkDetectionService: register new walkDetector


 解决： 检测Service实例从而判断是否在运行。


 */
public class LockScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "LockScreenReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        LogManager.d(TAG, "onReceive: " + intent);

        SharedPreferences preferences = context.getSharedPreferences(Config.SHAREFILE_NAME, MODE_PRIVATE);
        boolean isLocked = preferences.getBoolean(Config.LOCK_STATUS, false);
        if (isLocked)
            return;


        //有一定几率的bug。 当Service销毁时，刚好又接收到广播，广播接收器中又StartService，导致bug。
        // 获取服务实例，检查是否正在销毁
        WalkDetectionService service = WalkDetectionService.getInstance();
        if (service == null || service.isDestroying()) {
            Log.d(TAG, "Service is null or is being destroyed, ignoring broadcast");
            return;
        }

        String action = intent.getAction();
        if (action == null)
            return;

        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            // 设备锁屏。
            // 锁屏时如果关闭service，那么解锁时由于service取消，无法接收到解锁广播。
            // 因此这里最好不要完全关闭service，而是让service关闭传感器检测。
            Log.d(TAG, "onReceive: ACTION_SCREEN_OFF" );
            // 前面的if (service == null || service.isDestroying())，保证运行此处时service是存在的。
//            boolean isServiceRunning = SystemUtil.isServiceRunning(context , WalkDetectionService.class);
            boolean isServiceRunning = true;
            if (isServiceRunning) {
                // 发送停止服务的 Intent
                Intent stopSensorIntent = new Intent(context, WalkDetectionService.class);
                stopSensorIntent.setAction(WalkDetectionService.ACTION_STOP_SENSOR);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(stopSensorIntent);
                } else {
                    context.startService(stopSensorIntent);
                }
            }
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            // 用户解锁设备
            // 启动服务
            Log.d(TAG, "onReceive: ACTION_USER_PRESENT");
            Intent startSensorIntent = new Intent(context, WalkDetectionService.class);
            startSensorIntent.setAction(WalkDetectionService.ACTION_START_SENSOR);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startSensorIntent);
            } else {
                context.startService(startSensorIntent);
            }
        }
//        else if (Intent.ACTION_SCREEN_ON.equals(action)) {
//            // 设备屏幕亮起
//        }
    }
} 