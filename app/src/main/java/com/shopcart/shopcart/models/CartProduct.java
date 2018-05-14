package com.shopcart.shopcart.models;

/**
 * Created by John on 23/1/2018.
 */

public class CartProduct {
    private int number;
    private float total_price;

    public CartProduct(int number) {
        this.number = number;
    }

    public CartProduct(int number, float total_price) {
        this.number = number;
        this.total_price = total_price;
    }

    public float getTotal_price() {
        return total_price;
    }

    public void setTotal_price(float total_price) {
        this.total_price = total_price;
    }

    public CartProduct() {
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
