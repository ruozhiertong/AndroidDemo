package com.example.walklock.widget;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.safetywalk.R;
import com.example.walklock.ui.MainActivity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MyWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "MyWidgetProvider";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getAction());

        // 处理定时更新的广播
        if ("com.example.walklock.widget.update".equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, MyWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }

        super.onReceive(context, intent);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate: ");
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        
        // 获取当前时间
        long timestamp = System.currentTimeMillis();
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String readableDate = dateTime.format(formatter);
        views.setTextViewText(R.id.widget_content, "更新时间：" + readableDate);

        // 创建启动 MainActivity 的 PendingIntent
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // 为整个小部件设置点击事件
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled: ");

//        // 创建一个 Intent，用于触发更新
//        Intent intent = new Intent(context, MyWidgetProvider.class);
//        intent.setAction("com.example.walklock.widget.update");
//
//        // 创建 PendingIntent
//        PendingIntent pendingIntent;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            // 针对 Android 12 及以上版本，添加 FLAG_IMMUTABLE 标志
//            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//        } else {
//            // 针对 Android 11 及以下版本，使用旧的标志
//            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        }
//
//
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        if (alarmManager != null) {
//            //setReapting不是很精准。
//            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 10000, pendingIntent); // 每60秒更新一次
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                // 对于 Android 6.0 及以上版本，使用 setExactAndAllowWhileIdle
////                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);
////            } else {
////                // 对于较老的版本
////                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);
////            }
//        }
    }

}
