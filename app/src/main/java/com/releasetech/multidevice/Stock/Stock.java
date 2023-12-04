package com.releasetech.multidevice.Stock;


import android.content.Context;

import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Database.Data.DessertItem;
import com.releasetech.multidevice.Database.Data.Product;
import com.releasetech.multidevice.Database.DessertDataLoader;
import com.releasetech.multidevice.Manager.CartManager;

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
        long[] dessertIds = DessertDataLoader.loadProductIds(dbManager);
        for (int i = 0; i < dessertIds.length; i++) {
            dessertCurrent.put(dessertIds[i], 0);
            dessertInCart.put(dessertIds[i], 0);
        }
    }

    public void loadStock() {
        for (long id : dessertCurrent.keySet()) {
            dessertCurrent.put(id, DessertDataLoader.loadCurrentStockById(dbManager, id));
            dessertInCart.put(id, 0);
        }
    }

    public void applyCart(CartManager cartManager) {
        for (long id : dessertInCart.keySet()) {
            dessertInCart.put(id, 0);
        }
        for (int i = 0; i < cartManager.getCount(); i++) {
            DessertItem item = (DessertItem) cartManager.getItem(i);
            long id = DessertDataLoader.loadProductByNumber(dbManager, item.number).id;
            dessertInCart.put(id, dessertInCart.get(id) + 1);
        }
    }

    public boolean dessertInStock(Product product) {
        boolean result;
        if (dessertCurrent.containsKey(product.id) && dessertInCart.containsKey(product.id))
            result = dessertCurrent.get(product.id) - dessertInCart.get(product.id) > 0;
        else
            result = false;
        return result;
    }
}
