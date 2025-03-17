package com.example.safetywalk2.ui;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;


import com.example.safetywalk2.R;
import com.example.safetywalk2.service.WalkDetectionService;
import com.example.safetywalk2.util.Config;
import com.example.safetywalk2.util.LogManager;
import com.example.safetywalk2.util.MathProblemGenerator;
import com.example.safetywalk2.util.ThemeManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class LockActivity extends AppCompatActivity {
    private static final String TAG = "LockActivity";
    private static final long AUTO_CLOSE_DELAY = 8 * 60 * 1000; // 10分钟
    private MathProblemGenerator mathProblemGenerator;
    private EditText answerInput;
    private TextView problemText;
    private TextView erroMsg;
    private SharedPreferences settings;
    private boolean vibrationFlag, soundFlag, notificationFlag;

    //尽量和Serive的通知ID不一致，避免service关闭之后，lockactivity的通知也被关闭。
//    private static final String CHANNEL_ID = "motion_guard_channel";
//    private static final int NOTIFICATION_ID = 1;

    private TextView timerText;
    private CountDownTimer countDownTimer;



    private Handler foregroundCheckHandler;
    private static final int CHECK_INTERVAL = 500; // 检查间隔（毫秒）






    private void performServiceStartActions() {
        settings = getSharedPreferences(Config.SHAREFILE_NAME, MODE_PRIVATE);

        vibrationFlag = settings.getBoolean(Config.VIBRATION, true);
        soundFlag = settings.getBoolean(Config.SOUND, true);
        notificationFlag = settings.getBoolean(Config.NOTIFICATION, true);

        // 检查并执行震动
        if (vibrationFlag) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1000);
            }
        }

        // 检查并播放声音
        if (soundFlag) {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(0); // 重置到开始位置
                mediaPlayer.start();
//                mediaPlayer.release();
            }
        }

        // 检查并发送通知
        if (notificationFlag) {
//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//
//                Log.d(TAG, "performServiceStartActions: notificationFlag");
//
//                createNotificationChannel();
//                showServiceNotification();
//            }, 500);
            createNotificationChannel();
            showServiceNotification();
        }
    }

    private void showServiceNotification() {

        // 获取默认通知音效
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 设置震动模式
        long[] vibrationPattern = {0, 1000, 500, 1000}; // 延迟0ms，震动1000ms，暂停500ms，震动1000ms

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Config.LOCK_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_help)//MIUI中不起作用. 默认使用应用图标
                .setContentTitle("MotionGuard")
                .setContentText("已进入锁屏状态")
                .setOngoing(true)
                .setAutoCancel(false)// 关键：不要让通知被自动清除
                // 添加声音
                .setSound(soundUri)
                // 添加震动
                .setVibrate(vibrationPattern)
                // 设置通知的重要性
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

