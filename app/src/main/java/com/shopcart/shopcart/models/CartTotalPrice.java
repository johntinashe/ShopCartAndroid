package com.shopcart.shopcart.models;

/**
 * Created by John on 23/1/2018.
 */

public class CartTotalPrice {
    float total_from_cart;

    public CartTotalPrice(float total_from_cart) {
        this.total_from_cart = total_from_cart;
    }

    public float getTotal_from_cart() {
        return total_from_cart;
    }

    public void setTotal_from_cart(float total_from_cart) {
        this.total_from_cart = total_from_cart;
    }

    public CartTotalPrice() {
    }

}
