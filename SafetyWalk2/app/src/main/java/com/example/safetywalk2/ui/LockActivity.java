package com.example.safetywalk2.ui;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.KeyEvent;
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

import java.util.List;

public class LockActivity extends AppCompatActivity {
    private static final String TAG = "LockActivity";
    private static final int LOCK_FLAGS =
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
    private static final long AUTO_CLOSE_DELAY = 8 * 60 * 1000; // 10分钟

    private MathProblemGenerator mathProblemGenerator;
    private EditText answerInput;
    private TextView problemText;
    private TextView erroMsg;
//    private Handler autoCloseHandler;
//    private Runnable autoCloseRunnable;

    private SharedPreferences settings;

    private boolean vibrationFlag, soundFlag, notificationFlag;

    //尽量和Serive的通知ID不一致，避免service关闭之后，lockactivity的通知也被关闭。
//    private static final String CHANNEL_ID = "motion_guard_channel";
//    private static final int NOTIFICATION_ID = 1;

    private TextView timerText;
    private CountDownTimer countDownTimer;


    private void performServiceStartActions() {
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

        settings = getSharedPreferences(Config.SHAREFILE_NAME, MODE_PRIVATE);

        vibrationFlag = settings.getBoolean(Config.VIBRATION, true);
        soundFlag = settings.getBoolean(Config.SOUND, true);
        notificationFlag = settings.getBoolean(Config.NOTIFICATION, true);

        setContentView(R.layout.activity_lock);


        setupWindow();

        initViews();
        showMathProblem();


        // 初始化倒计时
        startCountdown(AUTO_CLOSE_DELAY);


        performServiceStartActions();


        // 初始化自动关闭计时器
//        setupAutoCloseTimer();

        recordLock(true);

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
        Window window = getWindow();

        // 设置基本窗口标志
        window.setFlags(LOCK_FLAGS, LOCK_FLAGS);

        // MIUI 特定处理
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        );

//        // 确保在锁屏上显示
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setShowWhenLocked(true);
//            setTurnScreenOn(true);
//        }

        // This is important - set this BEFORE setContentView
        // 显示 设置其透明，以及显示壁纸。
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);


        // 禁止状态栏下拉
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

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
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();

        // 禁用最近任务
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

        // 每次回到前台时重置计时器
//        startAutoCloseTimer();
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
            answerInput.requestFocus();
        } else {
            // 如果失去焦点，立即重新获取
            moveTaskToFront();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller != null) {
                // 隐藏所有系统栏
                controller.hide(
                        WindowInsets.Type.statusBars() |
                                WindowInsets.Type.navigationBars() |
                                WindowInsets.Type.systemBars()
                );
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            // 对于 Android R 以下版本使用旧的 API
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_IMMERSIVE;

            decorView.setSystemUiVisibility(flags);
        }
    }

    @Override
    public void onBackPressed() {
        // 禁用返回键
        return;
    }




    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 处理系统按键
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_RECENT_APPS:
            case KeyEvent.KEYCODE_APP_SWITCH:
            case KeyEvent.KEYCODE_MENU:
                // 拦截系统导航按键
                return true;
            default:
                // 允许其他所有按键通过（包括数字键、删除键等）
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onUserLeaveHint() {
        // 用户尝试离开应用时（如按 HOME 键）立即返回前台
        moveTaskToFront();
    }

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
        recordLock(false);
        Intent serviceIntent = new Intent(this, WalkDetectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        finish();
    }

//    private void setupAutoCloseTimer() {
//        autoCloseHandler = new Handler(Looper.getMainLooper());
//        autoCloseRunnable = () -> {
//            LogManager.d(TAG, "Auto closing LockScreenActivity after 10 minutes, it completed!");
//            finishActivity();
//        };
//        // 启动计时器
//        startAutoCloseTimer();
//    }

//    private void startAutoCloseTimer() {
//
//        Log.d(TAG, "startAutoCloseTimer: ");
//        // 移除之前的计时器（如果有）
//        stopAutoCloseTimer();
//        // 设置新的计时器
//        autoCloseHandler.postDelayed(autoCloseRunnable, AUTO_CLOSE_DELAY);
//    }

//    private void stopAutoCloseTimer() {
//        if (autoCloseHandler != null && autoCloseRunnable != null) {
//            autoCloseHandler.removeCallbacks(autoCloseRunnable);
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停时停止计时器
//        stopAutoCloseTimer();
    }

    @Override
    protected void onDestroy() {
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