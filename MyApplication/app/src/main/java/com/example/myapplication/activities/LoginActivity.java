package com.example.myapplication.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private static final String PREF_NAME = "CarRentalPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UserDAO
//        DatabaseHelper dbHelper = new DatabaseHelper(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        userDAO = new UserDAO(dbHelper);

        // Initialize views
        initViews();
        // Set up click listeners
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        // Find and set click listeners for forgot password and register
        View tvForgotPassword = findViewById(R.id.tv_forgot_password);
        View tvRegister = findViewById(R.id.tv_register);

        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        tvRegister.setOnClickListener(v -> handleRegister());
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(username)) {
            etUsername.setError(getString(R.string.error_username_required));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_password_required));
            return;
        }

        // Attempt authentication
        User user = userDAO.authenticateUser(username, password);

        if (user != null) {
            if (user.getStatus() == 1) { // Assuming 1 means active status
                // Save login state
                saveLoginState(user.getId());

                // Start main activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, R.string.error_account_inactive, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_LONG).show();
        }
    }

    private void saveLoginState(int userId) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    private void handleForgotPassword() {
        // Launch forgot password activity
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void handleRegister() {
        // Launch registration activity
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}