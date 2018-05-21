package com.shopcart.shopcart.models;

import android.support.annotation.Nullable;

/**
 * Created by John on 22/1/2018.featured_products
 */

public class Product{
    private String id;
    private float product_price;
    private String product_name;
    private String product_description;
    private float product_rating;
    private String product_category_id;
    private int product_quantity;
    private boolean product_status;
    private String product_image;
    private String product_thumb_image;
    private String product_short_desc;
    @Nullable private String product_ingredients;
    @Nullable private String product_nutritional_facts;


    public Product() {
    }

    public Product(String id, float product_price, String product_name, String product_description, float product_rating, String product_category_id, int product_quantity, boolean product_status, String product_image, String product_thumb_image, String product_short_desc, @Nullable String product_ingredients, @Nullable String product_nutritional_facts) {
        this.id = id;
        this.product_price = product_price;
        this.product_name = product_name;
        this.product_description = product_description;
        this.product_rating = product_rating;
        this.product_category_id = product_category_id;
        this.product_quantity = product_quantity;
        this.product_status = product_status;
        this.product_image = product_image;
        this.product_thumb_image = product_thumb_image;
        this.product_short_desc = product_short_desc;
        this.product_ingredients = product_ingredients;
        this.product_nutritional_facts = product_nutritional_facts;
    }

    @Nullable
    public String getProduct_ingredients() {
        return product_ingredients;
    }

    public void setProduct_ingredients(@Nullable String product_ingredients) {
        this.product_ingredients = product_ingredients;
    }

    @Nullable
    public String getProduct_nutritional_facts() {
        return product_nutritional_facts;
    }

    public void setProduct_nutritional_facts(@Nullable String product_nutritional_facts) {
        this.product_nutritional_facts = product_nutritional_facts;
    }

    public String getProduct_short_desc() {
        return product_short_desc;
    }

    public void setProduct_short_desc(String product_short_desc) {
        this.product_short_desc = product_short_desc;
    }

    public String getProduct_image() {
        return product_image;
    }

    public void setProduct_image(String product_image) {
        this.product_image = product_image;
    }

    public String getProduct_thumb_image() {
        return product_thumb_image;
    }

    public void setProduct_thumb_image(String product_thumb_image) {
        this.product_thumb_image = product_thumb_image;
    }

    public String getProduct_category_id() {
        return product_category_id;
    }

    public void setProduct_category_id(String product_category_id) {
        this.product_category_id = product_category_id;
    }

    public int getProduct_quantity() {
        return product_quantity;
    }

    public void setProduct_quantity(int product_quantity) {
        this.product_quantity = product_quantity;
    }

    public boolean isProduct_status() {
        return product_status;
    }

    public void setProduct_status(boolean product_status) {
        this.product_status = product_status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getProduct_price() {
        return product_price;
    }

    public void setProduct_price(float product_price) {
        this.product_price = product_price;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_description() {
        return product_description;
    }

    public void setProduct_description(String product_description) {
        this.product_description = product_description;
    }

    public float getProduct_rating() {
        return product_rating;
    }

    public void setProduct_rating(float product_rating) {
        this.product_rating = product_rating;
    }
}
