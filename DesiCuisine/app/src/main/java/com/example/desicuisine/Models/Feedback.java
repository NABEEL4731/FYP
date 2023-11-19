package com.example.desicuisine.Models;
public class Feedback {
    public String
            id,
            userID,
            userName,
            description,
            userType,
            date;

    public Feedback(String id, String userID, String userName, String description, String userType, String date) {
        this.id = id;
        this.userID = userID;
        this.userName = userName;
        this.description = description;
        this.userType = userType;
        this.date = date;
    }
}

