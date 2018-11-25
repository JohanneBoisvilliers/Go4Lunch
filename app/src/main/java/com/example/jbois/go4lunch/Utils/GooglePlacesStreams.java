package com.example.jbois.go4lunch.Utils;

import android.support.annotation.Nullable;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantDetails;
import com.example.jbois.go4lunch.Models.RestaurantListJson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class GooglePlacesStreams {

    private static final String apiKey = "AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4";
    private static final String rankby = "distance";
    private static final String type = "restaurant";
    public static final int maxwidth = 0;

    //Observable to fetch Restaurants list from google
    private static Observable<RestaurantListJson> streamFetchRestaurants(@Nullable String location, @Nullable String pageToken){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurants(location,rankby,type,pageToken,apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
    //Observable to fetch Restaurants Details
    private static Observable<RestaurantDetails> streamFetchRestaurantDetails(String placeId){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurantDetails(placeId,apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<RestaurantListJson.Photo> streamFetchRestaurantPhoto(String photoreference){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurantPhotos(photoreference,maxwidth,apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<List<Restaurant>> streamFetchRestaurantsWithNeededInfos(String location){
        List<Restaurant> restaurantList = new ArrayList<>();
        GooglePlacesStreams googlePlacesStreams = new GooglePlacesStreams();

        return streamFetchRestaurants(location,null)
                .flatMap((Function<RestaurantListJson, Observable<RestaurantListJson>>) restaurantListJson -> {
                    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                    return Observable.just(restaurantListJson);
                })
                .delay(2, TimeUnit.SECONDS)
                .flatMap((Function<RestaurantListJson, Observable<RestaurantListJson>>) restaurantListJsonNextPage -> {
                    if(restaurantListJsonNextPage.getNextPageToken()!=null||!restaurantListJsonNextPage.getNextPageToken().equals("")){
                        return streamFetchRestaurants(location,restaurantListJsonNextPage.getNextPageToken())
                                .flatMap((Function<RestaurantListJson, Observable<RestaurantListJson>>) restaurantListJson -> {
                                    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
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
                                    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                                    return Observable.just(restaurantList);
                                });
                    }
                    return Observable.just(restaurantList);
                })
                .flatMapIterable(restaurants ->restaurants)
                .flatMap((Function<Restaurant, Observable<RestaurantDetails>>) restaurant -> streamFetchRestaurantDetails(restaurant.getId()))
                .flatMap((Function<RestaurantDetails,Observable<List<Restaurant>>>)restdet ->{
                    googlePlacesStreams.extrudeDetailsInfo(restaurantList,restdet);
                    return Observable.just(restaurantList);
                });

        }

        //-------Private methods---------

        private void extrudePlaceInfo(RestaurantListJson restlist, List<Restaurant> list){
            for(int i=0;i<restlist.getResults().size();i++){
                Restaurant restaurant = new Restaurant();
                restaurant.setId(restlist.getResults().get(i).getPlaceId());
                //if(restlist.getResults().get(i).getPhotos().get(0).getPhotoReference()!=null){
                //    restaurant.setPhotoReference(restlist.getResults().get(i).getPhotos().get(0).getPhotoReference());
                //}
                restaurant.setLat(restlist.getResults().get(i).getGeometry().getLocation().getLat());
                restaurant.setLng(restlist.getResults().get(i).getGeometry().getLocation().getLng());
                list.add(restaurant);
            }
        }

        private void extrudeDetailsInfo(List<Restaurant> restaurantList, RestaurantDetails restaurantDetails){
            for(int i=0;i<restaurantList.size();i++){
                restaurantList.get(i).setName(restaurantDetails.getResult().getName());
                restaurantList.get(i).setAdress(restaurantDetails.getResult().getFormattedAddress());
                restaurantList.get(i).setUrl(restaurantDetails.getResult().getWebsite());
                restaurantList.get(i).setPhoneNumber(restaurantDetails.getResult().getFormattedPhoneNumber());
            }
        }

}
