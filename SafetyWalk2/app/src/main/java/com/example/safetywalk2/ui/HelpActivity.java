package com.example.safetywalk2.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.safetywalk2.R;
import com.example.safetywalk2.util.ThemeManager;

public class HelpActivity extends AppCompatActivity {

    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this); // 应用当前主题
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> finish());
        
        // 初始化视图
        TextView helpText = findViewById(R.id.help_text);
        helpText.setText(getHelpContent());
    }

    private String getHelpContent() {
        return "欢迎使用 MotionGuard！\n\n" +

                "初衷：\n" +
                "\t现在生活越来越离不开手机，在路上总是看到时时刻刻盯着手机的行人。边走边看的不良习惯不仅会给眼睛带来伤害，也会带来很多安全隐患。为了解决这个隐患，开发了这个小工具。\n\n" +

                "如何使用：\n" +
                "\t安装后，授予权限，打开服务开关。就没了。只需首次安装后打开一次就行。\n\n" +

                "功能：\n" +
                "\t1. 运动检测：检测运动状态。\n" +
                "\t2. 锁屏保护：当检测到处于行走或跑步等运动状态时，会进入锁屏状态。\n" +
                "\t3. 锁屏状态：在锁屏状态下，无法进行其他的操作，也无法强力退出。\n" +
                "\t4. 隐藏桌面图标： 为避免一时脑热卸载应用。\n" +
                "TIPS：工具只是工具，不能代替一切。一切意志的都在于您。 卸载 以及 强力停止应用，自然就不会再保护您了。  \n\n" +

                "权限说明：\n" +
                "\t1. 运动健康权限：用于检测您的运动状态。\n" +
                "\t2. 悬浮窗权限：用于应用可以在其他应用的窗口弹出显示。\n" +
                "\t3. 后台弹出界面权限：当应用不在前台时，依然可以弹出界面。\n" +
                "\t4. 锁屏显示权限：当手机处于锁屏时，依然可以弹出界面。避免通过系统锁屏来绕开弹出锁屏保护。\n" +
                "\t5. 自启动权限：确保应用在后台正常运行。在应用被杀死或关机后，可以自启动\n" +
                "\t6. 通知权限：用于通知服务的状态。\n" +
                "\t7. 省电策略：无限制。 为了避免省电策略对传感器的影响，建议修改成无限制。\n" +
                "\t8. 使用情况访问权限： 为检测当前前台应用是否是地图类应用，如果是，那么会阻止进入锁屏保护状态。\n" +
                "小米手机的以上权限，可以通过 长按桌面的应用图标 -> 应用信息 进行设置。\n" +
                "请确保您已授予这些权限，以便应用正常工作。这些权限使用情况全部在本地，不会有联网的功能，不会泄漏您的任何隐私，请放心。\n\n";
    }
} 