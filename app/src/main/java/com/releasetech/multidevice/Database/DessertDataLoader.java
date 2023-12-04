package com.releasetech.multidevice.Database;

import static android.provider.BaseColumns._ID;

import android.annotation.SuppressLint;
import android.database.Cursor;

import com.releasetech.multidevice.Database.Data.Category;
import com.releasetech.multidevice.Database.Data.ImageSet;
import com.releasetech.multidevice.Database.Data.Order;
import com.releasetech.multidevice.Database.Data.Product;
import com.releasetech.multidevice.Tool.Utils;

import java.io.File;
import java.util.ArrayList;

@SuppressLint("Range")
public class DessertDataLoader {

    private static final String TAG = "[DESSERT_DATA_LOADER]";

    public static ArrayList<Category> loadCategories(DBManager dbManager){
        ArrayList<Category> categories = new ArrayList<>();
        Cursor iCursor = dbManager.sortColumn(DBManager.CATEGORY_DESSERT, "idx");
        if (iCursor == null) return null;
        while (iCursor.moveToNext()) {
            long tempID = iCursor.getLong(iCursor.getColumnIndex("_id"));
            String tempName = iCursor.getString(iCursor.getColumnIndex("name"));
            int tempAvailable = iCursor.getInt(iCursor.getColumnIndex("available"));
            int tempIndex = iCursor.getInt(iCursor.getColumnIndex("idx"));
            String tempImage = iCursor.getString(iCursor.getColumnIndex("image"));
            categories.add(new Category(tempID, tempName, tempAvailable, tempIndex, tempImage));
        }
        return categories;
    }

    public static long[] loadProductIds(DBManager dbManager) {
        Cursor iCursor = dbManager.sortColumn(DBManager.PRODUCT_DESSERT, DataBase.CreateProductDessertDB.INDEX);
        long[] ids = new long[iCursor.getCount()];
        int i = 0;
        while (iCursor.moveToNext()) {
            long tempId = iCursor.getLong(iCursor.getColumnIndex(_ID));
            ids[i] = tempId;
            i++;
        }
        return ids;
    }

