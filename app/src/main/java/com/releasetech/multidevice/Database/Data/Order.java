package com.releasetech.multidevice.Database.Data;

import android.content.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order {
    public long id;
    public int year;
    public int month;
    public int day;
    public String time;
    public String category;
    public String product;
    public String hotIceOption;
    public String sizeOption;
    public String blendOption;
    public int shotOption;
    public int price;
    public String approvalId;
    public String cancelId;


    public Order(long id, int year, int month, int day, String time, String category, String product, String hotIceOption, String sizeOption, String blendOption, int shotOption, int price, String approvalId, String cancelId){
        this.id = id;
        this.year = year;
        this.month = month;
        this.day = day;
        this.time = time;
        this.category = category;
        this.product = product;
        this.hotIceOption = hotIceOption;
        this.sizeOption = sizeOption;
        this.blendOption = blendOption;
        this.shotOption = shotOption;
        this.price = price;
        this.approvalId = approvalId;
        this.cancelId = cancelId;
    }


    public Order(Context context, LocalDateTime now, DessertItem dessertItem, String approvalId) {
        this(context, now, dessertItem, approvalId, "");
    }

    public Order(Context context, LocalDateTime now, DessertItem dessertItem, String approvalId, String cancelId) {
        this(context, now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.format(DateTimeFormatter.ISO_LOCAL_TIME), dessertItem, approvalId, cancelId);
    }

    public Order(Context context, int year, int month, int day, String time, DessertItem dessertItem, String approvalId, String cancelId) {
        this(0, year, month, day, time, dessertItem.categoryName, dessertItem.productName, "", "", "", 0, dessertItem.price, approvalId, cancelId);
    }

}
