package com.releasetech.multidevice.Database;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.releasetech.multidevice.Client.Payco;
import com.releasetech.multidevice.Database.Data.Category;
import com.releasetech.multidevice.Database.Data.ImageSet;
import com.releasetech.multidevice.Database.Data.Order;
import com.releasetech.multidevice.Database.Data.Product;
import com.releasetech.multidevice.Tool.Utils;

import java.io.IOException;

public class DBManager {
    private static final String TAG = "[DATABASE]";

    public static final String DATABASE_NAME = "MainDatabase.db";
    private static final int DATABASE_VERSION = 1;
    public static final int CATEGORY = 0;
    public static final int PRODUCT = 1;
    public static final int SIZE_RECIPE_PACK = 2;
    public static final int SHOT_RECIPE_PACK = 3;
    public static final int RECIPE = 4;
    public static final int PRODUCT_IMAGE = 5;
    public static final int SALES = 6;
    public static final int CHECKOUT = 7;
    public static final int CHECKOUT_PAYCO = 8;
    public static final int SALES_NICE = 9;

    public static final int CATEGORY_DESSERT = 10;
    public static final int PRODUCT_DESSERT = 11;

    public static final int BREW_INFO = 12;

    public static SQLiteDatabase mDB;
    private DatabaseOpenHelper mDBHelper;
    private final Context mCtx;

    public DBManager open() throws SQLException {
        mDBHelper = new DatabaseOpenHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        try {
            mDB = mDBHelper.getWritableDatabase();
        } catch (android.database.sqlite.SQLiteException e) {
            Utils.logE(TAG, e.getMessage());
            mDB = mDBHelper.getReadableDatabase();
        }
        Utils.logD(TAG, "Database Opened");
        return this;
    }

    public DBManager(Context context) {
        this.mCtx = context;
    }

    public void create() {
        mDBHelper.onCreate(mDB);
        Utils.logD(TAG, "Database Created");
    }

    public void close() {
        mDB.close();
        Utils.logD(TAG, "Database Closed");
    }

