package com.example.myapplication.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 3000; // 3 seconds
    private static final String PREF_NAME = "CarRentalPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Check if user is logged in
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

            // Determine which activity to start
            Intent intent;
            if (isLoggedIn) {
//                intent = new Intent(SplashActivity.this, MainActivity.class);
                Log.d(TAG, "go to main");
                intent = new Intent(SplashActivity.this, MainActivity.class);

            } else {
                Log.d(TAG, "go to login");
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish(); // Close splash activity
        }, SPLASH_DELAY);
    }
}