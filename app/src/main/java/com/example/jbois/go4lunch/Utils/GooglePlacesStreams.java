package com.example.jbois.go4lunch.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.example.jbois.go4lunch.Models.DistanceJson;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantDetailsJson;
import com.example.jbois.go4lunch.Models.RestaurantListJson;
import com.example.jbois.go4lunch.R;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
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
    public Context ctx;

    //Observable to fetch Restaurants list from google
    private static Observable<RestaurantListJson> streamFetchRestaurants(@Nullable String location, @Nullable String pageToken){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurants(location,rankby,type,pageToken,apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
    //Observable to fetch Restaurants Details
    private static Observable<RestaurantDetailsJson> streamFetchRestaurantDetails(String placeId){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurantDetails(placeId,apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<DistanceJson> streamComputeRestaurantDistance(String currentLocation, String placeId){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getDistanceRestaurantFromUser(currentLocation,placeId,apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<List<Restaurant>> streamFetchRestaurantsWithNeededInfos(String location){
        List<Restaurant> restaurantList = new ArrayList<>();
        List<RestaurantDetailsJson> restaurantDetailsJsonList = new ArrayList<>();
        GooglePlacesStreams googlePlacesStreams = new GooglePlacesStreams();

        return streamFetchRestaurants(location,null)
                .flatMap((Function<RestaurantListJson, Observable<RestaurantListJson>>) restaurantListJson -> {
                    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                    return Observable.just(restaurantListJson);
                })
                .delay(1, TimeUnit.SECONDS)
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
                .delay(1, TimeUnit.SECONDS)
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
                .flatMap((Function<Restaurant, Observable<List<RestaurantDetailsJson>>>) restaurant -> streamFetchRestaurantDetails(restaurant.getId()).toList().toObservable())
                .flatMap ((Function<List<RestaurantDetailsJson>, Observable<List<Restaurant>>>) finalrestaurantDetailsList -> {
                    googlePlacesStreams.compareAndSetList(restaurantList,finalrestaurantDetailsList);
                    return Observable.just(restaurantList);
                });

        }

        //-------Private methods---------

        private void extrudePlaceInfo(RestaurantListJson restList, List<Restaurant> list){
            for(int i=0;i<restList.getResults().size();i++){
                Restaurant restaurant = new Restaurant();
                restaurant.setId(restList.getResults().get(i).getPlaceId());
                if(restList.getResults().get(i).getPhotos()!=null){
                    restaurant.setPhotoReference(restList.getResults().get(i).getPhotos().get(0).getPhotoReference());
                }
                restaurant.setLat(restList.getResults().get(i).getGeometry().getLocation().getLat());
                restaurant.setLng(restList.getResults().get(i).getGeometry().getLocation().getLng());
                list.add(restaurant);
            }
        }

        private void compareAndSetList(List<Restaurant> restaurantList, List<RestaurantDetailsJson> restaurantDetailsJsonList){
            for (int i=0;i<restaurantList.size();i++){
                for (int j = 0; j < restaurantDetailsJsonList.size(); j++) {
                    if(restaurantDetailsJsonList.get(j).getResult().getPlaceId().equals(restaurantList.get(i).getId())){
                        restaurantList.get(i).setName(restaurantDetailsJsonList.get(j).getResult().getName());
                        restaurantList.get(i).setAdress(restaurantDetailsJsonList.get(j).getResult().getFormattedAddress());
                        restaurantList.get(i).setUrl(restaurantDetailsJsonList.get(j).getResult().getWebsite());
                        restaurantList.get(i).setPhoneNumber(restaurantDetailsJsonList.get(j).getResult().getFormattedPhoneNumber());
                        restaurantList.get(i).setOpeningHours(checkOpeningHours(restaurantDetailsJsonList.get(j)));
                    }
                }
            }
        }

        private String checkOpeningHours(RestaurantDetailsJson restaurantDetailsJson){
            String openingHours="";
            Calendar today = Calendar.getInstance();
            List<RestaurantDetailsJson.Period> periodList=new ArrayList<>();
            if (restaurantDetailsJson.getResult().getOpeningHours() == null) {
                openingHours="???";
            }else{
                periodList = restaurantDetailsJson.getResult().getOpeningHours().getPeriods();
                if(restaurantDetailsJson.getResult().getOpeningHours().getOpenNow()){
                    for (int i = 0; i < periodList.size() ; i++) {
                        if(periodList.get(i).getOpen().getDay()+1 == today.DAY_OF_WEEK){
                            openingHours = convertHours(periodList.get(i).getOpen().getTime());
                        }
                    }
                }else{
                    //openingHours = Resources.getSystem().getString(R.string.closed_status);
                    openingHours ="Closed";
                }
            }

            return openingHours;
        }

        private String convertHours(String stringToConvert){
            String hoursAsString = stringToConvert;

            DateTimeFormatter dtf = DateTimeFormat.forPattern("HHmm");
            DateTime dateTime = dtf.parseDateTime(hoursAsString);
            DateTimeFormatter outputFormat = DateTimeFormat.forPattern("K.mma");
            hoursAsString = outputFormat.print(dateTime);

            return hoursAsString;
        }
}
