package com.releasetech.multidevice;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Manager.PasswordManager;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.Tool.Utils;

public class MainActivity extends AppCompatActivity {

    public static final String DEVTAG = "DEV";
    private static final String TAG = "[MENU]";

    /* Password Related */
    private int settingsCount = 0;
    private PasswordManager passwordManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.hideNavBar(getWindow());

        Button triggerButton = findViewById(R.id.trigger_button);
        View clickInterceptor = findViewById(R.id.click_interceptor);
        triggerButton.setText(PreferenceManager.getString(getApplicationContext(), "message_idle"));
        String idleColorString = PreferenceManager.getString(getApplicationContext(), "message_color");

        try {
            if (idleColorString != null && !idleColorString.trim().isEmpty()) {
                triggerButton.setTextColor(Color.parseColor(idleColorString));
            } else {
                triggerButton.setTextColor(Color.parseColor("#000000")); // 기본 검정색
            }
        } catch (IllegalArgumentException e) {
            // 색상 파싱 에러가 발생한 경우 기본 색상 설정
            triggerButton.setTextColor(Color.parseColor("#000000"));
        }

        triggerButton.setOnClickListener(v -> {
            if (clickInterceptor.getVisibility() == View.VISIBLE) {
            } else {
                Intent intent = new Intent(MainActivity.this, OrderActivity.class);
                startActivity(intent);
            }
        });

        Button hiddenButton = findViewById(R.id.setting_button);
        hiddenButton.setOnClickListener(view -> {
            settingsCount++;
            clickInterceptor.setVisibility(View.VISIBLE);
            Utils.logD(TAG, "설정 진입 버튼 : " + settingsCount);
            if (settingsCount == 10) {
                //if (passwordManager.wrongPasswordCount >= 10) return;
                passwordManager.passwordDialog(this);
                settingsCount = 0;
            } else if (settingsCount == 100) {
                passwordManager.resetPasswords();
                settingsCount = 0;
            } else if (settingsCount > 90) {
                Utils.showToast(this, "패스워드 초기화까지 남은 횟수 : " + (100 - settingsCount));
            }
        });

        passwordManager = new PasswordManager(this);
    }
}