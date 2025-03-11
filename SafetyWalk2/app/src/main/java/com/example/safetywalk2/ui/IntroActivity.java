package com.example.safetywalk2.ui;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.safetywalk2.R;
import com.example.safetywalk2.ui.fragmet.IntroPageFragment;
import com.example.safetywalk2.util.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ArrayList<ImageView> dots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        final List<Map<String, Object>> pages = new ArrayList<>();

        Map<String, Object> page1 = new HashMap<>();
        page1.put("image", R.drawable.ic_vibration);
        page1.put("title", "功能特性 1");
        page1.put("desc", "这里是第一个主要功能的详细介绍");
        pages.add(page1);

        Map<String, Object> page2 = new HashMap<>();
        page2.put("image", R.drawable.ic_icon);
        page2.put("title", "功能特性 2");
        page2.put("desc", "这里是第二个核心功能的说明");
        pages.add(page2);

        Map<String, Object> page3 = new HashMap<>();
        page3.put("image", R.drawable.ic_notification);
        page3.put("title", "功能特性 3");
        page3.put("desc", "这里是第三个亮点的详细介绍");
        pages.add(page3);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Bundle args = new Bundle();
                args.putInt("image", (Integer) pages.get(position).get("image"));
                args.putString("title", (String) pages.get(position).get("title"));
                args.putString("desc", (String) pages.get(position).get("desc"));
                IntroPageFragment fragment = new IntroPageFragment();
                fragment.setArguments(args);
                return fragment;
            }

            @Override
            public int getItemCount() {
                return pages.size();
            }
        });

        setupDots(pages.size());

        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < pages.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                startMainActivity();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                btnNext.setText(position == pages.size() - 1 ? "开始使用" : "下一步");
            }
        });
    }

    private void setupDots(int count) {
        LinearLayout dotsContainer = findViewById(R.id.dotsContainer);
        dotsContainer.removeAllViews();

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(8), dpToPx(8));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);

            dots.add(dot);
            dotsContainer.addView(dot, params);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < dots.size(); i++) {
            dots.get(i).setImageResource(
                    i == position ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void startMainActivity() {
        SharedPreferences prefs = getSharedPreferences(Config.SHAREFILE_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(Config.INTRO_SHOWN, true).apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
