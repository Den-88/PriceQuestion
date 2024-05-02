package com.den.shak.pq.models;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String phone;
    private String name;
    private Boolean isPerformer;

    // Геттер для поля id
    public String getId() {
        return id;
    }

    // Сеттер для поля id
    public void setId(String id) {
        this.id = id;
    }

    // Геттер для поля phone
    public String getPhone() {
        return phone;
    }

    // Сеттер для поля phone
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Геттер для поля name
    public String getName() {
        return name;
    }

    // Сеттер для поля name
    public void setName(String name) {
        this.name = name;
    }

    // Геттер для поля isPerformer
    public Boolean isPerformer() {
        return isPerformer;
    }

    // Сеттер для поля isPerformer
    public void setPerformer(Boolean isPerformer) {
        this.isPerformer = isPerformer;
    }
}
