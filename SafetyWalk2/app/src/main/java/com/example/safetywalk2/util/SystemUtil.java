package com.example.safetywalk2.util;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.util.Log;

import com.example.safetywalk2.ui.HiddenActivity;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class SystemUtil {


    /**
     * 手机是否亮屏
     * @param context
     * @return
     */
    public static boolean isScreenOn(Context context) {

        boolean isScreenOn = false;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isScreenOn = powerManager.isInteractive();
            if (isScreenOn) {
                // 屏幕亮着
                System.out.println("屏幕状态：亮");
            } else {
                // 屏幕熄灭
                System.out.println("屏幕状态：熄灭");
            }
        }

        return isScreenOn;
    }

    /**
     * 手机是否锁屏
     * @param context
     * @return
     */
    public static boolean isScreenLocked(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isLock = false;
        isLock = keyguardManager != null && keyguardManager.isKeyguardLocked();
        if (isLock) {
            System.out.println("手机锁屏");
        } else {
            System.out.println("手机未锁屏");
        }
        return isLock;
    }


    /**
     * 服务是否在运行
     * @param context
     * @param serviceClass
     * @return
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }


    public static void toggleLauncherIcon(Context context, String launchClass, boolean show) {
        Log.d("SystemUtil", "toggleLauncherIcon: " + show);
        PackageManager pm = context.getPackageManager();
        ComponentName component = new ComponentName(context, launchClass);
        pm.getComponentEnabledSetting(component);

        // 设置启动activity为disabled，桌面图标也会跟着隐藏。
        int state = show ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        pm.setComponentEnabledSetting(
                component,
                state,
                PackageManager.DONT_KILL_APP
        );
    }


    public static String getTopPackageName(Context context) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();
        // 获取最近1秒的应用使用情况
        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - 1000,
                currentTime
        );

        if (stats != null && !stats.isEmpty()) {
            SortedMap<Long, UsageStats> sortedMap = new TreeMap<>();
            for (UsageStats usageStats : stats) {
                sortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!sortedMap.isEmpty()) {
                return sortedMap.get(sortedMap.lastKey()).getPackageName();
            }
        }
        return null;
    }
}
