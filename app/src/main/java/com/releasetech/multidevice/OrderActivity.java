package com.releasetech.multidevice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;

import com.releasetech.multidevice.Manager.PasswordManager;
import com.releasetech.multidevice.Tool.Utils;

import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends AppCompatActivity {

    private static final String TAG = "[ORDER]";

    /* Password Related */
    private int settingsCount = 0;
    private PasswordManager passwordManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Button hiddenButton = findViewById(R.id.setting_button);
        hiddenButton.setOnClickListener(view -> {
            settingsCount++;
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

        numberClick();

        Button button = findViewById(R.id.button);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(OrderActivity.this, DischargeActivity.class);
            startActivity(intent);
        });
    }

    public void numberClick() {
        Button buttonNumber[] = new Button[10];
        buttonNumber[0] = findViewById(R.id.button0);
        buttonNumber[1] = findViewById(R.id.button1);
        buttonNumber[2] = findViewById(R.id.button2);
        buttonNumber[3] = findViewById(R.id.button3);
        buttonNumber[4] = findViewById(R.id.button4);
        buttonNumber[5] = findViewById(R.id.button5);
        buttonNumber[6] = findViewById(R.id.button6);
        buttonNumber[7] = findViewById(R.id.button7);
        buttonNumber[8] = findViewById(R.id.button8);
        buttonNumber[9] = findViewById(R.id.button9);
        Button buttonBack = findViewById(R.id.buttonBack);
        Button buttonAdd = findViewById(R.id.buttonAdd);

        StringBuilder number = new StringBuilder();
        EditText numberText = findViewById(R.id.numberText);

        for (int i = 0; i < 10; i++) {
            buttonNumber[i].setOnClickListener(view -> {
                Button btn1 = (Button) view;
                if (number.length() == 0 && btn1.getText().toString().equals("0")) {
                    return;
                } else if (number.length() >= 2) {
                    return;
                }
                number.append(btn1.getText().toString());
            });
        }
        buttonBack.setOnClickListener(view -> {
            String text = number.toString();
            if (text.length() > 0) number.deleteCharAt(text.length() - 1);
        });
        buttonAdd.setOnClickListener(view -> {
            numberText.setText(number);
            number.setLength(0);
        });
    }
}