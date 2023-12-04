package com.releasetech.multidevice.Update;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.releasetech.multidevice.BuildConfig;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateApp extends AsyncTask<String, Void, Void> {
    private Context context;
    private static final String TAG = "[UPDATE]";

    public void setContext(Context context) {
        this.context = context;
    }

    private static boolean sameMajor(Context context) {
        try {
            URL url = new URL(context.getString(R.string.latest_app_version_url));
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
            int versionMajor = Integer.parseInt(in.readLine().split("\\.")[0]);
            return (versionMajor == Integer.parseInt(BuildConfig.VERSION_NAME.split("\\.")[0])) || versionMajor == 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void updatedDialog(Context context, String oldVersionName, String currentVersionName) {
        PreferenceManager.setString(context, "version", BuildConfig.VERSION_NAME);

        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout updatedLayout = (LinearLayout) vi.inflate(R.layout.dialog_updated, null);
        TextView oldVersion = (TextView) updatedLayout.findViewById(R.id.previous_version);
        oldVersion.setText(oldVersionName);
        TextView currentVersion = (TextView) updatedLayout.findViewById(R.id.current_version_updated);
        currentVersion.setText(currentVersionName);
        AlertDialog.Builder updatedBuilder = new AlertDialog.Builder(context).setView(updatedLayout);
        AlertDialog updatedDialog = updatedBuilder.create();

        updatedDialog.getWindow().
                setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        updatedDialog.show();

        updatedDialog.getWindow().
                clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        updatedDialog.setCanceledOnTouchOutside(false);


        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(updatedDialog::dismiss, 3000);
    }

    public static int getLatestVersion(Context context) {
        try {
            URL url = new URL(context.getString(R.string.latest_app_version_url));
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setConnectTimeout(2000);
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
            String str;
            str = in.readLine();
            return Integer.parseInt(str);
        } catch (Exception e) {
            Utils.logD(TAG, "Error getting latest version: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public static int getCurrentVersion() {
        return BuildConfig.VERSION_CODE;
    }

    @Override
    protected Void doInBackground(String... args) {
        if (sameMajor(context)) {
            return null;
        }

        int currentVersion = getCurrentVersion();
        int latestVersion = getLatestVersion(context);

        Utils.logD(TAG, "현재 앱 버전 : " + currentVersion + "\t최신 앱 버전 : " + latestVersion);
        if (currentVersion >= latestVersion) {
            Utils.logD(TAG, "업데이트 필요 없음");
            return null;
        }
        Utils.logD(TAG, "새로운 업데이트 시도");

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            PreferenceManager.setString(context, "version", BuildConfig.VERSION_NAME);
            URL url = new URL(context.getString(R.string.latest_app_url));
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            Utils.logD(TAG, "서버에 연결됨");

            String PATH = context.getExternalCacheDir() + "/updates/";
            File file = new File(PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            File outputFile = new File(file, "update.apk");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();
            Utils.logD(TAG, "새로운 버전 다운로드 완료");

            Utils.reserveLaunch(context);

            final String command = "pm install -t -r " + outputFile.getAbsolutePath();
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            proc.waitFor();
            Utils.logD(TAG, "업데이트 완료");

        } catch (Exception e) {
            Utils.logE(TAG, "업데이트 에러" + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


}