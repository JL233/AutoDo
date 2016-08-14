package com.jl.autodo.service;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.jl.autodo.Constants.ConstantsFund;
import com.jl.autodo.MyApplication;
import com.jl.autodo.common.BaseAccessibility;

import java.util.List;

/**
 * Created by JL on 2016/8/11.
 */
public class BuyFund extends BaseAccessibility {
    private static final String TAG = "BuyFund";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        if (isScreenLocked()) {
            unLockScreen();
        }
        Log.d(TAG, event.getPackageName().toString());
        switch (eventType) {
            //opening a PopupWindow, Menu, Dialog, etc.包括通知栏点击后跳转
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                Log.i(TAG, className);
                if (MyApplication.map.isEmpty()) {
                    break;
                } else if (className.equals(ConstantsFund.TAB_ACTIVITY)) {
                    click(ConstantsFund.NEXTTIME_BTN_ID);
                    if (!click(ConstantsFund.SEARCH_BTN_ID_1)) {
                        click(ConstantsFund.SEARCH_BTN_ID_2);
                    }
                } else if (className.equals(ConstantsFund.SEARCH_ACTIVITY)) {
                    input(ConstantsFund.SEARCH_EDIT_ID, MyApplication.map.get("code"));
                    try {
                        AccessibilityNodeInfo fundsNode = findNodesById(ConstantsFund.SEARCH_RESULTS_ID).get(0);
                        while (!click(fundsNode.getChild(0))) {
                            fundsNode = findNodesById(ConstantsFund.SEARCH_RESULTS_ID).get(0);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(TAG, MyApplication.map.get("code") + "不合法", e);
                        MyApplication.map.clear();
                    }
                } else if (className.equals(ConstantsFund.FUND_ACTIVITY)) {
                    List<AccessibilityNodeInfo> nodes = findNodesById(ConstantsFund.BUYNEXT_BTN_ID);
                    if (!nodes.isEmpty()) {
                        click(nodes.get(0));
                    }
                } else if (className.equals(ConstantsFund.TRADE_ACTIVITY)) {
                    //如果是购买成功后的完成界面，则点击完成返回，并且清除基金
//                    if (click(ConstantsFund.SUCCESS_BTN_ID)) {
                    //如果是输入密码购买界面而不是购买完成界面
                    if (input(ConstantsFund.PASSWORD_EDIT_ID, "Gemj3617")) {
                        click(ConstantsFund.BUYCONFIRM_BTN_ID);
                        MyApplication.map.clear();
                        back2Home();
                        //release();
                    }

                } else if (className.equals("android.app.Dialog")) {
                    input(ConstantsFund.PAYAMOUNT_EDIT_ID, MyApplication.map.get("money"));
                    click(ConstantsFund.PAYNEXT_BTN_ID);
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.e(TAG, "onServiceConnected");

    }
}
