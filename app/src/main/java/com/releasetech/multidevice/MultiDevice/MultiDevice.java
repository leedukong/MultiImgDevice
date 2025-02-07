package com.releasetech.multidevice.MultiDevice;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.epton.sdk.callback.ADH812ResultListener;
import com.epton.sdk.callback.ADH812StateListener;
import com.epton.sdk.callback.DevicesStateListener;
import com.epton.sdk.callback.ResultCallBack;
import com.epton.sdk.port.PortController;
import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Database.Data.DessertItem;
import com.releasetech.multidevice.Database.Data.Product;
import com.releasetech.multidevice.Database.DataLoader;
import com.releasetech.multidevice.MainActivity;
import com.releasetech.multidevice.Manager.CheckoutManager;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.Sound.SoundService;
import com.releasetech.multidevice.ThrowOutActivity;
import com.releasetech.multidevice.Tool.Utils;

import java.lang.ref.WeakReference;
import java.util.Stack;

public class MultiDevice implements DevicesStateListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener, ADH812StateListener, ADH812ResultListener, RadioGroup.OnCheckedChangeListener {
    private static TestHandler handler;

    private static void openConnect(Context context){
        PortController.init(context, "/dev/ttyUSB", Integer.parseInt(PreferenceManager.getString(context, "connected_port")), 38400);
    }

    public static boolean locked = false;

    public static void throwOut(Context context, int number, OnThrowOutListener onThrowOutDoneListener) {
        if (locked) return;
        locked = true;
        int a = 10 * ((int) (number - 1) / 6);
        int b = (number - 1) % 6;
        int coordinate = a + b;
        onThrowOutDoneListener.onThrowOut(PreferenceManager.getString(context, "product_"+number+"_name"));
        //DBManager dbManager = new DBManager(context);
        openConnect(context);
        Log.i("작동 테스트", coordinate+"");
        PortController.outGoods(coordinate, new ResultCallBack() {
            @Override
            public void onSuccess(int i, int i1) {
                Log.i("작동 테스트", "8");
                Log.i("출하", "성공");
                //dbManager.updateColumnTodecreaseCount(product.id);
                if (onThrowOutDoneListener != null) onThrowOutDoneListener.onThrowOutDone();
                locked = false;
            }

            @Override
            public void onFailure(int i, String s, String s1) {
                Log.i("출하", "실패");
                Utils.restart(context);
                locked = false;
            }
        });
    }

    public static void throwOutNext(Context context, Stack stack, OnThrowOutListener onThrowOutDoneListener) {
        if (stack.isEmpty()) {
            locked = false;
            if (onThrowOutDoneListener != null) onThrowOutDoneListener.onThrowOutDone();
            return;
        }
//        DessertItem item = (DessertItem) stack.pop();
//        int number = item.number;
        int number = (int) stack.pop();
        int a = 10 * ((int) (number - 1) / 6);
        int b = (number - 1) % 6;
        int coordinate = a + b;
//        onThrowOutDoneListener.onThrowOut(item.productName);
        onThrowOutDoneListener.onThrowOut(PreferenceManager.getString(context, "product_"+number+"_name"));
        openConnect(context);

        PortController.outGoods(coordinate, new ResultCallBack() {
            @Override
            public void onSuccess(int i, int i1) {
                Log.i("출하", "성공");

//                SoundService.play(context, SoundService.DESSERT_OK);
                throwOutNext(context, stack, onThrowOutDoneListener);
            }

            @Override
            public void onFailure(int i, String s, String s1) {
                Log.i("출하", "실패");
                SoundService.play(context, SoundService.OUT_OF_ORDER);
                ThrowOutActivity throwOutActivity = (ThrowOutActivity) context;
                CheckoutManager.cancel(throwOutActivity);
                throwOutActivity.finish();
            }
        });
    }

    private static class TestHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        private TestHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            switch (msg.what) {
                case 10000:
                    Toast.makeText(activity, "adh815 출하 성공!", Toast.LENGTH_SHORT).show();
                    break;
                case 10001:
                    Toast.makeText(activity, "adh815 출하 실패!" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case 10002:
                    Toast.makeText(activity, "adh812 출하 성공!", Toast.LENGTH_SHORT).show();
                    break;
                case 10003:
                    Toast.makeText(activity, "adh812 출하 실패!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

    }

    @Override
    public void on812StateReturned(int var1) {

    }

    @Override
    public void onVersionReturn(boolean var1, String var2) {

    }

    @Override
    public void on812CoordinateReturned(int[] var1) {

    }

    @Override
    public void onADH812Disconnected() {

    }

    @Override
    public void onStateChanged(int[] var1, byte[] var2) {

    }

    @Override
    public void onModeChanged() {

    }

    @Override
    public void onDisconnected(int var1) {

    }

    @Override
    public void on812Success(int i) {

    }

    @Override
    public void on812Failure(int i) {

    }

    @Override
    public void on812Located() {

    }

    public interface OnThrowOutListener {
        void onThrowOut(String productName);

        void onThrowOutDone();
    }
}
