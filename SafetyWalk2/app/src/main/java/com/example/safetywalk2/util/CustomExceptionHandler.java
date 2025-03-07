package com.example.safetywalk2.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler defaultUEH;
    private Context context;

    public CustomExceptionHandler(Context context) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String stackTrace = Log.getStackTraceString(e);
        writeToFile(stackTrace, "crash_log.txt");
        defaultUEH.uncaughtException(t, e);
    }

    private void writeToFile(String stackTrace, String fileName) {
        //context.getFilesDir()：返回的是应用的内部存储目录路径，通常类似于 /data/data/your.package.name/files。
        //内部存储和外部存储。
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(context.getFilesDir() + "/" + fileName))) {
            writer.write(stackTrace);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
