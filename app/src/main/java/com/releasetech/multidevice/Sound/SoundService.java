package com.releasetech.multidevice.Sound;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;

public class SoundService extends IntentService {
    public static final String CHECKOUT_FAIL = "checkout_fail";
    public static final String CHECKOUT_OK = "checkout_ok";
    public static final String CHECKOUT_PAYCO_FAIL = "checkout_payco_fail";
    public static final String CHECKOUT_PAYCO_OK = "checkout_payco_ok";
    public static final String APPROVAL_PAYCO = "approval_payco";
    public static final String THANK_YOU = "thank_you";
    public static final String DESSERT_OK = "dessert_ok";
    public static final String DESSERT_FAIL = "dessert_fail";
    public static final String WARN = "warn";
    private static final String TAG = "[SOUND]";

    private static MediaPlayer player_checkout_fail = null;
    private static MediaPlayer player_checkout_ok = null;
    private static MediaPlayer player_checkout_payco_fail = null;
    private static MediaPlayer player_checkout_payco_ok = null;
    private static MediaPlayer player_approval_payco = null;
    private static MediaPlayer player_thank_you = null;
    private static MediaPlayer player_warn = null;
    private static MediaPlayer player_dessert_ok = null;
    private static MediaPlayer player_dessert_fail = null;
    private static AudioManager audioManager = null;



    public SoundService() {
        super("SoundService");
    }

    public static void play(Context context, String action) {
        Intent intent = new Intent(context, SoundService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (audioManager == null) {
            audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
        if (intent != null) {
            final String action = intent.getAction();
            Utils.logD(TAG, action);

            new Thread(() -> {
                switch (action) {
                    case CHECKOUT_FAIL:
                        if (player_checkout_fail == null) {
                            player_checkout_fail = MediaPlayer.create(getApplicationContext(), R.raw.checkout_fail, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(), audioManager.generateAudioSessionId());
                            player_checkout_fail.setOnPreparedListener(MediaPlayer::start);
                            player_checkout_fail.setOnCompletionListener(mediaPlayer -> mediaPlayer.seekTo(0));
                        } else {
                            player_checkout_fail.start();
                        }
                        break;
                    case CHECKOUT_OK:
                        if (player_checkout_ok == null) {
                            player_checkout_ok = MediaPlayer.create(getApplicationContext(), R.raw.checkout_ok, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(), audioManager.generateAudioSessionId());
                            player_checkout_ok.setOnPreparedListener(MediaPlayer::start);
                            player_checkout_ok.setOnCompletionListener(mediaPlayer -> mediaPlayer.seekTo(0));
                        } else {
                            player_checkout_ok.start();
                        }
                        break;
                    case CHECKOUT_PAYCO_FAIL:
                        if (player_checkout_payco_fail == null) {
                            player_checkout_payco_fail = MediaPlayer.create(getApplicationContext(), R.raw.checkout_payco_fail, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(), audioManager.generateAudioSessionId());
                            player_checkout_payco_fail.setOnPreparedListener(MediaPlayer::start);
                            player_checkout_payco_fail.setOnCompletionListener(mediaPlayer -> mediaPlayer.seekTo(0));
                        } else {
                            player_checkout_payco_fail.start();
                        }
                        break;
                    case CHECKOUT_PAYCO_OK:
                        if (player_checkout_payco_ok == null) {
                            player_checkout_payco_ok = MediaPlayer.create(getApplicationContext(), R.raw.checkout_payco_ok, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(), audioManager.generateAudioSessionId());
                            player_checkout_payco_ok.setOnPreparedListener(MediaPlayer::start);
                            player_checkout_payco_ok.setOnCompletionListener(mediaPlayer -> mediaPlayer.seekTo(0));
                        } else {
                            player_checkout_payco_ok.start();
                        }
                        break;
                    case APPROVAL_PAYCO:
                        if (player_approval_payco == null) {
                            player_approval_payco = MediaPlayer.create(getApplicationContext(), R.raw.approval_payco, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(), audioManager.generateAudioSessionId());
                            player_approval_payco.setOnPreparedListener(MediaPlayer::start);
                            player_approval_payco.setOnCompletionListener(mediaPlayer -> mediaPlayer.seekTo(0));
                        } else {
                            player_approval_payco.start();
                        }
                        break;
                    case THANK_YOU:
                        if (player_thank_you == null) {
                            player_thank_you = MediaPlayer.create(getApplicationContext(), R.raw.thank_you, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(), audioManager.generateAudioSessionId());
                            player_thank_you.setOnPreparedListener(MediaPlayer::start);
                            player_thank_you.setOnCompletionListener(mediaPlayer -> mediaPlayer.seekTo(0));
                        } else {
                            player_thank_you.start();
                        }
                        break;
                    case WARN:
                        if (player_warn == null) {
                            player_warn = MediaPlayer.create(getApplicationContext(), R.raw.warn, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(), audioManager.generateAudioSessionId());
                            player_warn.setOnPreparedListener(MediaPlayer::start);
                            player_warn.setOnCompletionListener(mediaPlayer -> mediaPlayer.seekTo(0));
                        } else {
                            player_warn.start();
                        }
                        break;

                    case DESSERT_OK:
                        if (player_dessert_ok == null) {
                            player_dessert_ok = MediaPlayer.create(getApplicationContext(), R.raw.dessert_ok, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(), audioManager.generateAudioSessionId());
                            player_dessert_ok.setOnPreparedListener(MediaPlayer::start);
                            player_dessert_ok.setOnCompletionListener(mediaPlayer -> mediaPlayer.seekTo(0));
                        } else {
                            player_dessert_ok.start();
                        }
                        break;

                    case DESSERT_FAIL:
                        if (player_dessert_fail == null) {
                            player_dessert_fail = MediaPlayer.create(getApplicationContext(), R.raw.dessert_fail, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(), audioManager.generateAudioSessionId());
                            player_dessert_fail.setOnPreparedListener(MediaPlayer::start);
                            player_dessert_fail.setOnCompletionListener(mediaPlayer -> mediaPlayer.seekTo(0));
                        } else {
                            player_dessert_fail.start();
                        }
                        break;
                }
            }).start();
        }
    }

}