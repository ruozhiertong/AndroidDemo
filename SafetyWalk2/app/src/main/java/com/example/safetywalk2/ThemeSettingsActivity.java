package com.example.safetywalk2;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.view.ContextThemeWrapper;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class ThemeSettingsActivity extends AppCompatActivity {

    private RadioGroup themeRadioGroup;
    private RadioButton themeBlue, themeDark, themeGreen, themePurple;
    private Button applyButton;
    private ImageButton backButton;

    private LinearLayout lnPreview;
    private ImageView previewIcon;
    private TextView tvPreview;
    private Switch swPreview;
    private SeekBar sbPreview;
    private Button btnPreviw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply current theme before setting content view
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_settings);

        // Initialize views
        initializeViews();
        
        // Set current theme selection
        setCurrentThemeSelection();
        
        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        themeRadioGroup = findViewById(R.id.theme_radio_group);
        themeBlue = findViewById(R.id.theme_blue);
        themeDark = findViewById(R.id.theme_dark);
        themeGreen = findViewById(R.id.theme_green);
        themePurple = findViewById(R.id.theme_purple);
        applyButton = findViewById(R.id.apply_theme_button);
        backButton = findViewById(R.id.back_button);
        lnPreview = findViewById(R.id.preview_lnout);
        previewIcon = findViewById(R.id.preview_icon);
        btnPreviw = findViewById(R.id.preview_button);
        tvPreview = findViewById(R.id.tv_setting);
        swPreview = findViewById(R.id.preview_switch);
        sbPreview = findViewById(R.id.preview_seekbar);
    }

    private void setCurrentThemeSelection() {
        int currentTheme = ThemeManager.getTheme(this);
        switch (currentTheme) {
            case ThemeManager.THEME_BLUE:
                themeBlue.setChecked(true);
                updatePreviewTheme(R.style.Theme_MotionGuard_Blue);
                break;
            case ThemeManager.THEME_DARK:
                themeDark.setChecked(true);
                updatePreviewTheme(R.style.Theme_MotionGuard_Dark);
                break;
            case ThemeManager.THEME_GREEN:
                themeGreen.setChecked(true);
                updatePreviewTheme(R.style.Theme_MotionGuard_Green);
                break;
            case ThemeManager.THEME_PURPLE:
                themePurple.setChecked(true);
                updatePreviewTheme(R.style.Theme_MotionGuard_Purple);
                break;
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        // 设置RadioGroup的监听器，实现实时预览
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.theme_blue) {
                updatePreviewTheme(R.style.Theme_MotionGuard_Blue);
            } else if (checkedId == R.id.theme_dark) {
                updatePreviewTheme(R.style.Theme_MotionGuard_Dark);
            } else if (checkedId == R.id.theme_green) {
                updatePreviewTheme(R.style.Theme_MotionGuard_Green);
            } else if (checkedId == R.id.theme_purple) {
                updatePreviewTheme(R.style.Theme_MotionGuard_Purple);
            }
        });

        applyButton.setOnClickListener(v -> {
            int selectedTheme;
            int checkedId = themeRadioGroup.getCheckedRadioButtonId();
            
            if (checkedId == R.id.theme_blue) {
                selectedTheme = ThemeManager.THEME_BLUE;
            } else if (checkedId == R.id.theme_dark) {
                selectedTheme = ThemeManager.THEME_DARK;
            } else if (checkedId == R.id.theme_green) {
                selectedTheme = ThemeManager.THEME_GREEN;
            } else if (checkedId == R.id.theme_purple) {
                selectedTheme = ThemeManager.THEME_PURPLE;
            } else {
                return;
            }

            ThemeManager.setTheme(this, selectedTheme);
            Toast.makeText(this, "主题已更新", Toast.LENGTH_SHORT).show();
            
            // Restart activity to apply new theme
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updatePreviewTheme(int themeResId) {
        Context themedContext = new ContextThemeWrapper(this, themeResId);
        
        try {
            // 获取主题颜色
            int colorPrimary = getThemeColor(themedContext, android.R.attr.colorPrimary);
            int colorSecondary = getThemeColor(themedContext, android.R.attr.colorSecondary);
            int colorBackground = getThemeColor(themedContext, android.R.attr.colorBackground);
            int textColorPrimary = getThemeColor(themedContext, android.R.attr.textColorPrimary);
            // int colorOnPrimary = getThemeColor(themedContext, com.google.android.material.R.attr.colorOnPrimary);
            // int thumbTint = getThemeColor(themedContext, com.google.android.material.R.attr.thumbTint);
            // int trackTint = getThemeColor(themedContext, com.google.android.material.R.attr.trackTint);
            // int sbThumbTint = getThemeColor(themedContext, android.R.attr.thumbTint);
            // int progressTint = getThemeColor(themedContext, android.R.attr.progressTint);
            int colorOnPrimary = getThemeColor(themedContext, com.google.android.material.R.attr.colorOnPrimary);


            // 更新基础UI元素
            lnPreview.setBackgroundColor(colorBackground);
            previewIcon.setColorFilter(colorPrimary);
            tvPreview.setTextColor(textColorPrimary);
            btnPreviw.setBackgroundColor(colorPrimary);
            btnPreviw.setTextColor(colorOnPrimary);

            // 为Switch创建状态颜色列表
            int[][] switchStates = new int[][] {
                new int[] {android.R.attr.state_checked},
                new int[] {}
            };
            int[] switchTrackColors = new int[] {
                colorPrimary,
                colorSecondary
            };
            int[] switchThumbColors = new int[] {
                colorPrimary,
                colorSecondary
            };
            
            // 设置Switch的颜色
            swPreview.setTrackTintList(new ColorStateList(switchStates, switchTrackColors));
            swPreview.setThumbTintList(new ColorStateList(switchStates, switchThumbColors));
            swPreview.setTextColor(textColorPrimary);
            //swPreview.setTrackTintList(ColorStateList.valueOf(trackTint));
            //swPreview.setThumbTintList(ColorStateList.valueOf(thumbTint));


            // sbPreview.setProgressTintList(ColorStateList.valueOf(progressTint));
            // sbPreview.setThumbTintList(ColorStateList.valueOf(sbThumbTint));


            // btnPreviw.setBackgroundColor(colorPrimary);
            // btnPreviw.setTextColor(colorOnPrimary);

            // 为SeekBar创建颜色状态列表
            ColorStateList seekBarColors = ColorStateList.valueOf(colorPrimary);
            
            // 设置SeekBar的颜色
            sbPreview.setProgressTintList(seekBarColors);
            sbPreview.setThumbTintList(seekBarColors);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getThemeColor(Context context, int attr) {
        try {
            android.util.TypedValue typedValue = new android.util.TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(attr, typedValue, true);
            return typedValue.data;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}