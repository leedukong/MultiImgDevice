package com.releasetech.multidevice.Database.Data;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class DessertItem extends CartItem implements Serializable, Cloneable {

    public int number = 0;
    public DessertItem() {
    }

    public DessertItem(String CategoryName, String productName, int price, int number) {
        super(CategoryName, productName, price);
        this.number = number;
    }

    @NonNull
    @Override
    public DessertItem clone() {
        return (DessertItem) super.clone();
    }
}
