package com.releasetech.multidevice.Manager;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserManager;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.releasetech.multidevice.MainActivity;
import com.releasetech.multidevice.Receiver.AdminReceiver;
import com.releasetech.multidevice.Tool.Utils;

import java.io.IOException;

public class UIManager {
    private static final String TAG = "[UI Manager]";
    private static DevicePolicyManager devicePolicyManager;
    private static ComponentName adminComponentName;

    private static int hide_flags =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

    private static int show_flags =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

    public static void hideSystemUI(AppCompatActivity activity) {
//        if (!IS_DEBUG) {
        String device_name = Settings.Global.getString(activity.getContentResolver(), "device_name");
        if (device_name.equals("eightpresso_basic_b")) {
            Utils.logD(TAG, "2023.07.13 이후 기기");
            //send a broadcast "com.outform.hidebar" to the system to make the statusbar disappear
            Intent intent = new Intent("com.outform.hidebar");
            activity.sendBroadcast(intent);
        } else {
            setDefaultMyKioskPolicies(activity, true);
            if (Settings.System.canWrite(activity)) {
                try {
                    Runtime.getRuntime().exec("su -c settings put system hide_navbar 1");
                    Runtime.getRuntime().exec("su -c settings put system swipe_show_nav_bar 0");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//        }
        activity.getSupportActionBar().hide();
        activity.getWindow().getDecorView().setSystemUiVisibility(hide_flags);
        activity.getWindow().
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    public static void showSystemUI(AppCompatActivity activity) {
//        if (!IS_DEBUG) {
        String device_name = Settings.Global.getString(activity.getContentResolver(), "device_name");
        if (device_name.equals("eightpresso_basic_b")) {
            Utils.logD(TAG, "2023.07.13 이후 기기");
            //send a broadcast "com.outform.unhidebar" to the system to make the statusbar disappear
            Intent intent = new Intent("com.outform.unhidebar");
            activity.sendBroadcast(intent);
        } else {
            setDefaultMyKioskPolicies(activity, false);
            if (Settings.System.canWrite(activity)) {
                try {
                    Runtime.getRuntime().exec("su -c settings put system hide_navbar 0");
                    Runtime.getRuntime().exec("su -c settings put system swipe_show_nav_bar 1");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//        }
        activity.getSupportActionBar().show();
        activity.getWindow().getDecorView().setSystemUiVisibility(show_flags);
    }


    private static void setDefaultMyKioskPolicies(AppCompatActivity activity, boolean active) {
        devicePolicyManager = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponentName = AdminReceiver.getComponentName(activity);

        // set user restrictions
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, false);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, false);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, false);
        // disable keyguard and status bar
        devicePolicyManager.setKeyguardDisabled(adminComponentName, active);
        devicePolicyManager.setStatusBarDisabled(adminComponentName, active);

//        2023.10.10 기존 모니터에서 '화면 고정됨' 팝업이 뜨고, 매니저 설정에 들어가지지 않아서 주석처리, 화면은 정상적으로 고정됨.
//        try {
//            if (active) {
//                activity.startLockTask();
//            } else {
//                activity.stopLockTask();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (active) {
            // set KIOSK activity as home intent receiver so that it is started
            // on reboot
            devicePolicyManager.addPersistentPreferredActivity(
                    adminComponentName, intentFilter, new ComponentName(
                            activity.getPackageName(), MainActivity.class.getName()));
        } else {
            devicePolicyManager.clearPackagePersistentPreferredActivities(
                    adminComponentName, activity.getPackageName());
        }
    }

    private static void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            devicePolicyManager.addUserRestriction(adminComponentName,
                    restriction);
        } else {
            devicePolicyManager.clearUserRestriction(adminComponentName,
                    restriction);
        }
    }
}