    public static int loadCurrentStockById(DBManager dbManager, long productId) {
        Cursor iCursor = dbManager.sortColumn(DBManager.PRODUCT_DESSERT, DataBase.CreateProductDessertDB.INDEX);
        if (iCursor == null) return 0;
        while (iCursor.moveToNext()) {
            long id = iCursor.getLong(iCursor.getColumnIndex(_ID));
            if (id == productId) {
                int currentCount = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.CURRENT_COUNT));
                return currentCount;
            }
        }
        return 0;
    }

    public static Product loadProductById(DBManager dbManager, long productId) {
        Product product = null;
        Cursor iCursor = dbManager.sortColumn(DBManager.PRODUCT_DESSERT, DataBase.CreateProductDessertDB.INDEX);
        if (iCursor == null) {
            return null;
        }
        while (iCursor.moveToNext()) {
            long id = iCursor.getLong(iCursor.getColumnIndex(_ID));
            if (id == productId) {
                String name = iCursor.getString(iCursor.getColumnIndex("name"));
                int index = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.INDEX));
                long category = iCursor.getLong(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.CATEGORY));
                int available = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.AVAILABLE));
                int number = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.NUMBER));
                int price = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.PRICE));
                int totalCount = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.TOTAL_COUNT));
                long imageSetID = iCursor.getLong(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.IMAGE_SET));
                product = new Product.ProductBuilder(id, name, category, index)
                        .setAvailable(available)
                        .setPrice(price)
                        .setNumber(number)
                        .setTotalCount(totalCount)
                        .setImageSet(imageSetID)
                        .build();
                return product;
            }
        }
        return product;
    }

    public static Product loadProductByNumber(DBManager dbManager, int num) {
        Product product = null;
        Cursor iCursor = dbManager.sortColumn(DBManager.PRODUCT_DESSERT, DataBase.CreateProductDessertDB.NUMBER);
        if (iCursor == null) {
            return null;
        }
        while (iCursor.moveToNext()) {
            int tempNumber = iCursor.getInt(iCursor.getColumnIndex("number"));
            if (tempNumber == num) {
                String name = iCursor.getString(iCursor.getColumnIndex("name"));
                long id = iCursor.getLong(iCursor.getColumnIndex(_ID));
                int index = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.INDEX));
                long category = iCursor.getLong(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.CATEGORY));
                int available = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.AVAILABLE));
                int number = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.NUMBER));
                int price = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.PRICE));
                int totalCount = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.TOTAL_COUNT));
                long imageSetID = iCursor.getLong(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.IMAGE_SET));
                product = new Product.ProductBuilder(id, name, category, index)
                        .setAvailable(available)
                        .setPrice(price)
                        .setNumber(number)
                        .setTotalCount(totalCount)
                        .setImageSet(imageSetID)
                        .build();
                return product;
            }
        }
        return product;
    }

    public static ArrayList<Product> loadProductsByCategoryId(DBManager dbManager, long categoryId, boolean checkAvailability) {
        ArrayList<Product> products = new ArrayList<>();
        Cursor iCursor = dbManager.sortColumn(DBManager.PRODUCT_DESSERT, DataBase.CreateProductDessertDB.INDEX);
        if (iCursor == null) {
            return products;
        }
        while (iCursor.moveToNext()) {
            long category = iCursor.getLong(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.CATEGORY));
            if (category == categoryId) {
                int available = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.AVAILABLE));
                if (checkAvailability && available == 0) continue;
                long id = iCursor.getLong(iCursor.getColumnIndex(_ID));
                String name = iCursor.getString(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.NAME));
                int index = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.INDEX));
                long imageSetID = iCursor.getLong(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.IMAGE_SET));
                int price = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.PRICE));
                int number = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.NUMBER));
                int totalCount = iCursor.getInt(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.TOTAL_COUNT));
                products.add(new Product.ProductBuilder(id, name, category, index)
                        .setAvailable(available)
                        .setImageSet(imageSetID)
                        .setPrice(price)
                        .setTotalCount(totalCount)
                        .setNumber(number)
                        .build());
            }
        }
        return products;
    }

    public static ImageSet loadImageSet(DBManager dbManager, long imageSetId){
        ImageSet imageSet = null;
        Cursor iCursor = dbManager.sortColumn(DBManager.PRODUCT_IMAGE, "_id");
        if (iCursor == null) return null;
        while (iCursor.moveToNext()) {
            long tempImageSetId = iCursor.getLong(iCursor.getColumnIndex("_id"));
            if (tempImageSetId == imageSetId) {
                String menuIdle = iCursor.getString(iCursor.getColumnIndex(DataBase.CreateProductImageDB.MENU_IDLE));
                String menuSelected = iCursor.getString(iCursor.getColumnIndex(DataBase.CreateProductImageDB.MENU_SELECTED));
                String cartIdle = iCursor.getString(iCursor.getColumnIndex(DataBase.CreateProductImageDB.CART_IDLE));
                String cartSelected = iCursor.getString(iCursor.getColumnIndex(DataBase.CreateProductImageDB.CART_SELECTED));
                String quantity = iCursor.getString(iCursor.getColumnIndex(DataBase.CreateProductImageDB.QUANTITY));
                imageSet = new ImageSet(menuIdle, menuSelected, cartIdle, cartSelected, quantity);
                return imageSet;
            }
        }
        return imageSet;
    }

    public static void removeCategory(DBManager dbManager, long categoryId) {
        dbManager.deleteColumn(DBManager.CATEGORY_DESSERT, categoryId);
        removeProduct(dbManager, categoryId);
    }

    public static void removeProduct(DBManager dbManager, long productId) {
        Cursor iCursor = dbManager.sortColumn(DBManager.PRODUCT_DESSERT, DataBase.CreateProductDessertDB.INDEX);
        ArrayList<Long> toDelete = new ArrayList<>();
        if (iCursor == null) return;
        while (iCursor.moveToNext()) {
            long tempId = iCursor.getLong(iCursor.getColumnIndex(_ID));
            if (tempId == productId) {
                long imageSetId = iCursor.getLong(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.IMAGE_SET));
                removeImageSet(dbManager, imageSetId);
                toDelete.add(productId);
            }
        }
        for (long id : toDelete) {
            dbManager.deleteColumn(DBManager.PRODUCT_DESSERT, id);
        }
    }

    public static void removeProductToRemoveCategory(DBManager dbManager, long categoryId) {
        Cursor iCursor = dbManager.sortColumn(DBManager.PRODUCT_DESSERT, DataBase.CreateProductDessertDB.INDEX);
        ArrayList<Long> toDelete = new ArrayList<>();
        if (iCursor == null) return;
        while (iCursor.moveToNext()) {
            long tempId = iCursor.getLong(iCursor.getColumnIndex(_ID));
            long category = iCursor.getLong(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.CATEGORY));
            if (category == categoryId) {
                long imageSetId = iCursor.getLong(iCursor.getColumnIndex(DataBase.CreateProductDessertDB.IMAGE_SET));
                removeImageSet(dbManager, imageSetId);
                toDelete.add(categoryId);
            }
        }
        for (long id : toDelete) {
            dbManager.deleteColumn(DBManager.PRODUCT_DESSERT, id);
        }
    }

    public static void removeImageSet(DBManager dbManager, long imageSetId){
        Cursor iCursor = dbManager.sortColumn(DBManager.PRODUCT_IMAGE, _ID);
        if (iCursor == null) return;
        while (iCursor.moveToNext()) {
            long tempImageSetId = iCursor.getLong(iCursor.getColumnIndex(_ID));
            if (tempImageSetId == imageSetId) {
                String menuIdle = iCursor.getString(iCursor.getColumnIndex(DataBase.CreateProductImageDB.MENU_IDLE));
                String menuSelected = iCursor.getString(iCursor.getColumnIndex(DataBase.CreateProductImageDB.MENU_SELECTED));
                String cartIdle = iCursor.getString(iCursor.getColumnIndex(DataBase.CreateProductImageDB.CART_IDLE));
                String cartSelected = iCursor.getString(iCursor.getColumnIndex(DataBase.CreateProductImageDB.CART_SELECTED));
                String quantity = iCursor.getString(iCursor.getColumnIndex(DataBase.CreateProductImageDB.QUANTITY));
                File tempImage = new File(menuIdle);
                if(tempImage.exists()) tempImage.delete();
                tempImage = new File(menuSelected);
                if(tempImage.exists()) tempImage.delete();
                tempImage = new File(cartIdle);
                if(tempImage.exists()) tempImage.delete();
                tempImage = new File(cartSelected);
                if(tempImage.exists()) tempImage.delete();
                tempImage = new File(quantity);
                if(tempImage.exists()) tempImage.delete();
                dbManager.deleteColumn(DBManager.PRODUCT_IMAGE, tempImageSetId);
                return;
            }
        }
    }

    public static ArrayList<Order> loadOrders(DBManager dbManager, int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay, boolean coupon, boolean freeOfCharge) {
        Utils.logD(TAG, "loadOrders");
        ArrayList<Order> orders = new ArrayList<>();
        String[] selectionArgs = new String[]{String.valueOf(startYear), String.valueOf(startMonth), String.valueOf(startDay), String.valueOf(startMonth), String.valueOf(startYear), String.valueOf(endYear), String.valueOf(endMonth), String.valueOf(endDay), String.valueOf(endMonth), String.valueOf(endYear)};
        Cursor iCursor = dbManager.selectColumns(DBManager.SALES,
                null,
                "((year=? AND ((month=? AND CAST(day AS INT)>=?) OR (CAST(month AS INT)>?))) OR CAST(year AS INT)>?) AND ((year=? AND ((month=? AND CAST(day AS INT)<=?) OR (CAST(month AS INT)<?))) OR CAST(year AS INT)<?)" + (coupon? "OR approval_id='쿠폰'" : "AND approval_id<>'쿠폰'") + (freeOfCharge? "OR approval_id='무결제'" : "AND approval_id<>'무결제'"), selectionArgs,
                null, null,
                "_id");
        if (iCursor == null) return orders;
        while (iCursor.moveToNext()) {
            long tempID = iCursor.getLong(iCursor.getColumnIndex("_id"));
            int tempYear = iCursor.getInt(iCursor.getColumnIndex("year"));
            int tempMonth = iCursor.getInt(iCursor.getColumnIndex("month"));
            int tempDay = iCursor.getInt(iCursor.getColumnIndex("day"));
            String tempTime = iCursor.getString(iCursor.getColumnIndex("time"));
            String tempCategory = iCursor.getString(iCursor.getColumnIndex("category"));
            String tempProduct = iCursor.getString(iCursor.getColumnIndex("product"));
            String tempHotIceOption = iCursor.getString(iCursor.getColumnIndex("hot_ice_option"));
            String tempSizeOption = iCursor.getString(iCursor.getColumnIndex("size_option"));
            String tempBlendOption = iCursor.getString(iCursor.getColumnIndex("blend_option"));
            int tempShotOption = iCursor.getInt(iCursor.getColumnIndex("shot_option"));
            int tempPrice = iCursor.getInt(iCursor.getColumnIndex("price"));
            String tempApprovalId = iCursor.getString(iCursor.getColumnIndex("approval_id"));
            String tempCancelId = iCursor.getString(iCursor.getColumnIndex("cancel_id"));
//            Utils.logD(TAG, "tempID : " + tempID + " year : " + tempYear + " month : " + tempMonth + " day : " + tempDay + " time : " + tempTime + " category : " + tempCategory + " product : " + tempProduct + " hot_ice_option : " + tempHotIceOption + " size_option : " + tempSizeOption + " blend_option : " + tempBlendOption + " shot_option : " + tempShotOption + " price : " + tempPrice + " approval_id : " + tempApprovalId + " cancel_id : " + tempCancelId);
            orders.add(new Order(tempID, tempYear, tempMonth, tempDay, tempTime, tempCategory, tempProduct, tempHotIceOption, tempSizeOption, tempBlendOption, tempShotOption, tempPrice, tempApprovalId, tempCancelId));
        }
        return orders;
    }
}
