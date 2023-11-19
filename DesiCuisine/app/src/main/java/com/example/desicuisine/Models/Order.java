package com.example.desicuisine.Models;

public class Order {
    public String
            orderId,
            userID,
            productID,
            productName,
            productPrice,
            productCategory,
            kitchenID,
            kitchenEmail,
            cookName,
            orderStatus,
            orderDate,
            orderDescription;

    public String userLatitude,
            userLongitude;
    public String productLatitude,
            productLongitude;

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Order(String orderId, String userID, String productID, String productName, String productPrice, String productCategory,
                 String kitchenID, String kitchenEmail, String cookName, String orderStatus, String orderDate,
                 String orderDescription, String userLatitude, String userLongitude, String productLatitude, String productLongitude) {
        this.orderId = orderId;
        this.userID = userID;
        this.productID = productID;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productCategory = productCategory;
        this.kitchenID = kitchenID;
        this.kitchenEmail = kitchenEmail;
        this.cookName = cookName;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
        this.orderDescription = orderDescription;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.productLatitude = productLatitude;
        this.productLongitude = productLongitude;
    }

    public Order() {
    }

}
