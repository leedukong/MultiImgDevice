package com.releasetech.multidevice.Tool;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Cache {
    private static final String TAG = "[CACHE]";

    public static File getCacheDir(Context context) {
        File cacheDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(context.getExternalCacheDir(), "cachefolder");
            if (!cacheDir.isDirectory()) {
                cacheDir.mkdirs();
            }
        }
        if (!cacheDir.isDirectory()) {
            cacheDir = context.getCacheDir();
        }
        return cacheDir;
    }

    public static void write(Context context, String cacheName, Object obj) throws IOException {
        File cacheDir = getCacheDir(context);
        File cacheFile = new File(cacheDir, cacheName + ".txt");
        if (!cacheFile.exists()) {
            cacheFile.createNewFile();
        }

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(cacheFile));
        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
        objectOutputStream.close();
    }

    public static Object Read(Context context, String cacheName) throws IOException, ClassNotFoundException {
        File cacheDir = getCacheDir(context);
        File cacheFile = new File(cacheDir, cacheName + ".txt");

        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(cacheFile));
        Object object = objectInputStream.readObject();
        objectInputStream.close();
        return object;
    }
}

