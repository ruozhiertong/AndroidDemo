package com.xxgl.lhz.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.xxgl.lhz.database.DatabaseHelper;
import com.xxgl.lhz.models.User;
import com.example.myapplication.R;
import com.xxgl.lhz.dao.UserDAO;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREF_NAME = "CarRentalPrefs";
    private static final String KEY_USER_ID = "userId";

    private TextInputLayout tilRealName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPhone;
    private TextInputLayout tilCurrentPassword;
    private TextInputLayout tilNewPassword;
    private TextInputLayout tilConfirmPassword;
    private EditText etRealName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnSaveProfile;
    private Button btnChangePassword;

    private UserDAO userDAO;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize database helper and DAO
//        DatabaseHelper dbHelper = new DatabaseHelper(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);

        initializeViews();
        loadUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        tilRealName = findViewById(R.id.til_real_name);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        tilCurrentPassword = findViewById(R.id.til_current_password);
        tilNewPassword = findViewById(R.id.til_new_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);

        etRealName = findViewById(R.id.et_real_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);

        if (userId != -1) {
            currentUser = userDAO.getUser(userId);
            System.out.println("loadUserData :" + currentUser.getPassword());
            if (currentUser != null) {
                updateUI();
            }
        }
    }

    private void updateUI() {
        etRealName.setText(currentUser.getRealName());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhone());
    }

    private void setupClickListeners() {
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void saveProfileChanges() {
        String realName = etRealName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate inputs
        if (!validateProfileInputs(realName, email, phone)) {
            return;
        }

        // Update user object
        currentUser.setRealName(realName);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);

        // Save to database
        int result = userDAO.updateUser(currentUser);
        if (result > 0) {
            loadUserData();
            Toast.makeText(this, R.string.profile_update_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.profile_update_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateProfileInputs(String realName, String email, String phone) {
        boolean isValid = true;

        if (TextUtils.isEmpty(realName)) {
            tilRealName.setError(getString(R.string.error_field_required));
            isValid = false;
        } else {
            tilRealName.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_field_required));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError(getString(R.string.error_field_required));
            isValid = false;
        } else if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            tilPhone.setError(getString(R.string.error_invalid_phone));
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        return isValid;
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        System.out.println("changePassword :" + currentPassword + "," + newPassword);
        // Validate password inputs
        if (!validatePasswordInputs(currentPassword, newPassword, confirmPassword)) {
            return;
        }

        // Verify current password
        if (!currentPassword.equals(currentUser.getPassword())) {
            tilCurrentPassword.setError(getString(R.string.error_incorrect_password));
            return;
        }

        // Update password
        currentUser.setPassword(newPassword);
        int result = userDAO.updateUser(currentUser);

        if (result > 0) {
            loadUserData();
            clearPasswordFields();
            Toast.makeText(this, R.string.password_change_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.password_change_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword, String confirmPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(currentPassword)) {
            tilCurrentPassword.setError(getString(R.string.error_field_required));
            isValid = false;
        } else {
            tilCurrentPassword.setError(null);
        }

        if (TextUtils.isEmpty(newPassword)) {
            tilNewPassword.setError(getString(R.string.error_field_required));
            isValid = false;
        } else if (newPassword.length() < 6) {
            tilNewPassword.setError(getString(R.string.error_password_too_short));
            isValid = false;
        } else {
            tilNewPassword.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_field_required));
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        return isValid;
    }

    private void clearPasswordFields() {
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
        tilCurrentPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            System.out.println("back home");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}