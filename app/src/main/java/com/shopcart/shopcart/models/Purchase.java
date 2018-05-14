package com.shopcart.shopcart.models;

public class Purchase {
    private String id;
    private Object data;

    public Purchase() {
    }

    public Purchase(String id, Object data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
