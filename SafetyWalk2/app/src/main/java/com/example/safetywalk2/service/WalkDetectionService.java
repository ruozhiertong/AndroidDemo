package com.example.safetywalk2.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.safetywalk2.R;
import com.example.safetywalk2.receiver.LockScreenReceiver;
import com.example.safetywalk2.ui.LockActivity;
import com.example.safetywalk2.util.Config;
import com.example.safetywalk2.util.LogManager;
import com.example.safetywalk2.util.WalkDetectorUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * 检测算法。
 *
 * status： walk ， still.
 *
 *
 * isWalking
 *  走路：
 *      步数增长。
 *      增长间隔小于10s。
 *
 *  停止状态：
 *      步数不增长。
 *      且不增常持续2分钟。
 *
 *
 * 步速：正常步行速度约为 4~5公里/小时（即每分钟约80~100步）。步频：快步走可达120~140步/分钟，慢走可能低于80步/分钟。
 *
 * 走路状态的判断：
 *      方式1. 按照上述方式。
 *      方式2. 按照步数。按照每分钟80～100步， 5分钟内的步数达到400～500， 就可以判定为在走路。
 *      方式3. 按照距离。几分钟内，距离多少米就认定为走路。
 *
 * 认定为走路，就进入锁住屏幕状态。 因为已经有几分钟的缓冲，因此可以认为是在行进过程，而不是临时的走动。
 *
 *
 * 功能都好实现，主要是防止作弊 以及 保活。
 * 作弊手段：
 *      0.在系统中关闭服务，或者 删除app。 （这无解）
 *      1.在走路过程中，通过频繁关 开 屏幕，来重置检测。 解决：使用惩罚措施算法来避免重复开和关。
 *      2.利用重启来重置。 解决：可以利用sharepreference来记录处于锁住屏幕状态。 重启后，会重新启动
 *      3.用户点击强制停止应用。 做一些保活策略。 强制停止之后，所有的都停了，包括开/关屏的广播等，因此陷入完全的停止。要做一些保护策略。
 *                          对于强制停止应用，一些保护策略都不行。
 *                           - 双进程守护方案。 不行，因为强制关闭应用，会想相关的进程全部关闭。
 *                           - 系统广播。 只能在重启的时候接收boot的广播，其他动态注册的广播（生命周期是随应用组件），因为由于应用被关闭，也就无法接收到了。
 *                           - 使用JobScheduler 或 workmanager 同理。 当应用被强制停止(Force Stop)后，JobScheduler 和 WorkManager 的任务也会被系统取消。
 *                           - 使用账号同步同能，同理。 在强制关闭应用后，还是无法工作。
 *
 *                           所以，在强制停止应用下，无解。 不过，我们可以隐藏app图标，使得无法直接进入到应用信息页面进行强制停止操作。当然只是增加操作路径而已，根本上还是无法解决。
 *
 *                          Android系统（尤其是Android 3.1+）会将强制停止的应用标记为"停止状态"（stopped state），这类应用无法接收任何广播（包括BOOT_COMPLETED）。 直到用户手动启动应用或通过其他组件显式激活。
 *
 *     4.不给相关的权限。 无解。
 *
 *     对于无解的，只能靠用户自觉了！
 *
 *
 * 检测时机。
 *     开屏的时候检测。  （不管走路不走路，都在检测）
 *     锁屏的时候不检测。（但是，如果是在走路，只有5次机会不检测，超过5次都检测。避免利用这个来规避检测）。
 */

public class WalkDetectionService extends Service {
    private static final String TAG = "WalkDetectionService";
    public static final String ACTION_ACTIVITY_UPDATE = "com.example.walklock.ACTION_ACTIVITY_UPDATE";
    public static final String EXTRA_ACTIVITY_TYPE = "activity_type";
    public static final String EXTRA_TRANSITION_TYPE = "transition_type";
    public static final String ACTION_STOP_SERVICE = "com.example.walklock.STOP_SERVICE";
    public static final String ACTION_STOP_SENSOR = "com.example.walklock.STOP_SENSOR";
    public static final String ACTION_START_SENSOR = "com.example.walklock.START_SENSOR";




    private boolean isNormalStop = false; // 标记是否是正常停止
//    private static final int WALKING_THRESHOLD = 3000; // 3秒，用于测试。实际使用时改为30000(30秒)
//    private static final int STATIONARY_THRESHOLD = 1200000; // 20分钟

//    private ActivityRecognitionClient activityRecognitionClient;