//        //好像也不起作用。 都是使用应用图标
//        if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi") ||
//                Build.MANUFACTURER.equalsIgnoreCase("Redmi")) {
//            builder.setSmallIcon(R.mipmap.ic_launcher2);  // 在小米设备上使用主图标
//        } else {
//            builder.setSmallIcon(R.drawable.ic_shield_check);  // 在其他设备上使用矢量图标
//        }

        try {
            notificationManager.notify(Config.LOCK_NOTIFICATION_ID, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Failed to show notification", e);
        }
    }

    private void createNotificationChannel() {
        //以应用为上下文
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MotionGuard Service";
            String description = "Notifications for MotionGuard service status";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Config.LOCK_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogManager.d(TAG, "onCreate: Starting LockScreenActivity");

        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lock);

        setupWindow();

        initViews();
        showMathProblem();


        // 初始化倒计时
        startCountdown(AUTO_CLOSE_DELAY);


        performServiceStartActions();

        recordLock(true);

        // 初始化前台应用检查
        setupForegroundCheck();



        //很棒。但是就是有提示。 而且会让其他应用完全不可用。
        // 启用锁定任务模式
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            startLockTask();
//        }
    }


    private void setupForegroundCheck() {
        foregroundCheckHandler = new Handler(Looper.getMainLooper());
        startForegroundCheck();
    }

    private void startForegroundCheck() {
        foregroundCheckHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String topPackage = getTopPackageName();
//                LogManager.d(TAG, "run: " + topPackage);
                if (topPackage != null && !topPackage.equals(getPackageName())) {
                    if (!Config.WHITE_LIST_PACKAGES.contains(topPackage)) {
                        // 如果不是白名单应用，将 LockActivity 切回前台
                        LogManager.d(TAG, "run: moveTaskToFront");
                        moveTaskToFront();
                    }
                }
                foregroundCheckHandler.postDelayed(this, CHECK_INTERVAL);
            }
        }, CHECK_INTERVAL);
    }

    private String getTopPackageName() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
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


    private void recordLock(boolean lock) {

        LogManager.d(TAG, "recordLock:" + lock);

        // 记录锁屏
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Config.LOCK_STATUS, lock);
        editor.apply();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: Received new intent");
        super.onNewIntent(intent);
    }

    private void setupWindow() {

        int LOCK_FLAGS =
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        Window window = getWindow();

        // 设置基本窗口标志
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
//            WindowManager.LayoutParams.FLAG_FULLSCREEN |
//            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
//            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
//            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
//            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
//            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
//            WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE |
//            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
//
//            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
//            WindowManager.LayoutParams.FLAG_FULLSCREEN |
//            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
//            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
//            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
//            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
//            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
//            WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE |
//            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
//        );

        window.addFlags(LOCK_FLAGS);


        // 设置系统UI可见性
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                // 隐藏系统手势
                controller.hide(WindowInsets.Type.systemGestures());
                // 隐藏导航栏
                controller.hide(WindowInsets.Type.navigationBars());
                // 设置系统栏行为
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );

                // 设置状态栏和导航栏透明
                window.setDecorFitsSystemWindows(false);
                controller.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        } else {
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LOW_PROFILE
            );
        }

        // 显示 设置其透明，以及显示壁纸。
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        // 确保输入法可以正常显示
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        // 设置窗口类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }

        // 设置窗口全屏显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        // Android O 及以上版本的锁屏显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        }

        /**
         *
         * 通过 WindowManager 在 Android 系统上添加一个全屏的透明覆盖层（overlayView），并拦截触摸事件。这种技术常用于实现自定义锁屏界面、悬浮窗或其他需要覆盖系统界面的功能。
         */
        // 创建透明覆盖层，系统通知会提示。
//        createBlockingView();
    }


    private void createBlockingView() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 创建一个透明的View来拦截所有上滑手势
        View blockingView = new View(this) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                // 消费所有触摸事件，不让它们传递到系统
                Log.d("blockingView", "onTouchEvent: ");
                return true;
            }
        };

        // 设置透明背景
        blockingView.setBackgroundColor(0x00000000);

//        // 创建全屏参数
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.MATCH_PARENT,
//                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
//                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//                        : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
//                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
//                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                PixelFormat.TRANSLUCENT
//        );

        // 创建全屏覆盖层
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );

        // 设置为底部的覆盖，主要拦截上滑手势
        params.gravity = Gravity.BOTTOM;
        params.height = getResources().getDisplayMetrics().heightPixels;

        // 添加视图到窗口
        windowManager.addView(blockingView, params);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();

        // 禁止出现在最近任务，防止手动停止应用。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                List<ActivityManager.AppTask> tasks = am.getAppTasks();
                if (!tasks.isEmpty()) {
                    tasks.get(0).setExcludeFromRecents(true);
                }
            }
        }

        // 确保Activity始终在前台
        moveTaskToFront();
    }

    private void moveTaskToFront() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (!tasks.isEmpty()) {
                int taskId = tasks.get(0).taskId;
                am.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME);
            }
        }
    }


    /**
     * 虽然实现了功能： 禁止返回桌面，最近任务。
     * 但是，总的来说，这个形式上还是不够优雅，没有番茄ToDo 锁机功能那样优雅：直接禁止上滑，禁止返回。
     *
     * 这个可以是的回到桌面或者其他app时，将锁机界面再拉回来。
     *
     *
     * 使用白名单功能后，这个也不需要了，直接startForegroundCheck进行检测。
     * 但是，startForegroundCheck 在遇到尝试回到桌面时可能不是立即起作用。可以配合onWindowFocusChanged使用。
     *
     * @param hasFocus
     */

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        LogManager.d(TAG, "onWindowFocusChanged: " + hasFocus);
        if (hasFocus) {
//            hideSystemUI();
//            answerInput.requestFocus();
        } else {
            // 如果失去焦点，立即重新获取
            moveTaskToFront();
        }
    }


    /**
     *
     * 禁止返回键等。
     *
     * 也可以防止取消startLockTask (上滑停顿退出)。
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LogManager.d(TAG, "dispatchKeyEvent: ");
        int keyCode = event.getKeyCode();
        // 拦截系统按键
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_RECENT_APPS:
            case KeyEvent.KEYCODE_APP_SWITCH:
            case KeyEvent.KEYCODE_MENU:
                return true;
            default:
                // 允许其他所有按键通过（包括数字键、删除键等）
                return super.dispatchKeyEvent(event);
        }
    }


    /**
     * 防止从下拉通知栏中点击其他app通知，从而离开锁屏界面。
     *
     * onUserLeaveHint：
     *                  从下拉通知中切换到其他app。 会调用
     *                  最近任务切换其他app，home键进离开app。 不一定都会调用。 所以使用前台检查比较靠谱。
     *
     * 如果要添加白名单功能。 这个就不适用了，因为要进入白名单应用，不应该onUserLeaveHint，要允许进入白名单app。 而且进入白名单后，再回到其他app，这个onUserLeaveHint就不起作用。
     * 所以，直接在startForegroundCheck进行检测处理。
     */
