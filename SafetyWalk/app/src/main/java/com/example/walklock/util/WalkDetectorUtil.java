package com.example.walklock.util;

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

import androidx.annotation.NonNull;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;


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

    /**
     * 使用 Activity Recognition API.
     */
    public void startActivityRecognition(Activity activity, BroadcastReceiver receiver){
        Log.d(TAG, "startMotionDetect: ");
        int[] activityTyes = {
                DetectedActivity.STILL,
                DetectedActivity.WALKING,
                DetectedActivity.RUNNING
        };

        List<ActivityTransition> transitions = new ArrayList<>();

        // 为每个活动类型添加进入和退出的转换
        for (int activityType : activityTyes) {
            transitions.add(new ActivityTransition.Builder()
                    .setActivityType(activityType)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build());

            transitions.add(new ActivityTransition.Builder()
                    .setActivityType(activityType)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build());
        }
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);


        // 创建明确的 Intent
        //创建一个 PendingIntent，用于接收活动转换的通知。通常使用 BroadcastReceiver 来处理通知。
        Intent intent = new Intent(activity, receiver.getClass());
        intent.setPackage(activity.getPackageName()); // 添加包名，使其成为明确的 Intent
        intent.setAction("com.example.jibunew.ACTIVITY_TRANSITION"); // 设置 action
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        //使用 ActivityRecognitionClient 注册活动转换更新。
        ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(activity);

        activityRecognitionClient.requestActivityTransitionUpdates(request, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("ActivityRecognition", "Activity transition updates requested successfully");

                        // 同时注册活动识别更新，用于调试
                        requestActivityUpdates(activityRecognitionClient, pendingIntent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ActivityRecognition", "Failed to request activity transition updates", e);
                    }
                });
    }


    // 添加一个辅助方法来获取常规的活动识别更新
    @SuppressLint("MissingPermission")
    private void requestActivityUpdates(ActivityRecognitionClient client, PendingIntent pendingIntent) {
        client.requestActivityUpdates(0, pendingIntent)  // 0 表示尽可能快地更新
                .addOnSuccessListener(aVoid -> {
                    Log.d("ActivityRecognition", "注册活动识别更新成功");
                })
                .addOnFailureListener(e -> {
                    Log.e("ActivityRecognition", "注册活动识别更新失败", e);
                });
    }
}