    private WalkDetectorUtil walkDetector;
    private LockScreenReceiver lockScreenReceiver;

    private Queue<Long> stepTimestamps; // 用于存储步数事件的时间戳
    private boolean isMoving = false; // 是否在动。 但不代表一定在走路或跑路。

    //减少非走路状态的误判。
    // 时间窗口和步数阈值。
    private static final long TIME_WINDOW = 5 * 60 * 1000; // 时间窗口。 在5分钟内，触发10次运动，可视为在行走或跑步 运动。
    private static final int STEP_THRESHOLD = 6; // 测试 6 效果比较好。 相对激进些。

    private static final int MAX_THREADHOLD = 20;

    private int stepValue = 0;
    private final IBinder binder = new LocalBinder();
    private int changeCount = 0;

    private boolean isLocked;


    private volatile boolean isDestroying = false; // 添加标志位表示服务正在销毁

    private static volatile WalkDetectionService instance;


    private SharedPreferences preferences;


    // 添加白名单应用包名
    private static final Set<String> WHITE_LIST_PACKAGES = new HashSet<>(Arrays.asList(
            "com.autonavi.minimap",        // 高德地图
            "com.baidu.BaiduMap",          // 百度地图
            "com.tencent.map",             // 腾讯地图
            "com.google.android.apps.maps"  // Google Maps
    ));


    public class LocalBinder extends Binder {
        public WalkDetectionService getService() {
            return WalkDetectionService.this;
        }
    }


