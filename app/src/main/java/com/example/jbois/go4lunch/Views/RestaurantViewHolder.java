package com.example.jbois.go4lunch.Views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantViewHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.restaurant_name)TextView mRestaurantName;
    @BindView(R.id.restaurant_location)TextView mRestaurantLocation;

    public RestaurantViewHolder(View itemView){
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void updateRestaurantName(Restaurant restaurant){
        this.mRestaurantName.setText(restaurant.getName());
        this.mRestaurantLocation.setText(restaurant.getAdress());
    }
}
