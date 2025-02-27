package com.example.safetywalk2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate and setContentView
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupMainScreen();
    }

    private void setupMainScreen() {
        // Setup theme settings click listener
        LinearLayout themeSettings = findViewById(R.id.theme_setting);
        themeSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ThemeSettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setupVerificationScreen() {
        // This method would set up the verification screen as shown in Image 2
    }
}