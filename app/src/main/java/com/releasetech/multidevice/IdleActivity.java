package com.releasetech.multidevice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.Manager.UIManager;
import com.releasetech.multidevice.Tool.AdLoader;
import com.releasetech.multidevice.Tool.Utils;

public class IdleActivity extends AppCompatActivity {
    private static final String TAG = "[IDLE]";
    private static int flipperIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UIManager.hideSystemUI(this);
        ViewFlipper viewFlipper = findViewById(R.id.idle_ad);
        if (!AdLoader.loadIdleAds(this, viewFlipper)) {
            Utils.logD(TAG, "광고가 없습니다.");
            PreferenceManager.setBoolean(this, "use_idle_screen", false);
            finish();
        }

        int imageDuration = Integer.parseInt(PreferenceManager.getString(IdleActivity.this, "idle_screen_image_duration")) * 1000;
        if (viewFlipper.getChildCount() > 1) {
            viewFlipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    int childIndex = viewFlipper.getDisplayedChild();
                    View view = viewFlipper.getChildAt(childIndex);
                    if (flipperIndex != childIndex) {
                        if (view instanceof ImageView) {
                            Utils.logD(TAG, "다음 이미지");
                            viewFlipper.stopFlipping();
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(viewFlipper::startFlipping, imageDuration);
                        } else {
                            Utils.logD(TAG, "다음 영상");
                            ((VideoView) view).start();
                            viewFlipper.stopFlipping();
                        }
                        flipperIndex = childIndex;
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            viewFlipper.setFlipInterval(100);
            viewFlipper.startFlipping();
        } else {
            int childIndex = viewFlipper.getDisplayedChild();
            View view = viewFlipper.getChildAt(childIndex);
            if (view instanceof VideoView) {
                ((VideoView) view).setOnCompletionListener(mediaPlayer -> {
                });
                ((VideoView) view).setOnPreparedListener(mediaPlayer -> {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                });

            }
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        finish();
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
        return;
    }
}