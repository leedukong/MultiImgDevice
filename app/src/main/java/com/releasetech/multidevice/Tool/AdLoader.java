package com.releasetech.multidevice.Tool;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.ViewFlipper;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.releasetech.multidevice.MainActivity;
import com.releasetech.multidevice.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AdLoader {
    private static final String TAG = "[AD LOADER]";

    public static boolean loadIdleAds(Context context, View view) {
        ViewFlipper viewFlipper = (ViewFlipper) view;
        File[] files = getListFiles(context, "/광고_대기");
        if (files == null) {
            return false;
        }
        Arrays.sort(files);
        for (File file : files) {
            Utils.logD(TAG, file.getName());
            if (Utils.isImage(file)) {
                ImageView ad = new ImageView(context);
                ad.setImageURI(Uri.fromFile(file));
                ad.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                ad.setScaleType(ImageView.ScaleType.FIT_XY);
                ad.setAdjustViewBounds(true);
                viewFlipper.addView(ad);
            } else if (Utils.isVideo(file)) {
                VideoView ad = new VideoView(context);
                ad.setVideoURI(Uri.fromFile(file));
                ad.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                ad.setOnCompletionListener(mediaPlayer -> {
                    Utils.logD(TAG, "영상 종료");
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);
                    viewFlipper.startFlipping();
                });
                viewFlipper.addView(ad);
            }
        }
        return true;
    }

    public static void loadMenuAds(Context context, View view) {
        ViewFlipper viewFlipper = (ViewFlipper) view;
        File[] files = getListFiles(context, "/광고_메뉴");
        if (files == null) return;
        for (File file : files) {
            if (Utils.isImage(file)) {
                ImageView ad = new ImageView(context);
                ad.setImageURI(Uri.fromFile(file));
                ad.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
                ad.setMaxWidth(636);
                ad.setScaleType(ImageView.ScaleType.FIT_XY);
                ad.setAdjustViewBounds(true);
                viewFlipper.addView(ad);
                viewFlipper.startFlipping();
                viewFlipper.setFlipInterval(20000);
            }
        }
    }

    /*
    public static void loadMakingAds(Context context, View view) {
        ConstraintLayout mainLayout = (ConstraintLayout) view;
        File[] files = getListFiles(context, "/광고_제조");
        if (files == null) return;

        HashMap<String, File[]> adMap = new HashMap<>();
        Arrays.asList(files).forEach(f -> {
            String[] tempNameSplit = f.getName().split("-");
            String prefix = tempNameSplit[0];
            String suffix = tempNameSplit[1].split("\\.")[0];
            if (!adMap.containsKey(prefix)) {
                adMap.put(prefix, new File[3]);
            }
            int index = Integer.parseInt(suffix) - 1;
            adMap.get(prefix)[index] = f;
        });

        ArrayList<File[]> adPack = new ArrayList<>();
        for (Map.Entry<String, File[]> stringEntry : adMap.entrySet()) {
            File[] value = stringEntry.getValue();
            adPack.add(value);
        }

        if (adPack.size() == 0) return;

        int index = MainActivity.makeCount % adPack.size();
        File[] ad = adPack.get(index);

        VideoView ad1 = mainLayout.findViewById(R.id.making_ad_1);
        ad1.setVideoURI(Uri.fromFile(ad[0]));
        ad1.setOnPreparedListener(MediaPlayer::start);

        if (Utils.isImage(ad[1])) {
            ImageView ad2 = mainLayout.findViewById(R.id.making_ad_2_image);
            ad2.setVisibility(View.VISIBLE);
            mainLayout.findViewById(R.id.making_ad_2_video).setVisibility(View.GONE);
            ad2.setImageURI(Uri.fromFile(adPack.get(index)[1]));
        } else {
            VideoView ad2 = mainLayout.findViewById(R.id.making_ad_2_video);
            ad2.setVisibility(View.VISIBLE);
            mainLayout.findViewById(R.id.making_ad_2_image).setVisibility(View.GONE);
            ad2.setVideoURI(Uri.fromFile(adPack.get(index)[1]));
            ad2.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setVolume(0, 0);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            });
        }

        if (Utils.isImage(ad[2])) {
            ImageView ad3 = mainLayout.findViewById(R.id.making_ad_3_image);
            ad3.setVisibility(View.VISIBLE);
            mainLayout.findViewById(R.id.making_ad_3_video).setVisibility(View.GONE);
            ad3.setImageURI(Uri.fromFile(adPack.get(index)[2]));
        } else {
            VideoView ad3 = mainLayout.findViewById(R.id.making_ad_3_video);
            ad3.setVisibility(View.VISIBLE);
            mainLayout.findViewById(R.id.making_ad_3_image).setVisibility(View.GONE);
            ad3.setVideoURI(Uri.fromFile(adPack.get(index)[2]));
            ad3.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setVolume(0, 0);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            });
        }
    }*/


    /*
    public static void loadMakingAds(Context context, View view, int ad1Time, int ad2Time, int ad3Time) {
        ConstraintLayout mainLayout = (ConstraintLayout) view;
        File[] files = getListFiles(context, "/광고_제조");
        if (files == null) return;

        HashMap<String, File[]> adMap = new HashMap<>();
        Arrays.asList(files).forEach(f -> {
            String[] tempNameSplit = f.getName().split("-");
            String prefix = tempNameSplit[0];
            String suffix = tempNameSplit[1].split("\\.")[0];
            if (!adMap.containsKey(prefix)) {
                adMap.put(prefix, new File[3]);
            }
            int index = Integer.parseInt(suffix) - 1;
            adMap.get(prefix)[index] = f;
        });

        ArrayList<File[]> adPack = new ArrayList<>();
        for (Map.Entry<String, File[]> stringEntry : adMap.entrySet()) {
            File[] value = stringEntry.getValue();
            adPack.add(value);
        }

        if (adPack.size() == 0) return;

        int index = MainActivity.makeCount % adPack.size();
        File[] ad = adPack.get(index);

        VideoView ad1 = mainLayout.findViewById(R.id.making_ad_1);
        ad1.setVideoURI(Uri.fromFile(ad[0]));
        ad1.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.start();
            mediaPlayer.seekTo(ad1Time);
        });

        if (Utils.isImage(ad[1])) {
            ImageView ad2 = mainLayout.findViewById(R.id.making_ad_2_image);
            ad2.setVisibility(View.VISIBLE);
            mainLayout.findViewById(R.id.making_ad_2_video).setVisibility(View.GONE);
            ad2.setImageURI(Uri.fromFile(adPack.get(index)[1]));
        } else {
            VideoView ad2 = mainLayout.findViewById(R.id.making_ad_2_video);
            ad2.setVisibility(View.VISIBLE);
            mainLayout.findViewById(R.id.making_ad_2_image).setVisibility(View.GONE);
            ad2.setVideoURI(Uri.fromFile(adPack.get(index)[1]));
            ad2.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setVolume(0, 0);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                mediaPlayer.seekTo(ad2Time);
            });
        }

        if (Utils.isImage(ad[2])) {
            ImageView ad3 = mainLayout.findViewById(R.id.making_ad_3_image);
            ad3.setVisibility(View.VISIBLE);
            mainLayout.findViewById(R.id.making_ad_3_video).setVisibility(View.GONE);
            ad3.setImageURI(Uri.fromFile(adPack.get(index)[2]));
        } else {
            VideoView ad3 = mainLayout.findViewById(R.id.making_ad_3_video);
            ad3.setVisibility(View.VISIBLE);
            mainLayout.findViewById(R.id.making_ad_3_image).setVisibility(View.GONE);
            ad3.setVideoURI(Uri.fromFile(adPack.get(index)[2]));
            ad3.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setVolume(0, 0);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                mediaPlayer.seekTo(ad3Time);
            });
        }
    }*/

    private static File[] getListFiles(Context context, String basePath) {
        String path = context.getExternalFilesDir(null) + basePath;
        File directory = new File(path);
        return directory.listFiles();
    }

    public static String[] getListFileNames(Context context, String basePath) {
        ArrayList<String> fileNames = new ArrayList<>();
        Arrays.stream(getListFiles(context, basePath))
                .sorted()
                .forEach(file -> fileNames.add(file.getName()));
        return fileNames.toArray(new String[0]);
    }
}