//    @Override
//    public void onUserLeaveHint() {
//        LogManager.d(TAG, "onUserLeaveHint: ");
//        // 用户尝试离开应用时（如按 HOME 键）立即返回前台
//        moveTaskToFront();
//    }

    private void showMathProblem() {
        mathProblemGenerator = new MathProblemGenerator();
        mathProblemGenerator.generateNewProblem();
        problemText.setText(mathProblemGenerator.getProblem());
    }

    private void checkAnswer() {
        try {
            int answer = Integer.parseInt(answerInput.getText().toString());
            if (mathProblemGenerator.checkAnswer(answer)) {
                finishActivity();
            } else {
                erroMsg.setVisibility(TextView.VISIBLE);
//                Toast.makeText(this, "答案错误，请重试", Toast.LENGTH_SHORT).show();
                answerInput.setText("");
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        timerText = findViewById(R.id.timer_text);
        problemText = findViewById(R.id.math_problem);
        answerInput = findViewById(R.id.answer_input);
        erroMsg = findViewById(R.id.error_message);
        Button submitButton = findViewById(R.id.submit_button);

        // 设置输入框属性
        answerInput.setFocusableInTouchMode(true);
        answerInput.setFocusable(true);

        // 设置软键盘的动作按钮
        answerInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer();
                return true;
            }
            return false;
        });

        // 监听输入变化，只允许数字输入
        answerInput.setOnKeyListener((v, keyCode, event) -> {
            // 允许删除键和数字键
            return false; // 返回 false 表示不拦截按键
        });

        submitButton.setOnClickListener(v -> checkAnswer());
    }


    private void finishActivity() {
        LogManager.d(TAG, "finishActivity");
        recordLock(false);
        Intent serviceIntent = new Intent(this, WalkDetectionService.class);
        serviceIntent.setAction(WalkDetectionService.ACTION_START_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        finish();
    }
    @Override
    protected void onPause() {
        LogManager.d(TAG, "onPause: ");
        super.onPause();
        // 暂停时停止计时器
//        stopAutoCloseTimer();
    }

    @Override
    protected void onDestroy() {
        LogManager.d(TAG, "onDestroy: ");
        super.onDestroy();
        // 确保清理计时器
//        stopAutoCloseTimer();
//        autoCloseHandler = null;
//        autoCloseRunnable = null;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (notificationFlag) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(Config.LOCK_NOTIFICATION_ID); // 取消单个通知
            // notificationManager.cancelAll(); // 取消所有通知
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stopLockTask();
        }

        // 取消所有回调和消息
        if (foregroundCheckHandler != null) {
            foregroundCheckHandler.removeCallbacksAndMessages(null);
        }
    }


    // 添加倒计时方法
    private void startCountdown(long duration) {
        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 更新倒计时显示
                long seconds = millisUntilFinished / 1000;
                timerText.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
            }

            @Override
            public void onFinish() {
                // 倒计时结束时的操作
                timerText.setText("00:00");
                // 这里可以添加自动解锁或其他操作
                finishActivity();
            }
        }.start();
    }
}