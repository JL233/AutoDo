package com.jl.autodo;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by JL on 2016/8/12.
 */
public class MyApplication extends Application {
    public static Map<String, String> map = new HashMap<String, String>();

    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }
}
