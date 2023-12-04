package com.releasetech.multidevice.Log;

import android.content.Context;

import com.releasetech.multidevice.Tool.Utils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogThread extends Thread {
    private static final String TAG = "[LOG THREAD]";
    private String logDirectoryPath = "";
    private boolean rebooted = false;
    private Context context;

    public LogThread(Context context, String logDirectoryPath, boolean rebooted) {
        this.context = context;
        this.logDirectoryPath = logDirectoryPath;
        this.rebooted = rebooted;
    }

    public void run() {
        super.run();

        File logDirectory = new File(logDirectoryPath);

        if (!logDirectory.exists()) {
            logDirectory.mkdirs();
        }
        String serialNo = Utils.getSerialNumber(context);
//        String macAddress = getMacAddr();
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        String dateString = now.format(formatter);
        File dateDirectory = new File(logDirectory, "/" + dateString);

        if (rebooted) Utils.logD(TAG, "재부팅됨");
        else Utils.logD(TAG, "재부팅되지 않음");
        if (rebooted && dateDirectory.exists()) {
            String newDateString = dateString + "_" + now.format(DateTimeFormatter.ofPattern("HH_mm_ss"));
            File newDateDirectory = new File(logDirectory, "/" + newDateString);
            dateDirectory.renameTo(newDateDirectory);
            rebooted = false;
        }

        if (!dateDirectory.exists()) {
            dateDirectory.mkdirs();
        }

        File logFile = new File(dateDirectory, "/" + serialNo + "_log");

        try {
            String cmd = "logcat -s DEV:D -n 100 -r 256 -f " + logFile;
            Process logcatProcess = Runtime.getRuntime().exec(cmd);
            logcatProcess.waitFor();
            Utils.logD(TAG, "로그 저장됨 : " + logFile);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
