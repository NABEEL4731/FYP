package com.example.desicuisine.Models;

public class User {
    public String
            id,
            firstName,
            lastName,
            emailID,
            phone,
            photoUrl,
            category,
            rating,
            noOfRating;
    public String latitude,
            longitude;
    public String status;

    public User() {
    }

    public User(String id, String firstName, String lastName, String emailID, String phone, String photoUrl, String category, String rating, String noOfRating, String latitude, String longitude, String status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailID = emailID;
        this.phone = phone;
        this.photoUrl = photoUrl;
        this.category = category;
        this.rating = rating;
        this.noOfRating = noOfRating;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }
}
