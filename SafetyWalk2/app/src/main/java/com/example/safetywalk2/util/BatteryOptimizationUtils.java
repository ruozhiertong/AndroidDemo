package com.example.safetywalk2.util;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import java.lang.reflect.Method;

public class BatteryOptimizationUtils {

    /**
     * 判断应用是否被设置为“忽略电池优化”
     */
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return false;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }

        return true; // Android 6.0 以下默认忽略电池优化
    }

    /**
     * 获取 MIUI 省电策略。
     *
     * MIUI 系统中的省电策略通常分为以下几种：
     *
     * 无限制：应用在后台运行时不受系统限制。
     *
     * 智能限制：系统根据应用的使用情况动态调整限制。
     *
     * 严格限制：应用在后台运行时会被系统严格限制，可能导致功能异常。
     */
    public static String getMiuiBatteryOptimization(Context context) {
        try {
            Class<?> powerProfileClass = Class.forName("com.miui.powerkeeper.provider.PowerProvider");
            Method method = powerProfileClass.getMethod("getBatteryOptimizationMode", String.class);
            Object result = method.invoke(null, context.getPackageName());
            return result != null ? result.toString() : "Unknown";
        } catch (Exception e) {
            Log.e("BatteryOptimization", "Failed to get MIUI battery optimization mode", e);
        }
        return "Unknown";
    }
}