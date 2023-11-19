package com.example.desicuisine.Models;

public class Product {

    public String
            id,
            productName,
            productPrice,
            productQuantity,
            productCategory,
            status,
            cookName,
            kichenEmail,
            kitchenID,
            date,
            photoUrl;
    public String latitudeP,
            longitudeP;


    public Product() {
    }

    public Product(String id, String productName, String productPrice, String productQuantity, String productCategory,
                   String status, String cookName, String kichenEmail, String kitchenID, String date, String photoUrl,
                   String latitudeP, String longitudeP) {
        this.id = id;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productCategory = productCategory;
        this.status = status;
        this.cookName = cookName;
        this.kichenEmail = kichenEmail;
        this.kitchenID = kitchenID;
        this.date = date;
        this.photoUrl = photoUrl;
        this.latitudeP = latitudeP;
        this.longitudeP = longitudeP;
    }
}
