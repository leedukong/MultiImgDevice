package com.releasetech.multidevice.Database.Data;

public class Category {
    public long id;
    public String name;
    public int available;
    public int index;
    public String image;

    public Category(long id, String name, int available, int index, String image){
        this.id = id;
        this.name = name;
        this.available = available;
        this.index = index;
        this.image = image;
    }
}
