package com.releasetech.multidevice.Log;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class LogService extends Service {

    private static final String TAG = "[LOG]";

    private static boolean isStarted = false;
    private static LogThread logThread = null;

    IBinder mBinder = new LogBinder();

    public static String logDirectoryPath = "";

    public LogService() {
    }

    public static File[] loadLogs() {

        File logDirectory = new File(logDirectoryPath);
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        String dateString = now.format(formatter);
        File dateDirectory = new File(logDirectory, "/" + dateString);

        File[] tempFileList;
        do {
            tempFileList = dateDirectory.listFiles();
            if (tempFileList == null) {
                Utils.logD(TAG, "파일이 없음");
                return null;
            }
        } while (tempFileList.length == 0);

        return tempFileList;
    }

    public static boolean uploadLogsToServer(Context context, String message) {

        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024 * 10;

        File logDirectory = new File(logDirectoryPath);
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        String[] dateString = new String[3];
        ArrayList<File> dateDirectories = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            dateString[i] = now.minusDays(i).format(formatter);
            //find files that start with the date
            int finalI = i;
            File[] tempFileList = logDirectory.listFiles((dir, name) -> name.startsWith(dateString[finalI]));
            if (tempFileList != null) {
                dateDirectories.addAll(Arrays.asList(tempFileList));
            }
        }

        File[] tempFileList = dateDirectories.toArray(new File[0]);
        File tempDirectory = new File(logDirectory, "/temp");
        if (tempDirectory.exists()) {
            Utils.deleteDirectory(tempDirectory);
        }
        tempDirectory.mkdirs();
        for (int i = 0; i < tempFileList.length; i++) {
            File tempFile = new File(tempDirectory, "/" + tempFileList[i].getName());
            try {
                Utils.copyFileOrDirectory(tempFileList[i], tempFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //make a text file with message
        String deviceName = PreferenceManager.getString(context, "device_name");
        File messageFile = new File(tempDirectory, "/" + deviceName + ".txt");
        Utils.writeToFile(messageFile, message);

        File data = Environment.getDataDirectory();
        //copy database to temp directory
        String currentDBPath = "//data//" + context.getPackageName()
                + "//databases//" + DBManager.DATABASE_NAME;
        File currentDB = new File(data, currentDBPath);
        File tempDB = new File(tempDirectory, "/" + DBManager.DATABASE_NAME);
        try {
            Utils.copyFile(currentDB, tempDB);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //copy preference to temp directory
        String currentSPPath = "//data//" + context.getPackageName()
                + "//shared_prefs//" + context.getPackageName() + "_preferences.xml";
        File currentSP = new File(data, currentSPPath);
        File tempSP = new File(tempDirectory, "/" + context.getPackageName() + "_preferences.xml");
        try {
            Utils.copyFile(currentSP, tempSP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //make a zip file
        String dateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        String koreanName = PreferenceManager.getString(context, "device_name");
        //convert korean letters to unicode
        koreanName = Utils.convertKoreanToRoman(koreanName);
        String zipFileName = "[" + koreanName + "]_" + Utils.getSerialNumber(context) + "_" + dateTime + ".zip";
        File zipFile = new File(logDirectory, "/" + zipFileName);

        File sourceFile = null;
        try {
            sourceFile = Utils.zipDirectory(tempDirectory, zipFile);
            String sourceFileUri = sourceFile.getAbsolutePath();
            //korean letters to ascii

            if (sourceFile.isFile()) {
                try {

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(context.getString(R.string.report_url));

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", sourceFileUri);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + sourceFileUri + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    int serverResponseCode = conn.getResponseCode();

                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                    conn.disconnect();

                    if (serverResponseCode == 200) {
                        sourceFile.delete();
                        tempDirectory.delete();
                        return true;
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    try {
                        sourceFile.delete();
                        tempDirectory.delete();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    return false;
                }
            }
        } catch (IOException e0) {
            e0.printStackTrace();
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (isStarted) return mBinder;
        isStarted = true;
        try {
            boolean rebooted = intent.getBooleanExtra("rebooted", false);
            logThread = new LogThread(getApplicationContext(), logDirectoryPath, rebooted);
            logThread.start();
        } catch (RuntimeException e) {
            Utils.logE(TAG, e.getMessage());
        }
        Utils.logD(TAG, "Log Service start");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class LogBinder extends Binder {
        public LogService getService() { // 서비스 객체를 리턴
            return LogService.this;
        }
    }
}
