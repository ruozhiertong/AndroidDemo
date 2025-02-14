//package com.example.walklock.service;
//
//import android.app.job.JobParameters;
//import android.app.job.JobService;
//import android.content.Intent;
//import android.os.Build;
//
//import com.example.walklock.util.SystemUtil;
//
//public class WalkJobService extends JobService {
//    @Override
//    public boolean onStartJob(JobParameters params) {
//        // 检查并启动主服务
//        if (!SystemUtil.isServiceRunning(this, WalkDetectionService.class)) {
//            Intent intent = new Intent(this, WalkDetectionService.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(intent);
//            } else {
//                startService(intent);
//            }
//        }
//        return false; // 任务已完成
//    }
//
//    @Override
//    public boolean onStopJob(JobParameters params) {
//        return true; // 重新调度任务
//    }
//}