    public void init() {

        //动态注册广播接收器。
        lockScreenReceiver = new LockScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF); // 屏幕关闭 / 锁屏。
//        filter.addAction(Intent.ACTION_SCREEN_ON); // 亮屏，不一定解锁。
        filter.addAction(Intent.ACTION_USER_PRESENT); // 解锁，并且进入主屏幕。 ACTION_USER_PRESENT 仅解锁，不一定进入主屏幕。
        registerReceiver(lockScreenReceiver, filter);
        walkDetector = new WalkDetectorUtil();
        // 初始化 SharedPreferences
        preferences = getSharedPreferences(Config.SHAREFILE_NAME, MODE_PRIVATE);
    }

    public void cleanup () {


        // 清理资源

        if (lockScreenReceiver != null) {
            try {
                unregisterReceiver(lockScreenReceiver);
            } catch (IllegalArgumentException e) {
                // 接收器可能已经被注销
                LogManager.e(TAG, "Receiver already unregistered", e);
            }
            lockScreenReceiver = null;
        }

        if (walkDetector != null) {
            walkDetector.stopStepCount();
            walkDetector = null;
        }
    }



    private void startDetector(){
        LogManager.d(TAG, "startDetector: ");
        if (walkDetector != null && walkDetector.isRegistered)
            return;
        LogManager.d(TAG, "register new walkDetector");

        stepTimestamps = new ArrayBlockingQueue<>(100);

//        walkDetector.startStepDetector(this, new SensorEventListener() {
//            @Override
//            public void onSensorChanged(SensorEvent sensorEvent) {
//                Log.d(TAG, "onSensorChanged: " +  sensorEvent.values[0]);
//                Log.d(TAG, "onSensorChanged: " +  sensorEvent.accuracy);
//
//                // stepSensorType == Sensor.TYPE_STEP_DETECTOR
//                    if (sensorEvent.values[0] == 1.0) {
//                        nowStep++;
//                        Log.d(TAG, "onSensorChanged: " + nowStep);
//                        Toast.makeText(MainActivity.this, "Steps: " + nowStep, 1).show();
//                    }
//            }
//
//            @Override
//            public void onAccuracyChanged(Sensor sensor, int i) {
//
//            }
//        });

        //如果传感器有变化。一般会10s回调一次。
        walkDetector.startStepCount(this, new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
                LogManager.d(TAG, "Sensor data received at: " + time);
                changeCount++;
                int tempStep = (int) sensorEvent.values[0];
                Log.d(TAG, "onSensorChanged: changeCount " + changeCount);
                Log.d(TAG, "onSensorChanged: " + sensorEvent.values[0]);
                Log.d(TAG, "onSensorChanged: " + sensorEvent.accuracy);

//                SystemUtil.isScreenOn(getApplicationContext());

//                SystemUtil.isScreenLocked(getApplicationContext());

                //步数没有变化，不处理。
                if (tempStep != stepValue){
                    checkStatus();
                }

                stepValue = tempStep;
//                Toast.makeText(MainActivity.this, "steps: " + tempStep, 1).show();

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        });
    }
    @Override
    public void onCreate() {
        LogManager.d(TAG, "onCreate:" + this);
        Log.d(TAG, "onCreate: Process ID: " + android.os.Process.myPid());
//        Log.d(TAG, "onCreate: Stack trace: " + Log.getStackTraceString(new Exception()));
        super.onCreate();
        instance = this;
        init();
    }


    private boolean isWhiteListAppInForeground() {
//        ActivityManager activityManager;
//        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        if (activityManager == null) return false;
//        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
//        if (processes == null) return false;
//
//        String foregroundApp = "";
//        for (ActivityManager.RunningAppProcessInfo process : processes) {
//            if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                if (process.pkgList.length > 0) {
//                    foregroundApp = process.pkgList[0];
//                    break;
//                }
//            }
//        }
//
//
//        Log.d(TAG, "isWhiteListAppInForeground: " + foregroundApp);

        String foregroundApp = "";

        // 如果是 Android 11 及以上版本，使用另一种方式获取前台应用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && foregroundApp.isEmpty()) {
            try {
                UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                long endTime = System.currentTimeMillis();
                long beginTime = endTime - 60 * 1000;
                List<UsageStats> usageStats = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);

                if (usageStats != null && !usageStats.isEmpty()) {
                    UsageStats recentStats = null;
                    for (UsageStats stats : usageStats) {
                        if (stats.getLastTimeUsed() > 0) { // 过滤掉无效数据
//                        System.out.println("Package: " + stats.getPackageName());
//                        System.out.println("Last used: " + stats.getLastTimeUsed());
//                        System.out.println("Total time in foreground: " + stats.getTotalTimeInForeground() + " ms");
//                        System.out.println("Last time foreground: " + stats.getLastTimeForegroundServiceUsed());
////                        System.out.println("Launch count: " + stats.getAppLaunchCount());
//                        System.out.println("-----------------------------");

                            if (recentStats == null || stats.getLastTimeUsed() > recentStats.getLastTimeUsed()) {
                                recentStats = stats;
                            }
                        }
                    }
                    if (recentStats != null) {
                        foregroundApp = recentStats.getPackageName();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting usage stats", e);
            }
        }

        Log.d(TAG, "Current foreground app: " + foregroundApp);
        return WHITE_LIST_PACKAGES.contains(foregroundApp);
    }

    private void checkStatus(){
        //如果在白名单中，不做check。
        if (isWhiteListAppInForeground()) {
            stepTimestamps.clear();
            isMoving = false;
            return;
        }

        long currentTime = System.currentTimeMillis();

        // 添加当前步数事件的时间戳
        stepTimestamps.add(currentTime);

        // 移除时间窗口之外的步数事件
        while (!stepTimestamps.isEmpty() && (currentTime - stepTimestamps.peek() > TIME_WINDOW)) {
            stepTimestamps.poll();
            //如果在时间窗口之外，那么可以重置一些变量。
//            niceTry = 0;
            isMoving = false;
            LogManager.d(TAG, "checkStatus over TIME_WINDOW");
        }

        LogManager.d(TAG, "step size " + stepTimestamps.size());

        int sentivity = preferences.getInt(Config.SENSITIVITY,100);

        Log.d(TAG, "checkStatus: " + sentivity);

        int finalThread  = (int) (STEP_THRESHOLD * (100.0 / sentivity)); // STEP_THRESHOLD ~ MAX_THREADHOLD

        if (finalThread > MAX_THREADHOLD)
            finalThread = MAX_THREADHOLD;

        Log.d(TAG, "checkStatus: " + finalThread);
        if (stepTimestamps.size() >= finalThread / 3) {
            isMoving = true;
            LogManager.d(TAG, "step size over 1/3 and isMoving true" );
        }

        // 判断运动状态
        if (stepTimestamps.size() >= finalThread) {
            Log.d(TAG, "User started moving");
            startLockScreenActivity();
        } else {
            Log.d(TAG, "checkStatus: " + "User maybe in move");
        }
    }

    private void startLockScreenActivity() {
        Log.d(TAG, "startLockScreenActivity: attempting to start lock screen");
        LogManager.d(TAG, "startLockScreenActivity...");

        // 原来的备选启动方式代码
//        if (!Settings.canDrawOverlays(this)) {
//            Log.e(TAG, "No overlay permission");
//            return;
//        }

        // 创建启动 LockScreenActivity 的 Intent
        Intent lockIntent = new Intent(getApplicationContext(), LockActivity.class);
        lockIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |          // 从服务启动 Activity 需要
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |         // 清除顶部的 Activity
                        Intent.FLAG_ACTIVITY_SINGLE_TOP |       // 确保只有一个实例
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED  // 重置任务栈
//                        Intent.FLAG_ACTIVITY_NO_HISTORY |        // 不加入历史栈
//                        Intent.FLAG_ACTIVITY_CLEAR_TASK |        // 清除目标Activity所在任务栈的所有Activity
//                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |   // 从最近任务中排除
//                        Intent.FLAG_FROM_BACKGROUND |
//                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        );

        // 使用 PendingIntent 来确保即使在后台也能启动
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),  // 使用 ApplicationContext
                0,
                lockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE);

        try {
            // 尝试使用 PendingIntent 启动
            //手动触发。 一般都是和Alarmanager等结合使用，由他们去调用其send方法，不用自己手动调用。
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            LogManager.e(TAG, "Failed to start LockScreenActivity with PendingIntent", e);
            try {
                // 如果 PendingIntent 失败，尝试直接启动
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(lockIntent);
            } catch (Exception e2) {
                LogManager.e(TAG, "Failed to start LockScreenActivity directly", e2);
                // 如果直接启动也失败，尝试使用广播
//                Intent broadcastIntent = new Intent("com.example.walklock.START_LOCK_SCREEN");
//                broadcastIntent.setPackage(getPackageName());
//                sendBroadcast(broadcastIntent);
            }
        }

        // 延迟停止服务，确保有足够时间启动 Activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isNormalStop = true;
            stopSelf();
        }, 1000);
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogManager.d(TAG, "onStartCommand, intent: " + intent + "flags : " + flags + "startId: " + startId);

        // 如果服务正在销毁，拒绝新的启动请求
        if (isDestroying) {
            LogManager.d(TAG, "Service is being destroyed, ignoring start command");
            return START_NOT_STICKY;
        }

        //变成前台服务。
        createNotificationChannel();
        startForegroundWithNotification();


        if (intent != null && ACTION_STOP_SERVICE.equals(intent.getAction())) {
            isNormalStop = true;
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent != null && ACTION_STOP_SENSOR.equals(intent.getAction())) {
            LogManager.d(TAG, "ACTION_STOP_SENSOR " + "isMoving " +  isMoving);
//            if (isMoving)
//                niceTry++;
            //锁屏时候不检测。
            //但是，在走路过程，并且开锁屏超过3次. 依然要检测，不能关闭检测。
//            if (isMoving && niceTry > 3) {
//                return START_STICKY;
//            }
            // 在行进过程中，不能通过锁屏 关闭检测。
            if (isMoving) {
                return START_STICKY;
            }

            //暂停sensor
//            long currentTime = System.currentTimeMillis();

            //只有5次机会 关闭检测。
            //如果过于频繁开 关屏幕，那么认定为在规避检测，那么下次进入锁屏时 依旧保持检测。
            //所以要注意，如果不想在关键时刻进入锁屏，不要作弊。
            //另外，还有一个规避的方法，就是进行关机。 这个要想办法，关机后开机，如果是处于锁住状态，可以有3分钟的使用时间，之后就立即进入锁屏。（3分钟的使用时间是为了让你紧急使用的）

            //如果不关闭，会在锁屏下进入锁住屏幕。所以想规避检测的话，会被惩罚。
//            if (currentTime - preUnlockTime > LOCK_INTER) {
//                if (walkDetector != null) {
//                    walkDetector.stopStepCount();
//                    walkDetector = null;
//                }
//            }

            walkDetector.stopStepCount();
            return START_STICKY;
        }

        if (intent != null && ACTION_START_SENSOR.equals(intent.getAction())) {
            LogManager.d(TAG, "ACTION_START_SENSOR");
            startDetector();
//            preUnlockTime = System.currentTimeMillis();
            return START_STICKY;
        }


        //正常command。
        SharedPreferences settings = getSharedPreferences("app_settings", 0);
        isLocked = settings.getBoolean("is_lock", false);
        if (isLocked) {
            startLockScreenActivity();
            return START_NOT_STICKY;
        } else {
            startDetector();
            return START_STICKY;
        }

