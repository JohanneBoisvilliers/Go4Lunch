package com.example.jbois.go4lunch.Utils;

import android.support.annotation.Nullable;

import com.example.jbois.go4lunch.Models.DistanceJson;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantDetailsJson;
import com.example.jbois.go4lunch.Models.RestaurantListJson;

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
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(20, TimeUnit.SECONDS);
    }

    public static Observable<DistanceJson> streamComputeRestaurantDistance(String currentLocation, String placeId){
        GooglePlaceServices googlePlaceServices = GooglePlaceServices.retrofit.create(GooglePlaceServices.class);
        return googlePlaceServices.getDistanceRestaurantFromUser(currentLocation,placeId,apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(20, TimeUnit.SECONDS);

    }

    public static Observable<List<Restaurant>> streamFetchRestaurantsWithNeededInfos(String location){
        List<Restaurant> restaurantList = new ArrayList<>();
        GooglePlacesStreams googlePlacesStreams = new GooglePlacesStreams();

        return streamFetchRestaurants(location,null)
                //.flatMap((Function<RestaurantListJson, Observable<RestaurantListJson>>) restaurantListJson -> {
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
                       // return streamFetchRestaurants(location,restaurantListJsonNextPage.getNextPageToken())
                                .flatMap((Function<RestaurantListJson, Observable<List<Restaurant>>>) restaurantListJson -> {
                                    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                                    return Observable.fromCallable(() -> restaurantList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                                //});
                    //}
                    //return Observable.fromCallable(() -> restaurantList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                })
                .flatMapIterable(restaurants ->restaurants)
                .flatMap((Function<Restaurant, Observable<List<RestaurantDetailsJson>>>) restaurant -> streamFetchRestaurantDetails(restaurant.getId()).toList().toObservable())
                .flatMap ((Function<List<RestaurantDetailsJson>, Observable<List<Restaurant>>>) finalrestaurantDetailsList -> {
                    googlePlacesStreams.compareAndSetList(restaurantList,finalrestaurantDetailsList);
                    return Observable.fromCallable(() -> restaurantList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                });
                //.flatMapIterable(restaurants ->restaurants)
                //.flatMap((Function<Restaurant, Observable<List<Restaurant>>>) restaurant -> streamComputeRestaurantDistance(location,"place_id:"+restaurant.getId())
                //        .flatMap((Function<DistanceJson, Observable<Restaurant>>) distanceJson -> {
                //            restaurant.setDistance(distanceJson.getRows().get(0).getElements().get(0).getDistance().getValue());
                //            return Observable.fromCallable(() -> restaurant);
                //        })
                //        .toList()
                //        .toObservable());

        }

        //-------Private methods---------
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
        private void compareAndSetList(List<Restaurant> restaurantList, List<RestaurantDetailsJson> restaurantDetailsJsonList){
            for (int i=0;i<restaurantList.size();i++){
                for (int j = 0; j < restaurantDetailsJsonList.size(); j++) {
                    if(restaurantDetailsJsonList.get(j).getResult().getPlaceId().equals(restaurantList.get(i).getId())){
                        restaurantList.get(i).setName(restaurantDetailsJsonList.get(j).getResult().getName());
                        restaurantList.get(i).setAdress(extrudeAdressFromJson(restaurantDetailsJsonList.get(j)));
                        restaurantList.get(i).setUrl(restaurantDetailsJsonList.get(j).getResult().getWebsite());
                        restaurantList.get(i).setPhoneNumber(restaurantDetailsJsonList.get(j).getResult().getFormattedPhoneNumber());
                        restaurantList.get(i).setOpeningHours(checkOpeningHours(restaurantDetailsJsonList.get(j)));
                        restaurantList.get(i).setRating(restaurantDetailsJsonList.get(j).getResult().getRating());
                    }
                }
            }
        }
        //Get openingHours for each restaurant 
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
        //convert hours from api into AM PM format
        private String convertHours(String stringToConvert){
            String hoursAsString = stringToConvert;

            DateTimeFormatter dtf = DateTimeFormat.forPattern("HHmm");
            DateTime dateTime = dtf.parseDateTime(hoursAsString);
            DateTimeFormatter outputFormat = DateTimeFormat.forPattern("K.mma");
            hoursAsString = outputFormat.print(dateTime);

            return hoursAsString;
        }
        //extrude from Json a compact adress ( street number + road )
        private String extrudeAdressFromJson(RestaurantDetailsJson restaurantDetailsJson){
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
