
package com.example.desicuisine.Models;

public class CardViewModel {
    
    public String title;
	public int imageID;
    public int getImageID() {
        return imageID;
    }

    public String getTitle() {
        return title;
    }

    public CardViewModel(int imageID, String title) {
        this.imageID = imageID;
        this.title = title;
    }
}
