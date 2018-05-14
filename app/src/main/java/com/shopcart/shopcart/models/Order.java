package com.shopcart.shopcart.models;

import android.support.annotation.Nullable;

public class Order {

    private long placed_at;
    @Nullable private long shipped_at;
    @Nullable private  long delivered_at;
    private String user_id;
    private String status;

    public Order() {
    }

    public Order(long placed_at, long shipped_at, long delivered_at, String user_id, String status) {
        this.placed_at = placed_at;
        this.shipped_at = shipped_at;
        this.delivered_at = delivered_at;
        this.user_id = user_id;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getPlaced_at() {
        return placed_at;
    }

    public void setPlaced_at(long placed_at) {
        this.placed_at = placed_at;
    }

    @Nullable
    public long getShipped_at() {
        return shipped_at;
    }

    public void setShipped_at(@Nullable long shipped_at) {
        this.shipped_at = shipped_at;
    }

    @Nullable
    public long getDelivered_at() {
        return delivered_at;
    }

    public void setDelivered_at(@Nullable long delivered_at) {
        this.delivered_at = delivered_at;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
