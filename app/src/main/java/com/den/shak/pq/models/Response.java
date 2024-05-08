package com.den.shak.pq.models;

public class Response {
    // Приватные поля класса Response
    private String id;            // Уникальный идентификатор ответа
    private String idOrder;       // Идентификатор заказа, к которому относится ответ
    private String idPerformer;   // Идентификатор исполнителя ответа
    private String text;          // Текст ответа
    private Integer price;        // Цена ответа
    private Boolean isAccepted;   // Флаг, указывающий, принят ли ответ

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(String idOrder) {
        this.idOrder = idOrder;
    }

    public String getIdPerformer() {
        return idPerformer;
    }

    public void setIdPerformer(String idPerformer) {
        this.idPerformer = idPerformer;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Boolean getAccepted() {
        return isAccepted;
    }

    public void setAccepted(Boolean accepted) {
        isAccepted = accepted;
    }
}