    public long insertColumn(int table, Order order) {
        if (table == SALES) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateSalesDB.YEAR, order.year);
            values.put(DataBase.CreateSalesDB.MONTH, order.month);
            values.put(DataBase.CreateSalesDB.DAY, order.day);
            values.put(DataBase.CreateSalesDB.TIME, order.time);
            values.put(DataBase.CreateSalesDB.CATEGORY, order.category);
            values.put(DataBase.CreateSalesDB.PRODUCT, order.product);
            values.put(DataBase.CreateSalesDB.HOT_ICE_OPTION, order.hotIceOption);
            values.put(DataBase.CreateSalesDB.SIZE_OPTION, order.sizeOption);
            values.put(DataBase.CreateSalesDB.BLEND_OPTION, order.blendOption);
            values.put(DataBase.CreateSalesDB.SHOT_OPTION, order.shotOption);
            values.put(DataBase.CreateSalesDB.PRICE, order.price);
            values.put(DataBase.CreateSalesDB.APPROVAL_ID, order.approvalId);
            values.put(DataBase.CreateSalesDB.CANCEL_ID, order.cancelId);
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.insert(DataBase.CreateSalesDB._TABLENAME0, null, values);
        } else return 0;
    }

    // Insert DB
    public long insertColumn(int table, Category c) {
        if (table == CATEGORY) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateCategoryDB.NAME, c.name);
            values.put(DataBase.CreateCategoryDB.AVAILABLE, c.available);
            values.put(DataBase.CreateCategoryDB.INDEX, c.index);
            values.put(DataBase.CreateCategoryDB.IMAGE, c.image);
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.insert(DataBase.CreateCategoryDB._TABLENAME0, null, values);
        } else if (table == CATEGORY_DESSERT) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateCategoryDessertDB.NAME, c.name);
            values.put(DataBase.CreateCategoryDessertDB.AVAILABLE, c.available);
            values.put(DataBase.CreateCategoryDessertDB.INDEX, c.index);
            values.put(DataBase.CreateCategoryDessertDB.IMAGE, c.image);
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.insert(DataBase.CreateCategoryDessertDB._TABLENAME0, null, values);
        } else return 0;
    }

    public long insertColumn(int table, Product p) {
        if (table == PRODUCT_DESSERT) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateProductDessertDB.NAME, p.name);
            values.put(DataBase.CreateProductDessertDB.CATEGORY, p.category);
            values.put(DataBase.CreateProductDessertDB.INDEX, p.index);
            values.put(DataBase.CreateProductDessertDB.NUMBER, p.number);
            values.put(DataBase.CreateProductDessertDB.AVAILABLE, p.available);
            values.put(DataBase.CreateProductDessertDB.PRICE, p.price);
            values.put(DataBase.CreateProductDessertDB.CURRENT_COUNT, 0);
            values.put(DataBase.CreateProductDessertDB.TOTAL_COUNT, p.total_count);
            values.put(DataBase.CreateProductDessertDB.IMAGE_SET, p.image_set);
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.insert(DataBase.CreateProductDessertDB._TABLENAME0, null, values);
        } else return 0;
    }

    public long insertColumn(int table, ImageSet is) {
        if (table == PRODUCT_IMAGE) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateProductImageDB.MENU_IDLE, is.menu_idle);
            values.put(DataBase.CreateProductImageDB.MENU_SELECTED, is.menu_selected);
            values.put(DataBase.CreateProductImageDB.CART_IDLE, is.cart_idle);
            values.put(DataBase.CreateProductImageDB.CART_SELECTED, is.cart_selected);
            values.put(DataBase.CreateProductImageDB.QUANTITY, is.quantity);
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.insert(DataBase.CreateProductImageDB._TABLENAME0, null, values);
        } else return 0;
    }

    public boolean updateColumn(int table, Order order) {
        if (table == SALES) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateSalesDB.YEAR, order.year);
            values.put(DataBase.CreateSalesDB.MONTH, order.month);
            values.put(DataBase.CreateSalesDB.DAY, order.day);
            values.put(DataBase.CreateSalesDB.TIME, order.time);
            values.put(DataBase.CreateSalesDB.CATEGORY, order.category);
            values.put(DataBase.CreateSalesDB.PRODUCT, order.product);
            values.put(DataBase.CreateSalesDB.HOT_ICE_OPTION, order.hotIceOption);
            values.put(DataBase.CreateSalesDB.SIZE_OPTION, order.sizeOption);
            values.put(DataBase.CreateSalesDB.BLEND_OPTION, order.blendOption);
            values.put(DataBase.CreateSalesDB.SHOT_OPTION, order.shotOption);
            values.put(DataBase.CreateSalesDB.PRICE, order.price);
            values.put(DataBase.CreateSalesDB.APPROVAL_ID, order.approvalId);
            values.put(DataBase.CreateSalesDB.CANCEL_ID, order.cancelId);
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.update(DataBase.CreateSalesDB._TABLENAME0, values, "_id=" + order.id, null) > 0;
        } else return false;
    }

    public long insertColumn(int table, Intent data) {
        if (table == CHECKOUT) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateCheckoutDB.TRANS_NO, data.getStringExtra("TRAN_NO"));
            values.put(DataBase.CreateCheckoutDB.TRANS_TYPE, data.getStringExtra("TRAN_TYPE"));
            values.put(DataBase.CreateCheckoutDB.CARD_NUM, data.getStringExtra("CARD_NUM"));
            values.put(DataBase.CreateCheckoutDB.CARD_NAME, data.getStringExtra("CARD_NAME"));
            values.put(DataBase.CreateCheckoutDB.ISSUER_CODE, data.getStringExtra("ISSUER_CODE"));
            values.put(DataBase.CreateCheckoutDB.TOTAL_AMOUNT, data.getStringExtra("TOTAL_AMOUNT"));
            values.put(DataBase.CreateCheckoutDB.TAX, data.getStringExtra("TAX"));
            values.put(DataBase.CreateCheckoutDB.RESULT_CODE, data.getStringExtra("RESULT_CODE"));
            String resultMsg = data.getStringExtra("RESULT_MSG");
            if (resultMsg == null) resultMsg = "정상 승인";
            values.put(DataBase.CreateCheckoutDB.RESULT_MSG, resultMsg);
            values.put(DataBase.CreateCheckoutDB.APPROVAL_NUM, data.getStringExtra("APPROVAL_NUM"));
            values.put(DataBase.CreateCheckoutDB.APPROVAL_DATE, data.getStringExtra("APPROVAL_DATE"));
            values.put(DataBase.CreateCheckoutDB.MERCHANT_NUM, data.getStringExtra("MERCHANT_NUM"));
            values.put(DataBase.CreateCheckoutDB.SHOP_TID, data.getStringExtra("SHOP_TID"));
            values.put(DataBase.CreateCheckoutDB.SHOP_BIZ_NUM, data.getStringExtra("SHOP_BIZ_NUM"));
            values.put(DataBase.CreateCheckoutDB.SHOP_NAME, data.getStringExtra("SHOP_NAME"));
            values.put(DataBase.CreateCheckoutDB.SHOP_TEL, data.getStringExtra("SHOP_TEL"));
            values.put(DataBase.CreateCheckoutDB.SHOP_ADDRESS, data.getStringExtra("SHOP_ADDRESS"));
            values.put(DataBase.CreateCheckoutDB.SHOP_OWNER, data.getStringExtra("SHOP_OWNER"));
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.insert(DataBase.CreateCheckoutDB._TABLENAME0, null, values);
        } else return 0;
    }

    public long insertColumnNice(int table, String tranNo, String tranType, String cardNum, String cardName, String issuerCode, int totalAmount, String tax, String resultCode, String resultMsg, String approvalNum, String approvalDate, String merchantNum, String shopTid, String shopBizNum, String shopName, String shopTel, String shopAddress, String shopOwner) {
        if (table == CHECKOUT) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateCheckoutDB.TRANS_NO, tranNo);
            values.put(DataBase.CreateCheckoutDB.TRANS_TYPE, tranType);
            values.put(DataBase.CreateCheckoutDB.CARD_NUM, cardNum.replaceAll(" ", ""));
            values.put(DataBase.CreateCheckoutDB.CARD_NAME, cardName);
            values.put(DataBase.CreateCheckoutDB.ISSUER_CODE, issuerCode);
            values.put(DataBase.CreateCheckoutDB.TOTAL_AMOUNT, totalAmount);
            values.put(DataBase.CreateCheckoutDB.TAX, tax);
            values.put(DataBase.CreateCheckoutDB.RESULT_CODE, resultCode);
            values.put(DataBase.CreateCheckoutDB.RESULT_MSG, resultMsg);
            values.put(DataBase.CreateCheckoutDB.APPROVAL_NUM, approvalNum);
            values.put(DataBase.CreateCheckoutDB.APPROVAL_DATE, approvalDate);
            values.put(DataBase.CreateCheckoutDB.MERCHANT_NUM, merchantNum);
            values.put(DataBase.CreateCheckoutDB.SHOP_TID, shopTid);
            values.put(DataBase.CreateCheckoutDB.SHOP_BIZ_NUM, shopBizNum);
            values.put(DataBase.CreateCheckoutDB.SHOP_NAME, shopName);
            values.put(DataBase.CreateCheckoutDB.SHOP_TEL, shopTel);
            values.put(DataBase.CreateCheckoutDB.SHOP_ADDRESS, shopAddress);
            values.put(DataBase.CreateCheckoutDB.SHOP_OWNER, shopOwner);
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.insert(DataBase.CreateCheckoutDB._TABLENAME0, null, values);
        } else return 0;
    }

    public long insertColumn(int table, Payco payco) {
        if (table == CHECKOUT_PAYCO) {
            for (int i = 0; i < payco.getPaycoApprovalInfoArray("approvalNo").size(); i++) {
                ContentValues values = new ContentValues();
                values.put(DataBase.CreateCheckoutPaycoDB.RESULT_CODE, payco.getPaycoApprovalInfoString("resultCode"));
                values.put(DataBase.CreateCheckoutPaycoDB.SIGNATURE, payco.getPaycoApprovalInfoString("signature"));
                values.put(DataBase.CreateCheckoutPaycoDB.TRADE_REQUEST_NO, payco.getPaycoApprovalInfoString("tradeRequestNo"));
                values.put(DataBase.CreateCheckoutPaycoDB.TRADE_NO, payco.getPaycoApprovalInfoString("tradeNo"));
                values.put(DataBase.CreateCheckoutPaycoDB.PIN_CODE, payco.getPinCode());
                values.put(DataBase.CreateCheckoutPaycoDB.APPROVAL_NO, payco.getPaycoApprovalInfoArray("approvalNo").get(i));
                values.put(DataBase.CreateCheckoutPaycoDB.APPROVAL_AMOUNT, payco.getPaycoApprovalInfoArray("approvalAmount").get(i));
                values.put(DataBase.CreateCheckoutPaycoDB.APPROVAL_DATE_TIME, payco.getPaycoApprovalInfoArray("approvalDateTime").get(i));
                values.put(DataBase.CreateCheckoutPaycoDB.APPROVAL_CARD_NO, payco.getPaycoApprovalInfoArray("approvalCardNo").get(i));
                values.put(DataBase.CreateCheckoutPaycoDB.APPROVAL_COMPANY_NAME, payco.getPaycoApprovalInfoArray("approvalCompanyName").get(i));
                values.put(DataBase.CreateCheckoutPaycoDB.TOTAL_AMOUNT, payco.getPaycoTotalAmount());
                if (!DBManager.mDB.isOpen()) {
                    open();
                    create();
                }
                mDB.insert(DataBase.CreateCheckoutPaycoDB._TABLENAME0, null, values);
            }
        } else return 0;
        return 0;
    }

    // Update DB
    public boolean updateColumn(int table, Category c) {
        if (table == CATEGORY) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateCategoryDB.NAME, c.name);
            values.put(DataBase.CreateCategoryDB.AVAILABLE, c.available);
            values.put(DataBase.CreateCategoryDB.INDEX, c.index);
            values.put(DataBase.CreateCategoryDB.IMAGE, c.image);
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.update(DataBase.CreateCategoryDB._TABLENAME0, values, "_id=" + c.id, null) > 0;
        } else if (table == CATEGORY_DESSERT) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateCategoryDB.NAME, c.name);
            values.put(DataBase.CreateCategoryDB.AVAILABLE, c.available);
            values.put(DataBase.CreateCategoryDB.INDEX, c.index);
            values.put(DataBase.CreateCategoryDB.IMAGE, c.image);
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.update(DataBase.CreateCategoryDessertDB._TABLENAME0, values, "_id=" + c.id, null) > 0;
        } else return false;
    }

    public boolean updateColumn(int table, Product p) {
        if (table == PRODUCT_DESSERT) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateProductDessertDB.NAME, p.name);
            values.put(DataBase.CreateProductDessertDB.CATEGORY, p.category);
            values.put(DataBase.CreateProductDessertDB.INDEX, p.index);
            values.put(DataBase.CreateProductDessertDB.NUMBER, p.number);
            values.put(DataBase.CreateProductDessertDB.AVAILABLE, p.available);
            values.put(DataBase.CreateProductDessertDB.PRICE, p.price);
            values.put(DataBase.CreateProductDessertDB.CURRENT_COUNT, 0);
            values.put(DataBase.CreateProductDessertDB.TOTAL_COUNT, p.total_count);
            values.put(DataBase.CreateProductDessertDB.IMAGE_SET, p.image_set);
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.update(DataBase.CreateProductDessertDB._TABLENAME0, values, "_id=" + p.id, null) > 0;
        } else return false;
    }

    public boolean updateColumnTodecreaseCount(long id) {
        ContentValues values = new ContentValues();
        values.put(DataBase.CreateProductDessertDB.CURRENT_COUNT, DessertDataLoader.loadCurrentStockById(this, id) - 1);
        if (!DBManager.mDB.isOpen()) {
            open();
            create();
        }
        return mDB.update(DataBase.CreateProductDessertDB._TABLENAME0, values, "_id=" + id, null) > 0;
    }

    public boolean updateColumnToRechargeCount(long id) {
        ContentValues values = new ContentValues();
        values.put(DataBase.CreateProductDessertDB.CURRENT_COUNT, DessertDataLoader.loadProductById(this, id).total_count);
        if (!DBManager.mDB.isOpen()) {
            open();
            create();
        }
        return mDB.update(DataBase.CreateProductDessertDB._TABLENAME0, values, "_id=" + id, null) > 0;
    }

    public boolean updateColumn(int table, ImageSet is) {
        if (table == PRODUCT_IMAGE) {
            ContentValues values = new ContentValues();
            values.put(DataBase.CreateProductImageDB.MENU_IDLE, is.menu_idle);
            values.put(DataBase.CreateProductImageDB.MENU_SELECTED, is.menu_selected);
            values.put(DataBase.CreateProductImageDB.CART_IDLE, is.cart_idle);
            values.put(DataBase.CreateProductImageDB.CART_SELECTED, is.cart_selected);
            values.put(DataBase.CreateProductImageDB.QUANTITY, is.quantity);
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            return mDB.update(DataBase.CreateProductImageDB._TABLENAME0, values, "_id=" + is.id, null) > 0;
        } else return false;
    }

    void replaceSubText(int table, String src, String dst) {
        if (table == CATEGORY) {
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            mDB.execSQL("UPDATE " + DataBase.CreateCategoryDB._TABLENAME0 + " SET " + DataBase.CreateCategoryDB.IMAGE + " = REPLACE(" + DataBase.CreateCategoryDB.IMAGE + ", '" + src + "', '" + dst + "')");
        } else if (table == PRODUCT_IMAGE) {
            if (!DBManager.mDB.isOpen()) {
                open();
                create();
            }
            mDB.execSQL("UPDATE " + DataBase.CreateProductImageDB._TABLENAME0 + " SET " + DataBase.CreateProductImageDB.MENU_IDLE + " = REPLACE(" + DataBase.CreateProductImageDB.MENU_IDLE + ", '" + src + "', '" + dst + "')");
            mDB.execSQL("UPDATE " + DataBase.CreateProductImageDB._TABLENAME0 + " SET " + DataBase.CreateProductImageDB.MENU_SELECTED + " = REPLACE(" + DataBase.CreateProductImageDB.MENU_SELECTED + ", '" + src + "', '" + dst + "')");
            mDB.execSQL("UPDATE " + DataBase.CreateProductImageDB._TABLENAME0 + " SET " + DataBase.CreateProductImageDB.CART_IDLE + " = REPLACE(" + DataBase.CreateProductImageDB.CART_IDLE + ", '" + src + "', '" + dst + "')");
            mDB.execSQL("UPDATE " + DataBase.CreateProductImageDB._TABLENAME0 + " SET " + DataBase.CreateProductImageDB.CART_SELECTED + " = REPLACE(" + DataBase.CreateProductImageDB.CART_SELECTED + ", '" + src + "', '" + dst + "')");
            mDB.execSQL("UPDATE " + DataBase.CreateProductImageDB._TABLENAME0 + " SET " + DataBase.CreateProductImageDB.QUANTITY + " = REPLACE(" + DataBase.CreateProductImageDB.QUANTITY + ", '" + src + "', '" + dst + "')");
        }
    }

    // Delete All
    public void deleteAllColumns(int table) {
        switch (table) {
            case CATEGORY:
                mDB.delete(DataBase.CreateCategoryDB._TABLENAME0, null, null);
                break;
            case PRODUCT_IMAGE:
                mDB.delete(DataBase.CreateProductImageDB._TABLENAME0, null, null);
                break;
            case SALES:
                mDB.delete(DataBase.CreateSalesDB._TABLENAME0, null, null);
                break;
            case CHECKOUT:
                mDB.delete(DataBase.CreateCheckoutDB._TABLENAME0, null, null);
                break;
            case CHECKOUT_PAYCO:
                mDB.delete(DataBase.CreateCheckoutPaycoDB._TABLENAME0, null, null);
                break;
            case CATEGORY_DESSERT:
                mDB.delete(DataBase.CreateCategoryDessertDB._TABLENAME0, null, null);
                break;
            case PRODUCT_DESSERT:
                mDB.delete(DataBase.CreateProductDessertDB._TABLENAME0, null, null);
                break;
        }
    }

    // Delete DB
    public boolean deleteColumn(int table, long id) {
        if (!DBManager.mDB.isOpen()) {
            open();
            create();
        }
        switch (table) {
            case CATEGORY:
                return mDB.delete(DataBase.CreateCategoryDB._TABLENAME0, "_id=" + id, null) > 0;
            case PRODUCT_IMAGE:
                return mDB.delete(DataBase.CreateProductImageDB._TABLENAME0, "_id=" + id, null) > 0;
            case SALES:
                return mDB.delete(DataBase.CreateSalesDB._TABLENAME0, "_id=" + id, null) > 0;
            case CHECKOUT:
                return mDB.delete(DataBase.CreateCheckoutDB._TABLENAME0, "_id=" + id, null) > 0;
            case CHECKOUT_PAYCO:
                return mDB.delete(DataBase.CreateCheckoutPaycoDB._TABLENAME0, "_id=" + id, null) > 0;
            case CATEGORY_DESSERT:
                return mDB.delete(DataBase.CreateCategoryDessertDB._TABLENAME0, "_id=" + id, null) > 0;
            case PRODUCT_DESSERT:
                return mDB.delete(DataBase.CreateProductDessertDB._TABLENAME0, "_id=" + id, null) > 0;
        }
        return false;
    }

    // Select DB
    public Cursor selectColumns(int table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        if (!DBManager.mDB.isOpen()) {
            open();
            create();
        }
        switch (table) {
            case CATEGORY:
                return mDB.query(DataBase.CreateCategoryDB._TABLENAME0, columns, selection, selectionArgs, groupBy, having, orderBy);
            case PRODUCT_IMAGE:
                return mDB.query(DataBase.CreateProductImageDB._TABLENAME0, columns, selection, selectionArgs, groupBy, having, orderBy);
            case SALES:
                return mDB.query(DataBase.CreateSalesDB._TABLENAME0, columns, selection, selectionArgs, groupBy, having, orderBy);
            case CHECKOUT:
                return mDB.query(DataBase.CreateCheckoutDB._TABLENAME0, columns, selection, selectionArgs, groupBy, having, orderBy);
            case CHECKOUT_PAYCO:
                return mDB.query(DataBase.CreateCheckoutPaycoDB._TABLENAME0, columns, selection, selectionArgs, groupBy, having, orderBy);
            case CATEGORY_DESSERT:
                return mDB.query(DataBase.CreateCategoryDessertDB._TABLENAME0, columns, selection, selectionArgs, groupBy, having, orderBy);
            case PRODUCT_DESSERT:
                return mDB.query(DataBase.CreateProductDessertDB._TABLENAME0, columns, selection, selectionArgs, groupBy, having, orderBy);
        }
        return null;
    }

    // sort by column
    public Cursor sortColumn(int table, String sort) {
        if (!DBManager.mDB.isOpen()) {
            open();
            create();
        }
        Cursor c;
        switch (table) {
            case CATEGORY:
                c = mDB.rawQuery("SELECT * FROM " + DataBase.CreateCategoryDB._TABLENAME0 + " ORDER BY " + sort + ";", null);
                return c;
            case PRODUCT_IMAGE:
                c = mDB.rawQuery("SELECT * FROM " + DataBase.CreateProductImageDB._TABLENAME0 + " ORDER BY " + sort + ";", null);
                return c;
            case SALES:
                c = mDB.rawQuery("SELECT * FROM " + DataBase.CreateSalesDB._TABLENAME0 + " ORDER BY " + sort + ";", null);
                return c;
            case CHECKOUT:
                c = mDB.rawQuery("SELECT * FROM " + DataBase.CreateCheckoutDB._TABLENAME0 + " ORDER BY " + sort + ";", null);
                return c;
            case CHECKOUT_PAYCO:
                c = mDB.rawQuery("SELECT * FROM " + DataBase.CreateCheckoutPaycoDB._TABLENAME0 + " ORDER BY " + sort + ";", null);
                return c;
            case CATEGORY_DESSERT:
                c = mDB.rawQuery("SELECT * FROM " + DataBase.CreateCategoryDessertDB._TABLENAME0 + " ORDER BY " + sort + ";", null);
                return c;
            case PRODUCT_DESSERT:
                c = mDB.rawQuery("SELECT * FROM " + DataBase.CreateProductDessertDB._TABLENAME0 + " ORDER BY " + sort + ";", null);
                return c;
        }
        return null;
    }

    public void query(String sql) {
        mDB.execSQL(sql);
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        public DatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                String permission_command = "";
                permission_command = "su -c chmod 777 /data/data/com.releasetech.multidevice/databases/MainDatabase.db";
                Runtime.getRuntime().exec(permission_command);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            db.execSQL(DataBase.CreateCategoryDB._CREATE0);
            db.execSQL(DataBase.CreateProductImageDB._CREATE0);
            db.execSQL(DataBase.CreateSalesDB._CREATE0);
            db.execSQL(DataBase.CreateCheckoutDB._CREATE0);
            db.execSQL(DataBase.CreateCheckoutPaycoDB._CREATE0);
            db.execSQL(DataBase.CreateCategoryDessertDB._CREATE0);
            db.execSQL(DataBase.CreateProductDessertDB._CREATE0);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            db.execSQL("DROP TABLE IF EXISTS " + DataBase.CreateCategoryDB._TABLENAME0);
//            db.execSQL("DROP TABLE IF EXISTS " + DataBase.CreateProductDB._TABLENAME0);
//            db.execSQL("DROP TABLE IF EXISTS " + DataBase.CreateSizeRecipePackDB._TABLENAME0);
//            db.execSQL("DROP TABLE IF EXISTS " + DataBase.CreateShotRecipePackDB._TABLENAME0);
//            db.execSQL("DROP TABLE IF EXISTS " + DataBase.CreateRecipeDB._TABLENAME0);
//            db.execSQL("DROP TABLE IF EXISTS " + DataBase.CreateProductImageDB._TABLENAME0);
//            db.execSQL("DROP TABLE IF EXISTS " + DataBase.CreateSalesDB._TABLENAME0);
//            db.execSQL("DROP TABLE IF EXISTS " + DataBase.CreateCheckoutDB._TABLENAME0);
//            db.execSQL("DROP TABLE IF EXISTS " + DataBase.CreateCheckoutPaycoDB._TABLENAME0);
            onCreate(db);
        }
    }
}
