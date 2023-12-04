package com.releasetech.multidevice.Database;

import android.content.Context;
import android.os.Environment;

import com.releasetech.multidevice.Tool.Utils;

import java.io.DataOutputStream;
import java.io.File;

public class Backup {
    private static final String TAG = "[BACKUP]";

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_NO_DISK = 1;
    public static final int RESULT_NO_FILE = 2;
    public static final int RESULT_UNKNOWN = 3;

    public static int importData(Context context, int trial) {
        try {
            String removableStoragePath = Utils.getRemovableStorage();
            if (removableStoragePath == null) {
                return RESULT_NO_DISK;
            }
            File sd = new File(removableStoragePath, "Backup");
            File data = Environment.getDataDirectory();
            String currentDBPath = "//data//" + context.getPackageName()
                    + "//databases//" + DBManager.DATABASE_NAME;
            String backupDBPath = "//" + DBManager.DATABASE_NAME;
            String currentSPPath = "//data//" + context.getPackageName()
                    + "//shared_prefs//" + context.getPackageName() + "_preferences.xml";
            String backupSPPath = "//" + context.getPackageName() + "_preferences.xml";
            String currentFilesPath = "//data//" + context.getPackageName();
            String backupFilesPath = "//files";
            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);
            File currentSP = new File(data, currentSPPath);
            File backupSP = new File(sd, backupSPPath);
            File currentFiles = new File(data, currentFilesPath);
            File backupFiles = new File(sd, backupFilesPath);

            boolean previousVersion = false;
            if(!backupDB.exists()) return RESULT_NO_FILE;
            if(!backupSP.exists()) {
                backupSPPath = "//" + context.getPackageName().replace(".mini", "") + "_preferences.xml";
                backupSP = new File(sd, backupSPPath);
                if(!backupSP.exists()){
                    return RESULT_NO_FILE;
                }
                // 여기까지 온 경우에는 com.releasetech.multidevice_preferences.xml가 있는 경우니까
                previousVersion = true;
            }

            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("find " + currentFiles.getAbsolutePath() + "-type f -delete\n");
            dos.writeBytes("yes | cp -rf " + backupDB.getAbsolutePath() + " " + currentDB.getAbsolutePath() + "\n");
            dos.writeBytes("yes | cp -rf " + backupSP.getAbsolutePath() + " " + currentSP.getAbsolutePath() + "\n");
            dos.writeBytes("yes | cp -rf " + backupFiles.getAbsolutePath() + " " + currentFiles.getAbsolutePath() + "\n");
            dos.writeBytes("chmod 777 -R " + currentDB.getAbsolutePath() + "\n");
            dos.writeBytes("chmod 777 -R " + currentSP.getAbsolutePath() + "\n");
            dos.writeBytes("chmod 777 -R " + currentFiles.getAbsolutePath() + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            p.waitFor();

            if (backupDB.length() == currentDB.length()) {
                Utils.logD(TAG, "DB 복사: 성공");
                DBManager dbManager = new DBManager(context);
                dbManager.open();
                dbManager.create();
                if(previousVersion) {

                    //todo 여기 수
                    dbManager.replaceSubText(DBManager.CATEGORY, "com.releasetech.multidevice", "com.releasetech.multidevice");
                    dbManager.replaceSubText(DBManager.PRODUCT_IMAGE, "com.releasetech.multidevice", "com.releasetech.multidevice");
                }
            } else {
                Utils.logD(TAG, "DB 복사: 실패");
                if (trial < 5) {
                    return exportData(context, trial + 1);
                } else {
                    return RESULT_UNKNOWN;
                }
            }
            if (backupSP.length() == currentSP.length()) {
                Utils.logD(TAG, "SP 복사: 성공");
            } else {
                Utils.logD(TAG, "SP 복사: 실패");
                if (trial < 5) {
                    return exportData(context, trial + 1);
                } else {
                    return RESULT_UNKNOWN;
                }
            }
            if (Utils.directorySize(backupFiles) == Utils.directorySize(currentFiles)) {
                Utils.logD(TAG, "Files 복사: 성공");
            } else {
                Utils.logD(TAG, "Files 복사: 실패");
                if (trial < 5) {
                    return exportData(context, trial + 1);
                } else {
                    return RESULT_UNKNOWN;
                }
            }

            return RESULT_SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();
            return RESULT_UNKNOWN;

        }
    }

    public static int exportData(Context context, int trial) {
        try {
            String removableStoragePath = Utils.getRemovableStorage();
            if (removableStoragePath == null) {
                return RESULT_NO_DISK;
            }
            File sd = new File(removableStoragePath, "Backup");
            File data = Environment.getDataDirectory();
            String currentDBPath = "//data//" + context.getPackageName()
                    + "//databases//" + DBManager.DATABASE_NAME;
            String backupDBPath = "//" + DBManager.DATABASE_NAME;
            String currentSPPath = "//data//" + context.getPackageName()
                    + "//shared_prefs//" + context.getPackageName() + "_preferences.xml";
            String backupSPPath = "//" + context.getPackageName() + "_preferences.xml";
            String currentFilesPath = "//data//" + context.getPackageName()
                    + "//files";
            String backupFilesPath = "//files";
            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);
            File currentSP = new File(data, currentSPPath);
            File backupSP = new File(sd, backupSPPath);
            File currentFiles = new File(data, currentFilesPath);
            File backupFiles = new File(sd, backupFilesPath);
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("rm -rf " + sd.getAbsolutePath() + "\n");
            dos.writeBytes("mkdir " + sd.getAbsolutePath() + "\n");
            dos.writeBytes("chmod 777 -R " + currentDB.getAbsolutePath() + "\n");
            dos.writeBytes("chmod 777 -R " + currentSP.getAbsolutePath() + "\n");
            dos.writeBytes("chmod 777 -R " + currentFiles.getAbsolutePath() + "\n");
            dos.writeBytes("yes | cp -rf " + currentDB.getAbsolutePath() + " " + backupDB.getAbsolutePath() + "\n");
            dos.writeBytes("yes | cp -rf " + currentSP.getAbsolutePath() + " " + backupSP.getAbsolutePath() + "\n");
            dos.writeBytes("yes | cp -rf " + currentFiles.getAbsolutePath() + " " + sd.getAbsolutePath() + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            p.waitFor();

            if (backupDB.length() == currentDB.length()) {
                Utils.logD(TAG, "DB 복사: 성공");
            } else {
                Utils.logD(TAG, "DB 복사: 실패");
                if (trial < 5) {
                    return exportData(context, trial + 1);
                } else {
                    return RESULT_UNKNOWN;
                }
            }
            if (backupSP.length() == currentSP.length()) {
                Utils.logD(TAG, "SP 복사: 성공");
            } else {
                Utils.logD(TAG, "SP 복사: 실패");
                if (trial < 5) {
                    return exportData(context, trial + 1);
                } else {
                    return RESULT_UNKNOWN;
                }
            }
            if (Utils.directorySize(backupFiles) == Utils.directorySize(currentFiles)) {
                Utils.logD(TAG, "Files 복사: 성공");
            } else {
                Utils.logD(TAG, "Files 복사: 실패");
                if (trial < 5) {
                    return exportData(context, trial + 1);
                } else {
                    return RESULT_UNKNOWN;
                }
            }
            return RESULT_SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();
            return RESULT_UNKNOWN;
        }
    }
}
