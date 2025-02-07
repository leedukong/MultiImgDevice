package com.releasetech.multidevice.Manager;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.releasetech.multidevice.Tool.Cache;
import com.releasetech.multidevice.Tool.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class CheckoutManager {
    private static final String TAG = "[CHECKOUT]";

    private static final int SEND_REQUEST_CODE = 1;
    final int SEND_REQUEST_CHKVALID = 2;
    final int SEND_REQUEST_CHKCARDBIN = 3;
    final int SEND_REQUEST_CHKCASHIC = 4;
    final int SEND_REQUEST_CHKMEMBERSHIP = 5;
    final int SEND_REQUEST_NORMAL = 6;
    private static final char fs = 0x1C;

    String strRecv01, strRecv02, strRecv03, strRecv04, strRecv05, strRecv06, strRecv07, strRecv08, strRecv09, strRecv10, strRecv11, strRecv12, strRecv13, strRecv14, strRecv15, strRecv16, strRecv17, strRecv18, strRecv19, strRecv20, strRecv21, strRecv22, strRecv23, strRecv24, strRecv25, strRecv26, strRecv27, strRecv28, strRecv29, strRecv30;

    private static void send(Activity activity, String senddata) {
        Intent sendIntent = new Intent();
        sendIntent.setAction("NICEVCAT"); //setAction에 함수명
        sendIntent.putExtra("NVCATSENDDATA", senddata); //NVCATSENDDATA에 요청전문
        sendIntent.setType("text/plain"); //setType은 text/plain 필수
        activity.startActivityForResult(sendIntent, SEND_REQUEST_CODE);
    }

    public static void checkout(Activity activity, int price) {
        String sendData;
        sendData = "0200" + fs + "10" + fs + "I" + fs + price + fs + "91" + fs + "0" + fs + "00" + fs + fs + fs + PreferenceManager.getString(activity, "prev_nice_checkout_approval_CATID") + fs + fs + fs + fs + "0" + fs + fs + fs + "" + fs + "" + fs + fs + "" + fs + "" + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
//        sendData = "0200" + fs + "10" + fs + "I" + fs + price + fs + "91" + fs + "0" + fs + "00" + fs + fs + fs + "2393300001" + fs + fs + fs + fs + "0" + fs + fs + fs + "" + fs + "" + fs + fs + "" + fs + "" + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        send(activity, sendData);
    }

    public static void cancel(Activity activity){
        String sendData = "";
        sendData = "0420" + fs + "10" + fs + "I" + fs + PreferenceManager.getInt(activity, "prev_nice_checkout_approval_price") + fs + "91" + fs + "0" + fs + "00" + fs + PreferenceManager.getString(activity, "prev_nice_checkout_approval_no") + fs + PreferenceManager.getString(activity, "prev_nice_checkout_approval_date") + fs + PreferenceManager.getString(activity, "prev_nice_checkout_approval_CATID") + fs + fs + fs + fs + "" + fs + fs + fs + "" + fs + "" + fs + fs + "" + fs + "" + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        send(activity, sendData);
    }

    public static void saveCheckoutCache(Context context, Intent data) {
        CheckoutCache cache = new CheckoutCache(
                data.getStringExtra("TOTAL_AMOUNT"),
                data.getStringExtra("APPROVAL_NUM"),
                data.getStringExtra("APPROVAL_DATE"),
                data.getStringExtra("TRAN_SERIALNO"));
        try {
            Cache.write(context, "checkout", cache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class CheckoutCache implements Serializable {
        String totalAmount;
        String approvalNum;
        String approvalDate;
        String tranSerialNo;

        public CheckoutCache(String totalAmount, String approvalNum, String approvalDate, String tranSerialNo) {
            this.totalAmount = totalAmount;
            this.approvalNum = approvalNum;
            this.approvalDate = approvalDate;
            this.tranSerialNo = tranSerialNo;
        }
    }

}

