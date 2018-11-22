package com.example.jbois.go4lunch.Utils;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.jbois.go4lunch.Controllers.Fragments.MapFragment;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantDetails;
import com.example.jbois.go4lunch.Models.RestaurantListJson;
import com.google.android.gms.common.api.Api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Optional;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.OPTIONS;

public class GooglePlacesStreams {

    public static final String apiKey = "AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4";
    public static final String rankby = "distance";
    public static final String type = "restaurant";

    //Observable to fetch Restaurants list from google
    public static Observable<RestaurantListJson> streamFetchRestaurants(@Nullable String location, @Nullable String pageToken){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurants(location,rankby,type,pageToken,apiKey)
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
        List<Restaurant> restaurantList = new ArrayList<>();
        GooglePlacesStreams googlePlacesStreams = new GooglePlacesStreams();

        return streamFetchRestaurants(location,null)
                .flatMap((Function<RestaurantListJson, Observable<RestaurantListJson>>) restaurantListJson -> {
                    googlePlacesStreams.browseResponseList(restaurantListJson,restaurantList);
                    return Observable.just(restaurantListJson);
                })
                .delay(2, TimeUnit.SECONDS)
                .flatMap((Function<RestaurantListJson, Observable<RestaurantListJson>>) restaurantListJsonNextPage -> {
                    if(restaurantListJsonNextPage.getNextPageToken()!=null||!restaurantListJsonNextPage.getNextPageToken().equals("")){
                        return streamFetchRestaurants(location,restaurantListJsonNextPage.getNextPageToken())
                                .flatMap((Function<RestaurantListJson, Observable<RestaurantListJson>>) restaurantListJson -> {
                                    googlePlacesStreams.browseResponseList(restaurantListJson,restaurantList);
                                    return Observable.just(restaurantListJsonNextPage);
                                });
                    }
                    return Observable.just(restaurantListJsonNextPage);
                })
                .delay(2, TimeUnit.SECONDS)
                .flatMap((Function<RestaurantListJson, Observable<List<Restaurant>>>) restaurantListJsonNextPage -> {
                    if(restaurantListJsonNextPage.getNextPageToken()!=null||!restaurantListJsonNextPage.getNextPageToken().equals("")){
                        return streamFetchRestaurants(location,restaurantListJsonNextPage.getNextPageToken())
                                .flatMap((Function<RestaurantListJson, Observable<List<Restaurant>>>) restaurantListJson -> {
                                    googlePlacesStreams.browseResponseList(restaurantListJson,restaurantList);
                                    return Observable.just(restaurantList);
                                });
                    }
                    return Observable.just(restaurantList);
                })
                .flatMapIterable(restaurants ->restaurants)
                .flatMap((Function<Restaurant, Observable<RestaurantDetails>>) restaurant -> streamFetchRestaurantDetails(restaurant.getId()));
        }

        public void browseResponseList (RestaurantListJson restlist,List<Restaurant> list){
            for(int i=0;i<restlist.getResults().size();i++){
                Restaurant restaurant = new Restaurant();
                restaurant.setId(restlist.getResults().get(i).getPlaceId());
                list.add(restaurant);
            }
        }
}
