package com.example.safetywalk2.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.safetywalk2.R;
import com.example.safetywalk2.util.Config;
import com.example.safetywalk2.util.LogManager;
import com.example.safetywalk2.util.ThemeManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class LogActivity extends AppCompatActivity {

    private static final String TAG = "LogActivity";

    private SharedPreferences preferences;
    private SwitchMaterial logSwitch;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 应用当前主题
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        initializeViews();
        loadSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        logSwitch = findViewById(R.id.log_switch);
        backButton = findViewById(R.id.back_button);
        preferences = getSharedPreferences(Config.SHAREFILE_NAME, MODE_PRIVATE);
    }

    private void loadSettings() {
        // 加载日志开关状态
        boolean logEnabled = preferences.getBoolean(Config.LOG_ENBALED, true);
        logSwitch.setChecked(logEnabled);
    }

    private void setupClickListeners() {
        // 返回按钮
        backButton.setOnClickListener(v -> finish());

        // 日志开关
        logSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            LogManager.setIfWrite2file(isChecked);
            preferences.edit().putBoolean(Config.LOG_ENBALED, isChecked).apply();
            Toast.makeText(this, 
                isChecked ? "日志记录已启用" : "日志记录已禁用", 
                Toast.LENGTH_SHORT).show();
        });

        // 查看日志按钮
        findViewById(R.id.viewlog_button).setOnClickListener(v -> showLogDialog());
    }

    private void showLogDialog() {
        File[] logFiles = LogManager.getLogFiles();
        if (logFiles == null || logFiles.length == 0) {
            Toast.makeText(this, "没有日志文件", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建日志文件列表，添加时间信息
        String[] fileNames = new String[logFiles.length];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        for (int i = 0; i < logFiles.length; i++) {
            String date = sdf.format(new Date(logFiles[i].lastModified()));
            fileNames[i] = date + " - " + logFiles[i].getName();
        }

        // 按时间降序排序
        Arrays.sort(logFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        Arrays.sort(fileNames, Collections.reverseOrder());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("日志文件")
                .setItems(fileNames, (d, which) -> showLogContent(logFiles[which]))
                .setPositiveButton("分享全部", (d, which) -> shareAllLogs(logFiles))
                .setNeutralButton("清除日志", (d, which) -> {
                    new AlertDialog.Builder(this)
                            .setTitle("确认清除")
                            .setMessage("确定要清除所有日志吗？")
                            .setPositiveButton("确定", (innerDialog, innerWhich) -> {
                                LogManager.clearLogs();
                                Toast.makeText(this, "日志已清除", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                })
                .setNegativeButton("关闭", null)
                .create();

        dialog.show();
    }

    private void showLogContent(File logFile) {
        try {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_log_content, null);
            TextView logContentView = dialogView.findViewById(R.id.logContentView);
            logContentView.setText(content.toString());

            new AlertDialog.Builder(this)
                    .setTitle(logFile.getName())
                    .setView(dialogView)
                    .setPositiveButton("分享", (dialog, which) -> shareLogFile(logFile))
                    .setNegativeButton("关闭", null)
                    .show();
        } catch (IOException e) {
            Toast.makeText(this, "无法读取日志文件", Toast.LENGTH_SHORT).show();
            LogManager.e(TAG, "Error reading log file", e);
        }
    }

    private void shareLogFile(File logFile) {
        try {
            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    logFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "应用日志");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "分享日志文件"));
        } catch (Exception e) {
            Toast.makeText(this, "分享日志失败", Toast.LENGTH_SHORT).show();
            LogManager.e(TAG, "Error sharing log file", e);
        }
    }

    private void shareAllLogs(File[] logFiles) {
        try {
            ArrayList<Uri> uris = new ArrayList<>();
            for (File file : logFiles) {
                Uri uri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        file);
                uris.add(uri);
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "应用日志");
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "分享所有日志文件"));
        } catch (Exception e) {
            Toast.makeText(this, "分享日志失败", Toast.LENGTH_SHORT).show();
            LogManager.e(TAG, "Error sharing all log files", e);
        }
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }
}
