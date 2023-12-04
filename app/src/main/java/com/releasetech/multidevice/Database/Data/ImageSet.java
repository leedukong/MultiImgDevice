package com.releasetech.multidevice.Database.Data;

public class ImageSet {
    public static final int MENU_IDLE = 0;
    public static final int MENU_SELECTED = 1;
    public static final int CART_IDLE = 2;
    public static final int CART_SELECTED = 3;
    public static final int QUANTITY = 4;

    public long id;
    public String menu_idle;
    public String menu_selected;
    public String cart_idle;
    public String cart_selected;
    public String quantity;

    public ImageSet(){
        this.menu_idle = "";
        this.menu_selected = "";
        this.cart_idle = "";
        this.cart_selected = "";
        this.quantity = "";
    }
    public ImageSet(String menu_idle, String menu_selected, String cart_idle, String cart_selected, String quantity){
        this.menu_idle = menu_idle;
        this.menu_selected = menu_selected;
        this.cart_idle = cart_idle;
        this.cart_selected = cart_selected;
        this.quantity = quantity;
    }

    public String getImagePath(int which){
        switch(which){
            case MENU_IDLE:
                return menu_idle;
            case MENU_SELECTED:
                return menu_selected;
            case CART_IDLE:
                return cart_idle;
            case CART_SELECTED:
                return cart_selected;
            case QUANTITY:
                return quantity;
            default:
                return "";
        }
    }
}
