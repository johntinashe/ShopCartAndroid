package com.shopcart.shopcart.models;

/**
 * Created by John on 17/1/2018.
 */

public class Review {
    String user;
    String user_id;
    String review;
    float rating;
    long time;
    String prodId;


    public Review(String user, String user_id, String review, float rating, long time , String prodId) {
        this.user = user;
        this.user_id = user_id;
        this.review = review;
        this.rating = rating;
        this.time = time;
        this.prodId = prodId;
    }

    public Review() {
    }

    public String getProdId() {
        return prodId;
    }

    public void setProdId(String prodId) {
        this.prodId = prodId;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
