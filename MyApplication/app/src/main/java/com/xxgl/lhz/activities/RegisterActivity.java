// RegisterActivity.java
package com.xxgl.lhz.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.xxgl.lhz.dao.UserDAO;
import com.xxgl.lhz.database.DatabaseHelper;
import com.xxgl.lhz.models.User;
import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputLayout tilRealName;
    private TextInputLayout tilPhone;
    private TextInputLayout tilEmail;
    private AutoCompleteTextView autoCompleteUserType;
    private String role;

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private TextInputEditText etRealName;
    private TextInputEditText etPhone;
    private TextInputEditText etEmail;

    private MaterialButton btnRegister;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
//            getSupportActionBar().setTitle(R.string.register_title);
//        }
    }

    private void initViews() {
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        tilRealName = findViewById(R.id.til_real_name);
        tilPhone = findViewById(R.id.til_phone);
        tilEmail = findViewById(R.id.til_email);
        // 初始化控件
        autoCompleteUserType = findViewById(R.id.spinner_user_type);

        // 获取数据
        String[] userTypes = getResources().getStringArray(R.array.user_types);

        // 创建适配器. 自定义。
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this,
//                R.layout.dropdown_menu_popup_item,  // 使用Material Design的下拉项布局
//                userTypes
//        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,  // 使用系统内置的下拉项布局
                userTypes
        );

        // 设置适配器
        autoCompleteUserType.setAdapter(adapter);

        // 设置默认值（可选）
        autoCompleteUserType.setText(userTypes[0], false);
        role = "admin";

        // 监听选择事件
        autoCompleteUserType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            if (position == 0)
                role = "admin";
            else
                role = "user";
//            Toast.makeText(this, "选中: " + selectedItem + ", 位置: " + position, Toast.LENGTH_SHORT).show();
        });


        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etRealName = findViewById(R.id.et_real_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);

        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        // Reset errors
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilRealName.setError(null);
        tilPhone.setError(null);
        tilEmail.setError(null);

        // Get input values
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String realName = etRealName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

//        String selectedUserType = spinnerUserType.getText().toString();



        // Validate input
        if (!validateInput(username, password, confirmPassword, realName, phone, email)) {
            return;
        }

        // Check if username exists
        if (userDAO.isUsernameExists(username)) {
            tilUsername.setError(getString(R.string.error_username_exists));
            return;
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
//        user.setRole("user"); // Default role
        user.setRole(role);
        user.setStatus(1); // Active status

        long userId = userDAO.createUser(user);

        if (userId > 0) {
            Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String username, String password, String confirmPassword,
                                  String realName, String phone, String email) {
        boolean isValid = true;

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError(getString(R.string.error_field_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_field_required));
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_too_short));
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            isValid = false;
        }

        if (TextUtils.isEmpty(realName)) {
            tilRealName.setError(getString(R.string.error_field_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError(getString(R.string.error_field_required));
            isValid = false;
        } else if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            tilPhone.setError(getString(R.string.error_invalid_phone));
            isValid = false;
        }

        if (!TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        return isValid;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}