package com.shopcart.shopcart.models;


/**
 * Created by John on 26/1/2018.
 */

public class User {

    private String name;
    private String surname;
    private String address;
    private String phoneNumber;
    private String deviceToken;
    private String membership;
    private String image;
    private String thumb_image;

    public User(String name, String address, String phoneNumber ,String surname ,String deviceToken ,String membership,String image,String thumb_image) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.surname = surname;
        this.deviceToken = deviceToken;
        this.membership = membership;
        this.image = image;
        this.thumb_image = thumb_image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getMembership() {
        return membership;
    }

    public void setMembership(String membership) {
        this.membership = membership;
    }

    public User() {
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
