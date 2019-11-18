package com.example.jbois.go4lunch.Utils;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import android.util.Base64;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantDetailsJson;
import com.example.jbois.go4lunch.Models.RestaurantListJson;
import com.example.jbois.go4lunch.R;
import com.google.android.libraries.places.compat.GeoDataClient;
import com.google.android.libraries.places.compat.PlacePhotoMetadata;
import com.google.android.libraries.places.compat.PlacePhotoMetadataBuffer;
import com.google.android.libraries.places.compat.PlacePhotoMetadataResponse;
import com.google.android.libraries.places.compat.PlacePhotoResponse;
import com.google.android.libraries.places.compat.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class GooglePlacesStreams {

    private static final String apiKey = /*"AIzaSyDZvqeraNLOceysL3s7LNUInkaaZjq5bSE";*/ApplicationContext.getContext().getString(R.string.APIKEY);
    private static final String rankby = "distance";
    private static final String type = "restaurant";
    public static final String TAG = "DEBUG_APPLICATION";

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
        List<RestaurantDetailsJson> restaurantDetailsJsonList = new ArrayList<>();
        GooglePlacesStreams googlePlacesStreams = new GooglePlacesStreams();

        return streamFetchRestaurants(location,null)
                .map((Function<RestaurantListJson, List<Restaurant>>) restaurantListJson -> {/*---------------Google Place------------------*/
                    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                    return restaurantList;
                })
                .map((Function<List<Restaurant>, List<Restaurant>>) restaurantListTemp -> {/*---------------Place Details------------------*/
                    for (Restaurant rest:restaurantListTemp) {
                        restaurantDetailsJsonList.addAll(streamFetchRestaurantDetails(rest.getId()).toList().blockingGet());
                    }
                    return restaurantList;
                })
                .delay(1, TimeUnit.SECONDS)
                .map(restaurants -> {/*---------------Google photos------------------*/
                    googlePlacesStreams.compareAndSetList(restaurants,restaurantDetailsJsonList);

                    for (Restaurant restaurant : restaurants) {
                        googlePlacesStreams.getPhotoMetadata(restaurant);
                    }
                    return restaurantList;
                })
                .delay(1, TimeUnit.SECONDS)
                .doOnComplete(() -> Observable.just(restaurantList))
                ;

    }
    public static Observable<List<Restaurant>> fakeStream() throws IOException {
        List<Restaurant> restaurantList = new ArrayList<>();
        GooglePlacesStreams googlePlacesStreams = new GooglePlacesStreams();
        List<RestaurantDetailsJson> restaurantDetailsJsonList = new ArrayList<>();

        Gson gson = new Gson();
        RestaurantListJson restaurantListJson = gson.fromJson(googlePlacesStreams.serializeJson(R.raw.restaurantlistjson),new TypeToken<RestaurantListJson>(){}.getType());
        restaurantDetailsJsonList.add(gson.fromJson(googlePlacesStreams.serializeJson(R.raw.tesoroditalia),new TypeToken<RestaurantDetailsJson>(){}.getType()));
        restaurantDetailsJsonList.add(gson.fromJson(googlePlacesStreams.serializeJson(R.raw.larosedetunis),new TypeToken<RestaurantDetailsJson>(){}.getType()));


        return Observable.just(restaurantListJson)
                .map((Function<RestaurantListJson, List<Restaurant>>) restaurantListJson1 -> {/*---------------Google Place------------------*/
                    googlePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
                    return restaurantList;//hide this to to light request
                })
                .map((Function<List<Restaurant>, List<Restaurant>>) restaurantListTemp -> {/*---------------Place Details------------------*/
                    googlePlacesStreams.fakecompareAndSetList(restaurantListTemp,restaurantDetailsJsonList);
                    return restaurantList;
                })
                .map(restaurants -> {/*---------------Google photos------------------*/
                    for (Restaurant restaurant : restaurants) {
                        googlePlacesStreams.getPhotoMetadata(restaurant);
                    }
                    return restaurantList;
                })
                .delay(3, TimeUnit.SECONDS)
                .doOnComplete(() -> Observable.just(restaurantList))
                ;

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
    private void compareAndSetList(List<Restaurant> restaurantList, List<RestaurantDetailsJson> restaurantDetailsJsonList){
        for (int i=0;i<restaurantList.size();i++){
            for (int j = 0; j < restaurantDetailsJsonList.size(); j++) {
                if(restaurantDetailsJsonList.get(j).getResult().getPlaceId().equals(restaurantList.get(i).getId())){
                    restaurantList.get(i).setName(restaurantDetailsJsonList.get(j).getResult().getName());
                    restaurantList.get(i).setAdress(extrudeAdressFromJson(restaurantDetailsJsonList.get(j)));
                    restaurantList.get(i).setUrl(restaurantDetailsJsonList.get(j).getResult().getWebsite());
                    restaurantList.get(i).setPhoneNumber(restaurantDetailsJsonList.get(j).getResult().getFormattedPhoneNumber());
                    restaurantList.get(i).setOpeningHours(checkOpeningHours(restaurantDetailsJsonList.get(j),restaurantList.get(i)));
                    double rating = restaurantDetailsJsonList.get(j).getResult().getRating() != null ?
                            restaurantDetailsJsonList.get(j).getResult().getRating()
                            : 0.0;
                    restaurantList.get(i).setRating(rating);
                }
            }
        }
    }
    //extrude place Details api infos for restaurants
    private void fakecompareAndSetList(List<Restaurant> restaurantList, List<RestaurantDetailsJson> restaurantDetailsJsonList){
        for (int i=0;i<restaurantList.size();i++){
            for (int j = 0; j < restaurantDetailsJsonList.size(); j++) {
                if(restaurantDetailsJsonList.get(j).getResult().getPlaceId().equals(restaurantList.get(i).getId())){
                    restaurantList.get(i).setName(restaurantDetailsJsonList.get(j).getResult().getName());
                    restaurantList.get(i).setAdress(extrudeAdressFromJson(restaurantDetailsJsonList.get(j)));
                    restaurantList.get(i).setUrl(restaurantDetailsJsonList.get(j).getResult().getWebsite());
                    restaurantList.get(i).setPhoneNumber(restaurantDetailsJsonList.get(j).getResult().getFormattedPhoneNumber());
                    restaurantList.get(i).setOpeningHours(checkOpeningHours(restaurantDetailsJsonList.get(j),restaurantList.get(i)));
                    Double rating = restaurantDetailsJsonList.get(j).getResult().getRating() != null ?
                            restaurantDetailsJsonList.get(j).getResult().getRating()
                            : 0.0;
                    restaurantList.get(i).setRating(rating);
                }
            }
        }
    }

    public String serializeJson(@RawRes int resources) throws IOException {
        InputStream is = ApplicationContext.getContext().getResources().openRawResource(resources);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        return writer.toString();
    }
    //Get openingHours for each restaurant
    private String checkOpeningHours(RestaurantDetailsJson restaurantDetailsJson, Restaurant restaurant){
        String openingHours="";
        List<RestaurantDetailsJson.Period> periodList=new ArrayList<>();
        Boolean isGoodSchedule = false;
        if (restaurantDetailsJson.getResult().getOpeningHours() != null) {
            periodList = restaurantDetailsJson.getResult().getOpeningHours().getPeriods();
            if(restaurantDetailsJson.getResult().getOpeningHours().getOpenNow()){
                for (int i = 0; i < periodList.size() ; i++) {
                    if (periodList.get(i).getOpen().getDay()+1 == Calendar.DAY_OF_WEEK) {
                        if(periodList.get(i).getClose()==null){
                            openingHours =ApplicationContext.getContext().getResources().getString(R.string.always_open);
                        }else{
                            DateTime open= new DateTime().withHourOfDay(convertHoursInDateTime(periodList.get(i).getOpen().getTime()).getHourOfDay())
                                    .withMinuteOfHour(convertHoursInDateTime(periodList.get(i).getOpen().getTime()).getMinuteOfHour());
                            if(open.isBeforeNow()){
                                openingHours = convertHoursInString(convertHoursInDateTime(periodList.get(i).getClose().getTime()));
                                DateTime close = new DateTime().withHourOfDay(convertHoursInDateTime(periodList.get(i).getClose().getTime()).getHourOfDay())
                                        .withMinuteOfHour(convertHoursInDateTime(periodList.get(i).getClose().getTime()).getMinuteOfHour());

                                int timeToClose = Minutes.minutesBetween(new DateTime(),close).getMinutes();
                                int closingSoon = 30;

                                if (timeToClose<closingSoon && timeToClose>=0){
                                    restaurant.setClosingSoon(true);
                                }
                                isGoodSchedule = true;
                            }
                            if (!isGoodSchedule) {
                                openingHours = ApplicationContext.getContext().getResources().getString(R.string.closed_status);
                            }
                        }
                    }
                    if(periodList.get(i).getOpen().getDay() == 0 && periodList.get(i).getClose()==null){
                        openingHours = ApplicationContext.getContext().getResources().getString(R.string.always_open);
                    }
                }
            }else {
                openingHours = ApplicationContext.getContext().getResources().getString(R.string.closed_status);;
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
                                if (task.isSuccessful()) {
                                    PlacePhotoResponse photo = task.getResult();
                                    Bitmap bitmap = photo.getBitmap();
                                    String photoAsString = BitMapToString(bitmap);
                                    restaurant.setPhotoReference(photoAsString);

                                }else{
                                    restaurant.setPhotoReference("");
                                }
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
        String temp=Base64.encodeToString(b, Base64.URL_SAFE);
        return temp;
    }
 }
