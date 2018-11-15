package com.example.jbois.go4lunch.Utils;

import android.support.annotation.Nullable;

import com.example.jbois.go4lunch.Models.RestaurantListJson;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GooglePlacesStreams {
    public static final String apiKey = "AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4";
    //Observable to fetch top stories articles
    public static Observable<RestaurantListJson> streamFetchRestaurants(@Nullable String location, String rankby, String type){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurant(location,rankby,type,apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
}
