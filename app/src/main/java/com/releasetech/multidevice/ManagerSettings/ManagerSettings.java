package com.releasetech.multidevice.ManagerSettings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.releasetech.multidevice.BuildConfig;
import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;
import com.releasetech.multidevice.databinding.ActivityManagerSettingsBinding;

public class ManagerSettings extends AppCompatActivity {
    private static final String TAG = "[MANAGER SETTINGS]";
    private static final int REFUND_REQUEST = 1;

    private ActivityManagerSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManagerSettingsBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                //R.id.navigation_system, R.id.navigation_refill_powder,
                R.id.navigation_history, R.id.navigation_statistics, R.id.navigation_logs)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_manager_settings);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView versionText = findViewById(R.id.version_text);
        versionText.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        DBManager dbManager = new DBManager(this);
        dbManager.open();
        dbManager.create();
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REFUND_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (PreferenceManager.getString(this, "checkout_reader").equals("kicc")) {
                    if (data.getStringExtra("RESULT_CODE").equals("0000")) {
                        dbManager.insertColumn(DBManager.CHECKOUT, data);
                        Utils.timedAlert(this, "결제가 취소되었습니다.", 3);
                    } else {
                        Utils.logD(TAG, data.getStringExtra("RESULT_CODE"));
                        Utils.logD(TAG, data.getStringExtra("TRAN_TYPE"));
                        Utils.logD(TAG, data.getStringExtra("CARD_NUM"));
                        Utils.logD(TAG, data.getStringExtra("CARD_NAME"));
                        Utils.logD(TAG, data.getStringExtra("RESULT_MSG"));
                        Utils.logD(TAG, data.getStringExtra("APPROVAL_NUM"));
                        Utils.logD(TAG, data.getStringExtra("APPROVAL_DATE"));
                    }
                }else if (PreferenceManager.getString(this, "checkout_reader").equals("nice")){
                    recvFS(data.getStringExtra("NVCATRECVDATA"));
                    Log.i("여기 찍힘 1", "1");
                    if (strRecv03.equals("0000") || strRecv03.equals("6003") || strRecv03.equals("6000")){
                        Log.i("여기 찍힘 2", "2");
                        dbManager.insertColumnNice(DBManager.CHECKOUT, "", strRecv01, cardNum, strRecv13, "", strPrice, String.valueOf(Math.floor(strPrice/11)), strRecv03, strRecv17, strRecv08, strRecv09, "", "", "", "", "", "", "");
                        Utils.timedAlert(this, "결제가 취소되었습니다.", 3);
                        Log.i("여기 찍힘 3", "3");
                    }else{
                        Utils.timedAlert(this, "결제 취소에 실패했습니다.", 3);
                    }
                }
            }
        }
    }

    char fs = 0x1C;
    String strRecv01, strRecv02, strRecv03, strRecv04, strRecv05, strRecv06, strRecv07, strRecv08, strRecv09, strRecv10, strRecv11, strRecv12, strRecv13, strRecv14, strRecv15, strRecv16, strRecv17, strRecv18, strRecv19, strRecv20, strRecv21, strRecv22, strRecv23, strRecv24, strRecv25, strRecv26, strRecv27, strRecv28, strRecv29, strRecv30;
    String cardNum;
    int strPrice;
    private void recvFS(String recvdata) {
        int i, j = 0, k = 0;
        for (i = 0; i < recvdata.length(); i++) {
            if (recvdata.substring(i, i + 1).equals(String.valueOf(fs))) {
                k++;
                switch (k) {
                    case 1: //거래구분
                        strRecv01 = recvdata.substring(j, i);
                        Log.i("환불 테스트 거래 구분", strRecv01);
                        j = i + 1;
                        break;
                    case 2: //거래유형
                        strRecv02 = recvdata.substring(j, i);
                        Log.i("환불 테스트 거래 유형", strRecv02);
                        j = i + 1;
                        break;
                    case 3: //응답코드
                        strRecv03 = recvdata.substring(j, i);
                        Log.i("환불 테스트 응답 코드", strRecv03);
                        j = i + 1;
                        break;
                    case 4: //거래금액
                        strRecv04 = recvdata.substring(j, i);
                        strPrice = Integer.valueOf(strRecv04);
                        PreferenceManager.setInt(this, "prev_nice_checkout_approval_price", strPrice);
                        Log.i("환불 테스트 거래 금액", strRecv04);
                        j = i + 1;
                        break;
                    case 5: //부가세
                        strRecv05 = recvdata.substring(j, i);
                        Log.i("환불 테스트 부가세", strRecv05);
                        j = i + 1;
                        break;
                    case 6: //봉사료
                        strRecv06 = recvdata.substring(j, i);
                        Log.i("환불 테스트 봉사료", strRecv06);
                        j = i + 1;
                        break;
                    case 7: //할부
                        strRecv07 = recvdata.substring(j, i);
                        Log.i("환불 테스트 할부", strRecv07);
                        j = i + 1;
                        break;
                    case 8: //승인번호
                        strRecv08 = recvdata.substring(j, i);
                        PreferenceManager.setString(this, "prev_nice_checkout_approval_no", strRecv08.replaceAll(" " , ""));
                        Log.i("나이스 테스트 8", PreferenceManager.getString(this, "prev_nice_checkout_approval_no"));
                        Log.i("환불 테스트 승인 번호", strRecv08);
                        j = i + 1;
                        break;
                    case 9: //승인일자
                        strRecv09 = recvdata.substring(j, i);
                        if(strRecv09.replaceAll(" ", "").length() > 6) {
                            PreferenceManager.setString(this, "prev_nice_checkout_approval_date", strRecv09.replaceAll(" ", "").substring(0, 6));
                            Log.i("나이스 테스트 9", PreferenceManager.getString(this, "prev_nice_checkout_approval_date"));
                            Log.i("환불 테스트 승인 일자", strRecv09);
                        }else {
                            PreferenceManager.setString(this, "prev_nice_checkout_approval_date", "");
                            Log.i("나이스 테스트 9-2", PreferenceManager.getString(this, "prev_nice_checkout_approval_date"));
                            Log.i("환불 테스트 승인 일자", strRecv09);
                        }
                        j = i + 1;
                        break;
                    case 10: //발급사코드
                        strRecv10 = recvdata.substring(j, i);
                        Log.i("환불 테스트 발급사 코드", strRecv10);
                        j = i + 1;
                        break;
                    case 11: //발급사명
                        strRecv11 = recvdata.substring(j, i);
                        Log.i("환불 테스트 발급사 이름", strRecv11);
                        j = i + 1;
                        break;
                    case 12: //매입사코드
                        strRecv12 = recvdata.substring(j, i);
                        Log.i("환불 테스트 매입사 코드", strRecv12);
                        j = i + 1;
                        break;
                    case 13: //매입사명
                        strRecv13 = recvdata.substring(j, i);
                        Log.i("환불 테스트 매입사 이름", strRecv13);
                        j = i + 1;
                        break;
                    case 14: //가맹점번호
                        strRecv14 = recvdata.substring(j, i);
                        Log.i("환불 테스트 가맹점 번호", strRecv14);
                        j = i + 1;
                        break;
                    case 15: //승인CATID
                        strRecv15 = recvdata.substring(j, i);
                        Log.i("환불 테스트 승인 CATID", strRecv15);
                        j = i + 1;
                        break;
                    case 16: //잔액
                        strRecv16 = recvdata.substring(j, i);
                        Log.i("환불 테스트 잔액", strRecv16);
                        j = i + 1;
                        break;
                    case 17: //응답메시지
                        strRecv17 = recvdata.substring(j, i);
                        Log.i("환불 테스트 응답 메시지", strRecv17);
                        j = i + 1;
                        break;
                    case 18: //카드BIN
                        strRecv18 = recvdata.substring(j, i);
                        cardNum = strRecv18.substring(0,6);
                        Log.i("환불 테스트 카드 BIN", cardNum);
                        j = i + 1;
                        break;
                    case 19: //카드구분
                        strRecv19 = recvdata.substring(j, i);
                        Log.i("환불 테스트 카드 구분", strRecv19);
                        j = i + 1;
                        break;
                    case 20: //전문관리번호
                        strRecv20 = recvdata.substring(j, i);
                        Log.i("환불 테스트 전문 관리 번호", strRecv20);
                        j = i + 1;
                        break;
                    case 21: //거래일련번호
                        strRecv21 = recvdata.substring(j, i);
                        Log.i("환불 테스트 거래 일련 번호", strRecv21);
                        //etCashnum.setText(strRecv21);
                        j = i + 1;
                        break;
                    case 22: //발생포인트(할인금액)
                        strRecv22 = recvdata.substring(j, i);
                        Log.i("환불 테스트 발생 포인트", strRecv22);
                        j = i + 1;
                        break;
                    case 23: //가용포인트(지불금액)
                        strRecv23 = recvdata.substring(j, i);
                        Log.i("환불 테스트 가용 포인트", strRecv23);
                        j = i + 1;
                        break;
                    case 24: //누적포인트(잔액한도)
                        strRecv24 = recvdata.substring(j, i);
                        Log.i("환불 테스트 누적 포인트", strRecv24);
                        j = i + 1;
                        break;
                    case 25: //캐시백가맹점
                        strRecv25 = recvdata.substring(j, i);
                        Log.i("환불 테스트 캐시백 가맹점", strRecv25);
                        j = i + 1;
                        break;
                    case 26: //캐시백승인번호
                        strRecv26 = recvdata.substring(j, i);
                        Log.i("환불 테스트 캐시백 승인 번호", strRecv26);
                        j = i + 1;
                        break;
                    case 27:
                        strRecv27 = recvdata.substring(j, i);
                        Log.i("환불 테스트 27", strRecv27);
                        j = i + 1;
                        break;
                    case 28:
                        strRecv28 = recvdata.substring(j, i);
                        Log.i("환불 테스트 28", strRecv28);
                        j = i + 1;
                        break;
                    case 29:
                        strRecv29 = recvdata.substring(j, i);
                        Log.i("환불 테스트 29", strRecv29);
                        j = i + 1;
                        break;
                    case 30:
                        strRecv30 = recvdata.substring(j, i);
                        Log.i("환불 테스트 30", strRecv30);
                        j = i + 1;
                        break;
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Utils.restart(this);
//            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            //Nothing
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        return;
    }


}