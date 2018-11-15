package com.example.jbois.go4lunch.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Restaurant implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    private String mName;

    public Restaurant(String name){
        this.mName = name;
    }

    public Restaurant(Parcel in){
        this.mName = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
