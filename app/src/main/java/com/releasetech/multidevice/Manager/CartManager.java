package com.releasetech.multidevice.Manager;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.releasetech.multidevice.Database.Data.CartItem;
import com.releasetech.multidevice.EventListener.DataManager;
import com.releasetech.multidevice.Tool.Utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

public class CartManager extends DataManager implements Serializable, Cloneable {
    public static final String KEY_SERIALIZED_CART_MANAGER = "KEY_SERIALIZED_CART_MANAGER";

    CartItem[] items;
    String[] image;
    int size;
    int occupied = 0;

    boolean locked = false;

    public CartManager(int size) {
        items = new CartItem[size];
        image = new String[size];
        this.size = size;
    }

    public CartManager(CartItem[] items, String[] image, int size, int occupied) {
        this.items = items;
        this.image = image;
        this.size = size;
        this.occupied = occupied;
    }

    public boolean available() {
        return occupied < size;
    }

    public void add(CartItem item) {
        if (occupied >= size) {
            return;
        }
        items[occupied] = item;
        occupied++;
        update();
    }

    public void remove(CartItem item) {
        if (locked) {
            return;
        }
        for (int i = 0; items[i] != null; i++) {
            if (item.number == items[i].number) {
                for (int j = i+1; j < size; j++) {
                    CartItem k1 = items[j];
                    String k2 = image[j];
                    items[j-1] = items[j];
                    image[j-1] = image[j];
                    items[j] = k1;
                    image[j] = k2;
                }
                if (occupied == 5) {
                    items[4] = null;
                }
                occupied--;
                return;
            }
        }
//        for (int i = index; i < size - 1; i++) {
//            items[i] = items[i + 1];
//            image[i] = image[i + 1];
//        }
//        items[size - 1] = null;
//        image[size - 1] = null;
//        occupied--;
//        update();
    }

    public void removeSameIndex(CartItem item) {
        if (locked) {
            return;
        }
        for (int i = 0; i < size -1; i++) {
            remove(item);
        }
        if (occupied == 1 && item.number == items[0].number) {
            items[0] = null;
            image[0] = null;
            occupied = 0;
        }
//        for (int i = 0; i < size -1; i++){
//            if (getItem(index) == getItem(i)){
//                remove(getItem(i));
//            }
//        }
    }

    public void clear() {
        if (locked) {
            return;
        }
        items = new CartItem[size];
        image = new String[size];
        occupied = 0;
        update();
    }

    public int getSize(){
        return size;
    }

    public CartItem getItem(int index) {
        if (index >= occupied) {
            return null;
        }
        return items[index];
    }

    public CartItem[] getItems() {
        return Arrays.copyOfRange(items, 0, occupied);
    }

    public String getImage(int index) {
        return image[index];
    }

    @SuppressLint("DefaultLocale")
    public String getPriceText(int index) {
        return items[index].getPriceText();
    }

    public int getCount() {
        return occupied;
    }
    public String getCountText(){
        String returnText = occupied+"개";
        return returnText;
    }
    public int getTotalPrice(){
        int totalPrice = 0;
        for(int i=0; i<occupied; i++){
            totalPrice += items[i].price;
        }
        return totalPrice;
    }

    @SuppressLint("DefaultLocale")
    public String getTotalPriceText() {
        int totalPrice = 0;
        for (int i = 0; i < occupied; i++) {
            Utils.logD("[CART MANAGER]", "제품 " + items[i].productName + " 가격 " + items[i].price);
            totalPrice += items[i].price;
        }
        String returnText = String.format("%,d₩", totalPrice);
        return returnText;
    }

    public CartItem getNextUndoneItem() {
        Optional<CartItem> tempItem = Arrays.stream(items).filter(item -> !item.makingDone).findFirst();
        return tempItem.orElse(null);
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    @NonNull
    @Override
    public CartManager clone() {
        try {
            CartManager clone = (CartManager) super.clone();
            clone.items = items.clone();
            clone.image = image.clone();
            clone.size = size;
            clone.occupied = occupied;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

