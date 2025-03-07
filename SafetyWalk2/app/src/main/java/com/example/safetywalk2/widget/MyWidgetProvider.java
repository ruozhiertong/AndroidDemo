package com.example.safetywalk2.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.safetywalk2.R;
import com.example.safetywalk2.service.WalkDetectionService;
import com.example.safetywalk2.ui.MainActivity;


public class MyWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "MyWidgetProvider";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getAction());

        // 处理服务状态变化的广播
        if ("com.example.safetywalk2.ACTION_SERVICE_STATE_CHANGED".equals(intent.getAction())) {
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
        
        // 获取服务状态
        boolean isServiceRunning = false;
        WalkDetectionService service = WalkDetectionService.getInstance();
        if (service == null || service.isDestroying())
            isServiceRunning = false;
        else
            isServiceRunning = true;


        // 获取当前主题
        boolean isDarkTheme = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        // 更新UI状态
        views.setImageViewResource(R.id.widget_icon,
                isServiceRunning ? R.drawable.ic_shield_check : R.drawable.ic_shield_outline);
        
//        // 设置图标颜色
//        views.setInt(R.id.widget_icon, "setColorFilter",
//            isDarkTheme ? 0xFF3377FF : 0xFF3377FF);  // 使用蓝色

//        // 设置文本颜色
//        int textColor = isDarkTheme ? 0xFFFFFFFF : 0xFF000000;
//        int secondaryTextColor = isDarkTheme ? 0xB3FFFFFF : 0xFF666666;
//
//        views.setTextColor(R.id.widget_title, textColor);
//        views.setTextColor(R.id.widget_content, secondaryTextColor);
        
        // 设置背景
        views.setInt(R.id.widget_layout, "setBackgroundResource",
            isDarkTheme ? R.drawable.widget_background_dark : R.drawable.widget_background_light);

        views.setTextViewText(R.id.widget_content,
                isServiceRunning ? "服务运行中" : "服务已停止");

        // 创建启动 MainActivity 的 PendingIntent
        Intent intent = new Intent(context, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 确保清除任务栈

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

        setReaptAction(context);
    }


    public void setReaptAction(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MyWidgetProvider.class);
        intent.setAction("com.example.safetywalk2.ACTION_SERVICE_STATE_CHANGED");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 设置每5秒触发一次
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, pendingIntent);
        }
    }
    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled: ");
    }

}
