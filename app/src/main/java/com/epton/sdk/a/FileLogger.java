//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.epton.sdk.a;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


//c
public class FileLogger {
    private static final String TAG = "Loggers";
    private static String logDirectory = null;
    private static SimpleDateFormat dateFormat;
    private static Date currentDate;
    private static String logFilePath;

    public FileLogger() {
    }

    //a
    public static void initializeLogger(Context context) {
        setupLogDirectory(context);
    }

    //b
    private static void setupLogDirectory(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            logDirectory = context.getExternalFilesDir((String)null).getPath() + File.separator + "log";
        }
        logFilePath = logDirectory + File.separator + "log_" + dateFormat.format(new Date()) + ".log";
        File directory = new File(logDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

    }

    //a
    public static void logMessage(String message) {
        writeToFile(message);
    }

    //b
    private static void writeToFile(String var0) {
        if (null == logDirectory) {
            Log.e("Loggers", "logPath == null ，未初始化");
        } else {
            dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss:SSS");
            Date timestamp = new Date();
            String formattedMessage = dateFormat.format(timestamp) + " " + var0 + "\n";
            FileOutputStream fileOutput = null;
            BufferedWriter bufferedWriter = null;

            try {
                fileOutput = new FileOutputStream(logFilePath, true);
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutput));
                bufferedWriter.write(formattedMessage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        currentDate = new Date();
    }
}
