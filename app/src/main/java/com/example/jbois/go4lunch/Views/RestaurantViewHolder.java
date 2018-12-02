package com.example.jbois.go4lunch.Views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.GlideApp;
import butterknife.BindView;
import butterknife.ButterKnife;

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
    //Get restaurant infos for setting up the recyclerview
    public void updateRestaurantInfos(Restaurant restaurant){
        this.mRestaurantName.setText(restaurant.getName());
        this.mRestaurantLocation.setText(restaurant.getAdress());
        this.glideRequest(restaurant);
        this.setOpeningHours(restaurant);
        this.mDistance.setText(mDistance.getContext().getResources().getString((R.string.distance_unit),restaurant.getDistance()));
    }
    //Use glide to fetch restaurant photo and set it into the recyclerview
    private void glideRequest(Restaurant restaurant){
            GlideApp.with(this.mRestaurantImage.getContext())
                    .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=128&photoreference="+restaurant.getPhotoReference()+"&key=AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4")
                    .apply(new RequestOptions().transforms(new CenterCrop(),new RoundedCorners(10)))
                    .error(R.drawable.no_image_small_icon)
                    .into(mRestaurantImage);
    }
    //check openingHours value and set hours in recyclerview
    private void setOpeningHours(Restaurant restaurant){
        if (restaurant.getOpeningHours()!=null) {
            if(restaurant.getOpeningHours().equals("Closed")){
                this.mClosingTime.setText(restaurant.getOpeningHours());
            }else if (restaurant.getOpeningHours().equals("???")){
                this.mClosingTime.setText(mRestaurantImage.getContext().getResources().getString((R.string.unknown_hours)));
            }else{
                this.mClosingTime.setText(mRestaurantImage.getContext().getResources().getString((R.string.open_status),restaurant.getOpeningHours()));
            }
        }
    }
}
