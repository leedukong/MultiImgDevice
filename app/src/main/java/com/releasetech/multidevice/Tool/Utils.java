package com.releasetech.multidevice.Tool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.releasetech.multidevice.MainActivity;

//import com.releasetech.multidevice.Database.DBManager;
//import com.releasetech.multidevice.MainActivity;
//import com.releasetech.multidevice.Manager.PreferenceManager;
//import com.releasetech.multidevice.R;
//import com.takisoft.preferencex.EditTextPreference;
//import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

    private static Toast sToast;

    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
//            e.printStackTrace();
            return -1;
        }
    }

    public static void showToast(Context context, String message) {
        if (sToast == null) {
            sToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            sToast.setGravity(Gravity.CENTER, 0, 0);
            ViewGroup group = (ViewGroup) sToast.getView();
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(30);
        } else {
            sToast.setText(message);
        }
        sToast.show();
    }

    @SuppressLint("Range")
    public static String getFileName(Activity activity, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isImage(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        return (options.outWidth != -1 && options.outHeight != -1);
    }

    public static boolean isVideo(String filePath) {
        String mimeType = URLConnection.guessContentTypeFromName(filePath);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static boolean isImage(File file) {
        return isImage(file.getAbsolutePath());
    }

    public static boolean isVideo(File file) {
        return isVideo(file.getAbsolutePath());
    }

    public static String getRemovableStorage() {
        String removableStoragePath = null;
        File fileList[] = new File("/storage/").listFiles();
        for (File file : fileList) {
            if (!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead())
                removableStoragePath = file.getAbsolutePath();
        }
        return removableStoragePath;
    }


    public static int getSelectedRadioButtonIndex(View view, int radioGroupId) {
        RadioGroup radioGroup = view.findViewById(radioGroupId);
        int radioButtonID = radioGroup.getCheckedRadioButtonId();
        View radioButton = radioGroup.findViewById(radioButtonID);
        return radioGroup.indexOfChild(radioButton);
    }


    public static void restart(Context context) {
        //A - causes Exception
//        PackageManager packageManager = context.getPackageManager();
//        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
//        ComponentName componentName = intent.getComponent();
//        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
//        context.startActivity(mainIntent);
//        Runtime.getRuntime().exit(0);
        //B
//        Intent mStartActivity = new Intent(context, MainActivity.class);
//        int mPendingIntentId = 123456;
//        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public static Dialog alert(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        disableDialogCancel(builder);
        Dialog tempDialog = builder.setMessage(msg).setPositiveButton("확인", null).show();
        TextView messageView = tempDialog.findViewById(android.R.id.message);
        messageView.setTextSize(26);
        return tempDialog;
    }

    public static void timedAlert(Context context, String msg, int duration) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            AlertDialog dialog = builder.setMessage(msg).show();

            TextView messageView = dialog.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.CENTER);
            messageView.setTextSize(32);
            dialog.getWindow().
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            dialog.getWindow().setLayout(550, 200);
            dialog.setCanceledOnTouchOutside(false);

            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(dialog::dismiss, duration * 1000L);
        } catch (WindowManager.BadTokenException e) {
            Utils.logD("timedAlert", "BadTokenException");
        }
    }

    public static void disableDialogCancel(AlertDialog.Builder builder) {
        builder.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK &&
                    event.getAction() == KeyEvent.ACTION_UP &&
                    !event.isCanceled()) {
                return true;
            }
            return false;
        });
    }

    public static String getSerialNumber(Context context) {
        String serialNo = null;
        try {

            String device_name = Settings.Global.getString(context.getContentResolver(), "device_name");
            String command = "getprop sys.serialno";
            if (device_name.equals("eightpresso_basic_b")) {
                command = "getprop vendor.serialno";
            }
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
            serialNo = log.toString();
        } catch (IOException e) {
        }
        return serialNo;
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    String hex = Integer.toHexString(b & 0xFF);
                    if (hex.length() == 1)
                        hex = "0".concat(hex);
                    res1.append(hex.concat(":"));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public static void logD(String tag, String str) {
        Log.d(MainActivity.DEVTAG, tag + " " + str);
    }

    public static void logE(String tag, String str) {
        Log.e(MainActivity.DEVTAG, tag + " " + str);
    }

    public static void setRangeFilter(PreferenceFragmentCompat preferenceFragmentCompat, String preferenceName, int min, int max) {

        EditTextPreference pref = preferenceFragmentCompat.findPreference(preferenceName);
        if (pref != null) {
            pref.setOnBindEditTextListener(
                    editText -> editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, max)})
            );
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.toString().isEmpty()) {
                    Utils.timedAlert(preferenceFragmentCompat.getContext(), "값을 입력하세요.", 2);
                    return false;
                }
                int value = Integer.parseInt(newValue.toString());
                if (value < min || value > max) {
                    Utils.timedAlert(preferenceFragmentCompat.getContext(), min + " ~ " + max + "범위에서 설정해주세요.", 2);
                    return false;
                }
                return true;
            });
        }
    }

    public static float getFloat(String str) {
        try {
            return Float.parseFloat(str);
        } catch (Exception e) {
            return 0;
        }
    }


    public static long directorySize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += directorySize(file);
        }
        return length;
    }

    public static void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    public static void copyDirectory(File src, File dst) throws IOException {
        if (src.isDirectory()) {
            if (!dst.exists()) {
                dst.mkdir();
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dst, file);
                copyDirectory(srcFile, destFile);
            }
        } else {
            copyFile(src, dst);
        }
    }

    public static void copyFileOrDirectory(File src, File dst) throws IOException {
        if (src.isDirectory()) {
            copyDirectory(src, dst);
        } else {
            copyFile(src, dst);
        }
    }

    public static File zipDirectory(File directory, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zip(directory, directory, zos);
        }
        return zipFile;
    }

    public static void zip(File directory, File base, ZipOutputStream zos) throws IOException {
        File[] files = directory.listFiles();
        byte[] buffer = new byte[8192];
        int read = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                zip(file, base, zos);
            } else {
                try (FileInputStream in = new FileInputStream(file)) {
                    ZipEntry entry = new ZipEntry(file.getPath().substring(base.getPath().length() + 1));
                    zos.putNextEntry(entry);
                    while (-1 != (read = in.read(buffer))) {
                        zos.write(buffer, 0, read);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    public static void writeToFile(File messageFile, String message) {
        try (FileOutputStream fos = new FileOutputStream(messageFile, true)) {
            fos.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        directory.delete();
    }

    public static boolean startsWithAny(String name, String[] prefixes) {
        for (String prefix : prefixes) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static Bitmap textToQR(String text) {
        String editText = text.trim();
        MultiFormatWriter writer = new MultiFormatWriter();
        Bitmap bitmap = null;
        try {
            //바코드 생성
            BitMatrix matrix = writer.encode(editText, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmap = encoder.createBitmap(matrix);

        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static byte[] getHmacSHA256(String key, String input)
            throws Exception {
        final String HMAC_SHA_256 = "HmacSHA256";
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(new SecretKeySpec(key.getBytes(), HMAC_SHA_256));
        byte[] bs = mac.doFinal(input.getBytes());
        return bs;
    }

    public static char shift(char c) {
        if (c == ';') {
            return ':';
        } else if (c == '1') {
            return '!';
        } else if (c == '2') {
            return '@';
        } else if (c == '3') {
            return '#';
        } else if (c == '4') {
            return '$';
        } else if (c == '5') {
            return '%';
        } else if (c == '6') {
            return '^';
        } else if (c == '7') {
            return '&';
        } else if (c == '8') {
            return '*';
        } else if (c == '9') {
            return '(';
        } else if (c == '0') {
            return ')';
        } else if (c == '-') {
            return '_';
        } else if (c == '=') {
            return '+';
        } else if (c == '[') {
            return '{';
        } else if (c == ']') {
            return '}';
        } else if (c == '\\') {
            return '|';
        } else if (c == ',') {
            return '<';
        } else if (c == '.') {
            return '>';
        } else if (c == '/') {
            return '?';
        } else if (c == '\'') {
            return '\"';
        } else if ('a' <= c && c <= 'z') {
            return (char) (c - 32);
        } else if ('A' <= c && c <= 'Z') {
            return (char) (c + 32);
        } else {
            return c;
        }
    }

    public static String getColorSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }

    public static int parsePrice(CharSequence cs) {
        String str = (String) cs;
        str = str.replace("+", "");
        int price = 0;
        try {
            price = Integer.parseInt(str);
            return price;
        } catch (Exception e) {
            return price;
        }
    }

    public static String convertKoreanToRoman(String koreanName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < koreanName.length(); i++) {
            char c = koreanName.charAt(i);
            if (c >= 0xAC00 && c <= 0xD7A3) {
                int uniVal = c - 0xAC00;
                int cho = ((uniVal - (uniVal % 28)) / 28) / 21;
                int jung = ((uniVal - (uniVal % 28)) / 28) % 21;
                int jong = (uniVal % 28);
                switch (cho) {
                    case 0:
                        sb.append("g");
                        break;
                    case 1:
                        sb.append("kk");
                        break;
                    case 2:
                        sb.append("n");
                        break;
                    case 3:
                        sb.append("d");
                        break;
                    case 4:
                        sb.append("tt");
                        break;
                    case 5:
                        sb.append("r");
                        break;
                    case 6:
                        sb.append("m");
                        break;
                    case 7:
                        sb.append("b");
                        break;
                    case 8:
                        sb.append("pp");
                        break;
                    case 9:
                        sb.append("s");
                        break;
                    case 10:
                        sb.append("ss");
                        break;
                    case 11:
                        sb.append("");
                        break;
                    case 12:
                        sb.append("j");
                        break;
                    case 13:
                        sb.append("jj");
                        break;
                    case 14:
                        sb.append("ch");
                        break;
                    case 15:
                        sb.append("k");
                        break;
                    case 16:
                        sb.append("t");
                        break;
                    case 17:
                        sb.append("p");
                        break;
                    case 18:
                        sb.append("h");
                        break;
                }
                switch (jung) {
                    case 0:
                        sb.append("a");
                        break;
                    case 1:
                        sb.append("ae");
                        break;
                    case 2:
                        sb.append("ya");
                        break;
                    case 3:
                        sb.append("yae");
                        break;
                    case 4:
                        sb.append("eo");
                        break;
                    case 5:
                        sb.append("e");
                        break;
                    case 6:
                        sb.append("yeo");
                        break;
                    case 7:
                        sb.append("ye");
                        break;
                    case 8:
                        sb.append("o");
                        break;
                    case 9:
                        sb.append("wa");
                        break;
                    case 10:
                        sb.append("wae");
                        break;
                    case 11:
                        sb.append("oe");
                        break;
                    case 12:
                        sb.append("yo");
                        break;
                    case 13:
                        sb.append("u");
                        break;
                    case 14:
                        sb.append("weo");
                        break;
                    case 15:
                        sb.append("we");
                        break;
                    case 16:
                        sb.append("wi");
                        break;
                    case 17:
                        sb.append("yu");
                        break;
                    case 18:
                        sb.append("eu");
                        break;
                    case 19:
                        sb.append("ui");
                        break;
                    case 20:
                        sb.append("i");
                        break;
                }
                switch (jong) {
                    case 0:
                        sb.append("");
                        break;
                    case 1:
                        sb.append("g");
                        break;
                    case 2:
                        sb.append("kk");
                        break;
                    case 3:
                        sb.append("gs");
                        break;
                    case 4:
                        sb.append("n");
                        break;
                    case 5:
                        sb.append("nj");
                        break;
                    case 6:
                        sb.append("nh");
                        break;
                    case 7:
                        sb.append("d");
                        break;
                    case 8:
                        sb.append("l");
                        break;
                    case 9:
                        sb.append("lg");
                        break;
                    case 10:
                        sb.append("lm");
                        break;
                    case 11:
                        sb.append("lb");
                        break;
                    case 12:
                        sb.append("ls");
                        break;
                    case 13:
                        sb.append("lt");
                        break;
                    case 14:
                        sb.append("lp");
                        break;
                    case 15:
                        sb.append("lh");
                        break;
                    case 16:
                        sb.append("m");
                        break;
                    case 17:
                        sb.append("b");
                        break;
                    case 18:
                        sb.append("bs");
                        break;
                    case 19:
                        sb.append("s");
                        break;
                    case 20:
                        sb.append("ss");
                        break;
                    case 21:
                        sb.append("ng");
                        break;
                    case 22:
                        sb.append("j");
                        break;
                    case 23:
                        sb.append("ch");
                        break;
                    case 24:
                        sb.append("k");
                        break;
                    case 25:
                        sb.append("t");
                        break;
                    case 26:
                        sb.append("p");
                        break;
                    case 27:
                        sb.append("h");
                        break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}