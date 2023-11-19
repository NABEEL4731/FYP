package com.example.desicuisine.Models;

public class WeeklyOffer {
    public String offerId, offerName, offerPrice;
    public String day1D, day1L, day1B, day2D, day2L, day2B, day3D, day3L, day3B, day4D, day4L, day4B,
            day5D, day5L, day5B, day6D, day6L, day6B, day7D, day7L, day7B;


    public String
            cookName,
            kichenEmail,
            kitchenID,
            date;
    public String latitudeKitchen,
            longitudeKitchen;

    public WeeklyOffer() {
    }

    public WeeklyOffer(String offerId, String offerName, String offerPrice, String day1D, String day1L, String day1B, String day2D, String day2L, String day2B, String day3D, String day3L, String day3B, String day4D, String day4L, String day4B, String day5D, String day5L, String day5B, String day6D, String day6L, String day6B, String day7D, String day7L, String day7B, String cookName, String kichenEmail, String kitchenID, String date, String latitudeP, String longitudeKitchen) {
        this.offerId = offerId;
        this.offerName = offerName;
        this.offerPrice = offerPrice;
        this.day1D = day1D;
        this.day1L = day1L;
        this.day1B = day1B;
        this.day2D = day2D;
        this.day2L = day2L;
        this.day2B = day2B;
        this.day3D = day3D;
        this.day3L = day3L;
        this.day3B = day3B;
        this.day4D = day4D;
        this.day4L = day4L;
        this.day4B = day4B;
        this.day5D = day5D;
        this.day5L = day5L;
        this.day5B = day5B;
        this.day6D = day6D;
        this.day6L = day6L;
        this.day6B = day6B;
        this.day7D = day7D;
        this.day7L = day7L;
        this.day7B = day7B;
        this.cookName = cookName;
        this.kichenEmail = kichenEmail;
        this.kitchenID = kitchenID;
        this.date = date;
        this.latitudeKitchen = latitudeP;
        this.longitudeKitchen = longitudeKitchen;
    }
}
