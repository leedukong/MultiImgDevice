package com.releasetech.multidevice.Database;


import android.provider.BaseColumns;

public final class DataBase {


    public static final class CreateCategoryDB implements BaseColumns {
        public static final String NAME = "name";
        public static final String AVAILABLE = "available";
        public static final String INDEX = "idx";
        public static final String IMAGE = "image";
        public static final String _TABLENAME0 = "category";
        public static final String _CREATE0 = "create table if not exists " + _TABLENAME0 + "("
                + _ID + " integer primary key autoincrement, "
                + NAME + " text not null , "
                + AVAILABLE + " integer not null , "
                + INDEX + " integer not null , "
                + IMAGE + " text not null );";
    }

    public static final class CreateCategoryDessertDB implements BaseColumns {
        public static final String NAME = "name";
        public static final String AVAILABLE = "available";
        public static final String INDEX = "idx";
        public static final String IMAGE = "image";
        public static final String _TABLENAME0 = "category_dessert";

        public static final String _CREATE0 = "create table if not exists " + _TABLENAME0 + "("
                + _ID + " integer primary key autoincrement, "
                + NAME + " text not null , "
                + AVAILABLE + " integer not null , "
                + INDEX + " integer not null , "
                + IMAGE + " text not null );";
    }

    public static final class CreateProductDessertDB implements BaseColumns {
        public static final String NAME = "name";
        public static final String CATEGORY = "category";
        public static final String INDEX = "idx";
        public static final String NUMBER = "number";
        public static final String AVAILABLE = "available";
        public static final String PRICE = "price";
        public static final String CURRENT_COUNT = "current_count";
        public static final String TOTAL_COUNT = "total_count";
        public static final String IMAGE_SET = "image_set";
        public static final String _TABLENAME0 = "product_dessert";
        public static final String _CREATE0 = "create table if not exists " + _TABLENAME0 + "("
                + _ID + " integer primary key autoincrement, "
                + NAME + " text not null , "
                + CATEGORY + " integer not null , "
                + INDEX + " integer not null , "
                + NUMBER + " integer not null , "
                + AVAILABLE + " integer not null , "
                + PRICE + " integer not null , "
                + CURRENT_COUNT + " integer not null , "
                + TOTAL_COUNT + " integer not null , "
                + IMAGE_SET + " integer not null );";
    }

    public static final class CreateProductImageDB implements BaseColumns {
        public static final String MENU_IDLE = "menu_idle";
        public static final String MENU_SELECTED = "menu_selected";
        public static final String CART_IDLE = "cart_idle";
        public static final String CART_SELECTED = "cart_selected";
        public static final String QUANTITY = "quantity";
        public static final String _TABLENAME0 = "product_image";
        public static final String _CREATE0 = "create table if not exists " + _TABLENAME0 + "("
                + _ID + " integer primary key autoincrement, "
                + MENU_IDLE + " text not null , "
                + MENU_SELECTED + " text not null , "
                + CART_IDLE + " text not null , "
                + CART_SELECTED + " text not null , "
                + QUANTITY + " text not null );";
    }

    public static final class CreateSalesDB implements BaseColumns {
        public static final String YEAR = "year";
        public static final String MONTH = "month";
        public static final String DAY = "day";
        public static final String TIME = "time";
        public static final String CATEGORY = "category";
        public static final String PRODUCT = "product";
        public static final String HOT_ICE_OPTION = "hot_ice_option";
        public static final String SIZE_OPTION = "size_option";
        public static final String BLEND_OPTION = "blend_option";
        public static final String SHOT_OPTION = "shot_option";
        public static final String PRICE = "price";
        public static final String APPROVAL_ID = "approval_id";
        public static final String CANCEL_ID = "cancel_id";
        public static final String _TABLENAME0 = "sales";
        public static final String _CREATE0 = "create table if not exists " + _TABLENAME0 + "("
                + _ID + " integer primary key autoincrement, "
                + YEAR + " integer not null , "
                + MONTH + " integer not null , "
                + DAY + " integer not null , "
                + TIME + " text not null , "
                + CATEGORY + " text not null , "
                + PRODUCT + " text not null , "
                + HOT_ICE_OPTION + " text not null , "
                + SIZE_OPTION + " text not null , "
                + BLEND_OPTION + " text not null , "
                + SHOT_OPTION + " text not null , "
                + PRICE + " text not null , "
                + APPROVAL_ID + " text not null , "
                + CANCEL_ID + " text not null );";
    }

