package com.example.journalapp;

import com.google.firebase.Timestamp;

public class Journal {

    private String title;
    private String thoughts;
    private String imageURL;
    private String userID;

    private Timestamp timeAdded;
    private String username;

    // When working with firebase always create an empty constructor.
    public Journal(){
    }

    public Journal(String title, String thoughts, String imageURL, String userID, Timestamp timeAdded, String username) {
        this.title = title;
        this.thoughts = thoughts;
        this.imageURL = imageURL;
        this.userID = userID;
        this.timeAdded = timeAdded;
        this.username = username;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThoughts() {
        return thoughts;
    }

    public void setThoughts(String thoughts) {
        this.thoughts = thoughts;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
