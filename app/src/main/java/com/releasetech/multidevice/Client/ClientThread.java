package com.releasetech.multidevice.Client;

import android.content.Context;

import com.releasetech.multidevice.BuildConfig;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

public class ClientThread extends Thread {
    /* TODO 다시 열기
    private static final String TAG = "[CLIENT THREAD]";

    private Context context;

    public ClientThread(Context context) {
        this.context = context;
    }

    public void run() {
        super.run();

        LocalDateTime lastReport = LocalDateTime.now();

        while (!isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String serialNo = Utils.getSerialNumber(context);
            String deviceName = PreferenceManager.getString(context, "device_name");
            String appVersion = BuildConfig.VERSION_NAME;
            String lastOrder = PreferenceManager.getString(context, "last_checkout");
            StringBuilder errors = new StringBuilder();

            if (errors.length() > 0) {
                errors.deleteCharAt(errors.length() - 1);
            } else {
                errors.append("-");
            }

            if (lastReport.getDayOfYear() != LocalDateTime.now().getDayOfYear()) {
                PreferenceManager.setInt(context, "orders_today", 0);
            }
            int ordersToday = PreferenceManager.getInt(context, "orders_today");


            StringBuilder postData = new StringBuilder();
            byte[] postDataBytes = postData.toString().getBytes();

            URL serverUrl;
            try {
                serverUrl = new URL(context.getString(R.string.machine_state_url));
                HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                connection.getOutputStream().write(postDataBytes);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Utils.logE(TAG, "HTTP response code: " + responseCode);
                }

                Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                for (int c; (c = in.read()) >= 0; ) {
                    response.append((char) c);
                }
                if (response.toString().contains("Error")) {
                    Utils.logE(TAG, "Error: " + response);
                    int index = response.indexOf("Error");
                    Utils.logE(TAG, "Substring at " + index);
                    Utils.logE(TAG, "Substring: " + response.substring(index - 5, index + 15));
                }

            } catch (IOException e) {
                e.printStackTrace();
                Utils.logE(TAG, "IOException: " + e.getMessage());
            }
            lastReport = LocalDateTime.now();
        }
    }*/
}
