package com.example.safetywalk2.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.safetywalk2.R;
import com.example.safetywalk2.util.Config;

public class SplashActivity extends AppCompatActivity {

    // 广告展示时间（单位：毫秒）
    private static final int SPLASH_DURATION = 300; //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 延迟跳转到主界面或广告页
        new Handler().postDelayed(() -> {
            // 检查是否有广告需要展示. 广告比较影响体验。
            if (hasAdToShow()) {
                startActivity(new Intent(SplashActivity.this, AdActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish(); // 结束 SplashActivity
        }, SPLASH_DURATION);
    }

    // 模拟检查是否有广告
    private boolean hasAdToShow() {
        // 这里可以替换为实际的广告检查逻辑
//        return true; // 默认返回 true，表示有广告

        SharedPreferences prefs = getSharedPreferences(Config.SHAREFILE_NAME, MODE_PRIVATE);

        return prefs.getBoolean(Config.AD_SHOWN, true);

    }


    private void fetchAdData() {
        // 模拟从服务器获取广告数据
        String adUrl = "https://example.com/ad_image.jpg";
        String targetUrl = "app://product_detail";

        // 缓存广告数据
        SharedPreferences prefs = getSharedPreferences("ad_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("ad_url", adUrl)
                .putString("ad_target_url", targetUrl)
                .apply();
    }
}
