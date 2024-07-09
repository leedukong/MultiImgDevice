package com.releasetech.multidevice.AdminSettings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputFilter;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.releasetech.multidevice.AdminSettings.ProductManage.ProductManageFragment;
import com.releasetech.multidevice.Manager.CheckoutManager;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.MediaReplacer;
import com.releasetech.multidevice.Tool.Utils;
import com.takisoft.preferencex.EditTextPreference;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import android_serialport_api.SerialPortFinder;


public class AdminSettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TAG = "[ADMIN SETTINGS]";

    protected static Intent rustDeskIntent;
    protected static Intent remoteViewIntent;


    //todo 핸들러
//    public Handler getHandler() {
//        return handler;
//    }

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
                CheckoutManager.openSettings(getActivity());
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
            ListPreference comPort = findPreference("com_port");
            String[] ports = SerialPortFinder.getAllDevicesPath();
            comPort.setEntries(ports);
            comPort.setEntryValues(ports);

            setupSpecialPreferences();
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
            Preference btnAndroidReboot = findPreference("android_reboot");
            btnAndroidReboot.setOnPreferenceClickListener(preference -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                Dialog tempDialog = builder.setMessage("기기를 재시작하시겠습니까?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            String cmd = "su -c reboot";
                            try {
                                Runtime.getRuntime().exec(cmd);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        })
                        .setNegativeButton("No", null).show();
                TextView messageView = tempDialog.findViewById(android.R.id.message);
                messageView.setTextSize(26);
                return true;
            });
        }
    }

    private static boolean isEntering = false;
    private static boolean shiftPressed = false;
    private static String queueString ="";
//    public static class DesignFragment extends PreferenceFragmentCompat{
//        @Override
//        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
//            setPreferencesFromResource(R.xml.design_screen_preferences, rootKey);
//
//            Preference btnReplaceDesign = findPreference("replace_design");
//            btnReplaceDesign.setOnPreferenceClickListener(preference -> {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                Dialog tempDialog = builder.setMessage("디자인을 교체하시겠습니까?\n\n*주의* 기존 파일은 복구되지 않습니다")
//                        .setPositiveButton("Yes", (dialog, which) ->
//                                Utils.alert(getContext(),
//                                        MediaReplacer.replaceDesign(getContext())
//                                )
//                        )
//                        .setNegativeButton("No", null).show();
//                TextView messageView = tempDialog.findViewById(android.R.id.message);
//                messageView.setTextSize(26);
//                return true;
//            });
//
//            Preference btnReplaceDesignFromServer = findPreference("replace_design_from_server");
//            btnReplaceDesignFromServer.setOnPreferenceClickListener(preference -> {
//                StringBuilder stringBuilder = new StringBuilder();
//                UpdateDesign updateDesign = new UpdateDesign(getContext());
//
//                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                ConstraintLayout scanQrDesignDialogLayout = (ConstraintLayout) vi.inflate(R.layout.dialog_scan_qr_design, null);
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                TextView scanQrDesignDialogTitle = new TextView(getContext());
//                scanQrDesignDialogTitle.setText("디자인 QR코드를 리더기에 스캔하세요");
//                scanQrDesignDialogTitle.setPadding(0, 32, 0, 32);
//                scanQrDesignDialogTitle.setGravity(Gravity.CENTER_HORIZONTAL);
//                scanQrDesignDialogTitle.setTextSize(32);
//                AlertDialog scanQrDesignDialog = builder.setCustomTitle(scanQrDesignDialogTitle)
//                        .setView(scanQrDesignDialogLayout)
//                        .setPositiveButton("닫기", (dialog, which) -> dialog.dismiss()).show();
//                scanQrDesignDialog.setOnKeyListener((view, i, keyEvent) -> {
//                    if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
//                        if(!isEntering) {
//                            isEntering = true;
//                            final Handler handler = new Handler(Looper.getMainLooper());
//                            handler.postDelayed(() -> {
//                                UpdateDesign.serverUrl = queueString;
//                                updateDesign.execute(UpdateDesign.SERVER);
//                                queueString = "";
//                                isEntering = false;
//                                handler.postDelayed(() ->{
//                                    if(UpdateDesign.failedItems.size() ==0){
//                                        Utils.alert(getContext(), "디자인 업데이트가 완료되었습니다.");
//                                        scanQrDesignDialog.dismiss();
//                                    }else{
//                                        Utils.alert(getContext(), "디자인 : " +UpdateDesign.failedItems.toString() + " 업데이트에 실패하였습니다.");
//                                        scanQrDesignDialog.dismiss();
//                                    }
//                                }, 5000);
//                            }, 2000);
//                        }
//                        char pressedKey = (char) keyEvent.getUnicodeChar();
//                        if(pressedKey == 0){
//                            shiftPressed = true;
//                            return false;
//                        }
//                        if(shiftPressed){
//                            queueString += Utils.shift(pressedKey);
//                            shiftPressed = false;
//                        }
//                        else {
//                            queueString += pressedKey;
//                        }
//                        return false;
//                    }
//                    return true;
//                });
//                return false;
//            });
//        }
//    }

