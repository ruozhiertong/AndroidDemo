package com.example.myapplication.activities;

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
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputLayout tilUser;
    private TextInputEditText etUser;
    private TextInputLayout tilPass;
    private TextInputEditText etPass;
    private MaterialButton btnResetPassword;
    private View progressBar;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize UserDAO
//        DatabaseHelper dbHelper = new DatabaseHelper(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);

        // Initialize views
        initViews();
        setupListeners();

//        // Setup toolbar
//        setSupportActionBar(findViewById(R.id.toolbar));
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle(R.string.forgot_password_title);
//        }
    }

    private void initViews() {
        tilUser = findViewById(R.id.til_username);
        etUser = findViewById(R.id.et_username);
        tilPass = findViewById(R.id.til_pass);
        etPass = findViewById(R.id.et_pass);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnResetPassword.setOnClickListener(v -> handlePasswordReset());
    }

    private void handlePasswordReset() {
        String username = etUser.getText().toString().trim();

        String pass = etPass.getText().toString().trim();


        // Reset error
        tilUser.setError(null);
        tilPass.setError(null);


        // Validate email
        if (TextUtils.isEmpty(username)) {
            tilUser.setError(getString(R.string.error_field_required));
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            tilPass.setError(getString(R.string.error_field_required));
            return;
        }

        User user = userDAO.getUser(username);

        if (user == null) {
            tilUser.setError(getString(R.string.error_empty_user));
            return;
        }


        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnResetPassword.setEnabled(false);

        // TODO: Implement actual password reset logic here
        // For now, we'll just simulate a delay and show a success message
        btnResetPassword.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnResetPassword.setEnabled(true);

            user.setPassword(pass);

            int ret = userDAO.updateUser(user);

            if (ret != 0) {
                // Show success message
                Toast.makeText(ForgotPasswordActivity.this,
                        R.string.password_reset_success,
                        Toast.LENGTH_LONG).show();

                // Close activity
                finish();
            } else {
                Toast.makeText(ForgotPasswordActivity.this,
                        R.string.password_reset_fail,
                        Toast.LENGTH_LONG).show();
            }

        }, 2000);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}