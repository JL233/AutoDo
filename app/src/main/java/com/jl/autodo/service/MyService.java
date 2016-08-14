package com.jl.autodo.service;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.jl.autodo.Constants.ConstantsFund;
import com.jl.autodo.MyApplication;
import com.jl.autodo.common.BaseAccessibility;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyService extends BaseAccessibility {

    private static String TAG = "MyService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        Log.d(TAG, event.getPackageName().toString());

        switch (eventType) {
            //监听通知栏消息
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                if (isScreenLocked()) {
                    unLockScreen();
                }
                List<CharSequence> texts = event.getText();
                if (texts.isEmpty()) {
                    return;
                }
                String content = "";
                for (CharSequence text : texts) {
                    content = text.toString();
                    Pattern pa = Pattern.compile("([0-9]{6}\\+[0-9.]*)");
                    Matcher mathcher = pa.matcher(content);
//                    if(mathcher.matches()){
                    if (mathcher.find()) {
                        String result = mathcher.group(1);
                        String[] ss = result.split("\\+");
                        if (ss.length == 2) {
                            MyApplication.map.put("code", ss[0]);
                            MyApplication.map.put("money", ss[1]);
                            PackageManager packageManager = getPackageManager();
                            Intent intent = new Intent();
                            intent = packageManager.getLaunchIntentForPackage(ConstantsFund.PACKAGE_NAME);
                            if (intent == null) {
                                System.out.println("APP not found!");
                            } else {
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }
}
