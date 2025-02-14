package com.example.walklock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.example.walklock.service.WalkDetectionService;

public class ActivityTransitionReceiver extends BroadcastReceiver {
    private static final String TAG = "ActivityTransitionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received activity transition update");

        
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            if (result != null && !result.getTransitionEvents().isEmpty()) {
                ActivityTransitionEvent event = result.getTransitionEvents().get(0);
                
                // 创建一个新的 Intent 发送给服务
                Intent serviceIntent = new Intent(context, WalkDetectionService.class);
                serviceIntent.setAction(WalkDetectionService.ACTION_ACTIVITY_UPDATE);
                serviceIntent.putExtra(WalkDetectionService.EXTRA_ACTIVITY_TYPE, event.getActivityType());
                serviceIntent.putExtra(WalkDetectionService.EXTRA_TRANSITION_TYPE, event.getTransitionType());
                
                // 启动服务并传递数据
                context.startService(serviceIntent);
            }
        }
    }
} 