package com.releasetech.multidevice.AdminSettings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.releasetech.multidevice.AdminSettings.ProductManage.ProductManageFragment;
import com.releasetech.multidevice.Manager.PasswordManager;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.MediaReplacer;
import com.releasetech.multidevice.Tool.Utils;
import com.takisoft.preferencex.EditTextPreference;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import android_serialport_api.SerialPortFinder;


public class AdminSettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TAG = "[ADMIN SETTINGS]";

    protected static Intent rustDeskIntent;
    protected static Intent remoteViewIntent;
    protected static Intent niceIntent;
    public static final int PICK_IMAGE_REQUEST = 1;

    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                //ImageView img = findViewById(R.id.product_serial_state);

            } catch (NullPointerException e) {
            }
        }
    };

    public Handler getHandler() {
        return handler;
    }

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // 선택된 파일의 URI 처리
                    Uri selectedFileUri = result.getData().getData();
                    // 선택한 파일 URI를 처리하는 로직 작성
                    // 예: 파일의 경로를 얻어오거나, Uri를 사용하여 파일 열기
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                setTitle("관리자 설정");
                return;
            }
            int index = getSupportFragmentManager().getBackStackEntryCount() - 1;
            if (index < 0) return;
            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(index);
            String tag = backEntry.getName();
            Utils.logD(TAG, "설정 페이지 : " + tag);
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment == null) return;
            if (fragment instanceof ParentPreferenceFragment) {
                setTitle(tag);
                ((ParentPreferenceFragment) fragment).changeTitles();
            }
        });
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        checkSettingsWritable();
        rustDeskIntent = getPackageManager().getLaunchIntentForPackage("com.carriez.flutter_hbb");
        remoteViewIntent = getPackageManager().getLaunchIntentForPackage("com.rsupport.mobile.agent");
        niceIntent = getPackageManager().getLaunchIntentForPackage("kr.co.nicevan.androidnvcat");
    }

    private void checkSettingsWritable() {
        if (!Settings.System.canWrite(this)) {

            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                startActivity(intent);
                Utils.showToast(this, "시스템 설정 수정 권한을 허용해주세요");
            } catch (Exception e) {
                // ask user to grant permission manually
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull androidx.preference.PreferenceFragmentCompat caller, @NonNull Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        String title = pref.getTitle().toString();
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment, title)
                .addToBackStack(title)
                .commit();
        setTitle(title);
        return true;
    }

    public static abstract class ParentPreferenceFragment extends PreferenceFragmentCompat {
        protected void changeTitles() {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                Utils.restart(this);
            } else {
                onBackPressed();
            }
            return true;
        }
        return false;
    }


    public static class AdminInfoFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.admin_info_preferences, rootKey);

            EditTextPreference managerPassword = findPreference("manager_password");
            EditTextPreference adminPassword = findPreference("admin_password");
            EditTextPreference dessertPassword = findPreference("dessert_password");

            if (managerPassword != null) {
                managerPassword.setOnBindEditTextListener(
                        editText -> editText.setFilters(new InputFilter[]{
                                new InputFilter.LengthFilter(4),
                        })
                );
                managerPassword.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (newValue.toString().length() != 4 || newValue.toString().equals(adminPassword.getText())) {
                        Utils.timedAlert(getContext(), "비밀번호는 4자리이며, 관리자 \n 비밀번호와 동일할 수 없습니다.", 2);
                        return false;
                    }
                    return true;
                });

                adminPassword.setOnPreferenceClickListener(preference -> {
                    if (managerPassword.getText().length() != 4) {
                        Utils.timedAlert(getContext(), "관리자 비밀번호를 먼저 설정해주세요.", 2);
                        return false;
                    }
                    return true;
                });
            }

            if (adminPassword != null) {
                adminPassword.setOnBindEditTextListener(
                        editText -> editText.setFilters(new InputFilter[]{
                                new InputFilter.LengthFilter(4),
                        })
                );
                adminPassword.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (newValue.toString().length() != 4 || newValue.toString().equals(managerPassword.getText())) {
                        Utils.timedAlert(getContext(), "비밀번호는 4자리이며, 매니저 \n 비밀번호와 동일할 수 없습니다.", 2);
                        return false;
                    }
                    return true;
                });
            }
        }
    }

    public static class CartFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.cart_preferences, rootKey);
            Utils.setRangeFilter(this, "cart_quantity", 1, 5);

            Preference btnCheckoutSettings = findPreference("checkout_settings");
            btnCheckoutSettings.setOnPreferenceClickListener(preference -> {
                startActivity(niceIntent);
                //CheckoutManager.openSettings(getActivity());
                return true;
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            Utils.restart(this);
        } else {
            super.onBackPressed();
        }
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.admin_preferences, rootKey);
            setupSpecialPreferences();
        }

        private boolean isSerialDevice(int vendorId, int productId) {
            // 여기에 특정 Vendor ID 및 Product ID의 조합으로 시리얼 장치를 식별하는 논리를 구현
            // 예시: USB-to-Serial 어댑터의 Vendor ID 및 Product ID
            return (vendorId == 0x1234 && productId == 0x5678);
        }

        private void setupSpecialPreferences() {

//            EditTextPreference deviceName = findPreference("device_name");
//            if (deviceName != null) {
//                deviceName.setOnPreferenceChangeListener((preference, newValue) -> {
//                    String name = ((String) newValue).replaceAll("\\s+", "");
//                    if (name.equals("데이터초기화")) {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                        Dialog tempDialog = builder.setMessage("판매 및 결제 데이터를 삭제하시겠습니까?\n\n*주의* 삭제된 데이터는 복구되지 않습니다")
//                                .setPositiveButton("Yes", (dialog, which) -> {
//                                            Utils.deleteAllData(getContext());
//                                            Utils.alert(getContext(), "데이터 초기화 완료");
//                                        }
//                                )
//                                .setNegativeButton("No", null).show();
//                        TextView messageView = tempDialog.findViewById(android.R.id.message);
//                        messageView.setTextSize(26);
//                        return false;
//                    }
//                    //remove spaces
//                    else if (name.equals("기본 홈")) {
//                        Intent launchIntent = requireContext().getPackageManager().getLaunchIntentForPackage("com.android.launcher3");
//                        startActivity(launchIntent);
//                    }
//                    return true;
//                });
//            }

            Preference btnAndroidSettings = findPreference("android_settings");
            btnAndroidSettings.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                return true;
            });

            Preference btnRestartApp = findPreference("restart_app");
            btnRestartApp.setOnPreferenceClickListener(preference -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                Dialog tempDialog = builder.setMessage("MULTIDEVICE를 재시작하시겠습니까?").setPositiveButton("Yes", (dialog, which) -> Utils.restart(getContext()))
                        .setNegativeButton("No", null).show();
                TextView messageView = tempDialog.findViewById(android.R.id.message);
                messageView.setTextSize(26);
                return true;
            });

        }
    }
    public static class DeviceInfoFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.device_info_preferences, rootKey);

            EditTextPreference ipAddress = findPreference("ip_address");
            ipAddress.setText(getWifiIpAddress(getActivity()));
        }

        protected String getWifiIpAddress(Context context) {
            try {
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf : interfaces) {
                    List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                    for (InetAddress addr : addrs) {
                        if (!addr.isLoopbackAddress()) {
                            String sAddr = addr.getHostAddress();
                            boolean isIPv4 = sAddr.indexOf(':') < 0;
                            if (isIPv4) return sAddr;
                        }
                    }
                }
            } catch (SocketException ignore) {
            }

            WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

            if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                ipAddress = Integer.reverseBytes(ipAddress);
            }

            byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

            String ipAddressString;
            try {
                ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
                return ipAddressString;
            } catch (UnknownHostException ex) {
                Utils.logE(TAG, "IP 주소 불러오기 실패");
                return "IP를 불러올 수 없습니다";
            }
        }
    }

    public static class IdleScreenFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.idle_screen_preferences, rootKey);
            ListPreference adType = findPreference("ad_type_preference");
            EditTextPreference idleScreenText = findPreference("message_idle");
            EditTextPreference idleScreenTextColor = findPreference("message_color");
            EditTextPreference imageTime = findPreference("ad_image_time");
            EditTextPreference imageCount = findPreference("ad_image");
            Preference adImage1 = findPreference("ad_image1");
            Preference adImage2 = findPreference("ad_image2");
            Preference adImage3 = findPreference("ad_image3");
            Preference adImage4 = findPreference("ad_image4");
            Preference adImage5 = findPreference("ad_image5");
            EditTextPreference videoCount = findPreference("ad_video");
            Preference adVideo1 = findPreference("ad_video1");
            Preference adVideo2 = findPreference("ad_video2");
            Preference adVideo3 = findPreference("ad_video3");
            Preference adVideo4 = findPreference("ad_video4");
            Preference adVideo5 = findPreference("ad_video5");

            if (adType != null) {
                adType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {

                        int index = adType.findIndexOfValue(newValue.toString());
                        if (index >= 0) {
                            // Entry 값을 Summary로 설정
                            adType.setSummary(adType.getEntries()[index]);
                        }
                        String value = newValue.toString();
                        // 선택한 값에 따라 처리
                        if (value.equals("text")) {
                            Log.i("test", "text");
                            PreferenceManager.setString(getContext(), "ad_type", "text");
                            idleScreenText.setVisible(true); idleScreenTextColor.setVisible(true);
                            imageCount.setVisible(false); imageTime.setVisible(false); adImage1.setVisible(false); adImage2.setVisible(false); adImage3.setVisible(false); adImage4.setVisible(false); adImage5.setVisible(false);
                            videoCount.setVisible(false); adVideo1.setVisible(false); adVideo2.setVisible(false); adVideo3.setVisible(false); adVideo4.setVisible(false); adVideo5.setVisible(false);
                        } else if (value.equals("image")) {
                            Log.i("test", "image");
                            PreferenceManager.setString(getContext(), "ad_type", "image");
                            idleScreenText.setVisible(false); idleScreenTextColor.setVisible(false);
                            imageCount.setVisible(true); imageTime.setVisible(true);
                            for(int image=1; image<= 5; image++){
                                Preference adImage = null;
                                switch (image) {
                                    case 1:
                                        adImage = adImage1;
                                        break;
                                    case 2:
                                        adImage = adImage2;
                                        break;
                                    case 3:
                                        adImage = adImage3;
                                        break;
                                    case 4:
                                        adImage = adImage4;
                                        break;
                                    case 5:
                                        adImage = adImage5;
                                        break;
                                }
                                if (adImage != null) {
                                    adImage.setVisible(image <= PreferenceManager.getInt(getContext(), "ad_video_count")); // imageCount 이하일 경우 true
                                }
                            }
                            videoCount.setVisible(false); adVideo1.setVisible(false); adVideo2.setVisible(false); adVideo3.setVisible(false); adVideo4.setVisible(false); adVideo5.setVisible(false);
                        } else if (value.equals("video")) {
                            Log.i("test", "video");
                            PreferenceManager.setString(getContext(), "ad_type", "video");
                            idleScreenText.setVisible(false); idleScreenTextColor.setVisible(false);
                            imageCount.setVisible(false); imageTime.setVisible(false); adImage1.setVisible(false); adImage2.setVisible(false); adImage3.setVisible(false); adImage4.setVisible(false); adImage5.setVisible(false);
                            videoCount.setVisible(true);
                            for(int video=1; video<= 5; video++){
                                Preference adVideo = null;
                                switch (video) {
                                    case 1:
                                        adVideo = adVideo1;
                                        break;
                                    case 2:
                                        adVideo = adVideo2;
                                        break;
                                    case 3:
                                        adVideo = adVideo3;
                                        break;
                                    case 4:
                                        adVideo = adVideo4;
                                        break;
                                    case 5:
                                        adVideo = adVideo5;
                                        break;
                                }
                                if (adVideo != null) {
                                    adVideo.setVisible(video <= PreferenceManager.getInt(getContext(), "ad_video_count")); // imageCount 이하일 경우 true
                                }
                            }
                        }
                        return true;
                    }
                });
            }else{
                Log.i("test", "비어있음");
            }

            if(PreferenceManager.getString(getContext(), "ad_type").equals("text")){
                idleScreenText.setVisible(true); idleScreenTextColor.setVisible(true);
                imageCount.setVisible(false); imageTime.setVisible(false); adImage1.setVisible(false); adImage2.setVisible(false); adImage3.setVisible(false); adImage4.setVisible(false); adImage5.setVisible(false);
                videoCount.setVisible(false); adVideo1.setVisible(false); adVideo2.setVisible(false); adVideo3.setVisible(false); adVideo4.setVisible(false); adVideo5.setVisible(false);
            }else if(PreferenceManager.getString(getContext(), "ad_type").equals("image")){
                idleScreenText.setVisible(false); idleScreenTextColor.setVisible(false);
                imageCount.setVisible(true); imageTime.setVisible(true);
                for(int image=1; image<= 5; image++){
                    Preference adImage = null;
                    switch (image) {
                        case 1:
                            adImage = adImage1;
                            break;
                        case 2:
                            adImage = adImage2;
                            break;
                        case 3:
                            adImage = adImage3;
                            break;
                        case 4:
                            adImage = adImage4;
                            break;
                        case 5:
                            adImage = adImage5;
                            break;
                    }
                    if (adImage != null) {
                        adImage.setVisible(image <= PreferenceManager.getInt(getContext(), "ad_image_count")); // imageCount 이하일 경우 true
                    }
                }
                videoCount.setVisible(false); adVideo1.setVisible(false); adVideo2.setVisible(false); adVideo3.setVisible(false); adVideo4.setVisible(false); adVideo5.setVisible(false);
            } else if(PreferenceManager.getString(getContext(), "ad_type").equals("video")){
                idleScreenText.setVisible(false); idleScreenTextColor.setVisible(false);
                imageCount.setVisible(false); imageTime.setVisible(false); adImage1.setVisible(false); adImage2.setVisible(false); adImage3.setVisible(false); adImage4.setVisible(false); adImage5.setVisible(false);
                videoCount.setVisible(true);
                for(int video=1; video<= 5; video++){
                    Preference adVideo = null;
                    switch (video) {
                        case 1:
                            adVideo = adVideo1;
                            break;
                        case 2:
                            adVideo = adVideo2;
                            break;
                        case 3:
                            adVideo = adVideo3;
                            break;
                        case 4:
                            adVideo = adVideo4;
                            break;
                        case 5:
                            adVideo = adVideo5;
                            break;
                    }
                    if (adVideo != null) {
                        adVideo.setVisible(video <= PreferenceManager.getInt(getContext(), "ad_video_count")); // imageCount 이하일 경우 true
                    }
                }
            }
            idleScreenText.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceManager.setString(getContext(), "message_idle", newValue.toString());
                idleScreenText.setText(newValue.toString());
                return true;
            });
            idleScreenTextColor.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceManager.setString(getContext(), "message_color", newValue.toString());
                idleScreenTextColor.setText(newValue+"");
                return true;
            });

            imageCount.setOnPreferenceChangeListener((preference, newValue) -> {
                Object obj = newValue;
                if (Integer.parseInt((String) obj) <=5){
                    PreferenceManager.setInt(getContext(), "ad_image_count", Integer.parseInt(newValue.toString()));
                    for(int image=1; image<= 5; image++){
                        Preference adImage = null;
                        switch (image) {
                            case 1:
                                adImage = adImage1;
                                break;
                            case 2:
                                adImage = adImage2;
                                break;
                            case 3:
                                adImage = adImage3;
                                break;
                            case 4:
                                adImage = adImage4;
                                break;
                            case 5:
                                adImage = adImage5;
                                break;
                        }
                        if (adImage != null) {
                            adImage.setVisible(image <= Integer.parseInt((String) obj)); // imageCount 이하일 경우 true
                        }
                    }
                }else{
                    Toast.makeText(getContext(), "이미지 광고 개수는 5개 이하로 설정해주세요.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            });
            videoCount.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceManager.setInt(getContext(), "ad_video_count", Integer.parseInt(newValue.toString()));
                Object obj = newValue;
                if(Integer.parseInt((String) obj) <= 5) {
                    for (int video = 1; video <= 5; video++) {
                        Preference adVideo = null;
                        switch (video) {
                            case 1:
                                adVideo = adVideo1;
                                break;
                            case 2:
                                adVideo = adVideo2;
                                break;
                            case 3:
                                adVideo = adVideo3;
                                break;
                            case 4:
                                adVideo = adVideo4;
                                break;
                            case 5:
                                adVideo = adVideo5;
                                break;
                        }
                        if (adVideo != null) {
                            adVideo.setVisible(video <= Integer.parseInt((String) obj)); // imageCount 이하일 경우 true
                        }
                    }
                }else{
                    Toast.makeText(getContext(), "영상 광고 개수는 5개 이하로 설정해주세요.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            });

            setupImagePicker(adImage1, 1);
            setupImagePicker(adImage2, 2);
            setupImagePicker(adImage3, 3);
            setupImagePicker(adImage4, 4);
            setupImagePicker(adImage5, 5);
            setupVideoPicker(adVideo1, 1);
            setupVideoPicker(adVideo2, 2);
            setupVideoPicker(adVideo3, 3);
            setupVideoPicker(adVideo4, 4);
            setupVideoPicker(adVideo5, 5);
        }
        private void setupImagePicker(Preference preference, int num) {
            preference.setOnPreferenceClickListener(pref -> {
                PreferenceManager.setInt(pref.getContext(), "ad_image_temp", num);
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");  // 이미지만 허용
                String[] mimeTypes = {"image/*"}; // 허용할 MIME 타입
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, 1);
                return true;
            });
        }
        private void setupVideoPicker(Preference preference, int num) {
            preference.setOnPreferenceClickListener(pref -> {
                PreferenceManager.setInt(pref.getContext(), "ad_video_temp", num);
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");  // 비디오만 허용
                String[] mimeTypes = {"video/*"}; // 허용할 MIME 타입
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, 2); // 요청 코드 변경 (1과 다른 값으로 설정)
                return true;
            });
        }
    }

    public static class MenuScreenFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.menu_screen_preferences, rootKey);
            EditTextPreference menuColor = findPreference("menu_background_color");

            if (menuColor != null) {
                menuColor.setOnBindEditTextListener(
                        editText -> editText.setFilters(new InputFilter[]{
                                new InputFilter.LengthFilter(6),
                        })
                );
                menuColor.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (!newValue.toString().matches("[a-fA-F0-9]{6}")) {
                        Utils.timedAlert(getContext(), "영문과 숫자로 구성된 \n 6자리의 HEX 코드를 입력해주세요.", 2);
                        return false;
                    }
                    return true;
                });
            }
        }
    }

    public static class ProductSettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.product_settings_preferences, rootKey);

            Preference dessertSettings = findPreference("dessert_settings");

            dessertSettings.setOnPreferenceClickListener(preference -> {
                Fragment productFragment = new ProductManageFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                String title = dessertSettings.getTitle().toString();
                getActivity().setTitle(title);
                transaction.replace(R.id.settings, productFragment, title); // give your fragment container id in first parameter
                transaction.addToBackStack(title);  // if written, this transaction will be added to backstack
                transaction.commit();
                return true;
            });

            String[] ports = SerialPortFinder.getAllDevicesPath();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            String mimeType = getContentResolver().getType(selectedUri);

//            try{
//                PreferenceManager.setString(this, "ad_uri", data.getData().toString());
//                Log.i("URI", PreferenceManager.getString(this, "ad_uri"));
//            }catch (Exception e){
//                e.printStackTrace();
//            }
            if (mimeType != null) {
                if (mimeType.startsWith("image/")) {
                    Log.i("URI 테스트", "이미지");
                    PreferenceManager.setString(this, "ad_image"+PreferenceManager.getInt(this, "ad_image_temp"), data.getData().toString());
                    Toast.makeText(this, "대기화면 광고 이미지가 교체되었습니다", Toast.LENGTH_SHORT).show();
                } else if (mimeType.startsWith("video/")) {
                    Log.i("URI 테스트", "비디오");
                    PreferenceManager.setString(this, "ad_video"+PreferenceManager.getInt(this, "ad_video_temp"), data.getData().toString());
                    Toast.makeText(this, "대기화면 광고 영상이 교체되었습니다", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
