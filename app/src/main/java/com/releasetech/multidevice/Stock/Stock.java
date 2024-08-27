package com.releasetech.multidevice.Stock;


import android.content.Context;

import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Database.Data.DessertItem;
import com.releasetech.multidevice.Database.Data.Product;
import com.releasetech.multidevice.Database.DataLoader;
import com.releasetech.multidevice.Manager.CartManager;
import com.releasetech.multidevice.Manager.PreferenceManager;

import java.util.HashMap;

public class Stock {
    private static final String TAG = "[STOCK]";

    private final Context context;
    private final DBManager dbManager;

    private HashMap<Long, Integer> dessertCurrent = new HashMap<>();
    private HashMap<Long, Integer> dessertInCart = new HashMap<>();

    public Stock(Context context, DBManager dbManager) {
        this.context = context;
        this.dbManager = dbManager;
        long[] dessertIds = DataLoader.loadProductIds(dbManager);
        for (int i = 0; i < dessertIds.length; i++) {
            dessertCurrent.put(dessertIds[i], 0);
            dessertInCart.put(dessertIds[i], 0);
        }
    }

    public void increaseStockCount(int num){
        String currentCount = PreferenceManager.getString(context, "product_"+num+"_current_count");
        int tempCount = Integer.parseInt(currentCount)-1;
        PreferenceManager.setString(context, "product_"+num+"_current_count", String.valueOf(tempCount));
    }

    public void decreaseStockCount(int num){
        String currentCount = PreferenceManager.getString(context, "product_"+num+"_current_count");
        int tempCount = Integer.parseInt(currentCount)+1;
        PreferenceManager.setString(context, "product_"+num+"_current_count", String.valueOf(tempCount));
    }
}
