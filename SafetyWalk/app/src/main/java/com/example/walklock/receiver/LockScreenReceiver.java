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

        Log.d(TAG, "onReceive: " + intent);

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