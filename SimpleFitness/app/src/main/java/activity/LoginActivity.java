package activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplefitness.R;

import database.DatabaseHelper;
import model.User;

public class LoginActivity extends AppCompatActivity {
    private EditText accountEdit, passwordEdit;
    private Button loginButton, registerButton;
    private DatabaseHelper dbHelper;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        initView();
    }

    private void initView() {
        accountEdit = findViewById(R.id.edit_account);
        passwordEdit = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.btn_login);
        registerButton = findViewById(R.id.btn_register);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                User user = dbHelper.getUser(account, password);
                if (user != null) {
                    saveUserToPreferences(user);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "登录失败，请检查账号密码", Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void saveUserToPreferences(User user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("nickname", user.getNickname());
        editor.putString("account", user.getAccount());
        editor.putString("password", user.getPassword());
        editor.putFloat("weight", (float) user.getWeight());
        editor.putFloat("height", (float) user.getHeight());
        editor.putInt("age", user.getAge());
        editor.putString("goal", user.getGoal());
        editor.apply();
    }
}