//    public static class AppInfoFragment extends PreferenceFragmentCompat {
//        int autoResetCount = 0;
//
//        @Override
//        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
//            setPreferencesFromResource(R.xml.app_info_preferences, rootKey);
//            Preference versionName = findPreference("version_name");
//            Preference launchUpdate = findPreference("launch_update");
//            launchUpdate.setOnPreferenceClickListener(preference -> {
//                UpdateApp updateApp = new UpdateApp();
//                updateApp.setContext(requireContext());
//                updateApp.execute();
//                return false;
//            });
//        }
//    }


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

            Utils.setRangeFilter(this, "back_to_idle_time", 0, 600);
            Utils.setRangeFilter(this, "idle_screen_image_duration", 0, 300);
            EditTextPreference backToIdleTimePreference = findPreference("back_to_idle_time");
            backToIdleTimePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                int value = Integer.parseInt(newValue.toString());
                if (value < 30) {
                    Utils.timedAlert(requireContext(), "30초 이상 설정해주세요.", 2);
                    return false;
                }
                return true;
            });

            Preference btnReplaceIdleAd = findPreference("ad_idle");
            btnReplaceIdleAd.setOnPreferenceClickListener(preference -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                Dialog tempDialog = builder.setMessage("[대기]화면 광고를 교체하시겠습니까?\n\n*주의* 기존 파일은 복구되지 않습니다")
                        .setPositiveButton("Yes", (dialog, which) -> Utils.alert(getContext(), MediaReplacer.replaceIdleAd(getContext())))
                        .setNegativeButton("No", null).show();
                TextView messageView = tempDialog.findViewById(android.R.id.message);
                messageView.setTextSize(26);
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

//    public static class DataFragment extends PreferenceFragmentCompat {
//
//        @Override
//        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
//            setPreferencesFromResource(R.xml.data_preferences, rootKey);
//
//            Preference btnDataBackup = findPreference("data_backup_header");
//            btnDataBackup.setOnPreferenceClickListener(preference -> {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                Dialog tempDialog = builder.setMessage("데이터를 백업하시겠습니까?").setPositiveButton("Yes", (dialogInterface, i) -> {
//                            try {
//                                String msg = "";
//                                switch (Backup.exportData(getContext(), 0)) {
//                                    case Backup.RESULT_SUCCESS:
//                                        msg = "백업을 완료했습니다.";
//                                        break;
//                                    case Backup.RESULT_NO_DISK:
//                                        msg = "USB 또는 마이크로 SD 카드가 없습니다.";
//                                        break;
//                                    case Backup.RESULT_UNKNOWN:
//                                        msg = "백업을 실패했습니다.";
//                                        break;
//                                    default:
//                                }
//                                Utils.alert(getContext(), msg);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        })
//                        .setNegativeButton("No", null).show();
//                TextView messageView = tempDialog.findViewById(android.R.id.message);
//                messageView.setTextSize(26);
//                return true;
//            });
//
//            Preference btnDataLoad = findPreference("data_load_header");
//            btnDataLoad.setOnPreferenceClickListener(preference -> {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                Dialog tempDialog = builder.setMessage("데이터를 불러오시겠습니까?\n\n*주의* 기존 데이터는 삭제됩니다.").setPositiveButton("Yes", (dialogInterface, i) -> {
//                            try {
//                                String msg = "";
//                                switch (Backup.importData(getContext(), 0)) {
//                                    case Backup.RESULT_SUCCESS:
//                                        msg = "로드를 완료했습니다.\n앱을 재시작합니다.";
//                                        final Handler handler = new Handler(Looper.getMainLooper());
//                                        handler.postDelayed(() -> Utils.restart(getContext()), 3000);
//                                        break;
//                                    case Backup.RESULT_NO_DISK:
//                                        msg = "USB 또는 마이크로 SD 카드가 없습니다.";
//                                        break;
//                                    case Backup.RESULT_NO_FILE:
//                                        msg = "불러올 백업 파일이 없습니다.";
//                                        break;
//                                    case Backup.RESULT_UNKNOWN:
//                                        msg = "로드를 실패했습니다.";
//                                        break;
//                                    default:
//                                }
//                                Utils.alert(getContext(), msg);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        })
//                        .setNegativeButton("No", null).show();
//                TextView messageView = tempDialog.findViewById(android.R.id.message);
//                messageView.setTextSize(26);
//                return true;
//            });
//
//        }
//    }

//    public static class PaycoSettingsFragment extends PreferenceFragmentCompat {
//        @Override
//        public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
//            setPreferencesFromResource(R.xml.payco_settings_preferences, rootKey);
//            SwitchPreference usePayco = findPreference("use_payco");
//
//            usePayco.setOnPreferenceChangeListener((preference, newValue) -> {
//                PreferenceManager.setBoolean(getContext(), "use_payco", (boolean) newValue);
//                return true;
//            });
//
//            EditTextPreference registrationNumber = findPreference("registration_number");
//            registrationNumber.setOnPreferenceChangeListener((preference, newValue) -> {
//                PreferenceManager.setString(getContext(), "registration_number", newValue.toString());
//                registrationNumber.setText(newValue.toString());
//                return false;
//            });
//
//            EditTextPreference vanPosTid = findPreference("van_pos_tid");
//            vanPosTid.setOnPreferenceChangeListener((preference, newValue) -> {
//                PreferenceManager.setString(getContext(), "van_pos_tid", newValue.toString());
//                vanPosTid.setText(newValue.toString());
//                return false;
//            });
//
//            Preference registPayco = findPreference("regist_payco");
//            registPayco.setOnPreferenceClickListener(preference -> {
//                Payco payco = new Payco(getContext());
//                payco.setOnRegisterCallback(new Payco.OnRegisterCallback(){
//                    Handler handler = new Handler();
//                    @Override
//                    public void onRegisterResult(String result) {
//                        if (result.equals("OK")) {
//                            handler.post(() -> Toast.makeText(getContext(), "가맹점 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show());
//                        }
//                    }
//                    @Override
//                    public void onRegisterException(Exception e) {
//                        handler.post(() -> Toast.makeText(getContext(), "가맹점 등록에 실패했습니다.", Toast.LENGTH_SHORT).show());
//                    }
//                });
//
//
//                payco.execute(Payco.REGISTER);
//                return false;
//            });
//
//            EditTextPreference paycoCountdown = findPreference("payco_countdown");
//            paycoCountdown.setOnPreferenceChangeListener((preference, newValue) -> {
//                PreferenceManager.setString(getContext(), "payco_countdown", newValue.toString());
//                paycoCountdown.setText(newValue.toString());
//                return false;
//            });
//        }
//    }

//    public static class VanCorpCodeSettingFragment extends PreferenceFragmentCompat {
//        @Override
//        public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
//            setPreferencesFromResource(R.xml.van_corp_code_setting, rootKey);
//
//            CheckBoxPreference vanCorpCode1 = findPreference("van_corp_code_1");
//            CheckBoxPreference vanCorpCode2 = findPreference("van_corp_code_2");
//            CheckBoxPreference vanCorpCode3 = findPreference("van_corp_code_3");
//            CheckBoxPreference vanCorpCode4 = findPreference("van_corp_code_4");
//
//            vanCorpCode1.setOnPreferenceChangeListener((preference, newValue) -> {
//                if(newValue.toString().equals("true")){
//                    PreferenceManager.setString(getContext(), "van_corp_code", "KCP");
//                    vanCorpCode1.setChecked(true);
//                    vanCorpCode2.setChecked(false);
//                    vanCorpCode3.setChecked(false);
//                    vanCorpCode4.setChecked(false);
//                }else{
//                    PreferenceManager.setString(getContext(), "van_corp_code", "");
//                }
//                return false;
//            });
//
//            vanCorpCode2.setOnPreferenceChangeListener((preference, newValue) -> {
//                if (newValue.toString().equals("true")) {
//                    PreferenceManager.setString(getContext(), "van_corp_code", "KICC");
//                    vanCorpCode1.setChecked(false);
//                    vanCorpCode2.setChecked(true);
//                    vanCorpCode3.setChecked(false);
//                    vanCorpCode4.setChecked(false);
//                }else{
//                    PreferenceManager.setString(getContext(), "van_corp_code", "");
//                }
//                return false;
//            });
//
//            vanCorpCode3.setOnPreferenceChangeListener((preference, newValue) -> {
//                if (newValue.toString().equals("true")) {
//                    PreferenceManager.setString(getContext(), "van_corp_code", "NICE");
//                    vanCorpCode1.setChecked(false);
//                    vanCorpCode2.setChecked(false);
//                    vanCorpCode3.setChecked(true);
//                    vanCorpCode4.setChecked(false);
//                }else{
//                    PreferenceManager.setString(getContext(), "van_corp_code", "");
//                }
//                return false;
//            });
//
//            vanCorpCode4.setOnPreferenceChangeListener((preference, newValue) -> {
//                if (newValue.toString().equals("true")) {
//                    PreferenceManager.setString(getContext(), "van_corp_code", "KIS");
//                    vanCorpCode1.setChecked(false);
//                    vanCorpCode2.setChecked(false);
//                    vanCorpCode3.setChecked(false);
//                    vanCorpCode4.setChecked(true);
//                }else{
//                    PreferenceManager.setString(getContext(), "van_corp_code", "");
//                }
//                return false;
//            });
//        }
//    }
}
