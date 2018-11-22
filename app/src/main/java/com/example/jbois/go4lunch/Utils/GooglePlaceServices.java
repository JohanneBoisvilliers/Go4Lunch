package com.example.jbois.go4lunch.Utils;

import com.example.jbois.go4lunch.Models.RestaurantDetails;
import com.example.jbois.go4lunch.Models.RestaurantListJson;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlaceServices {
    // Set the urls to request
    @GET("maps/api/place/nearbysearch/json")
    Observable<RestaurantListJson> getRestaurants(
            @Query("location") String location,
            @Query("rankby") String rankby,
            @Query("type") String type,
            @Query("pagetoken") String pageToken,
            @Query("key") String apiKey);

    @GET("maps/api/place/details/json")
    Observable<RestaurantDetails > getRestaurantDetails(
            @Query("placeid") String placeId,
            @Query("key") String apiKey);


    // Set a listener to know all about requests
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build();

    //New instance of retrofit to set the endpoint,the Gson converter and the RxJava adapter
    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build();
}
