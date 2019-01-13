package com.example.jbois.go4lunch.Models;

import android.support.annotation.Nullable;

public class User {

    private String mUid;
    private String mUsername;
    @Nullable
    private String mUrlPicture;
    @Nullable
    private String mRestaurantChose;

    public User() {}

    public User(String uid, String username, String urlPicture) {
        this.mUid = uid;
        this.mUsername = username;
        this.mUrlPicture = urlPicture;
    }

    // --- GETTERS ---
    public String getUid() { return mUid; }
    public String getUsername() { return mUsername; }
    public String getUrlPicture() { return mUrlPicture; }
    public String getRestaurantChose() { return mRestaurantChose; }

    // --- SETTERS ---
    public void setUsername(String username) { this.mUsername = username; }
    public void setUid(String uid) { this.mUid = uid; }
    public void setUrlPicture(String urlPicture) { this.mUrlPicture = urlPicture; }
    public void setRestaurantChose(String restaurantChose) { this.mRestaurantChose = restaurantChose; }
}
