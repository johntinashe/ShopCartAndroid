package com.shopcart.shopcart.models;

/**
 * Created by John on 25/1/2018.
 */

public class Category {
    private String name;
    private int number_of_product;
    private String catimage;

    public Category() {
    }

    public Category(String name, int number_of_product, String catimage) {
        this.name = name;
        this.number_of_product = number_of_product;
        this.catimage = catimage;
    }

    public String getCatimage() {
        return catimage;
    }

    public void setCatimage(String catimage) {
        this.catimage = catimage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber_of_product() {
        return number_of_product;
    }

    public void setNumber_of_product(int number_of_product) {
        this.number_of_product = number_of_product;
    }
}
