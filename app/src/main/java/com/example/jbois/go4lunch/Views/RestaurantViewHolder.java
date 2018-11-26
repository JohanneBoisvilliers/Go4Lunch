package com.example.jbois.go4lunch.Views;

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
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantViewHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.restaurant_name)TextView mRestaurantName;
    @BindView(R.id.restaurant_location)TextView mRestaurantLocation;
    @BindView(R.id.restaurant_image_recyclerview)ImageView mRestaurantImage;

    public RestaurantViewHolder(View itemView){
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void updateRestaurantName(Restaurant restaurant, RequestManager glide){
        this.mRestaurantName.setText(restaurant.getName());
        this.mRestaurantLocation.setText(restaurant.getAdress());
        glide
                .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=128&photoreference="+restaurant.getPhotoReference()+"&key=AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4")
                .apply(new RequestOptions().transforms(new CenterCrop(),new RoundedCorners(20)))
                .into(mRestaurantImage);
    }
}
