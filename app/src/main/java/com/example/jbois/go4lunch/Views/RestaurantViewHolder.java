package com.example.jbois.go4lunch.Views;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.jbois.go4lunch.Controllers.Activities.LunchActivity;
import com.example.jbois.go4lunch.Models.DistanceJson;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.GooglePlacesStreams;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class RestaurantViewHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.restaurant_name)TextView mRestaurantName;
    @BindView(R.id.restaurant_location)TextView mRestaurantLocation;
    @BindView(R.id.restaurant_image_recyclerview)ImageView mRestaurantImage;
    @BindView(R.id.closing_time)TextView mClosingTime;
    @BindView(R.id.how_far_is_it)TextView mDistance;


    public RestaurantViewHolder(View itemView){
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void updateRestaurantName(Restaurant restaurant, RequestManager glide){
        this.mRestaurantName.setText(restaurant.getName());
        this.mRestaurantLocation.setText(restaurant.getAdress());
        this.glideRequest(restaurant,glide);
        if (restaurant.getOpeningHours()!=null) {
            if(restaurant.getOpeningHours().equals("Closed")){
                this.mClosingTime.setText(restaurant.getOpeningHours());
            }else{
                this.mClosingTime.setText(mRestaurantImage.getContext().getResources().getString((R.string.open_status),restaurant.getOpeningHours()));
            }
        }
        this.mDistance.setText(restaurant.getDistance());
    }

    private void glideRequest(Restaurant restaurant, RequestManager glide){
        glide
                .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=128&photoreference="+restaurant.getPhotoReference()+"&key=AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4")
                .apply(new RequestOptions().transforms(new CenterCrop(),new RoundedCorners(10)))
                .into(mRestaurantImage);
    }


}
