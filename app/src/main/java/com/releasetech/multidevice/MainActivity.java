package com.releasetech.multidevice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
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
import com.releasetech.multidevice.Manager.SalesManager;
import com.releasetech.multidevice.Tool.Utils;

import java.util.ArrayList;
import java.util.List;

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
        passwordManager = new PasswordManager(this);
        PreferenceManager.setString(this, "idle_screen_time", "1");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button triggerButton = findViewById(R.id.trigger_button);
        View clickInterceptor = findViewById(R.id.click_interceptor);
        triggerButton.setText(PreferenceManager.getString(getApplicationContext(), "message_idle"));
        String idleColorString = PreferenceManager.getString(getApplicationContext(), "message_color");
        ImageButton imageButton = findViewById(R.id.ad_image);
        VideoView videoButton = findViewById(R.id.ad_video);

        if(PreferenceManager.getString(this, "ad_type").equals("text")) {
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
        }else if(PreferenceManager.getString(this, "ad_type").equals("image")) {
            Log.i("테스트", "이미지");
            try {
                List<String> imageUris = new ArrayList<>();
                for (int i = 1; i <= PreferenceManager.getInt(this, "ad_image_count"); i++) {
                    imageUris.add(PreferenceManager.getString(this, "ad_image" + i));
                }
                int[] currentIndex = {0};

                imageButton.setImageURI(Uri.parse(imageUris.get(currentIndex[0]))); // 초기 이미지 설정

                // Handler와 Runnable을 사용하여 주기적으로 이미지 변경
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.i("testt", PreferenceManager.getString(getApplicationContext(), "ad_image_time"));
                        imageButton.setImageURI(Uri.parse(imageUris.get(currentIndex[0])));
                        currentIndex[0] = (currentIndex[0] + 1) % imageUris.size();
                        handler.postDelayed(this, Integer.parseInt(PreferenceManager.getString(getApplicationContext(), "ad_image_time"))*1000); // 30초 = 30000ms

                    }
                };

                handler.postDelayed(runnable, 0);

                // 불필요한 뷰 숨기기
                triggerButton.setVisibility(View.GONE);
                triggerButton.setActivated(false);
                videoButton.setVisibility(View.GONE);
                videoButton.setActivated(false);
            } catch (Exception e) {
                Utils.logD(TAG, "이미지 로드 실패");
            }
        }else if(PreferenceManager.getString(this, "ad_type").equals("video")) {
            Log.i("테스트", "비디오");
            try {
                List<String> videoUris = new ArrayList<>();
                for(int i=1; i<=PreferenceManager.getInt(this, "ad_video_count"); i++){
                    videoUris.add(PreferenceManager.getString(this, "ad_video"+i));
                }
                int[] currentIndex ={0};
                videoButton.setVideoURI(Uri.parse(videoUris.get(currentIndex[0])));
                videoButton.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(false);  // 단일 동영상에서는 루프 비활성화
                    }
                });
                videoButton.start();
                videoButton.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // 현재 재생 중인 동영상의 인덱스 증가
                        currentIndex[0] = (currentIndex[0] + 1) % videoUris.size(); // 순환

                        // 다음 동영상 설정 및 재생 시작
                        videoButton.setVideoURI(Uri.parse(videoUris.get(currentIndex[0])));
                        videoButton.start();
                    }
                });

                triggerButton.setVisibility(View.GONE);
                triggerButton.setActivated(false);
                imageButton.setVisibility(View.GONE);
                imageButton.setActivated(false);
            } catch (Exception e) {
                Utils.logD(TAG, "광고 비디오 로드 실패");
            }
        }

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
    }
}