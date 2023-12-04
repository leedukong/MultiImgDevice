package com.releasetech.multidevice.Client;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.releasetech.multidevice.Tool.Utils;

public class ClientService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    /* todo 다시 열기

    private static final String TAG = "[CLIENT]";

    private static boolean isStarted = false;
    private static ClientThread clientThread = null;

    IBinder mBinder = new ClientBinder();


    public ClientService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        if (isStarted) return mBinder;
        isStarted = true;
        try {
            clientThread = new ClientThread(this);
            clientThread.start();
        } catch (RuntimeException e) {
            Utils.logE(TAG, e.getMessage());
        }
        Utils.logD(TAG, "Client Service start");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class ClientBinder extends Binder {
        public ClientService getService() { // 서비스 객체를 리턴
            return ClientService.this;
        }
    }*/
}
