package com.example.jbois.go4lunch.Utils;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.jbois.go4lunch.Controllers.Activities.LunchActivity;
import com.example.jbois.go4lunch.Models.DistanceJson;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantDetailsJson;
import com.example.jbois.go4lunch.Models.RestaurantListJson;
import com.example.jbois.go4lunch.R;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class GooglePlacesStreams extends Application {

    private static final String apiKey = "AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4";
    private static final String rankby = "distance";
    private static final String type = "restaurant";

    //Observable to fetch Restaurants list from google
    private static Observable<RestaurantListJson> streamFetchRestaurants(@Nullable String location, @Nullable String pageToken){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurants(location,rankby,type,pageToken,apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(20, TimeUnit.SECONDS);
    }
    //Observable to fetch Restaurants Details
    private static Observable<RestaurantDetailsJson> streamFetchRestaurantDetails(String placeId){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurantDetails(placeId,apiKey)
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread())
                .timeout(20, TimeUnit.SECONDS);
    }


    public static Observable<List<Restaurant>> streamFetchRestaurantsWithNeededInfos(String location){
        List<Restaurant> restaurantList = new ArrayList<>();
        GooglePlacesStreams googlePlacesStreams = new GooglePlacesStreams();

        return streamFetchRestaurants(location,null)
                //.flatMap((Function<RestaurantListJson, Observable<RestaurantListJson>>) restaurantListJson -> {//hide from this...
                //    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                //    return Observable.fromCallable(() -> restaurantListJson).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                //})
                //.delay(1, TimeUnit.SECONDS)
                //.flatMap((Function<RestaurantListJson, Observable<RestaurantListJson>>) restaurantListJsonNextPage -> {
                //    if(restaurantListJsonNextPage.getNextPageToken()!=null||!restaurantListJsonNextPage.getNextPageToken().equals("")){
                //        return streamFetchRestaurants(location,restaurantListJsonNextPage.getNextPageToken())
                //                .flatMap((Function<RestaurantListJson, Observable<RestaurantListJson>>) restaurantListJson -> {
                //                    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                //                    return Observable.fromCallable(() -> restaurantListJsonNextPage).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                //                });
                //    }
                //    return Observable.fromCallable(() -> restaurantListJsonNextPage).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                //})
                //.delay(1, TimeUnit.SECONDS)
                //.flatMap((Function<RestaurantListJson, Observable<List<Restaurant>>>) restaurantListJsonNextPage -> {
                //if(restaurantListJsonNextPage.getNextPageToken()!=null||!restaurantListJsonNextPage.getNextPageToken().equals("")){
                // return streamFetchRestaurants(location,restaurantListJsonNextPage.getNextPageToken())//...to this to light the request and have only 20 restaurants
                .map((Function<RestaurantListJson, List<Restaurant>>) restaurantListJson -> {
                    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                    Log.e("STREAMS", "size in stream :"+restaurantList.size());
                    //return Observable.fromCallable(() -> restaurantList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                    //});//hide this to to light request
                    //}//hide this to to light request
                    return restaurantList;//hide this to to light request
                })
                .map((Function<List<Restaurant>, List<Restaurant>>) restaurantListTemp -> {
                    Log.e("MAP", "size in stream :"+restaurantList.size());
                    for (Restaurant rest:restaurantListTemp) {
                        googlePlacesStreams.compareAndSetList(rest,streamFetchRestaurantDetails(rest.getId()).blockingFirst());
                    }
                    return restaurantList;
                });
                //.map(restaurants -> {
                //    for (Restaurant restaurant : restaurants) {
                //        Log.e("PHOTO", "Boucle photo");
                //        googlePlacesStreams.getPhotoMetadata(restaurant);
                //    }
                //    return restaurantList;
                //});

    }

    /*
    -------Private methods---------
     */
    //extrude place api info for restaurants
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
    //extrude place Details api infos for restaurants
    private void compareAndSetList(Restaurant restaurantList,RestaurantDetailsJson restaurantDetailsJsonList){

                if(restaurantDetailsJsonList.getResult().getPlaceId().equals(restaurantList.getId())){
                    restaurantList.setName(restaurantDetailsJsonList.getResult().getName());
                    restaurantList.setAdress(extrudeAdressFromJson(restaurantDetailsJsonList));
                    restaurantList.setUrl(restaurantDetailsJsonList.getResult().getWebsite());
                    restaurantList.setPhoneNumber(restaurantDetailsJsonList.getResult().getFormattedPhoneNumber());
                    restaurantList.setOpeningHours(checkOpeningHours(restaurantDetailsJsonList,restaurantList));
                    double rating = restaurantDetailsJsonList.getResult().getRating() != null ?
                            restaurantDetailsJsonList.getResult().getRating()
                            : 0.0;
                    restaurantList.setRating(rating);
                }

    }
    //Get openingHours for each restaurant
    private String checkOpeningHours(RestaurantDetailsJson restaurantDetailsJson, Restaurant restaurant){
        String openingHours="";
        List<RestaurantDetailsJson.Period> periodList=new ArrayList<>();
        if (restaurantDetailsJson.getResult().getOpeningHours() != null) {
            periodList = restaurantDetailsJson.getResult().getOpeningHours().getPeriods();
            if(restaurantDetailsJson.getResult().getOpeningHours().getOpenNow()){
                for (int i = 0; i < periodList.size() ; i++) {
                    if (periodList.get(i).getOpen().getDay() + 1 == Calendar.DAY_OF_WEEK) {
                        if(periodList.get(i).getClose()==null){
                            openingHours ="Open 24/7";
                        }else{
                            DateTime open= convertHoursInDateTime(periodList.get(i).getOpen().getTime());
                            if(open.isBeforeNow()){
                                openingHours = convertHoursInString(convertHoursInDateTime(periodList.get(i).getClose().getTime()));
                                DateTime close = convertHoursInDateTime(periodList.get(i).getClose().getTime());
                                DateTime now = new DateTime();
                                Duration duration = new Duration(now, close);
                                //int timeToClose = Minutes.minutesBetween(new DateTime(),close).getMinutes();
                                long timeToClose = duration.getStandardMinutes();
                                //long diffInMillis =  new DateTime().getMillis() - close.getMillis();
                                Log.e("GOOGLESTREAMS", "timetoclose :"+timeToClose);
                                int closingSoon = 120;

                                if (timeToClose<closingSoon){
                                    restaurant.setClosingSoon(true);
                                }
                            }
                        }
                    }
                }
            }else {
                //openingHours = Resources.getSystem().getString(R.string.closed_status);
                openingHours = "Closed";
            }
        }
        return openingHours;
    }
    //convert hours from api into AM PM format
    private DateTime convertHoursInDateTime(String stringToConvert){
        DateTimeFormatter dtf = DateTimeFormat.forPattern("HHmm");
        return dtf.parseDateTime(stringToConvert);
    }
    private String convertHoursInString(DateTime dateTime){
        DateTimeFormatter outputFormat = DateTimeFormat.forPattern("K.mma");
        return outputFormat.print(dateTime);
    }

    //extrude from Json a compact adress ( street number + road )
    private static String extrudeAdressFromJson(RestaurantDetailsJson restaurantDetailsJson){
        List<RestaurantDetailsJson.AddressComponent> addressComponentList = restaurantDetailsJson.getResult().getAddressComponents();
        String road = "";
        String streetNumber = "";
        for (RestaurantDetailsJson.AddressComponent adress : addressComponentList) {
            if (adress.getTypes().get(0).equals("street_number")){
                road = adress.getLongName();
            }else if(adress.getTypes().get(0).equals("route")){
                streetNumber = adress.getLongName();
            }
        }
        return road+" "+streetNumber;
    }

 }
