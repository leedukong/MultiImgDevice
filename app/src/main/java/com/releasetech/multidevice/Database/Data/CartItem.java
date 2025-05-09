package com.releasetech.multidevice.Database.Data;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class CartItem implements Serializable, Cloneable {
    public String categoryName = "";
    public String productName = "";

    public int price = 0;
    public int tempPrice = 0;
    public boolean makingDone = false;

    public int count = 1;
    public int number = 0;

    public CartItem() {
    }

    public CartItem(String CategoryName, String productName, int price, int count) {
        this.categoryName = CategoryName;
        this.productName = productName;
        this.price = price;
        this.count = count;
    }

    public CartItem(String CategoryName, String productName, int price, int count, int number) {
        this.categoryName = CategoryName;
        this.productName = productName;
        this.price = price;
        this.count = count;
        this.number = number;
    }

    @SuppressLint("DefaultLocale")
    public String getPriceText() {
        return String.format("%,d₩", price);
    }

    public String getProductName() {
        return productName;
    }

    public int getPrice() {
        return price;
    }


    @NonNull
    @Override
    public CartItem clone() {
        try {
            CartItem clone = (CartItem) super.clone();
            clone.categoryName = categoryName;
            clone.productName = productName;
            clone.price = price;
            clone.tempPrice = tempPrice;
            clone.makingDone = makingDone;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