//        START_STICKY 只在系统因内存不足等原因杀死 Service 时才起作用。
//        设置了`START_STICKY`的Service会在以下情况下被系统重启：
//
//        1. 系统资源不足时被系统杀死后
//        2. 用户从最近任务列表中移除应用后
//        3. 应用进程被强制终止（如通过系统设置中的"强制停止"功能）
//        4. 系统崩溃或重启后
//        5. 应用崩溃导致Service被终止后
//        6. OEM厂商的系统优化或电池管理功能杀死进程后
//
//        值得注意的是，`START_STICKY`重启时的行为特点：
//        - 系统会创建新的Service实例并调用`onCreate()`
//        - 然后调用`onStartCommand()`，但Intent通常为null
//                - 重启不会立即发生，而是取决于系统资源状况和调度策略
//                - 在某些高度定制的Android系统上，厂商可能限制了Service的自动重启行为
//
//`START_STICKY`不会在以下情况重启Service：
//        - 通过`stopSelf()`或`stopService()`正常停止Service
//                - 在`onTaskRemoved()`中调用`stopSelf()`主动停止Service
//                - 设备处于低电量模式且未被列为电池优化白名单
//
//        如果你需要更可靠的服务重启机制，可以考虑使用`JobScheduler`、`WorkManager`或前台Service来提高服务的存活率。
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "walk_detection",
                    "Walk Detection Service",
                    NotificationManager.IMPORTANCE_HIGH); // 提高通知重要性
            channel.setDescription("用于检测走路状态的服务");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundWithNotification() {
        // 创建打开主界面的 PendingIntent
        Intent mainIntent = new Intent(this, LockActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 创建前台服务通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "walk_detection")
                .setContentTitle("走路检测")
                .setContentText("服务正在运行")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true) // 设置为持续通知
