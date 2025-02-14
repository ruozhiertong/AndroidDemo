//package com.example.walklock.service;
//
//import android.app.ActivityManager;
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.IBinder;
//import android.util.Log;
//
//import com.example.safetywalk.R;
//import com.example.walklock.util.LogManager;
//
//public class GuardService extends Service {
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        // 需要创建通知渠道
//        createNotificationChannel();
//
//        // 创建前台服务通知
//        Notification notification = new Notification.Builder(this, "guard_service_channel")
//                .setContentTitle("Guard Service")
//                .setContentText("Guarding Main Service")
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .build();
//        startForeground(2, notification);
//
//        // 检查并重启主服务的逻辑需要优化
//        startWatchDog();
//    }
//
//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                "guard_service_channel",
//                "Guard Service Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            );
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            manager.createNotificationChannel(channel);
//        }
//    }
//
//    private void startWatchDog() {
//        new Thread(() -> {
//            while (true) {
//                try {
//                    Log.d("GuardService", "Thread: ");
//                    Log.d("GuardService", "Thread:  is working...");
//                    Log.d("GuardService", "Thread: Process ID: " + android.os.Process.myPid());
//                    LogManager.d("GuardService" ,"Thread: Process ID: " + android.os.Process.myPid() );
//                    // 检查主服务是否运行
//                    if (!isServiceRunning(WalkDetectionService.class)) {
//                        // 尝试重启主服务
//                        Intent intent = new Intent(this, WalkDetectionService.class);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            startForegroundService(intent);
//                        } else {
//                            startService(intent);
//                        }
//                        LogManager.d("GuardService", "Attempting to restart WalkService");
//                    }
//
//                    Thread.sleep(5000);
//                } catch (Exception e) {
//                    LogManager.e("GuardService", "Error in watchdog thread", e);
//                }
//            }
//        }).start();
//    }
//
//    private boolean isServiceRunning(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY;
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}
