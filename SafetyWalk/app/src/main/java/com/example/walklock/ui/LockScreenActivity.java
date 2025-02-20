package com.example.walklock.ui;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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

import com.example.safetywalk.R;
import com.example.walklock.service.WalkDetectionService;
import com.example.walklock.util.LogManager;
import com.example.walklock.util.MathProblemGenerator;

import java.util.List;

public class LockScreenActivity extends AppCompatActivity {
    private static final String TAG = "LockScreenActivity";
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
    private Handler autoCloseHandler;
    private Runnable autoCloseRunnable;

    private SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogManager.d(TAG, "onCreate: Starting LockScreenActivity");
        super.onCreate(savedInstanceState);
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "需要悬浮窗权限", Toast.LENGTH_SHORT).show();
            return;
        }

        // 确保在锁屏上显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        
        // MIUI 特定处理
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        );
        
        setupWindow();
        setContentView(R.layout.activity_lock_screen);
        initViews();
        showMathProblem();

        // 初始化自动关闭计时器
        setupAutoCloseTimer();

        recordLock(true);

    }



    private void recordLock(boolean lock) {

        LogManager.d(TAG, "recordLock:" + lock);
        if (settings == null)
            settings = getSharedPreferences("app_settings", 0);

        // 记录锁屏
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("is_lock", lock);
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
                Toast.makeText(this, "答案错误，请重试", Toast.LENGTH_SHORT).show();
                answerInput.setText("");
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initViews() {
        problemText = findViewById(R.id.problemText);
        answerInput = findViewById(R.id.answerInput);
        Button submitButton = findViewById(R.id.submitButton);
        
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

    private void setupAutoCloseTimer() {
        autoCloseHandler = new Handler(Looper.getMainLooper());
        autoCloseRunnable = () -> {
            LogManager.d(TAG, "Auto closing LockScreenActivity after 10 minutes, it completed!");
            finishActivity();
        };
        // 启动计时器
        startAutoCloseTimer();
    }

    private void startAutoCloseTimer() {

        Log.d(TAG, "startAutoCloseTimer: ");
        // 移除之前的计时器（如果有）
        stopAutoCloseTimer();
        // 设置新的计时器
        autoCloseHandler.postDelayed(autoCloseRunnable, AUTO_CLOSE_DELAY);
    }

    private void stopAutoCloseTimer() {
        if (autoCloseHandler != null && autoCloseRunnable != null) {
            autoCloseHandler.removeCallbacks(autoCloseRunnable);
        }
    }

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
        stopAutoCloseTimer();
        autoCloseHandler = null;
        autoCloseRunnable = null;
    }
} 