package com.example.jbois.go4lunch.Utils;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.jbois.go4lunch.Controllers.Fragments.MapFragment;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantDetails;
import com.example.jbois.go4lunch.Models.RestaurantListJson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class GooglePlacesStreams {

    public static final String apiKey = "AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4";
    public static final String rankby = "distance";
    public static final String type = "restaurant";

    //Observable to fetch Restaurants list from google
    public static Observable<RestaurantListJson> streamFetchRestaurants(@Nullable String location){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurants(location,rankby,type,apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
    //Observable to fetch Restaurants Details
    public static Observable<RestaurantDetails> streamFetchRestaurantDetails(String placeId){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurantDetails(placeId,apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<RestaurantDetails> streamFetchRestaurantsWithNeededInfos(String location){
        return streamFetchRestaurants(location)
                .flatMap((Function<RestaurantListJson, Observable<List<Restaurant>>>) restaurantListJson -> {
                    List<Restaurant> restaurantList = new ArrayList<>();
                    for(int i=0;i<restaurantListJson.getResults().size();i++){
                            Restaurant restaurant = new Restaurant();
                        restaurant.setId(restaurantListJson.getResults().get(i).getPlaceId());
                        restaurantList.add(restaurant);
                    }
                    return Observable.just(restaurantList);
                })
                .flatMapIterable(restaurants ->restaurants)
                .flatMap((Function<Restaurant, Observable<RestaurantDetails>>) restaurant -> streamFetchRestaurantDetails(restaurant.getId()));
        }
}
