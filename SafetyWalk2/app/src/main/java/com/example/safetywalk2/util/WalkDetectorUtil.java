package com.example.safetywalk2.util;

import static android.content.Context.SENSOR_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


/***
 * 开启服务后，一直在后台检测。
 * 走路检测：
 *      持续走路1分钟，可以视作在走路。开启锁屏。
 *      平静持续5分钟，可以视作在静止。关闭锁屏。
 *
 * 锁屏情况下，无法被杀死，无法开启其他app等。
 *
 *
 *
 *
 *
 */
public class WalkDetectorUtil {

    private static final String TAG = "WalkDetector";

    /**
     * 传感器
     */
    private SensorManager sensorManager;
    private SensorEventListener countEventListener;
    public boolean isRegistered = false;

    public void startStepDetector(Activity activity, SensorEventListener sensorEventListener) {
        sensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        /**
         * TYPE_STEP_COUNTER 和 TYPE_STEP_DETECTOR 的区别：
         * 特性	TYPE_STEP_COUNTER	TYPE_STEP_DETECTOR
         * 功能	记录设备启动以来的总步数	检测用户的每一步
         * 数据格式	返回累积步数（浮点数）	返回单步检测值（通常为 1.0）
         * 实时性	非实时，累积值	实时，每次检测到一步触发事件
         * 重启影响	设备重启后步数继续累加	设备重启后重新开始检测
         * 适用场景	记录总步数（如健身追踪）	实时步数检测（如跑步应用）
         */
//        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if (detectorSensor != null) {
            Log.d(TAG, "startStepDetector: registerListener");
            sensorManager.registerListener(sensorEventListener, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    public void startStepCount(Context ctx, SensorEventListener sensorEventListener) {
        if (isRegistered) {
            LogManager.d(TAG , "Sensor already registered");
            return ;
        }

        sensorManager = (SensorManager) ctx.getSystemService(SENSOR_SERVICE);

        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
//        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if (countSensor != null) {
            Log.d(TAG, "startStepCount: registerListener");
            countEventListener = sensorEventListener;
            boolean registered = sensorManager.registerListener(countEventListener, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
            if (registered) {
                isRegistered = true;
            } else {
                LogManager.e(TAG, "registerListener failed");
            }
        } else {
            LogManager.e(TAG, "countSensor is null");
        }

        /**
         * 检测走路 运动中：
         * 时间2分钟内，持续在走路。
         */
    }


    public void stopStepCount() {
        if (!isRegistered)
            return;
        if (sensorManager != null) {
            Log.d(TAG, "stopStepCount: ");
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (sensor != null && countEventListener != null) {
                sensorManager.unregisterListener(countEventListener, sensor);
                isRegistered = false;
            } else {
                LogManager.e(TAG, "stopStepCount failed");
            }
        }
    }
}
