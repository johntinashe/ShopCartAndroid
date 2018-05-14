package com.shopcart.shopcart.models;

import com.stripe.android.model.Token;

/**
 * Created by John on 26/1/2018.
 */

public class Payment {
    private int price;
    private Object token;
    private Object charge;
    private long time;

    public Payment() {
    }

    public Payment(int price, Object token, Object charge, long time) {
        this.price = price;
        this.token = token;
        this.charge = charge;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Object getToken() {
        return token;
    }

    public void setToken(Object token) {
        this.token = token;
    }

    public Object getCharge() {
        return charge;
    }

    public void setCharge(Object charge) {
        this.charge = charge;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int amount) {
        this.price = amount;
    }


}
