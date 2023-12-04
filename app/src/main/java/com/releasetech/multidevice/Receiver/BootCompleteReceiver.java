package com.releasetech.multidevice.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.releasetech.multidevice.MainActivity;
import com.releasetech.multidevice.Manager.PreferenceManager;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            startActivity(context);
        }
    }

    static void startActivity(Context context) {
        PreferenceManager.setBoolean(context, "rebooted", true);
        Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startForegroundService(i);
    }

}