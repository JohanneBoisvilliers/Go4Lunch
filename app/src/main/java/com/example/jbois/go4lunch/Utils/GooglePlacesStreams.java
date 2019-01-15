package com.example.jbois.go4lunch.Utils;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
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

public class GooglePlacesStreams {

    private static final String apiKey = "AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4";
    private static final String rankby = "distance";
    private static final String type = "restaurant";

    //Observable to fetch Restaurants list from google
    private static Observable<RestaurantListJson> streamFetchRestaurants(@Nullable String location, @Nullable String pageToken){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getRestaurants(location,rankby,type,pageToken,apiKey)
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread())
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
                //.map((Function<RestaurantListJson, RestaurantListJson>) restaurantListJson -> {//hide from this...
                //    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                //    return  restaurantListJson;
                //})
                //.delay(1, TimeUnit.SECONDS)
                //.map((Function<RestaurantListJson, RestaurantListJson>) restaurantListJsonNextPage -> {
                //    if(!TextUtils.isEmpty(restaurantListJsonNextPage.getNextPageToken())){
                //        return streamFetchRestaurants(location,restaurantListJsonNextPage.getNextPageToken())
                //                .map((Function<RestaurantListJson, RestaurantListJson>) restaurantListJson -> {
                //                    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                //                    return  restaurantListJson;
                //                }).blockingFirst();
                //    }else{
                //        return restaurantListJsonNextPage;
                //    }
                //})//...to this to light the request and have only 20 restaurants
                .map((Function<RestaurantListJson, List<Restaurant>>) restaurantListJson -> {/*---------------Google Place------------------*/
                    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                    //hide this to to light request
                    //hide this to to light request
                    return restaurantList;//hide this to to light request
                })
                .map((Function<List<Restaurant>, List<Restaurant>>) restaurantListTemp -> {/*---------------Place Details------------------*/
                    for (Restaurant rest:restaurantListTemp) {
                        googlePlacesStreams.compareAndSetList(rest,streamFetchRestaurantDetails(rest.getId()).blockingFirst());
                    }
                    return restaurantList;
                })
                .map(restaurants -> {/*---------------Google photos------------------*/
                    for (Restaurant restaurant : restaurants) {
                        googlePlacesStreams.getPhotoMetadata(restaurant);
                    }
                    return restaurantList;
                });

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

                                int timeToClose = Minutes.minutesBetween(new DateTime(),close).getMinutes();

                                Log.e("GOOGLESTREAMS", "timetoclose :"+timeToClose);
                                int closingSoon = 30;

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
    private void getPhotoMetadata(Restaurant restaurant){
        String placeId = restaurant.getId();

            GeoDataClient mGeoDataClient = Places.getGeoDataClient(ApplicationContext.getContext(), null);
            final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
            photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                    // Get the list of photos.
                    PlacePhotoMetadataResponse photos = task.getResult();
                    // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                    PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                    // Get the first photo in the list.
                    PlacePhotoMetadata photoMetadata = null;
                    if (photoMetadataBuffer.getCount() > 0) {
                        photoMetadata = photoMetadataBuffer.get(0);
                        // Get a full-size bitmap for the photo.
                        Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                        photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                                PlacePhotoResponse photo = task.getResult();
                                Bitmap bitmap = photo.getBitmap();
                                String photoAsString = BitMapToString(bitmap);
                                restaurant.setPhotoReference(photoAsString);
                            }
                        });
                    }else{
                        restaurant.setPhotoReference("");
                    }
                    photoMetadataBuffer.release();
                }
            });
    }
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos= new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
 }
