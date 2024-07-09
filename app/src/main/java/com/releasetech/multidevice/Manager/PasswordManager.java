package com.releasetech.multidevice.Manager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.releasetech.multidevice.AdminSettings.AdminSettingsActivity;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;

public class PasswordManager {
    private static final String TAG = "[PASSWORD]";
    
    public int wrongPasswordCount = 0;

    private String managerPassword;
    private String adminPassword;
    private String dessertPassword;

    private Context context;
    
    
    public PasswordManager(Context context){
        this.context = context;
        loadPasswords();
    }


    public void resetPasswords() {
        managerPassword = context.getString(R.string.manager_password);
        PreferenceManager.setString(context, "manager_password", managerPassword);
        adminPassword = context.getString(R.string.admin_password);
        PreferenceManager.setString(context, "admin_password", adminPassword);
        dessertPassword = context.getString(R.string.dessert_password);
        PreferenceManager.setString(context, "dessert_password", dessertPassword);
        Utils.showToast(context, "패스워드가 " + managerPassword + "로 초기화 되었습니다.");
        wrongPasswordCount = 0;
    }

    private void loadPasswords() {
        managerPassword = PreferenceManager.getString(context, "manager_password");
        adminPassword = PreferenceManager.getString(context, "admin_password");
        dessertPassword = PreferenceManager.getString(context, "dessert_password");
        if (adminPassword.equals("") || managerPassword.equals("") || dessertPassword.equals("")) {
            resetPasswords();
        }
    }

    public void passwordDialog(AppCompatActivity activity) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout passwordLayout = (LinearLayout) vi.inflate(R.layout.dialog_password, null);
        final EditText pw = (EditText) passwordLayout.findViewById(R.id.pw);
        AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder(context).setTitle("패스워드를 입력하세요.").setView(passwordLayout).setPositiveButton("OK", (dialog,which) -> {
            String userEnteredPassword = pw.getText().toString();
            if (userEnteredPassword.equals(adminPassword)) {
                //UIManager.showSystemUI(activity);
                Intent intent = new Intent(context, AdminSettingsActivity.class);
                context.startActivity(intent);
                Utils.logD(TAG, "설정 페이지 진입");
            }
            else {
                wrongPasswordCount++;
                if (wrongPasswordCount == 10) {
                    Utils.showToast(context, "기기 재시작 후 다시 시도하세요." + wrongPasswordCount);
                } else {
                    Utils.showToast(context, "패스워드 오류 : " + wrongPasswordCount);
                }
            }
        });

        AlertDialog passwordDialog = passwordDialogBuilder.create();
        InputMethodManager mImm = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mImm.showSoftInput(pw, InputMethodManager.SHOW_IMPLICIT);

        passwordDialog.getWindow().
                setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        passwordDialog.show();

        passwordDialog.getWindow().
                clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        passwordDialog.setCanceledOnTouchOutside(false);
    }
}
