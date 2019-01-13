package com.example.jbois.go4lunch.Views;

import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
        this.mDistance.setText(mDistance.getContext().getResources().getString((R.string.distance_unit),restaurant.getDistance()));
        if(restaurant.getRating()!=null){
            BaseUserActivity.setStars(restaurant.getRating(),mStars);
        }
    }
    //check openingHours value and set hours in recyclerview
    private void setOpeningHours(Restaurant restaurant){
        if (TextUtils.isEmpty(restaurant.getOpeningHours())) {
            this.mClosingTime.setText(mRestaurantImage.getContext().getResources().getString((R.string.unknown_hours)));
            mClosingTime.setTextColor(mRestaurantImage.getContext().getResources().getColor(R.color.black));
        }else{
            if(restaurant.getOpeningHours().equals("Closed")){
                this.mClosingTime.setText(restaurant.getOpeningHours());
                mClosingTime.setTextColor(mRestaurantImage.getContext().getResources().getColor(R.color.deactivated));
            } else{
                if (!restaurant.getClosingSoon()){
                    this.mClosingTime.setText(mRestaurantImage.getContext().getResources().getString((R.string.open_status),restaurant.getOpeningHours()));
                    mClosingTime.setTextColor(mRestaurantImage.getContext().getResources().getColor(R.color.black));
                }else{
                    this.mClosingTime.setText(mRestaurantImage.getContext().getResources().getString((R.string.closing_soon_status)));
                    mClosingTime.setTextColor(mRestaurantImage.getContext().getResources().getColor(R.color.colorPrimaryDark));
                }
            }
        }
    }
    //Use Glide to fetch restaurant's photo and set it into the imageview on top of view
    private void fetchRestaurantPhoto(Restaurant restaurant){
        String placeId = restaurant.getId();
        GeoDataClient mGeoDataClient = Places.getGeoDataClient(this.mRestaurantImage.getContext(), null);
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
                            if(photo.getBitmap()!=null) {
                                Bitmap bitmap = photo.getBitmap();
                                Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmap,128,128,false);
                                mRestaurantImage.setImageBitmap(bitmapResize);
                            }
                        }
                    });
                }else{
                    mRestaurantImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    mRestaurantImage.setImageDrawable(mRestaurantImage.getResources().getDrawable(R.drawable.no_photo_profile));
                }
                photoMetadataBuffer.release();
            }
        });
    }

}
