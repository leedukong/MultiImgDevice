package com.releasetech.multidevice.Manager;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.releasetech.multidevice.Tool.Cache;
import com.releasetech.multidevice.Tool.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class CheckoutManager {
    private static final String TAG = "[CHECKOUT]";

    public static final String CHECKOUT_APPROVE = "D1";
    public static final String CHECKOUT_REFUND = "D4";
    public static final String CHECKOUT_ROLLBACK = "RB";
    public static final String CHECKOUT_LAST_RESULT = "LA";

    public static void checkout(Activity activity, int price) {
        if (Objects.equals(PreferenceManager.getString(activity, "tran_no"), "")) {
            PreferenceManager.setString(activity, "tran_no", "1");
        }
        int tranNo = Integer.parseInt(PreferenceManager.getString(activity, "tran_no"));
        tranNo++;
        Utils.logD(TAG, "결제 번호 : " + tranNo);

        ComponentName compName = new ComponentName("kr.co.kicc.easycarda", "kr.co.kicc.easycarda.CallPopup");

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra("TRAN_NO", Integer.toString(tranNo));
        intent.putExtra("TRAN_TYPE", CHECKOUT_APPROVE);
        intent.putExtra("TERMINAL_TYPE", "40");
        intent.putExtra("TOTAL_AMOUNT", Integer.toString(price));
        intent.putExtra("TAX", "0");
        intent.putExtra("TAX_OPTION", "A");
        intent.putExtra("TIP", "0");
        intent.putExtra("TIMEOUT", "30");
        intent.putExtra("INSTALLMENT", "0");

        intent.putExtra("TEXT_PROCESS", "결제 진행중입니다");
        intent.putExtra("TEXT_COMPLETE", "결제가 완료되었습니다");

        intent.setComponent(compName);
        activity.startActivityForResult(intent, 1);

        PreferenceManager.setString(activity, "tran_no", Integer.toString(tranNo));
    }

    public static void cancelPayment(Activity activity, String approvalNum, String approvalDate, String price) {
        ComponentName compName = new ComponentName("kr.co.kicc.easycarda", "kr.co.kicc.easycarda.CallPopup");

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra("TRAN_TYPE", CHECKOUT_REFUND);
        intent.putExtra("TERMINAL_TYPE", "40");
        intent.putExtra("TOTAL_AMOUNT", price);
        intent.putExtra("TAX", "0");
        intent.putExtra("TAX_OPTION", "A");
        intent.putExtra("TIP", "0");
        intent.putExtra("TIMEOUT", "30");
        intent.putExtra("INSTALLMENT", "0");
        intent.putExtra("APPROVAL_NUM", approvalNum);
        intent.putExtra("APPROVAL_DATE", approvalDate);
        intent.putExtra("TEXT_PROCESS", "결제 취소중입니다");
        intent.putExtra("TEXT_COMPLETE", "결제가 취소되었습니다");

        intent.setComponent(compName);
        activity.startActivityForResult(intent, 1);
    }

    public static void rollBackPayment(Activity activity) {
        CheckoutCache checkoutCache = null;
        try {
            checkoutCache = (CheckoutCache) Cache.Read(activity, "checkout");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (checkoutCache == null) {
            Utils.logD(TAG, "결제 정보를 찾을 수 없어 결제를 취소할 수 없음");
            return;
        }
        Utils.logD(TAG, "취소할 결제 번호 : " + checkoutCache.tranSerialNo);
        ComponentName compName = new ComponentName("kr.co.kicc.easycarda", "kr.co.kicc.easycarda.CallPopup");

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra("TRAN_TYPE", CHECKOUT_REFUND);
        intent.putExtra("TERMINAL_TYPE", "40");
        intent.putExtra("TOTAL_AMOUNT", checkoutCache.totalAmount);
        intent.putExtra("TAX", "0");
        intent.putExtra("TAX_OPTION", "A");
        intent.putExtra("TIP", "0");
        intent.putExtra("TIMEOUT", "30");
        intent.putExtra("INSTALLMENT", "0");
        intent.putExtra("APPROVAL_NUM", checkoutCache.approvalNum);
        intent.putExtra("APPROVAL_DATE", checkoutCache.approvalDate.substring(0, 6));
        intent.putExtra("TRAN_SERIALNO", checkoutCache.tranSerialNo);

        intent.setComponent(compName);
        activity.startActivityForResult(intent, 1);
    }

    public static void openSettings(Activity activity) {

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
