package com.example.jbois.go4lunch.Views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.jbois.go4lunch.Controllers.Activities.BaseUserActivity;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantListJson;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.GlideApp;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class RestaurantViewHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.restaurant_name)TextView mRestaurantName;
    @BindView(R.id.restaurant_location)TextView mRestaurantLocation;
    @BindView(R.id.number_of_workmates)TextView mNumberOfWorkmates;
    @BindView(R.id.restaurant_image_recyclerview)ImageView mRestaurantImage;
    @BindView(R.id.closing_time)TextView mClosingTime;
    @BindView(R.id.how_far_is_it)TextView mDistance;
    @BindViews({R.id.stars1_recyclerView,R.id.stars2_recyclerView,R.id.stars3_recyclerView}) List<ImageView> mStars;

    public RestaurantViewHolder(View itemView){
        super(itemView);
        ButterKnife.bind(this,itemView);
    }
    //Get restaurant infos for setting up the recyclerview
    public void updateRestaurantInfos(Restaurant restaurant){
        this.mRestaurantName.setText(restaurant.getName());
        this.mRestaurantLocation.setText(restaurant.getAdress());
        this.fetchRestaurantPhoto(restaurant);
        this.setOpeningHours(restaurant);
        this.metersOrKilometers(restaurant);
        this.mNumberOfWorkmates.setText(mNumberOfWorkmates.getContext().getResources().getString((R.string.number_of_workmates),restaurant.getNumberOfWorkmates()));
        if(restaurant.getRating()!=null){
            BaseUserActivity.setStars(restaurant.getRating(),mStars);
        }
    }
    //check openingHours value and set hours in recyclerview
    private void setOpeningHours(Restaurant restaurant){
        if (TextUtils.isEmpty(restaurant.getOpeningHours())) {
            this.UiOpeningHours(mRestaurantImage.getContext().getResources().getString((R.string.unknown_hours)),
                    mRestaurantImage.getContext().getResources().getColor(R.color.black));
        }else{
            if(restaurant.getOpeningHours().equals("Closed")){
                this.UiOpeningHours(restaurant.getOpeningHours(),mRestaurantImage.getContext().getResources().getColor(R.color.deactivated));
            } else{
                if (!restaurant.getClosingSoon()){
                    this.UiOpeningHours(mRestaurantImage.getContext().getResources().getString((R.string.open_status),restaurant.getOpeningHours()),
                            mRestaurantImage.getContext().getResources().getColor(R.color.black));
                }else{
                    this.UiOpeningHours(mRestaurantImage.getContext().getResources().getString((R.string.closing_soon_status)),
                            mRestaurantImage.getContext().getResources().getColor(R.color.colorPrimaryDark));
                }
                if (restaurant.getOpeningHours().equals("Open 24/7")) {
                    this.UiOpeningHours(mRestaurantImage.getContext().getResources().getString((R.string.always_open)),
                            mRestaurantImage.getContext().getResources().getColor(R.color.black));
                }
            }
        }
    }
    //change UI : distance in meters or kilometers
    private void metersOrKilometers(Restaurant restau){
        if (restau.getDistance()>999) {
            this.mDistance.setText(mDistance.getContext().getResources().getString((R.string.distance_unit_in_kilometer),(restau.getDistance()/1000)));
        }else{
            this.mDistance.setText(mDistance.getContext().getResources().getString((R.string.distance_unit_in_meter),restau.getDistance()));
        }
    }
    //set text and text color for opening hours of each restaurant in recyclerview
    private void UiOpeningHours(String openingHours, int color){
        this.mClosingTime.setText(openingHours);
        mClosingTime.setTextColor(color);
    }
    //fetch restaurant's photo
    private void fetchRestaurantPhoto(Restaurant restaurant){

        if (TextUtils.isEmpty(restaurant.getPhotoReference())||restaurant.getPhotoReference().equals("null")) {
            mRestaurantImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mRestaurantImage.setImageDrawable(mRestaurantImage.getContext().getResources().getDrawable(R.drawable.no_image_small_icon));
        }else{
            Bitmap bitmap = StringToBitMap(restaurant.getPhotoReference());
            if (bitmap!=null) {
                Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmap,128,128,false);
                mRestaurantImage.setImageBitmap(bitmapResize);
            }
        }
    }
    //main stream return bitmap in string format and this method convert this string in bitmap
    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.URL_SAFE);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch(Exception e) {
            Log.e("ERROR BITMAP", "StringToBitMap:Restaurantviewholder "+ e.getMessage());
            return null;
        }
    }
}
