package com.example.walklock.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogManager {
    private static final String TAG = "LogManager";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    private static final String LOG_FILE_PREFIX = "walklock_log_";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    private static File logFile;
    private static Context applicationContext;

    //是否写入file
    private static boolean ifWrite2file = false;
    //设置写入的level.  e > w > i > d.  打印等级 >= level， 才打印输出。
    private static int level = 1; // e 4, w 3, i 2, d 1,


    public static void init(Context context) {
        Log.d(TAG, "init: ");
        applicationContext = context.getApplicationContext();
        level = 1;
        ifWrite2file = false;

//        createNewLogFile();

//        try {
//            File logDir = new File(applicationContext.getExternalFilesDir(null), "logs");
//            if (!logDir.exists()) {
//                boolean created = logDir.mkdirs();
//                Log.d(TAG, "init: create log dir: " + created);
//            }
//
//            // 创建新的日志文件
//            // 每天的日志
//            String dailyLog = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
//            logFile = new File(logDir, LOG_FILE_PREFIX + dailyLog+ ".txt");
//            if (!logFile.exists()) {
//                boolean created = logFile.createNewFile();
//                Log.d(TAG, "init: create log file: " + created);
//            }
//
//            Log.d(TAG, "init: logDir path: " + logDir.getAbsolutePath());
//            Log.d(TAG, "init: logFile path: " + logFile.getAbsolutePath());
//            Log.d(TAG, "init: logDir exists: " + logDir.exists());
//            Log.d(TAG, "init: logFile exists: " + logFile.exists());
//            Log.d(TAG, "init: logFile can write: " + logFile.canWrite());
//        } catch (Exception e) {
//            Log.e(TAG, "Error initializing log file", e);
//        }
    }


    public static void setLevel(int lev) {
        level = lev;
    }

    public static void setIfWrite2file (boolean flag) {
        ifWrite2file = flag;
    }

    public static boolean getIfWrite2file() {
        return ifWrite2file;
    }


    private static void createNewLogFile() {

        if (!ifWrite2file)
            return;

        if (applicationContext == null) return;

        try {
            File logDir = new File(applicationContext.getExternalFilesDir(null), "logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            // 创建新的日志文件，使用时间戳作为文件名
            // 每天的日志。
            String dailyLog = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
            logFile = new File(logDir, LOG_FILE_PREFIX + dailyLog + ".txt");

            if (!logFile.exists()) {
                boolean created = logFile.createNewFile();
                Log.d(TAG, "createNewLogFile: create log file: " + created);
            }

            Log.d(TAG, "createNewLogFile: logDir path: " + logDir.getAbsolutePath());
            Log.d(TAG, "createNewLogFile: logFile path: " + logFile.getAbsolutePath());
            Log.d(TAG, "createNewLogFile: logDir exists: " + logDir.exists());
            Log.d(TAG, "createNewLogFile: logFile exists: " + logFile.exists());
            Log.d(TAG, "createNewLogFile: logFile can write: " + logFile.canWrite());


            // 删除旧的日志文件
//            cleanOldLogs(logDir);
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating log file", e);
        }
    }

    private static void cleanOldLogs(File logDir) {
        File[] files = logDir.listFiles((dir, name) -> name.startsWith(LOG_FILE_PREFIX));
        if (files != null && files.length > 7) { // 保留最新的7个日志文件. 一周。
            // 按最后修改时间排序
            java.util.Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            // 删除旧文件
            for (int i = 5; i < files.length; i++) {
                files[i].delete();
            }
        }
    }

    public static void d(String tag, String message) {
        if (1 < level)
            return;
        Log.d(tag, message);
        writeToFile("D", tag, message);
    }

    public static void i(String tag, String message) {
        if (2 < level)
            return;
        Log.i(tag, message);
        writeToFile("I", tag, message);
    }

    public static void w(String tag, String message) {
        if (3 < level)
            return;
        Log.w(tag, message);
        writeToFile("W", tag, message);
    }

    public static void e(String tag, String message) {
        if (4 < level)
            return;
        Log.e(tag, message);
        writeToFile("E", tag, message);
    }

    public static void e(String tag, String message, Throwable tr) {
        if (4 < level)
            return;
        Log.e(tag, message, tr);
        writeToFile("E", tag, message + "\n" + Log.getStackTraceString(tr));
    }

    private static synchronized void writeToFile(String level, String tag, String message) {

        if (!ifWrite2file)
            return;

        if (logFile == null || !logFile.exists()) {
            Log.d(TAG, "writeToFile: logFile == null || !logFile.exists()");
            createNewLogFile();
        }

        if (logFile == null) return;

        // 检查文件大小
        if (logFile.length() > MAX_FILE_SIZE) {
            createNewLogFile();
        }

        try (FileWriter writer = new FileWriter(logFile, true)) {
            String timestamp = DATE_FORMAT.format(new Date());
            String logLine = String.format("%s %s/%s: %s\n", timestamp, level, tag, message);
            writer.append(logLine);
        } catch (IOException e) {
            Log.e(TAG, "Error writing to log file", e);
        }
    }

    public static File[] getLogFiles() {
        if (applicationContext == null) return new File[0];
        
        File logDir = new File(applicationContext.getExternalFilesDir(null), "logs");
        if (!logDir.exists()) return new File[0];

        return logDir.listFiles((dir, name) -> name.startsWith(LOG_FILE_PREFIX));
    }

    public static void clearLogs() {
        File[] files = getLogFiles();
        for (File file : files) {
            file.delete();
        }
//        createNewLogFile();
    }
} 