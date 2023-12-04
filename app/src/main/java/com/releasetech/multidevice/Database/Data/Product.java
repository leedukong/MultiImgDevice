package com.releasetech.multidevice.Database.Data;

public class Product {
    public long id;
    public String name;
    public long category;
    public int index;
    public int available;
    public int price;
    public int number;
    public int total_count;
    public long image_set;

    public Product(ProductBuilder builder){
        this.id = builder.id;
        this.name = builder.name;
        this.category = builder.category;
        this.index = builder.index;
        this.available = builder.available;
        this.price = builder.price;
        this.number = builder.number;
        this.total_count = builder.total_count;
        this.image_set = builder.image_set;
    }

    public static class ProductBuilder{
        private long id;
        private String name;
        private long category;
        private int index;
        private int available;
        private int price;
        private int total_count;
        private int number;
        private long image_set;

        public ProductBuilder(long id, String name, long category, int index){
            this.id = id;
            this.name = name;
            this.category = category;
            this.index = index;
        }

        public ProductBuilder setAvailable(int available) {
            this.available = available;
            return this;
        }

        public ProductBuilder setPrice(int price) {
            this.price = price;
            return this;
        }

        public ProductBuilder setTotalCount(int total_count) {
            this.total_count = total_count;
            return this;
        }

        public ProductBuilder setNumber(int number){
            this.number = number;
            return this;
        }

        public ProductBuilder setImageSet(long image_set) {
            this.image_set = image_set;
            return this;
        }

        public Product build(){
            return new Product(this);
        }
    }
}
