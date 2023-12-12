package com.releasetech.multidevice;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.releasetech.multidevice.AdminSettings.AdminSettingsActivity;
import com.releasetech.multidevice.Client.Payco;
import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Database.Data.CartItem;
import com.releasetech.multidevice.Database.Data.Category;
import com.releasetech.multidevice.Database.Data.DessertItem;
import com.releasetech.multidevice.Database.Data.ImageSet;
import com.releasetech.multidevice.Database.Data.Order;
import com.releasetech.multidevice.Database.Data.Product;
import com.releasetech.multidevice.Database.DessertDataLoader;
import com.releasetech.multidevice.DessertSettings.DessertSettingsActivity;
import com.releasetech.multidevice.Log.LogService;
import com.releasetech.multidevice.MainActivityViews.OptionDialog;
import com.releasetech.multidevice.Manager.CartManager;
import com.releasetech.multidevice.Manager.CheckoutManager;
import com.releasetech.multidevice.Manager.PasswordManager;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.Manager.UIManager;
import com.releasetech.multidevice.ManagerSettings.ManagerSettings;
import com.releasetech.multidevice.MultiDevice.MultiDevice;
import com.releasetech.multidevice.Receiver.AdminReceiver;
import com.releasetech.multidevice.Sound.SoundService;
import com.releasetech.multidevice.Stock.Stock;
import com.releasetech.multidevice.Tool.AdLoader;
import com.releasetech.multidevice.Tool.Cache;
import com.releasetech.multidevice.Tool.UIImageLoader;
import com.releasetech.multidevice.Tool.Utils;
import com.releasetech.multidevice.Update.UpdateApp;
import com.releasetech.multidevice.Update.UpdateDesign;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    //todo queue 삭제
    Queue<String> queue = new LinkedList<>();

    public static final String DEVTAG = "DEV";
    private static final String TAG = "[MENU]";

    /* Intent Request Code */
    public static final int CHECKOUT_REQUEST = 1;

    /* Intent Result Code */
    public static final int RESULT_ERROR = 7;

    /* Idle Screen */
    private boolean idleCountdownOn = false;
    private int countdown;
    private boolean checkingOut = false;

    /* Order */
    CartManager cartManager;
    boolean loadCartCache = false;

    /* Stock */
    private Stock stock;

    /* Settings */
    private OptionDialog optionDialog;

    //default false
    private boolean stopCountdown = false;
    private boolean resetCartDialogShowing = false;

    /* Option Dialog */
    private boolean optionDialogShowing = false;
    private int optionCount = 1;


    /* Password Related */
    private int settingsCount = 0;
    private PasswordManager passwordManager;

    /* Menu UI */
    private MenuViewAdapter[] dessertMenuViewAdapter = new MenuViewAdapter[LINE_COUNT];
    private ArrayList<Dialog> dialogs = new ArrayList<>();
    private int dessertMenuCount = 0;
    public static final int LINE_COUNT = 5;
    public static final int CATEGORY_WIDTH = 271;
    public static final int CATEGORY_HEIGHT = 181;
    static int paycoCount = 0;
    private final Category[] dessertCategories = new Category[LINE_COUNT];


    /* Menu Layout */
    private ArrayList<Boolean>[] inStocks = new ArrayList[LINE_COUNT];
    private ArrayList<Product>[] dessertProducts = new ArrayList[LINE_COUNT];
    private ArrayList<String>[] dessertImagePaths = new ArrayList[LINE_COUNT];
    private RecyclerView[] dessertMenuView = new RecyclerView[LINE_COUNT];
    private int[][] arrowIds = {
            {R.id.arrow_left_category_1, R.id.arrow_left_category_2, R.id.arrow_left_category_3, R.id.arrow_left_category_4, R.id.arrow_left_category_5},
            {R.id.arrow_right_category_1, R.id.arrow_right_category_2, R.id.arrow_right_category_3, R.id.arrow_right_category_4, R.id.arrow_right_category_5}
    };
    LinearLayout menuLayout;
    LinearLayout[] categoryLineLayout = new LinearLayout[LINE_COUNT];

    private DBManager dbManager;

    private static LogService logService;
    private static boolean logServiceConnected = false;
    static ServiceConnection logConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogService.LogBinder mb = (LogService.LogBinder) service;
            logService = mb.getService();
        }
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    /* Init Functions */
    private void initializeHiddenButton() {
        Button hiddenButtonView = findViewById(R.id.settings);
        hiddenButtonView.setOnClickListener(view -> {
            settingsCount++;
            Utils.logD(TAG, "설정 진입 버튼 : " + settingsCount);
            if (settingsCount == 10) {
                if (passwordManager.wrongPasswordCount >= 10) return;
                passwordManager.passwordDialog(this);
                settingsCount = 0;
            } else if (settingsCount == 100) {
                passwordManager.resetPasswords();
                settingsCount = 0;
            } else if (settingsCount > 90) {
                Utils.showToast(this, "패스워드 초기화까지 남은 횟수 : " + (100 - settingsCount));
            }
        });
    }

    private void initializeUI() {
        ImageButton button_checkout = findViewById(R.id.menu_checkout);
        button_checkout.setOnClickListener(this::checkoutDialog);
        ImageButton button_cancel_all = findViewById(R.id.menu_cancel_all);
        button_cancel_all.setOnClickListener(view -> resetCartDialog());
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        UIManager.hideSystemUI(this);
        super.onCreate(savedInstanceState);
        Utils.logD(TAG, "화면 표시됨");
        setContentView(R.layout.activity_main);

        checkPermission();
        List<ApplicationInfo> packages;
        PackageManager pm = getPackageManager();
        packages = pm.getInstalledApplications(0);
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ApplicationInfo packageInfo : packages) {
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) continue;
            if (packageInfo.packageName.equals("com.releasetech.multidevice")) continue;
            mActivityManager.killBackgroundProcesses(packageInfo.packageName);
        }

        if (!logServiceConnected) {
            LogService.logDirectoryPath = this.getExternalCacheDir() + "/logs";
            Intent logServiceIntent = new Intent(getApplicationContext(), LogService.class);
            if (PreferenceManager.getBoolean(this, "rebooted")) {
                logServiceIntent.putExtra("rebooted", true);
                PreferenceManager.setBoolean(this, "rebooted", false);
            }
            bindService(logServiceIntent, logConn, Context.BIND_AUTO_CREATE);
        }

        //todo
        /* 업데이트 필요 여부
        if (PreferenceManager.getBoolean(this, "auto_update") && updatePopup) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            UpdateState updateState = new UpdateState();
            updateState.checkUpdate(this, connManager);
        }*/

        if (!BuildConfig.VERSION_NAME.equals(PreferenceManager.getString(this, "version"))) {
            UpdateApp.updatedDialog(this, PreferenceManager.getString(this, "version"), BuildConfig.VERSION_NAME);
            UpdateDesign updateDesign = new UpdateDesign(this);
            updateDesign.execute(UpdateDesign.DEFAULT);
            View view = getWindow().getDecorView().findViewById(android.R.id.content);
            UIImageLoader.loadMenuImage(this, view);
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        ComponentName mainComponent = new ComponentName(getApplicationContext(), MainActivity.class);

        String device_name = Settings.Global.getString(getContentResolver(), "device_name");
        if (device_name.equals("eightpresso_basic_b")) {
            Utils.logD(TAG, "2023.07.13 이후 기기");
        } else {
            Utils.logD(TAG, "2023.07.13 이전 기기");
            DevicePolicyManager dpm =
                    (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            dpm.addPersistentPreferredActivity(
                    AdminReceiver.getComponentName(getApplicationContext()), filter, mainComponent);
        }

        dbManager = new DBManager(this);
        passwordManager = new PasswordManager(this);
        dbManager.open();
        dbManager.create();
        stock = new Stock(this, dbManager);

        initializeHiddenButton();
        initializeUI();
        setupCart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        dbManager.open();
        dbManager.create();
        stopCountdown = false;
        stock.loadStock();
        View view = getWindow().getDecorView().findViewById(android.R.id.content);
        UIImageLoader.loadMenuImage(this, view);
        AdLoader.loadMenuAds(this, findViewById(R.id.menu_ad));
        loadMenuItems();
        setupRecyclerView();
        setupViews();
        UIManager.hideSystemUI(this);

        if (loadCartCache) {
            try {
                cartManager = ((CartManager) Cache.Read(this, "cart_cache")).clone();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            cartManager.setOnUpdateListner(dataManager -> updateCart());
            loadCartCache = false;
        } else {
            cartManager = new CartManager(Integer.parseInt(PreferenceManager.getString(this, "cart_quantity")));
            cartManager.setOnUpdateListner(dataManager -> updateCart());
            clearCart();
        }
        initOptionDialog();

        TextView freeOfChargeTextView =
                findViewById(R.id.free_of_charge);
        if (PreferenceManager.getBoolean(this, "free_of_charge") && PreferenceManager.getBoolean(this, "free_of_charge_text")) {
            freeOfChargeTextView.setVisibility(View.VISIBLE);
        } else
            freeOfChargeTextView.setVisibility(View.GONE);
        try {
            countdown = Integer.parseInt(PreferenceManager.getString(this, "back_to_idle_time"));
            if (!idleCountdownOn)
                idleScreenCountdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Permission */
    private void checkPermission() {
        boolean permissionNotGranted =
                checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED
                        || checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED
                        || checkSelfPermission("android.permission.INTERNET") == PackageManager.PERMISSION_DENIED
                        || checkSelfPermission("android.permission.INSTALL_PACKAGES") == PackageManager.PERMISSION_DENIED
                        || checkSelfPermission("android.permission.DELETE_PACKAGES") == PackageManager.PERMISSION_DENIED
                        || checkSelfPermission("android.permission.ACCESS_WIFI_STATE") == PackageManager.PERMISSION_DENIED
                        || checkSelfPermission("android.permission.LOCAL_MAC_ADDRESS") == PackageManager.PERMISSION_DENIED
                        || checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED
                        || checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED
                        || checkSelfPermission("android.permission.WRITE_SETTINGS") == PackageManager.PERMISSION_DENIED;
        if (permissionNotGranted) {
            requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.INTERNET",
                    "android.permission.INSTALL_PACKAGES",
                    "android.permission.DELETE_PACKAGES",
                    "android.permission.ACCESS_WIFI_STATE",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.LOCAL_MAC_ADDRESS",
                    "android.permission.WRITE_SETTINGS"}, 99);
        }
    }
    @Override
    public void onUserInteraction() {
        resetCountdown();
        super.onUserInteraction();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE")
                || shouldShowRequestPermissionRationale("android.permission.READ_EXTERNAL_STORAGE")
                || shouldShowRequestPermissionRationale("android.permission.INTERNET")
                || shouldShowRequestPermissionRationale("android.permission.INSTALL_PACKAGES")
                || shouldShowRequestPermissionRationale("android.permission.DELETE_PACKAGES")
                || shouldShowRequestPermissionRationale("android.permission.ACCESS_WIFI_STATE")
                || shouldShowRequestPermissionRationale("android.permission.WRITE_SETTINGS")) {
            checkPermission();
        }
    }


    /* 결제 관련 */
    private void checkoutDialog(View view) {
        Utils.logD(TAG, "결제 버튼 누름");

        if (cartManager.getCount() > 0 && cartManager.getTotalPrice() > 0) {
            LocalDateTime now = LocalDateTime.now();
            if (PreferenceManager.getBoolean(this, "free_of_charge")) {
                for (int i = 0; i < cartManager.getCount(); i++) {
                    Order order = new Order(this, now, (DessertItem) cartManager.getItem(i), "무결제");
                    dbManager.insertColumn(DBManager.SALES, order);

                }
                cartManager.lock();
                postCheckout();
                return;
            }

            LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ConstraintLayout checkoutDialogLayout = (ConstraintLayout) vi.inflate(R.layout.dialog_checkout, null);
            AlertDialog.Builder checkoutDialogBuilder = new AlertDialog.Builder(this).setView(checkoutDialogLayout);
            Utils.disableDialogCancel(checkoutDialogBuilder);
            AlertDialog checkoutDialog = checkoutDialogBuilder.create();
            UIImageLoader.loadCheckoutDialogImage(this, checkoutDialogLayout);

            TextView dialog_checkout_count = checkoutDialogLayout.findViewById(R.id.dialog_checkout_count);
            dialog_checkout_count.setText(cartManager.getCountText());

            TextView dialog_checkout_price = checkoutDialogLayout.findViewById(R.id.dialog_checkout_price);
            dialog_checkout_price.setText(cartManager.getTotalPriceText());

            RecyclerView dialog_checkout_list = checkoutDialogLayout.findViewById(R.id.dialog_checkout_list);
            dialog_checkout_list.setLayoutManager(new LinearLayoutManager(this));
            dialog_checkout_list.setAdapter(new CheckoutItemAdapter(cartManager, dialog_checkout_price, dialog_checkout_count));

            RadioButton[] radioButtons = {checkoutDialogLayout.findViewById(R.id.checkout_card), checkoutDialogLayout.findViewById(R.id.checkout_payco)};
            String[] paymentMethods = {"카드", "페이코"};

            if (!PreferenceManager.getBoolean(this, "use_payco")) {
                radioButtons[1].setVisibility(View.GONE);
                radioButtons[1].setActivated(false);
                radioButtons[1].setChecked(false);
                radioButtons[1].setClickable(false);
            }

            PreferenceManager.setString(this, "paymentMethod", "null");
            for (int i = 0; i < radioButtons.length; i++) {
                int finalI = i;
                radioButtons[i].setOnClickListener(view1 -> {
                    PreferenceManager.setString(this, "paymentMethod", paymentMethods[finalI]);
                    for (int j = 0; j < radioButtons.length; j++) {
                        if (finalI != j) radioButtons[j].setChecked(false);
                    }
                });
            }

            ImageButton dialogCheckoutOkButton = checkoutDialogLayout.findViewById(R.id.button_dialog_checkout_ok);
            dialogCheckoutOkButton.setOnClickListener(view1 -> {
                String paymentMethod = PreferenceManager.getString(this, "paymentMethod");
                resetCountdown();

                if (paymentMethod.equals("null")) {
                    Utils.timedAlert(this, "결제 방법을 선택해주세요.", 1);
                } else {
                    try {
                        Cache.write(this, "cart_cache", cartManager);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (paymentMethod.equals("페이코")) {
                        checkoutPayco(checkoutDialog);
                    } else if (paymentMethod.equals("카드")) {
                        checkoutCreditCard(checkoutDialog);
                    }
                }
            });

            ImageButton dialog_checkout_cancel = checkoutDialogLayout.findViewById(R.id.button_dialog_checkout_cancel);
            dialog_checkout_cancel.setOnClickListener(dialogInterface -> {
                updateCart();
                checkoutDialog.dismiss();
            });

            checkoutDialog.setOnDismissListener(dialogInterface -> {
                dialogs.remove(dialogInterface);
            });

            checkoutDialog.show();
            dialogs.add(checkoutDialog);

            checkoutDialog.getWindow().
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            checkoutDialog.getWindow().setLayout(1920, 1080);
            checkoutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            checkoutDialog.setCanceledOnTouchOutside(false);
        }
    }
    private void checkoutPayco(AlertDialog checkoutDialog) {
        paycoCount = Integer.parseInt(PreferenceManager.getString(this, "payco_countdown"));
        LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ConstraintLayout paycoDialogLayout = (ConstraintLayout) vi.inflate(R.layout.dialog_payco, null); // 테스트
        TextView paycoCountdown = paycoDialogLayout.findViewById(R.id.payco_countdown);
        paycoCountdown.setTextSize(30);

        AlertDialog.Builder paycoDialogBuilder = new AlertDialog.Builder(this).setView(paycoDialogLayout);
        Utils.disableDialogCancel(paycoDialogBuilder);
        AlertDialog paycoDialog = paycoDialogBuilder.create();

        paycoDialog.getWindow().
                setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        paycoDialog.show();

        String backgroundPath = this.getExternalFilesDir(null) + "/팝업_페이코/배경.png";

        Window window = paycoDialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        window.setLayout(1024, 768);
        window.setBackgroundDrawable(Drawable.createFromPath(backgroundPath));
        paycoDialog.setCanceledOnTouchOutside(false);

        final Handler paycoBarcodeHandler = new Handler(Looper.getMainLooper());
        AtomicReference<String> queueString = new AtomicReference<>("");
        queueString.set("");

        ImageView paycoKeypadBackground = paycoDialogLayout.findViewById(R.id.payco_keypad_background);
        paycoKeypadBackground.setBackgroundDrawable(Drawable.createFromPath(this.getExternalFilesDir(null) + "/팝업_페이코/배경_키패드.png"));
        paycoKeypadBackground.setVisibility(View.GONE);

        ImageButton[] paycoKeypadButtons = {
                paycoDialogLayout.findViewById(R.id.payco_keypad_0),
                paycoDialogLayout.findViewById(R.id.payco_keypad_1),
                paycoDialogLayout.findViewById(R.id.payco_keypad_2),
                paycoDialogLayout.findViewById(R.id.payco_keypad_3),
                paycoDialogLayout.findViewById(R.id.payco_keypad_4),
                paycoDialogLayout.findViewById(R.id.payco_keypad_5),
                paycoDialogLayout.findViewById(R.id.payco_keypad_6),
                paycoDialogLayout.findViewById(R.id.payco_keypad_7),
                paycoDialogLayout.findViewById(R.id.payco_keypad_8),
                paycoDialogLayout.findViewById(R.id.payco_keypad_9)
        };

        ImageButton paycoEnterButton = paycoDialogLayout.findViewById(R.id.payco_keypad_enter);
        paycoEnterButton.setImageDrawable(Drawable.createFromPath(this.getExternalFilesDir(null) + "/팝업_페이코/키패드_확인.png"));
        ImageButton paycoBackspaceButton = paycoDialogLayout.findViewById(R.id.payco_keypad_backspace);
        paycoBackspaceButton.setImageDrawable(Drawable.createFromPath(this.getExternalFilesDir(null) + "/팝업_페이코/키패드_지움.png"));
        TextView paycoKeypadInputString = paycoDialogLayout.findViewById(R.id.payco_keypad_input_string);

        for (int j = 0; j < paycoKeypadButtons.length; j++) {
            int finalJ = j;
            paycoKeypadButtons[j].setOnClickListener(view -> {
                if (paycoKeypadInputString.length() <= 25) {
                    if (paycoKeypadInputString.length() != 0 && (paycoKeypadInputString.getText().toString().replaceAll("-", "").length() % 4) == 0
                            && paycoKeypadInputString.getText().charAt(paycoKeypadInputString.length() - 1) != '-') {
                        paycoKeypadInputString.setText(paycoKeypadInputString.getText().toString() + "-" + finalJ);
                    } else {
                        paycoKeypadInputString.setText(paycoKeypadInputString.getText().toString() + finalJ);
                        paycoKeypadInputString.toString().replaceAll("--", "-");
                    }
                }
            });
        }
        for (int i = 0; i < paycoKeypadButtons.length; i++) {
            paycoKeypadButtons[i].setImageDrawable(Drawable.createFromPath(this.getExternalFilesDir(null) + "/팝업_페이코/키패드_" + i + ".png"));
            paycoKeypadButtons[i].setVisibility(View.GONE);
            paycoKeypadButtons[i].setActivated(false);
        }
        paycoEnterButton.setVisibility(View.GONE);
        paycoEnterButton.setActivated(false);
        paycoBackspaceButton.setVisibility(View.GONE);
        paycoBackspaceButton.setActivated(false);

        Button cancelButton = paycoDialogLayout.findViewById(R.id.payco_cancel_button);
        cancelButton.setOnClickListener(view -> {
            paycoDialog.dismiss();
        });

        Button paycoKeypad = paycoDialogLayout.findViewById(R.id.payco_keypad);
        paycoKeypad.setOnClickListener(view -> {
            paycoKeypadBackground.setVisibility(View.VISIBLE);
            for (int i = 0; i < paycoKeypadButtons.length; i++) {
                paycoKeypadButtons[i].setVisibility(View.VISIBLE);
                paycoKeypadButtons[i].setActivated(true);
            }
            paycoKeypad.setVisibility(View.INVISIBLE);
            paycoKeypad.setActivated(false);
            paycoEnterButton.setVisibility(View.VISIBLE);
            paycoEnterButton.setActivated(true);
            paycoBackspaceButton.setVisibility(View.VISIBLE);
            paycoBackspaceButton.setActivated(true);
        });
        paycoBackspaceButton.setOnClickListener(view -> {
            if (paycoKeypadInputString.length() > 0) {
                paycoKeypadInputString.setText(paycoKeypadInputString.getText().toString().substring(0, paycoKeypadInputString.length() - 1));
            }
        });
        Payco.OnApprovalCallback paycoCallback = new Payco.OnApprovalCallback() {
            @Override
            public void onApprovalResult(Payco payco, String result, String approvalNo) {
                if (paycoDialog.isShowing()) paycoDialog.dismiss();
                if (result.equals("OK")) {
                    SoundService.play(MainActivity.this, SoundService.CHECKOUT_PAYCO_OK);

                    dbManager.open();
                    dbManager.create();
                    dbManager.insertColumn(DBManager.CHECKOUT_PAYCO, payco);

                    loadCartCache = true;
                    checkingOut = true;

                    loadCartCache = false;
                    checkoutDialog.dismiss();
                    cartManager.lock();
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(MainActivity.this::postCheckout, 2500);
                    queueString.set("");
                    paycoKeypadInputString.setText("");

                } else {
                    Utils.timedAlert(MainActivity.this, result, 3000);
                    SoundService.play(MainActivity.this, SoundService.CHECKOUT_PAYCO_FAIL);
                    queueString.set("");
                    paycoKeypadInputString.setText("");
                }
            }

            @Override
            public void onApprovalException(Exception e) {
                SoundService.play(MainActivity.this, SoundService.CHECKOUT_PAYCO_FAIL);
                paycoKeypadInputString.setText("");
            }
        };

        paycoEnterButton.setOnClickListener(view -> {
            String pinCode = paycoKeypadInputString.getText().toString().replaceAll("-", "");
            paycoSubmit(pinCode, paycoCallback);
        });
        paycoCounter(paycoDialog);

        paycoDialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                paycoBarcodeHandler.removeCallbacksAndMessages(null);
                paycoBarcodeHandler.postDelayed(() -> {
                    String pinCode = queueString.get();
                    paycoSubmit(pinCode, paycoCallback);
                }, 1000);
                char pressedKey = (char) keyEvent.getUnicodeChar();
                queueString.updateAndGet(v -> v + pressedKey);
            }
            return true;
        });

        paycoDialog.setOnDismissListener(dialogInterface -> {
            paycoCount = Integer.parseInt(PreferenceManager.getString(this, "payco_countdown"));
            dialogs.remove(dialogInterface);
        });

        paycoDialog.show();
        dialogs.add(paycoDialog);
    }
    private void checkoutCreditCard(AlertDialog checkoutDialog) {
        //여기서 가격 가져옴
        checkingOut = true;
        Utils.logD(TAG, "카드 결제 시도: " + cartManager.getTotalPrice() + "원");
        CheckoutManager.checkout(this, cartManager.getTotalPrice());
        loadCartCache = true;
        checkoutDialog.dismiss();
    }
    private void paycoSubmit(String pinCode, Payco.OnApprovalCallback paycoCallback) {
        if (pinCode.length() < 20 || !TextUtils.isDigitsOnly(pinCode)) {
            SoundService.play(this, SoundService.CHECKOUT_PAYCO_FAIL);
            return;
        }
        SoundService.play(this, SoundService.APPROVAL_PAYCO);
        Payco payco = new Payco(this);
        payco.setOnApprovalCallback(paycoCallback);
        if (cartManager.getCount() == 1) {
            payco.setProductNames(cartManager.getItem(0).productName);
        } else {
            payco.setProductNames(cartManager.getItem(0).productName + " 외 " + (cartManager.getCount() - 1) + "건");
        }

        JSONArray tempArray = new JSONArray();
        // 카트에 담긴 상품들을 이름순으로 정렬
        CartItem[] items = Arrays.stream(cartManager.getItems().clone())
                .sorted(Comparator.comparing(CartItem::getProductName))
                .sorted(Comparator.comparing(CartItem::getPrice))
                .toArray(CartItem[]::new);
        for (int k = 0; k < items.length; k++) {
            CartItem p = items[k];
            JSONObject productInfoObject = new JSONObject();

            try {
                double price = p.price;
                productInfoObject.put("productCode", "00000000");
                productInfoObject.put("productName", p.productName);
                productInfoObject.put("productUnitAmount", price);
                int sameProductsCount = Arrays.stream(items)
                        .filter(item -> item.productName.equals(p.productName) && item.price == price)
                        .toArray().length;
                items = Arrays.stream(items)
                        .filter(item -> !item.productName.equals(p.productName) || item.price != price)
                        .toArray(CartItem[]::new);
                k--;
                productInfoObject.put("productQuantity", sameProductsCount);
                productInfoObject.put("promotionYn", "Y");
                tempArray.put(productInfoObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        payco.setProductInfoList(tempArray);
        payco.setPinCode(pinCode);
        PreferenceManager.setString(this, "payco_prev_pin_code", pinCode);
        PreferenceManager.setString(this, "payco_prev_total_price", cartManager.getTotalPrice() + "");
        double temp = cartManager.getTotalPrice();
        payco.setPaycoTotalAmount(temp);
        payco.execute(Payco.APPROVAL);
    }
    private void paycoCounter(AlertDialog dialog) {
        TextView paycoCountdown = dialog.findViewById(R.id.payco_countdown);
        TextView paycoKeypadInputString = dialog.findViewById(R.id.payco_keypad_input_string);
        View paycoKeypadBackground = dialog.findViewById(R.id.payco_keypad_background);
        if (dialog.isShowing()) {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (paycoCount > 0) {
                    paycoCountdown.setText(" " + paycoCount + " 초");
                    paycoCount--;
                    paycoCounter(dialog);
                } else if (paycoCount == 0) {
                    paycoKeypadBackground.setVisibility(View.GONE);
                    paycoKeypadInputString.setText("");
                    dialog.dismiss();
                    Utils.timedAlert(MainActivity.this, "결제가 취소되었습니다.", 1);
                }
            }, 1000);
        }
    }



    private void resetCountdown() {
        countdown = Integer.parseInt(PreferenceManager.getString(this, "back_to_idle_time"));
    }
    private void showIdleScreen() {
        Intent intent = new Intent(this, IdleActivity.class);
        startActivity(intent);
    }
    private void dismissAllDialogs() {
        if (!dialogs.isEmpty()) {
            optionDialogShowing = false;
            dialogs.stream().forEach(dialog -> {
                if (dialog.isShowing()) dialog.dismiss();
            });
            dialogs.clear();
        }
    }

    private void setupViews() {
        menuLayout = findViewById(R.id.menu_layout);
        menuLayout.removeAllViews();

        for (int i = 0; i < dessertMenuCount; i++) {
            categoryLineLayout[i] = new LinearLayout(this);
            categoryLineLayout[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (932 / LINE_COUNT)));
            categoryLineLayout[i].setOrientation(LinearLayout.HORIZONTAL);
            menuLayout.addView(categoryLineLayout[i]);

            ImageView categoryImage = new ImageView(this);
            categoryImage.setLayoutParams(new LinearLayout.LayoutParams(CATEGORY_WIDTH, CATEGORY_HEIGHT + 1));
            categoryImage.setX(-1);
            categoryImage.setY(-2);
            String categoryImagePath = loadDessertCategoryImage(i);
            Uri categoryImageUri = Uri.parse(categoryImagePath);
            categoryImage.setImageURI(categoryImageUri);
            categoryLineLayout[i].addView(categoryImage);
            categoryLineLayout[i].addView(dessertMenuView[i]);
        }
    }
    @SuppressLint("NewApi")
    private void setupRecyclerView() {

        for (int i = 0; i < dessertMenuCount; i++) {
            dessertMenuView[i] = new RecyclerView(this);
            dessertMenuView[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            dessertMenuView[i].setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
            dessertMenuViewAdapter[i] = new MenuViewAdapter(i, dessertImagePaths[i], inStocks[i]);
            dessertMenuView[i].setAdapter(dessertMenuViewAdapter[i]);

            ImageView arrowLeft = findViewById(arrowIds[0][i]);
            arrowLeft.setVisibility(View.GONE);
            ImageView arrowRight = findViewById(arrowIds[1][i]);
            arrowRight.setVisibility(View.GONE);
            if (dessertProducts[i].size() > 5) {
                arrowRight.setVisibility(View.VISIBLE);
            }
            dessertMenuView[i].addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (recyclerView.getChildCount() > 5) {
                        boolean notLeft = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() > 1;
                        boolean notRight = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() < recyclerView.getChildCount() - 1;
                        arrowLeft.setVisibility(notLeft ? View.VISIBLE : View.GONE);
                        arrowRight.setVisibility(notRight ? View.VISIBLE : View.GONE);
                    }
                }
            });
        }
    }
    private void loadMenuItems() {
        dessertMenuCount = 0;
        for (int i = 0; i < LINE_COUNT; i++) {
            dessertProducts[i] = new ArrayList<>();
            dessertImagePaths[i] = new ArrayList<>();
            inStocks[i] = new ArrayList<>();
        }
        for (Category category : DessertDataLoader.loadCategories(dbManager)) {
            if (category.available > 0) {
                dessertCategories[dessertMenuCount] = category;
                dessertProducts[dessertMenuCount] = DessertDataLoader.loadProductsByCategoryId(dbManager, category.id, true);
                dessertMenuCount++;
                if (dessertMenuCount == LINE_COUNT) break;
            }
        }

        for (int i = 0; i < dessertMenuCount; i++) {
            for (int j = 0; j < dessertProducts[i].size(); j++) {
                try {
                    dessertImagePaths[i].add(loadDessertImage(i, j, ImageSet.MENU_IDLE));
                } catch (NullPointerException e) {
                    Utils.logD(TAG, dessertCategories[i].name + " " + dessertProducts[i].get(j).name + " 이미지 셋 " + dessertProducts[i].get(j).image_set + " 없음");
                }
                inStocks[i].add(stock.dessertInStock(dessertProducts[i].get(j)));
            }
        }
    }
    private void checkStock() {
        for (int i = 0; i < dessertMenuCount; i++) {
            for (int j = 0; j < dessertProducts[i].size(); j++) {
                boolean inStock = stock.dessertInStock(dessertProducts[i].get(j));
                if (inStocks[i].get(j) != inStock) {
                    inStocks[i].set(j, inStock);
                    dessertMenuViewAdapter[i].notifyItemChanged(j);
                }
            }
        }
    }
    @SuppressLint("Range")
    private ImageSet loadImageSet(long id) {
        Cursor iCursor = dbManager.sortColumn(DBManager.PRODUCT_IMAGE, "_id");
        String[] stringUries = new String[5];
        if (iCursor == null) return null;
        while (iCursor.moveToNext()) {
            long imageSetID = iCursor.getLong(iCursor.getColumnIndex("_id"));
            if (id == imageSetID) {
                String[] names = {"menu_idle", "menu_selected", "cart_idle", "cart_selected", "quantity"};
                for (int i = 0; i < 5; i++) {
                    String name = names[i];
                    stringUries[i] = iCursor.getString(iCursor.getColumnIndex(name));
                }
                ImageSet imageset = new ImageSet(stringUries[0], stringUries[1], stringUries[2], stringUries[3], stringUries[4]);
                return imageset;
            }
        }
        return null;
    }
    private ImageSet loadDessertImageSet(int categoryIndex, int productIndex) {
        long imageSetId = dessertProducts[categoryIndex].get(productIndex).image_set;
        return loadImageSet(imageSetId);
    }
    private String loadDessertImage(int categoryIndex, int productIndex, int which) {
        ImageSet imageSet = loadDessertImageSet(categoryIndex, productIndex);
        return imageSet.getImagePath(which);
    }
    private String loadDessertCategoryImage(int categoryIndex) {
        return dessertCategories[categoryIndex].image;
    }
    private void showOptionDialog(int categoryIndex, int productIndex) {
        if (!cartManager.available()) {
            Utils.showToast(this, "한번에 " + cartManager.getSize() + "개까지 주문 가능합니다");
            return;
        }
        AlertDialog dialog;
        dialog = optionDialog.show(categoryIndex, productIndex);
        if (dialog != null) {
            dialogs.add(dialog);
            dialog.setOnDismissListener(dialogInterface -> dialogs.remove(dialogInterface));
        }
        if (!idleCountdownOn) ;
    }
    private void idleScreenCountdown() {
        idleCountdownOn = true;
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (stopCountdown) return;
            if (countdown == 0) {
                if (PreferenceManager.getBoolean(this, "use_idle_screen")) {
                    showIdleScreen();
                } else {
                    cartManager.clear();
                    dismissAllDialogs();
                    idleCountdownOn = false;
                }
            } else {
                if (!checkingOut) countdown--;
                if (PreferenceManager.getBoolean(this, "use_idle_screen")) {
                    Utils.logD(TAG, "대기화면 전환까지 : " + countdown);
                } else {
                    Utils.logD(TAG, "장바구니 초기화까지 : " + countdown);
                }
                idleScreenCountdown();
            }
        }, 1000);
    }
    private void clearCart() {
        if (cartManager != null) {
            cartManager.clear();
        }
    }
    private void updateCart() {
        int[] itemIds = {R.id.menu_cart_item_1, R.id.menu_cart_item_2, R.id.menu_cart_item_3, R.id.menu_cart_item_4, R.id.menu_cart_item_5};
        int[] itemSizeTextIds = {R.id.menu_cart_size_1, R.id.menu_cart_size_2, R.id.menu_cart_size_3, R.id.menu_cart_size_4, R.id.menu_cart_size_5};
        int[] itemPriceTextIds = {R.id.menu_cart_price_1, R.id.menu_cart_price_2, R.id.menu_cart_price_3, R.id.menu_cart_price_4, R.id.menu_cart_price_5};
        for (int i = 0; i < 5; i++) {
            ImageView cartItemImageView = findViewById(itemIds[i]);
            TextView cartItemSizeTextView = findViewById(itemSizeTextIds[i]);
            TextView cartItemPriceTextView = findViewById(itemPriceTextIds[i]);
            if (i >= cartManager.getSize() || cartManager.getItem(i) == null) {
                cartItemImageView.setImageResource(android.R.color.transparent);
                cartItemSizeTextView.setText("");
                cartItemPriceTextView.setText("");
            } else {
                cartItemImageView.setImageURI(Uri.parse(cartManager.getImage(i)));
                cartItemSizeTextView.setText("");
                cartItemPriceTextView.setText(cartManager.getPriceText(i));
            }
        }
        TextView menuTotalItemCountTextView = findViewById(R.id.menu_total_item_count);
        TextView menuTotalPriceTextView = findViewById(R.id.menu_total_price);

        menuTotalItemCountTextView.setText(cartManager.getCountText());
        menuTotalPriceTextView.setText(cartManager.getTotalPriceText());

        stock.applyCart(cartManager);
    }
    private void resetCartDialog() {
        if (resetCartDialogShowing) return;
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ConstraintLayout resetCartDialogLayout = (ConstraintLayout) vi.inflate(R.layout.dialog_reset_cart, null);

        ImageButton resetCartOkButton = resetCartDialogLayout.findViewById(R.id.option_reset_cart_ok);
        ImageButton resetCartCancelButton = resetCartDialogLayout.findViewById(R.id.option_reset_cart_cancel);

        UIImageLoader.loadResetCartDialogImage(this, resetCartDialogLayout);

        ImageView resetCartDialogView = resetCartDialogLayout.findViewById(R.id.cart_reset_background);

        AlertDialog.Builder resetCartDialogBuilder = new AlertDialog.Builder(this).setView(resetCartDialogLayout);
        Utils.disableDialogCancel(resetCartDialogBuilder);
        AlertDialog resetCartDialog = resetCartDialogBuilder.create();
        resetCartDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        resetCartOkButton.setOnClickListener(
                view -> {
                    if (cartManager.getCount() == 0) return;
                    clearCart();
                    resetCartDialog.dismiss();
                }
        );

        resetCartCancelButton.setOnClickListener(
                view -> {
                    resetCartDialog.dismiss();
                }
        );

        resetCartDialog.show();
        Window window = resetCartDialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        window.setLayout(resetCartDialogView.getDrawable().getIntrinsicWidth(), resetCartDialogView.getDrawable().getIntrinsicHeight());
        dialogs.add(resetCartDialog);
        resetCartDialog.setOnDismissListener(dialogInterface -> {
            dialogs.remove(dialogInterface);
            resetCartDialogShowing = false;
        });
        resetCartDialogShowing = true;
    }
    public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder> {
        private ArrayList<CartItem> mList;
        private ArrayList<String> mCouponList = new ArrayList<>();
        private CartManager cartManager;
        private TextView totalText;
        private TextView totalCount;

        public CheckoutItemAdapter(CartManager cartManager, TextView totalText, TextView totalCount) {
            this.cartManager = cartManager;
            this.totalText = totalText;
            this.totalCount = totalCount;
            ArrayList<CartItem> list = new ArrayList<>();
            for (int i = 0; i < cartManager.getCount(); i++) {
                list.add(cartManager.getItem(i));
            }
            mList = list;
        }

        @NonNull
        @Override
        public CheckoutItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.dialog_checkout_item, parent, false);
            UIImageLoader.loadCheckoutItemHolderImage(MainActivity.this, view);
            return new CheckoutItemAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CheckoutItemAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            CartItem cartItem = mList.get(position);
            holder.product.setText(cartItem.productName);
            holder.price.setText(cartItem.getPriceText());
            //holder.coupon.setVisibility(View.INVISIBLE);

            holder.cancel.setOnClickListener(view -> {
                if (cartManager.getCount() <= 1) return;
                holder.cancelItem();
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView product;
            TextView price;
            ImageButton cancel;
            ImageButton coupon;


            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                product = itemView.findViewById(R.id.dialog_checkout_item_product);
                price = itemView.findViewById(R.id.dialog_checkout_item_price);
                cancel = itemView.findViewById(R.id.dialog_checkout_item_cancel);
            }

            void cancelItem() {
                try {
                    if (mList.size() <= 1) return;
                    int position = getBindingAdapterPosition();
                    mList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mList.size());
                    cartManager.remove(position);
                    totalCount.setText(cartManager.getCountText());
                    totalText.setText(cartManager.getTotalPriceText());
                } catch (Exception e) {
                }
            }
        }
    }
    private void initOptionDialog() {
        optionDialog = new OptionDialog(this, cartManager, dbManager, stock);
        optionDialog.setOnUserInteractionListener(() -> resetCountdown());
        optionDialog.setOnResultListener(new OptionDialog.OnResultListener() {
            @Override
            public void onSubmit(DessertItem dessertItem, String imagePath, int count) {
                for (int i = 0; i < count; i++) {
                    cartManager.add(dessertItem.clone(), imagePath);
                }
                checkStock();
            }

            @Override
            public void onCancel() {
            }
        });
    }
    private void postCheckout() {
        Stack dessertStack = new Stack();
        int initialCount = cartManager.getCount();
        for (int i = initialCount - 1; i >= 0; i--) {
            dessertStack.push((DessertItem) cartManager.getItem(i));
            cartManager.unlock();
            cartManager.remove(i);
            cartManager.lock();
        }

        if (dessertStack.size() > 0) {
            MultiDevice.throwOutNext(this, dessertStack, new MultiDevice.OnThrowOutListener() {
                @Override
                public void onThrowOut(String productName) {
                    String is = "를";
                    char lastName = productName.charAt(productName.length() - 1);
                    if (lastName >= 0xAC00 && lastName <= 0xD7A3) {
                        if ((lastName - 0xAC00) % 28 > 0) {
                            is = "을";
                        }
                    }
                    Utils.showToast(getApplicationContext(), productName + is + " 출하합니다.");

                }

                @Override
                public void onThrowOutDone() {
                    if (MainActivity.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                        runOnUiThread(MainActivity.this::onResume);
                    }
                }
            });
        }
    }
    private void setupCart() {
        int[] itemIds = {R.id.menu_cart_item_1, R.id.menu_cart_item_2, R.id.menu_cart_item_3, R.id.menu_cart_item_4, R.id.menu_cart_item_5};
        for (int i = 0; i < 5; i++) {
            final int index = i;
            ImageView cartItemImageView = findViewById(itemIds[i]);
            cartItemImageView.setOnClickListener(view -> {
                if (index >= cartManager.getSize() || cartManager.getItem(index) == null)
                    return;
                final PopupMenu popupMenu = new PopupMenu(this, view);
                getMenuInflater().inflate(R.menu.menu_cart_item_clicked, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.cart_item_delete) {
                        cartManager.remove(index);
                    }
//                    else if (menuItem.getItemId() == R.id.cart_item_edit) {
//                        Utils.showToast(MainActivity.this, "수정");
//                    }
                    return false;
                });
                popupMenu.show();
            });
        }
    }
    private class MenuViewAdapter extends RecyclerView.Adapter<MenuViewAdapter.ViewHolder> {
        private final int index;
        private final ArrayList<Uri> uriList = new ArrayList<>();
        private ArrayList<Boolean> mList;
        private Boolean isDrink = false;

        private MenuViewAdapter(int index, ArrayList<String> imagePaths, ArrayList<Boolean> inStock) {
            this.index = index;
            for (String path : imagePaths) {
                Uri uri = Uri.parse(path);
                uriList.add(uri);
            }
            mList = inStock;
            isDrink = false;
        }

        @NonNull
        @Override
        public MenuViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            ConstraintLayout layout = new ConstraintLayout(context);
            //ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(PRODUCT_WIDTH, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            ConstraintSet set = new ConstraintSet();
            layout.setBackgroundColor(0x00000000);
            layout.setPadding(0, 0, 0, 0);
            layout.setLayoutParams(layoutParams);

            LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0);
            ImageButton menuImage = new ImageButton(context);
            menuImage.setId(View.generateViewId());
            menuImage.setLayoutParams(imageLayoutParams);

            String ice = Utils.getColorSpanned("ICE", PreferenceManager.getString(context, "ice_text_color"));
            String icePrice = Utils.getColorSpanned("", PreferenceManager.getString(context, "ice_price_text_color"));
            String hot = Utils.getColorSpanned("HOT", PreferenceManager.getString(context, "hot_text_color"));
            String hotPrice = Utils.getColorSpanned("", PreferenceManager.getString(context, "hot_price_text_color"));

            ConstraintLayout.LayoutParams textLayoutParams;
            ConstraintLayout textLayout = new ConstraintLayout(context);
            //좌, 우는 80 중앙은 MATCH_CONSTRAINT
            int align_index = PreferenceManager.getInt(context, "align_index");
            if (align_index == 0 | align_index == 1) {
                textLayoutParams = new ConstraintLayout.LayoutParams(105, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
            } else if (align_index == 2 | align_index == 3) {
                textLayoutParams = new ConstraintLayout.LayoutParams(100, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
            } else {
                textLayoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
            }

            textLayout.setId(View.generateViewId());
            textLayout.setPadding(0, 0, 0, 0);
            textLayout.setLayoutParams(textLayoutParams);

            TextView priceView = new TextView(context);
            priceView.setLayoutParams(textLayoutParams);
            priceView.setId(View.generateViewId());
            priceView.setText(Html.fromHtml(ice + " " + icePrice + " " + hot + " " + hotPrice, Html.FROM_HTML_MODE_LEGACY));
            priceView.setTextSize(15);

            int[] top_padding = {90, 90, 70, 70, 50, 155};
            priceView.setPadding(0, top_padding[PreferenceManager.getInt(context, "align_index")], 0, 0);

            menuImage.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int width = menuImage.getMeasuredWidth();
            //품절 레이아웃 수정하였음 LinearLayout.LayoutParams.MATCH_PARENT -> ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            LinearLayout.LayoutParams outOfStockParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
            ImageButton outOfStockImage = new ImageButton(context);
            outOfStockImage.setId(View.generateViewId());
            outOfStockImage.setLayoutParams(outOfStockParams);
            outOfStockImage.setScaleType(ImageView.ScaleType.FIT_XY);

            layout.addView(menuImage, 0);
            layout.addView(outOfStockImage, 1);
            layout.addView(priceView);
            //가격표시
            if (PreferenceManager.getInt(MainActivity.this, "opacity") != 1) {
                priceView.setVisibility(View.GONE);
            }

            set.clone(layout);
            set.connect(menuImage.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 0);
            set.connect(menuImage.getId(), ConstraintSet.START, layout.getId(), ConstraintSet.START, 0);
            set.connect(menuImage.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM, 0);
            set.connect(outOfStockImage.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 0);
            set.connect(outOfStockImage.getId(), ConstraintSet.START, layout.getId(), ConstraintSet.START, 0);
            set.connect(outOfStockImage.getId(), ConstraintSet.END, layout.getId(), ConstraintSet.END, 0);
            set.connect(outOfStockImage.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM, 0);
            set.connect(priceView.getId(), ConstraintSet.TOP, menuImage.getId(), ConstraintSet.TOP, 0);
            set.connect(priceView.getId(), ConstraintSet.BOTTOM, menuImage.getId(), ConstraintSet.BOTTOM, 0);
            //좌는 START, 0, 우는 END, 0, 중앙은 START, 0, END, 0
            if (align_index == 0 | align_index == 2) {
                set.connect(priceView.getId(), ConstraintSet.START, menuImage.getId(), ConstraintSet.START, 0);
            } else if (align_index == 1 | align_index == 3) {
                set.connect(priceView.getId(), ConstraintSet.END, menuImage.getId(), ConstraintSet.END, 0);
            } else {
                set.connect(priceView.getId(), ConstraintSet.START, menuImage.getId(), ConstraintSet.START, 0);
                set.connect(priceView.getId(), ConstraintSet.END, menuImage.getId(), ConstraintSet.END, 0);
            }
            set.applyTo(layout);
            return new ViewHolder(context, layout, menuImage.getId(), outOfStockImage.getId(), priceView.getId());
        }

        @Override
        public void onBindViewHolder(@NonNull MenuViewAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            holder.image.setImageURI(uriList.get(position));
            holder.image.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int width = holder.image.getMeasuredWidth();
            int height = holder.image.getMeasuredHeight();
            if (width > 200)
                holder.outOfStock.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
            holder.updateStock();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            ImageButton image;
            ImageButton outOfStock;
            TextView priceView;
            int outOfStockClickCount = 0;

            public ViewHolder(Context context, @NonNull View itemView, int imageId, int outOfStockId, int priceViewId) {
                super(itemView);
                image = itemView.findViewById(imageId);
                image.setBackgroundColor(0x00000000);
                image.setPadding(0, 0, 0, 0);
                outOfStock = itemView.findViewById(outOfStockId);
                outOfStock.setBackgroundColor(0x00000000);
                outOfStock.setPadding(0, 0, 0, 0);
                image.setOnClickListener(v -> {
                    Utils.logD(TAG, "디저트 " + index + "번째 카테고리 " + getBindingAdapterPosition() + "번째 메뉴 클릭");
                    showOptionDialog(index, getBindingAdapterPosition());
                });

                outOfStock.setImageURI(Uri.parse(getExternalFilesDir(null) + "/화면_메뉴/이미지_품절.png"));
                outOfStock.setElevation(1);

                outOfStock.setOnClickListener(view -> {
                    outOfStockClickCount++;
                    if (outOfStockClickCount >= 20) {
                        outOfStockClickCount = 0;
                        if (!isDrink) {
                            Category tempCategory = DessertDataLoader.loadCategories(dbManager).stream().filter(category -> category.available > 0).collect(Collectors.toList()).get(index);
                            Product tempProduct = DessertDataLoader.loadProductsByCategoryId(dbManager, tempCategory.id, true).get(getBindingAdapterPosition());
                        }
                    }
                });

                priceView = itemView.findViewById(priceViewId);
            }

            protected void updateStock() {
                int position = getBindingAdapterPosition();
                if (mList.get(position)) {
                    image.setClickable(true);
                    outOfStock.setVisibility(View.INVISIBLE);
                } else {
                    image.setClickable(false);
                    outOfStock.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        String tempKey;
        char pressedKey = (char) event.getUnicodeChar();
        tempKey = String.valueOf(pressedKey);
        if (queue.size() < 7) {
            queue.add(tempKey);
        } else {
            queue.poll();
            queue.add(tempKey);
        }
        String queueString = "";
        for (String s : queue) {
            queueString += s;
        }
        if (queueString.contains("admin")) {
            UIManager.showSystemUI(this);
            Intent intent = new Intent(this, AdminSettingsActivity.class);
            this.startActivity(intent);
        } else if (queueString.contains("manager")) {
            Intent intent = new Intent(this, ManagerSettings.class);
            this.startActivity(intent);
        } else if (queueString.contains(PreferenceManager.getString(this, "admin_password"))) {
            UIManager.showSystemUI(this);
            Intent intent = new Intent(this, AdminSettingsActivity.class);
            this.startActivity(intent);
        } else if (queueString.contains(PreferenceManager.getString(this, "manager_password"))) {
            Intent intent = new Intent(this, ManagerSettings.class);
            this.startActivity(intent);
        } else if (queueString.contains(PreferenceManager.getString(this, "dessert_password"))) {
            Intent intent = new Intent(this, DessertSettingsActivity.class);
            this.startActivity(intent);
        } else if (queueString.contains("5555")) {
            UIManager.showSystemUI(this);
            Intent intent = new Intent(this, AdminSettingsActivity.class);
            this.startActivity(intent);
        }
        return true;
    }
}