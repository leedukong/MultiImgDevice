package com.releasetech.multidevice.Tool;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.releasetech.multidevice.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class UIImageLoader {
    private static final String MENU = "화면_메뉴/";
    private static final String CART = "장바구니/";
    private static final String OPTION = "팝업_옵션/";
    private static final String CHECKOUT = "팝업_결제/";
    private static final String RESET = "팝업_전체취소/";
    private static final String MAKING = "화면_제조/";
    private static final String PNG = ".png";
    public static HashMap<Integer, String> menuCartMap = new HashMap<>();
    public static HashMap<Integer, String> optionDialogStillMap = new HashMap<>();
    public static HashMap<Integer, String> dessertDialogStillMap = new HashMap<>();
    public static HashMap<Integer, String> checkoutDialogMap = new HashMap<>();
    public static HashMap<Integer, String> checkoutItemHolderMap = new HashMap<>();
    public static HashMap<Integer, String> resetCartDialogMap = new HashMap<>();
    public static HashMap<Integer, String[]> optionDialogSelectorMap = new HashMap<>();
    public static HashMap<Integer, String[]> checkoutDialogSelectorMap = new HashMap<>();
    public static HashMap<Integer, String[]> makingMap = new HashMap<>();

    static {
        menuCartMap.put(R.id.menu_bottom_bar, "배경");
        menuCartMap.put(R.id.menu_cart_bg_1, "배경_품목");
        menuCartMap.put(R.id.menu_cart_bg_2, "배경_품목");
        menuCartMap.put(R.id.menu_cart_bg_3, "배경_품목");
        menuCartMap.put(R.id.menu_cart_bg_4, "배경_품목");
        menuCartMap.put(R.id.menu_cart_bg_5, "배경_품목");
        menuCartMap.put(R.id.menu_checkout, "버튼_결제");
        menuCartMap.put(R.id.menu_cancel_all, "버튼_전체취소");
        menuCartMap.put(R.id.menu_logo, "이미지_로고");
        menuCartMap.put(R.id.menu_total, "이미지_합계");
    }

    static {
        dessertDialogStillMap.put(R.id.option_back, "버튼_뒤로가기");
        dessertDialogStillMap.put(R.id.option_ok, "버튼_담기");
        dessertDialogStillMap.put(R.id.option_incr, "버튼_배경2");
        dessertDialogStillMap.put(R.id.option_incr, "버튼_수량증가");
        dessertDialogStillMap.put(R.id.option_decr, "버튼_수량감소");
    }

    static {
        checkoutDialogMap.put(R.id.dialog_checkout_background, "배경");
        checkoutDialogMap.put(R.id.button_dialog_checkout_ok, "버튼_결제");
        checkoutDialogMap.put(R.id.button_dialog_checkout_cancel, "버튼_취소");
    }

    /*
    static {
        checkoutItemHolderMap.put(R.id.dialog_checkout_item_coupon, "버튼_쿠폰적용");
        checkoutItemHolderMap.put(R.id.dialog_checkout_item_cancel, "버튼_수량취소");
    }*/

    static{
        checkoutDialogSelectorMap.put(R.id.checkout_card,
                new String[]{
                        "버튼_카드",
                        "버튼_카드_선택"
                });
        checkoutDialogSelectorMap.put(R.id.checkout_payco,
                new String[]{
                        "버튼_페이코",
                        "버튼_페이코_선택"
                });

        /*
        checkoutDialogSelectorMap.put(R.id.checkout_samsungpay,
                new String[]{
                        "버튼_삼성페이",
                        "버튼_삼성페이"
                });*/
    }

    static {
        resetCartDialogMap.put(R.id.cart_reset_background, "배경");
        resetCartDialogMap.put(R.id.option_reset_cart_ok, "버튼_확인");
        resetCartDialogMap.put(R.id.option_reset_cart_cancel, "버튼_취소");
    }
    public static void loadMenuImage(Context context, View view) {
        loadImage(context, view, MENU + CART, menuCartMap);
    }
    public static void loadDessertDialogImage(Context context, View view){
        loadImage(context, view, OPTION, dessertDialogStillMap);
    }

    public static void loadResetCartDialogImage(Context context, View view) {
        loadImage(context, view, RESET, resetCartDialogMap);
    }

    public static void loadCheckoutDialogImage(Context context, View view) {
        loadImage(context, view, CHECKOUT, checkoutDialogMap);
        loadRadioImage2(context, view, checkoutDialogSelectorMap);
    }

    public static void loadCheckoutItemHolderImage(Context context, View view) {
        loadImage(context, view, CHECKOUT, checkoutItemHolderMap);
    }

    private static void loadImage(Context context, View view, String header, HashMap<Integer, String> map) {
        String PATH = context.getExternalFilesDir(null) + "/";
        map.forEach((key, value) -> setImage(view.findViewById(key), PATH + header + value));
    }

    private static void setImage(View view, String imagePath) {
        String path = imagePath + PNG;
        if (view instanceof ImageView) {
            ((ImageView) view).setImageURI(Uri.parse(imagePath + PNG));
        } else {
            view.setBackground(Drawable.createFromPath(path));
        }
    }

    private static void loadRadioImage(Context context, View view, HashMap<Integer, String[]> map) {
        String PATH = context.getExternalFilesDir(null) + "/";
        map.forEach((key, value) -> setRadioImage(view.findViewById(key),
                Arrays.stream(value)
                        .map(l -> PATH + UIImageLoader.OPTION + l + PNG)
                        .collect(Collectors.toList())
                        .toArray(new String[3])
        ));
    }

    private static void loadRadioImage2(Context context, View view, HashMap<Integer, String[]> map) {
        String PATH = context.getExternalFilesDir(null) + "/";
        map.forEach((key, value) -> setRadioImage(view.findViewById(key),
                Arrays.stream(value)
                        .map(l -> PATH + UIImageLoader.CHECKOUT + l + PNG)
                        .collect(Collectors.toList())
                        .toArray(new String[3])
        ));
    }

    private static void setRadioImage(View view, String[] imagePaths) {
        view.setBackground(
                makeSelector(
                        Drawable.createFromPath(imagePaths[0]),
                        Drawable.createFromPath(imagePaths[1]),
                        Drawable.createFromPath(imagePaths[2])
                )
        );
    }

    private static StateListDrawable makeSelector(Drawable idle, Drawable checked, Drawable disabled) {
        StateListDrawable res = new StateListDrawable();
        res.addState(new int[]{android.R.attr.state_enabled, -android.R.attr.state_checked}, idle);
        res.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_checked}, checked);
        res.addState(new int[]{-android.R.attr.state_enabled}, disabled);
        return res;
    }


}
