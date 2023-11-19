package com.example.desicuisine.Notification.Sending;

public class Data {

    private String userId;
    private int icon;
    private String title;
    private String body;
    private String orderData;
    private String receiver;


    public Data() {
    }

    public Data(String userId, int icon, String title, String body, String orderData, String receiver) {
        this.userId = userId;
        this.icon = icon;
        this.title = title;
        this.body = body;
        this.orderData = orderData;
        this.receiver = receiver;
    }

    public String getOrderData() {
        return orderData;
    }

    public void setOrderData(String orderData) {
        this.orderData = orderData;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}