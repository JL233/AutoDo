package com.jl.autodo.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.jl.autodo.Constants.ConstantsFund;
import com.jl.autodo.Constants.ConstantsWX;
import com.jl.autodo.MyApplication;
import com.jl.autodo.common.BaseAccessibility;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListenWx extends BaseAccessibility {

    private static String TAG = "ListenWx";

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
                    Pattern pa = Pattern.compile("老大: ([0-9]{6}\\+[0-9.]*)");
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
            //opening a PopupWindow, Menu, Dialog, etc.包括通知栏点击后跳转
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:

                String className = event.getClassName().toString();
                if (className.equals("com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI")) {
                    if (!click(ConstantsWX.FRIEND_ACCEPT_BTN)) {
                        while (!click(ConstantsWX.FRIEND_SEND_MSG)) {

                        }
                    }
                } else if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    //sendMessage("此条消息为机器人自动发送...");
                    sendMore();
                    //simulateKeystroke(KeyEvent.KEYCODE_BACK);
                } else if (className.equals("com.tencent.mm.ui.base.p")) {
                    //开始打开红包
                    //openPacket();
                } else if (className.equals("com.tencent.mm.ui.chatting.ChattingUI")) {
                    //第一次加好友成功会进入这里
                    sendMessage("此条消息为机器人自动发送...");
                } else if (className.equals("com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI")) {

                } else if (className.equals("com.tencent.mm.plugin.gallery.ui.ImagePreviewUI")) {

                }
                break;
        }
    }

    private void sendMore() {
        //会话界面点击更多
        click(ConstantsWX.CHAT_MORE_BTN);
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(ConstantsWX.CHAT_MORE_PICTURE);
        for (int i = 0; i < list.size(); i++) {
            AccessibilityNodeInfo node = list.get(i);
            click(node);
            //点击图片就可以了
            break;
        }
        //勾选图片，checkbox
        selectPicture(new int[]{0, 1});
        //发送图片
        while (!click(ConstantsWX.PICTURE_SEND)) {

        }
    }

    private void selectPicture(int[] indexs) {
        for (int index : indexs) {
            click(ConstantsWX.PICTURE_CHECKBOX, index);
        }

    }


    /**
     * 查找到
     */
    @SuppressLint("NewApi")
    private void openPacket() {
        click("com.tencent.mm:id/b_b");
    }

    @SuppressLint("NewApi")
    private void getPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        click(rootNode);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void sendMessage(String message) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/yv");
        for (AccessibilityNodeInfo node : list) {
            if (node.isEnabled() && node.getClassName().equals("android.widget.EditText") || true) {
                //先获取焦点，然后才能粘贴
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("message", message);
                clipboard.setPrimaryClip(clip);
                node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                //发送消息
                click(ConstantsWX.CHAT_SEND);
            }
        }
    }

   @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.e(TAG, "onServiceConnected");

    }
    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt");
    }
}