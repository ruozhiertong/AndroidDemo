package activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplefitness.R;

import database.DatabaseHelper;
import model.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText nicknameEdit, accountEdit, passwordEdit, confirmPasswordEdit;
    private Button registerButton;
    private TextView loginTextLink;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

//        dbHelper = new DatabaseHelper(this);
        dbHelper = DatabaseHelper.getInstance(this);
        initView();
    }

    private void initView() {
        nicknameEdit = findViewById(R.id.edit_nickname);
        accountEdit = findViewById(R.id.edit_account);
        passwordEdit = findViewById(R.id.edit_password);
        confirmPasswordEdit = findViewById(R.id.edit_confirm_password);
        registerButton = findViewById(R.id.btn_register);
        loginTextLink = findViewById(R.id.text_login_link);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 执行注册逻辑
                performRegistration();
            }
        });

        loginTextLink.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //返回登录
                finish();
            }
        });
    }

    private void performRegistration() {
        // 获取输入的信息
        String nickname = nicknameEdit.getText().toString().trim();
        String account = accountEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String confirmPassword = confirmPasswordEdit.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(nickname)) {
            nicknameEdit.setError("昵称不能为空");
            return;
        }

        if (TextUtils.isEmpty(account)) {
            accountEdit.setError("账号不能为空");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEdit.setError("密码不能为空");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEdit.setError("两次密码输入不一致");
            return;
        }

        // 检查账号是否已存在（这里简化处理，实际应用需要更复杂的验证）
        if (isAccountExists(account)) {
            Toast.makeText(this, "该账号已存在", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建用户
        User newUser = new User(nickname, account, password);
        // 设置默认值
        newUser.setWeight(0.0);
        newUser.setHeight(0.0);
        newUser.setAge(0);
        newUser.setGoal("lose_weight");

        // 添加用户到数据库
        dbHelper.addUser(newUser);

        // 注册成功
        Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();

        // 返回登录界面
        finish();
    }

    // 检查账号是否已存在
    private boolean isAccountExists(String account) {
        // 这里应该调用数据库方法检查账号是否存在
        // 为简化示例，这里直接返回false
        // 实际应用中应该在DatabaseHelper中添加检查账号是否存在的方法
        return false;
    }
}