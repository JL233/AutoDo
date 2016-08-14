package com.jl.autodo.common;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.jl.autodo.Constants.ConstantsWX;
import com.jl.autodo.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 辅助操作APP的基类，包括打开通知、点击控件
 * Created by JL on 2016/8/11.
 */
public abstract class BaseAccessibility extends AccessibilityService {
    private String Tag = "BaseAccessibility";


    protected void openNotiFication(AccessibilityEvent event) {
        List<CharSequence> texts = event.getText();
        if (!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String content = text.toString();
                //模拟打开通知栏消息
                if (event.getParcelableData() != null
                        &&
                        event.getParcelableData() instanceof Notification) {
                    Notification notification = (Notification) event.getParcelableData();
                    PendingIntent pendingIntent = notification.contentIntent;
                    try {
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
                //视红包而定，不是红包直接返回
                if (content.contains(getResources().getString(R.string.RED_PACKET))) {
                    //sendMore();
                } else {
                    click(ConstantsWX.CHAT_BACK);
                }
            }
        }
    }

    protected List<AccessibilityNodeInfo> findNodesById(String id, int time) {
        List<AccessibilityNodeInfo> nodesInfos = new ArrayList<AccessibilityNodeInfo>();
        //获取开始时间
        long startTime = System.currentTimeMillis();
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            nodesInfos = root.findAccessibilityNodeInfosByViewId(id);
            while (nodesInfos.size() == 0 && System.currentTimeMillis() - startTime < time) {
                nodesInfos = root.findAccessibilityNodeInfosByViewId(id);
            }
        }
        Log.d(Tag, "等待了" + (System.currentTimeMillis() - startTime));
        Log.i(Tag, "id=" + id + "size=" + nodesInfos.size());
        return nodesInfos;
    }

    protected boolean input(String id, String content) {
        List<AccessibilityNodeInfo> nodesInfos = findNodesById(id);
        if (nodesInfos.size() == 0) {
            return false;
        } else {
            return input(nodesInfos.get(0), content, true);
        }
    }

    protected List<AccessibilityNodeInfo> findNodesById(String id) {
        return findNodesById(id, 0);
    }

    protected boolean input(AccessibilityNodeInfo node, String content, boolean clearText) {
        try {
            if (node.isEnabled() && node.getClassName().equals("android.widget.EditText")) {
                //清除旧文本
                if (clearText) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                            AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                    arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                            true);
                    node.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                            arguments);
                }
                //先获取焦点，然后才能粘贴
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("content", content);
                clipboard.setPrimaryClip(clip);
                node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            }
            return true;
        } catch (Exception e) {
            Log.e(Tag, "", e);
            return false;
        }

    }


    //点击匹配id的节点的第一个，建议当元素只有一个的时候使用
    protected boolean click(String id) {
        List<AccessibilityNodeInfo> nodesInfos = findNodesById(id);
        if (nodesInfos.size() == 0) {
            return false;
        } else {
            return click(nodesInfos.get(0));
        }
    }

    //如果传入的节点不能点击，就遍历节点树挨个点击
    protected boolean click(AccessibilityNodeInfo node) {
        if (node == null) {
            Log.e(Tag, "节点为空");
            return false;
        }
        try {
            if (node.isEnabled() && node.isClickable()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } else {
                arround(node);
            }
            return true;
        } catch (NullPointerException e) {
            Log.e(Tag, "", e);
            return false;
        }

    }

    //根据id和序号进行点击
    protected void click(String id, int index) {
        List<AccessibilityNodeInfo> nodesInfos = findNodesById(id);
        try {
            AccessibilityNodeInfo node = nodesInfos.get(index);
            click(node);
        } catch (Exception e) {
            Log.e(Tag, e.getMessage());
        }
    }

    /**
     * 先找到传入节点的最小子孙节点，再从小到大挨个点击父节点
     *
     * @param info
     */
    public void arround(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            //enable是能点击点必要条件，clickable不是
            if (info.isEnabled()) {
                //这里有一个问题需要注意，就是需要找到一个可以点击的View
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                AccessibilityNodeInfo parent = info.getParent();
                while (parent != null) {
                    if (parent.isEnabled()) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        break;
                    }
                    parent = parent.getParent();
                }
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    arround(info.getChild(i));
                }
            }
        }
    }

    /**
     * 回到系统桌面
     */
    protected void back2Home() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }


    /**
     * 系统是否在锁屏状态
     *
     * @return
     */
    protected boolean isScreenLocked() {
        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
        return flag;
    }

    protected void unLockScreen() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        if (isScreenLocked()) {
            kl = km.newKeyguardLock("unLock");
            //解锁
            kl.disableKeyguard();
        }

        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        //PowerManager.FULL_WAKE_LOCK 这个参数是手机点亮的程度
        //PowerManager.ACQUIRE_CAUSES_WAKEUP使WalkLock不再依赖组件就可以点亮屏幕了
        PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Light");
        //点亮屏幕
        mWakelock.acquire();
    }

    public void lockScreen() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("lock");
        // release screen
        if (!km.inKeyguardRestrictedInputMode()) {
            // 锁键盘
            kl.reenableKeyguard();
        }
        // 使屏幕休眠
        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        //PowerManager.FULL_WAKE_LOCK 这个参数是手机点亮的程度
        //PowerManager.ACQUIRE_CAUSES_WAKEUP使WalkLock不再依赖组件就可以点亮屏幕了
        PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Light");
        if (mWakelock.isHeld()) {
            mWakelock.release();
        }
    }

    /**
     * 模拟back按键
     */
    private void pressBackButton() {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //允许锁屏
    private void releaseAndLock() {

        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
        //得到键盘锁管理器对象
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //允许锁屏
        kl.reenableKeyguard();
    }

}
