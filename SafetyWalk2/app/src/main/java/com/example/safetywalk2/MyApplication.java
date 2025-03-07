package com.example.safetywalk2;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import android.content.res.Configuration;

import com.example.safetywalk2.util.Config;
import com.example.safetywalk2.util.CustomExceptionHandler;
import com.example.safetywalk2.util.LogManager;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));

        Log.d(TAG, "onCreate: Application 初始化" + android.os.Process.myPid());
        // 初始化日志管理器
        LogManager.init(this);
        //设置日志打印级别。
//        LogManager.setLevel(1);

        SharedPreferences settings = getSharedPreferences(Config.SHAREFILE_NAME, Context.MODE_PRIVATE);
        boolean ifWrite2file = settings.getBoolean(Config.LOG_ENBALED, true);
        LogManager.setIfWrite2file(ifWrite2file);

        // 初始化全局资源（如数据库、分析工具等）
        initThirdPartyLibs();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: 配置发生变化");
        // 处理配置变化（如语言切换）
        handleConfigurationChange(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "onLowMemory: 内存不足，释放非关键资源");
        releaseNonCriticalResources();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.w(TAG, "onTrimMemory: 内存回收通知，Level=" + level);
        // 根据 level 释放不同级别的资源
        handleTrimMemory(level);
    }

    // 以下为模拟方法
    private void initThirdPartyLibs() {
        // 例如：Firebase、数据库初始化
    }

    private void handleConfigurationChange(Configuration newConfig) {
        // 例如：更新语言资源
    }

    private void releaseNonCriticalResources() {
        // 例如：释放缓存
    }

    private void handleTrimMemory(int level) {
        // 例如：释放不同级别的资源
    }
}