//                .setContentIntent(pendingIntent) //不需要
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE);

        Notification notification = builder.build();

        // 启动前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, notification);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogManager.d(TAG, "onBind: ");
        return binder;
    }


//用户从最近任务列表中移除应用任务：
//  用户通过长按Home键或最近任务按钮，打开最近任务列表。
//  用户选择并滑动移除应用的任务。
//系统清理后台任务：
//  在某些情况下，系统可能会自动清理后台任务以释放资源。这种情况下，onTaskRemoved 方法也会被调用。

//   在用户手动关闭(从最近任务列表移除)应用时，Service 的生命周期是这样的：
//首先会调用 onTaskRemoved()
//然后调用 onDestroy()

// 当用户从最近任务列表移除应用时，实际生命周期是这样的：
//
//首先调用 onTaskRemoved()
//系统直接杀死进程，不会调用 onDestroy()
//
//这是因为：
//
//从最近任务列表移除应用时，系统会直接杀死应用进程
//进程被直接杀死时，不会走正常的生命周期回调
//onDestroy() 是正常生命周期的一部分，所以不会被调用
//
//如果想在应用被移除时执行一些清理工作，应该放在 onTaskRemoved() 中，而不是 onDestroy() 中。因为只有 onTaskRemoved() 能保证被调用。
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        LogManager.d(TAG, "onTaskRemoved: " + this);
        Log.d(TAG, "onTaskRemoved: Process ID: " + android.os.Process.myPid());
        super.onTaskRemoved(rootIntent);

        // 当用户从最近任务列表中移除应用， 会onCreate -> onTaskRemoved, 因此在onTaskRemoved这里不做cleanup. 如果做cleanup，又StartService，会导致onStartCommand中walkDetector空指针。

        // cleanup();

        // 只有在非正常停止时才重启服务
        if (!isNormalStop) {
            LogManager.d(TAG, "onTaskRemoved: restarting...");
            Intent restartServiceIntent = new Intent(getApplicationContext(), WalkDetectionService.class);
            restartServiceIntent.setAction("just_test_no_sense");
            restartServiceIntent.setPackage(getPackageName());
            PendingIntent restartServicePendingIntent = PendingIntent.getService(
                    getApplicationContext(), 1, restartServiceIntent,
                    PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmService = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmService.set(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 1000,
                    restartServicePendingIntent);
        } else {
            //如果正常停止，也不会调用onTaskRemoved.
            //所以，这里也不会执行到。
            cleanup();
        }
    }

    @Override
    public void onDestroy() {
        LogManager.d(TAG, "onDestroy: " + this);
        LogManager.d(TAG, "onDestroy: Process ID: " + android.os.Process.myPid());
        super.onDestroy();

        isDestroying = true;
        instance = null;

        cleanup();

    }


    public static WalkDetectionService getInstance() {
        return instance;
    }

    public boolean isDestroying() {
        return isDestroying;
    }
}