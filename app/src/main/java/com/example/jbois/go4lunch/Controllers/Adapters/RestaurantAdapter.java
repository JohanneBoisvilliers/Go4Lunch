package com.example.jbois.go4lunch.Controllers.Adapters;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jbois.go4lunch.Controllers.Activities.BaseUserActivity;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Views.RestaurantViewHolder;

import java.util.List;

import io.reactivex.disposables.Disposable;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    private List<Restaurant> mRestaurantList;
    private Location mLocation;

    public RestaurantAdapter(List<Restaurant> restaurantList,Location location){
        this.mRestaurantList = restaurantList;
        this.mLocation = location;
    }
    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CREATE VIEW HOLDER AND INFLATING ITS XML LAYOUT
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_restaurant_list_item, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        try{
            mRestaurantList.get(position).setDistance(this.extrudeDistance(mRestaurantList,position));
            holder.updateRestaurantInfos(mRestaurantList.get(position));
        }catch (Exception e){
            Log.e("NORESTAURANT", "onBindViewHolder: ");
        }

    }

    private int extrudeDistance(List<Restaurant> restaurantList,int index){
        int distance;
        Float tempDistance;
        Location location = new Location("targetRestaurant");
        location.setLatitude(restaurantList.get(index).getLat());
        location.setLongitude(restaurantList.get(index).getLng());
        tempDistance = mLocation.distanceTo(location);
        distance = tempDistance.intValue();
        return distance;
    }


    @Override
    public int getItemCount() {
        return this.mRestaurantList.size();
    }

}
