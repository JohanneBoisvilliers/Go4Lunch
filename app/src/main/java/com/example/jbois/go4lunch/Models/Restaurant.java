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

    private String mId;
    private String mName;
    private String mAdress;
    private String mUrl;
    private String mPhoneNumber;
    private String mPhotoReference;
    private Double mLat;
    private Double mLng;

    public Restaurant(){}

    public Restaurant(Parcel in){
        this.mId = in.readString();
        this.mName = in.readString();
        this.mAdress = in.readString();
        this.mUrl = in.readString();
        this.mPhoneNumber = in.readString();
        this.mPhotoReference = in.readString();
        this.mLat = in.readDouble();
        this.mLng = in.readDouble();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
        dest.writeString(this.mName);
        dest.writeString(this.mAdress);
        dest.writeString(this.mUrl);
        dest.writeString(this.mPhoneNumber);
        dest.writeString(this.mPhotoReference);
        dest.writeDouble(this.mLat);
        dest.writeDouble(this.mLng);
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getAdress() {
        return mAdress;
    }

    public void setAdress(String adress) {
        mAdress = adress;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getPhotoReference() {
        return mPhotoReference;
    }

    public void setPhotoReference(String photoReference) {
        mPhotoReference = photoReference;
    }

    public Double getLat() {
        return mLat;
    }

    public void setLat(Double lat) {
        mLat = lat;
    }

    public Double getLng() {
        return mLng;
    }

    public void setLng(Double lng) {
        mLng = lng;
    }
}
