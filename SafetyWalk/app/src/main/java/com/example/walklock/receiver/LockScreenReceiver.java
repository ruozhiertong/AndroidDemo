package com.example.walklock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.walklock.service.WalkDetectionService;
import com.example.walklock.util.SystemUtil;

/**
 * 系统锁屏和解锁的广播接收器。
 */
public class LockScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "LockScreenReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            // 设备锁屏。
            // 锁屏时如果关闭service，那么解锁时由于service取消，无法接收到解锁广播。
            // 因此这里最好不要完全关闭service，而是让service关闭传感器检测。
            Log.d(TAG, "onReceive: ACTION_SCREEN_OFF" );
            boolean isServiceRunning = SystemUtil.isServiceRunning(context , WalkDetectionService.class);
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