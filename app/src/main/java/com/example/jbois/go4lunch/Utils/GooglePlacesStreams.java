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

    public static Observable<List<RestaurantDetails>> streamFetchRestaurantsWithNeededInfos(String location){
        return streamFetchRestaurants(location)
                .flatMap(new Function<RestaurantListJson,Observable<List<Restaurant>>>() {

                    @Override
                    public Observable<List<Restaurant>> apply(RestaurantListJson restaurantListJson) throws Exception {
                        List<Restaurant> restaurantList = new ArrayList<>();
                        for(int i=0;i<restaurantListJson.getResults().size();i++){
                                Restaurant restaurant = new Restaurant();
                            restaurant.setId(restaurantListJson.getResults().get(i).getPlaceId());
                            restaurantList.add(restaurant);
                        }
                        return Observable.just(restaurantList);
                    }
                })
                .flatMap(new Function<List<Restaurant>, Observable<List<RestaurantDetails>>>() {

                    List<RestaurantDetails> restaurantDetailsList = new ArrayList<>();
                    Boolean onComplete = false;
                    @Override
                    public Observable<List<RestaurantDetails>> apply(List<Restaurant> restaurants) throws Exception {

                        for(int i=0;i<restaurants.size();i++){
                            streamFetchRestaurantDetails(restaurants.get(i).getId())
                                    .map(restDet -> restDet)
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(new Observer<RestaurantDetails>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                        }

                                        @Override
                                        public void onNext(RestaurantDetails restDet) {
                                            restaurantDetailsList.add(restDet);
                                            Log.e("TAILLE_LISTE_BOUCLE",""+restaurantDetailsList.size());
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                        }

                                        @Override
                                        public void onComplete() {
                                            onComplete=true;
                                        }
                                    });
                        }

                        Log.e("TAILLE_LISTE_FINALE",""+restaurantDetailsList.size());
                        return Observable.just(restaurantDetailsList);
                    }
                });
    }

}
