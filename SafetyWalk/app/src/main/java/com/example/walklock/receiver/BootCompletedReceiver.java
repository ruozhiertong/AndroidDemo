package com.example.walklock.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.walklock.service.WalkDetectionService;
import com.example.walklock.util.LogManager;


/**
 * 手机开机的广播。
 *
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    private static final String WAKE_ACTION = "com.example.walklock.wakeup";
    private static final long DELAY_TIME = 2 * 60 * 1000; // 2分钟延迟

    @Override
    public void onReceive(Context context, Intent intent) {
        LogManager.d(TAG, "onReceive: " + intent.getAction());

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            LogManager.d(TAG, "Boot completed, scheduling service start after delay");
            scheduleServiceStart(context);
        } else if (WAKE_ACTION.equals(intent.getAction())) {
            LogManager.d(TAG, "Delayed wake up, starting service now");
            startService(context);
        }
    }

    private void scheduleServiceStart(Context context) {
        Intent delayIntent = new Intent(context, BootCompletedReceiver.class);
        delayIntent.setAction(WAKE_ACTION);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, delayIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 0, delayIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        //Alarm + PendingIntent.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long triggerTime = System.currentTimeMillis() + DELAY_TIME;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 对于 Android 6.0 及以上版本，使用 setExactAndAllowWhileIdle
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                // 对于较老的版本
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }

            LogManager.d(TAG, "Scheduled service start after " + (DELAY_TIME / 1000) + " seconds");
        } else {
            LogManager.d(TAG, "alarmManager is null");
        }
    }

    private void startService(Context context) {
        Intent serviceIntent = new Intent(context, WalkDetectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        LogManager.d(TAG, "Service started");
    }
} 