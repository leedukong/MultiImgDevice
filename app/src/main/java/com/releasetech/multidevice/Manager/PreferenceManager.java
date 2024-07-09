package com.releasetech.multidevice.Manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;

import java.util.HashSet;
import java.util.Set;

@SuppressLint("NewApi")
public class PreferenceManager {
    private static final String TAG = "[PREFERENCE MANAGER]";
    private static final String DEFAULT_VALUE_STRING = "";
    private static final boolean DEFAULT_VALUE_BOOLEAN = false;
    private static final int DEFAULT_VALUE_INT = -1;
    private static final long DEFAULT_VALUE_LONG = -1L;
    private static final float DEFAULT_VALUE_FLOAT = -1F;

    private static SharedPreferences.OnSharedPreferenceChangeListener listener;

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.preferences_name), Context.MODE_PRIVATE);
    }

    public static boolean hasKey(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.contains(key);
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setStringSet(Context context, String key, Set<String> value) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void setInt(Context context, String key, int value) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void setLong(Context context, String key, long value) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void setFloat(Context context, String key, float value) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) {
        SharedPreferences prefs = getPreferences(context);
        Resources res = context.getResources();
        String defaultValue = DEFAULT_VALUE_STRING;
        try {
            defaultValue = res.getString(Utils.getResId(key, R.string.class));
        }catch(Exception e){
            try{
                defaultValue = Integer.toString(res.getInteger(Utils.getResId(key, R.integer.class)));
            }catch(Exception e2){
//                Utils.logD(TAG, key + " string not in initial preferences");
            }
        }
        String value;
        try {
            value = prefs.getString(key, defaultValue);
            if (value.isEmpty()) {
                value = defaultValue;
            }
        }catch(Exception e){
            SharedPreferences.Editor editor = prefs.edit();
            if(prefs.contains(key)) {
                editor.remove(key);
            }
            editor.putString(key, defaultValue);
            editor.apply();
            value = defaultValue;
        }
        return value;
    }

    public static Set<String> getStringSet(Context context, String key){
        SharedPreferences prefs = getPreferences(context);
        Resources res = context.getResources();
        Set<String> defaultValue = new HashSet<>();
        try {
            defaultValue = Set.of(res.getStringArray(Utils.getResId(key, R.array.class)));
        }catch(Exception e){
//            Utils.logD(TAG, key + " string set not in initial preferences");
        }
        Set<String> value;
        try {
            value = prefs.getStringSet(key, Set.of());
        }catch(Exception e){
            SharedPreferences.Editor editor = prefs.edit();
            if(prefs.contains(key)) {
                editor.remove(key);
            }
            editor.putStringSet(key, defaultValue);
            editor.apply();
            value = defaultValue;
        }
        return value;
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences prefs = getPreferences(context);
        Resources res = context.getResources();
        boolean defaultValue = DEFAULT_VALUE_BOOLEAN;
        try {
            defaultValue = res.getBoolean(Utils.getResId(key, R.bool.class));
        }catch(Exception e){
//            Utils.logD(TAG, key + " boolean not in initial preferences");
        }
        boolean value;
        try {
            value = prefs.getBoolean(key, defaultValue);
        }catch(Exception e){
            SharedPreferences.Editor editor = prefs.edit();
            if(prefs.contains(key)) {
                editor.remove(key);
            }
            editor.putBoolean(key, defaultValue);
            editor.apply();
            value = defaultValue;
        }
        return value;
    }

    public static int getInt(Context context, String key) {
        SharedPreferences prefs = getPreferences(context);
        Resources res = context.getResources();
        int defaultValue = DEFAULT_VALUE_INT;
        try{
            defaultValue = res.getInteger(Utils.getResId(key, R.integer.class));
        }catch(Exception e2){
//                Utils.logD(TAG, key + " string not in initial preferences");
        }
        int value;
        try {
             value = prefs.getInt(key, defaultValue);
        }catch (ClassCastException e){
            value = getString(context, key).isEmpty() ? defaultValue : Integer.parseInt(getString(context, key));
        }
        return value;
    }

    public static long getLong(Context context, String key) {
        SharedPreferences prefs = getPreferences(context);
        long value = prefs.getLong(key, DEFAULT_VALUE_LONG);
        return value;
    }

    public static float getFloat(Context context, String key) {
        SharedPreferences prefs = getPreferences(context);
        float value;
        try {
            value = prefs.getFloat(key, DEFAULT_VALUE_FLOAT);
        } catch (ClassCastException e) {
            value = getString(context, key).isEmpty() ? DEFAULT_VALUE_FLOAT : Utils.getFloat(getString(context, key));
        }
        return value;
    }

    public static void removeKey(Context context, String key) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(key);
        edit.apply();
    }

    public static void clear(Context context) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.apply();
    }

    public static void registerChangeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences prefs = getPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

}