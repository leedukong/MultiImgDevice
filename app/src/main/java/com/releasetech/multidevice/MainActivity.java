package com.releasetech.multidevice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
        ImageButton imageButton = findViewById(R.id.ad_image);
        VideoView videoButton = findViewById(R.id.ad_video);

        Log.i("테스트", "uri "+ PreferenceManager.getString(this, "ad_uri"));
        Log.i("테스트", "ad_type "+ PreferenceManager.getString(this, "ad_type"));
        if(PreferenceManager.getString(this, "ad_uri") != null) {
            if(PreferenceManager.getString(this, "ad_type").equals("image")) {
                Log.i("테스트", "이미지");
                try {
                    Log.i("테스트", "이미지1");
                    imageButton.setImageURI(Uri.parse(PreferenceManager.getString(this, "ad_uri")));
                    triggerButton.setVisibility(View.GONE);
                    triggerButton.setActivated(false);
                    videoButton.setVisibility(View.GONE);
                    videoButton.setActivated(false);
                } catch (Exception e) {
                    Utils.logD(TAG, "광고 이미지 로드 실패");
                }
            }else if(PreferenceManager.getString(this, "ad_type").equals("video")) {
                Log.i("테스트", "비디오");
                try {
                    Log.i("테스트", "비디오1");
                    videoButton.setVideoURI(Uri.parse(PreferenceManager.getString(this, "ad_uri")));
                    videoButton.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.setLooping(true);  // 무한 반복 재생
                        }
                    });
                    videoButton.start();
                    triggerButton.setVisibility(View.GONE);
                    triggerButton.setActivated(false);
                    imageButton.setVisibility(View.GONE);
                    imageButton.setActivated(false);
                } catch (Exception e) {
                    Utils.logD(TAG, "광고 비디오 로드 실패");
                }
            }
        }else {
            Log.i("테스트", "둘다아님");
            try {
                imageButton.setActivated(false);
                imageButton.setVisibility(View.GONE);
                if (idleColorString != null && !idleColorString.trim().isEmpty()) {
                    triggerButton.setTextColor(Color.parseColor(idleColorString));
                } else {
                    triggerButton.setTextColor(Color.parseColor("#000000")); // 기본 검정색
                }
            } catch (IllegalArgumentException e) {
                // 색상 파싱 에러가 발생한 경우 기본 색상 설정
                triggerButton.setTextColor(Color.parseColor("#000000"));
            }
        }

//        ImageButton imageButton = findViewById(R.id.imageButton);
//        imageButton.setImageURI(imageUri);

        videoButton.setOnClickListener(v -> {
            if (clickInterceptor.getVisibility() == View.VISIBLE) {
            } else {
                Intent intent = new Intent(MainActivity.this, OrderActivity.class);
                startActivity(intent);
            }
        });

        triggerButton.setOnClickListener(v -> {
            if (clickInterceptor.getVisibility() == View.VISIBLE) {
            } else {
                Intent intent = new Intent(MainActivity.this, OrderActivity.class);
                startActivity(intent);
            }
        });

        imageButton.setOnClickListener(v -> {
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