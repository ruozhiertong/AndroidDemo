package com.example.safetywalk2.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.safetywalk2.R;

public class AdActivity extends AppCompatActivity {

    private static final int AD_DURATION = 3000; // 5秒
    private Handler handler = new Handler();
    private Runnable runnable;


    private String adTargetUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        // 获取广告数据
        adTargetUrl = getIntent().getStringExtra("ad_target_url");
        if (adTargetUrl == null) {
            SharedPreferences prefs = getSharedPreferences("ad_prefs", MODE_PRIVATE);
            adTargetUrl = prefs.getString("ad_target_url", null);
        }


        // 加载广告图片
        ImageView adImage = findViewById(R.id.adImage);
        if (adTargetUrl != null) {
            String imageUrl = getIntent().getStringExtra("ad_image_url");
            if (imageUrl == null) {
                SharedPreferences prefs = getSharedPreferences("ad_prefs", MODE_PRIVATE);
                imageUrl = prefs.getString("ad_url", null);
            }
            if (imageUrl != null) {
//                Glide.with(this).load(imageUrl).into(adImage);
            } else {
                adImage.setImageResource(R.drawable.ic_help); // 替换为你的广告图片
            }
        }


        // 广告点击事件
        adImage.setOnClickListener(v -> {
            if (adTargetUrl != null) {
                handleAdClick(adTargetUrl);
            }
        });

        // 延迟跳转到主界面
        runnable = () -> {
            startActivity(new Intent(AdActivity.this, MainActivity.class));
            finish();
        };
        handler.postDelayed(runnable, AD_DURATION);

        // 跳过按钮点击事件
        Button btnSkip = findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(v -> {
            handler.removeCallbacks(runnable); // 移除延迟任务
            startActivity(new Intent(AdActivity.this, MainActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // 防止内存泄漏
    }

    /**
     * 处理广告点击事件
     *
     * @param targetUrl 广告目标 URL
     */
    private void handleAdClick(String targetUrl) {
        if (targetUrl.startsWith("http")) {
            // 如果是一个网页链接，跳转到浏览器
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
            startActivity(intent);
        } else if (targetUrl.startsWith("app://")) {
            // 如果是 App 内部链接，跳转到指定界面
            if (targetUrl.equals("app://product_detail")) {
                Intent intent = new Intent(this, ProductDetailActivity.class);
                startActivity(intent);
            } else if (targetUrl.equals("app://settings")) {
//                Intent intent = new Intent(this, SettingsActivity.class);
//                startActivity(intent);
            }
        }
        finish(); // 结束 AdActivity
    }
}