    public static final class CreateCheckoutDB implements BaseColumns {
        public static final String TRANS_NO = "trans_no";
        public static final String TRANS_TYPE = "trans_type";
        public static final String CARD_NUM = "card_num";
        public static final String CARD_NAME = "card_name";
        public static final String ISSUER_CODE = "issuer_code";
        public static final String TOTAL_AMOUNT = "total_amount";
        public static final String TAX = "tax";
        public static final String RESULT_CODE = "result_code";
        public static final String RESULT_MSG = "result_msg";
        public static final String APPROVAL_NUM = "approval_num";
        public static final String APPROVAL_DATE = "approval_date";
        public static final String MERCHANT_NUM = "merchant_num";
        public static final String SHOP_TID = "shop_tid";
        public static final String SHOP_BIZ_NUM = "shop_biz_num";
        public static final String SHOP_NAME = "shop_name";
        public static final String SHOP_TEL = "shop_tel";
        public static final String SHOP_ADDRESS = "shop_address";
        public static final String SHOP_OWNER = "shop_owner";
        public static final String _TABLENAME0 = "checkout";
        public static final String _CREATE0 = "create table if not exists " + _TABLENAME0 + "("
                + _ID + " integer primary key autoincrement, "
                + TRANS_NO + " integer not null , "
                + TRANS_TYPE + " text not null , "
                + CARD_NUM + " text not null , "
                + CARD_NAME + " text not null , "
                + ISSUER_CODE + " text not null , "
                + TOTAL_AMOUNT + " integer not null , "
                + TAX + " integer not null , "
                + RESULT_CODE + " text not null , "
                + RESULT_MSG + " text not null , "
                + APPROVAL_NUM + " text not null , "
                + APPROVAL_DATE + " text not null , "
                + MERCHANT_NUM + " text not null , "
                + SHOP_TID + " text not null , "
                + SHOP_BIZ_NUM + " text not null , "
                + SHOP_NAME + " text not null , "
                + SHOP_TEL + " text not null , "
                + SHOP_ADDRESS + " text not null , "
                + SHOP_OWNER + " text not null );";
    }


    public static final class CreateCheckoutPaycoDB implements BaseColumns {
        public static final String RESULT_CODE = "result_code";
        public static final String SIGNATURE = "signature";
        public static final String PIN_CODE = "pin_code";
        public static final String TRADE_REQUEST_NO = "trade_request_no";
        public static final String TRADE_NO = "trade_no";
        public static final String TOTAL_AMOUNT = "total_amount";
        public static final String APPROVAL_NO = "approval_no";
        public static final String APPROVAL_DATE_TIME = "approval_date_time";
        public static final String APPROVAL_AMOUNT = "approval_amount";
        public static final String APPROVAL_CARD_NO = "approval_card_no";
        public static final String APPROVAL_COMPANY_NAME = "approval_company_name";
        public static final String _TABLENAME0 = "checkout_payco";
        public static final String _CREATE0 = "create table if not exists " + _TABLENAME0 + "("
                + _ID + " integer primary key autoincrement, "
                + RESULT_CODE + " text not null , "
                + SIGNATURE + " text not null , "
                + PIN_CODE + " text not null , "
                + TRADE_REQUEST_NO + " text not null , "
                + TRADE_NO + " text not null , "
                + APPROVAL_COMPANY_NAME + " text not null, "
                + APPROVAL_NO + " text not null , "
                + APPROVAL_AMOUNT + " integer not null , "
                + APPROVAL_DATE_TIME + " text not null , "
                + APPROVAL_CARD_NO + " text not null , "
                + TOTAL_AMOUNT + " integer not null );";
    }
}