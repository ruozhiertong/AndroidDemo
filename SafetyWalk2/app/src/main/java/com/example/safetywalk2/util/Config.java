package com.example.safetywalk2.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Config {


    public static final String SHAREFILE_NAME = "settings";

    public static final String LOG_ENBALED = "log_enabled";
    public static final String LAUNCH_ICON = "launch_icon";
    public static final String NOTIFICATION = "notification";
    public static final String SENSITIVITY = "sensitivity";
    public static final String SOUND = "sound";
    public static final String VIBRATION = "vibration";
//    public static final String SERVICE_STATUS = "service_state";
    public static final String LOCK_STATUS = "lock_status";
    public static final String SELECTED_THEME = "selected_theme";


    public static final String INTRO_SHOWN = "intro_shown";

    public static final String AD_SHOWN = "ad_shown";



    public static final String LOCK_CHANNEL_ID = "lock_screen_channel";
    public static final String SERVICE_CHANNEL_ID = "walk_detection_service_channel";

    public static final int SERVICE_NOTIFICATION_ID = 1;
    public static final int LOCK_NOTIFICATION_ID = 2;


    public static final Set<String> WHITE_LIST_PACKAGES = new HashSet<>(Arrays.asList(
            "com.autonavi.minimap",        // 高德地图
            "com.baidu.BaiduMap",          // 百度地图
            "com.tencent.map",             // 腾讯地图
            "com.google.android.apps.maps", // Google Maps
            "com.android.browser"
            // 添加其他白名单应用
    ));






}
