package com.den.shak.pq.models;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Order {
    private String id;
    private int category_id;
    private String title;
    private String description;
    private LatLng location;
    private Integer price;

    public Order() {

    }

    // Геттеры
    public String getId() {
        return id;
    }

    public int getCategoryID() {
        return category_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPrice() {
        return price;
    }
    public LatLng getLocation() {
        Log.d("Location---", String.valueOf(location));
        return location;

    }

    // Сеттеры
    public void setId(String id) {
        this.id = id;
    }

    public void setCategory(int category_id) {
        this.category_id = category_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
    public void setLocation(LatLng location) {
        this.location = location;
        Log.d("Location+++", String.valueOf(location));
    }
}
