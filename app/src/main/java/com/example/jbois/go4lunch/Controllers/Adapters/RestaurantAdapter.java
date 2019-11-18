package com.example.jbois.go4lunch.Controllers.Adapters;

import android.content.Context;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Views.RestaurantViewHolder;

import java.util.ArrayList;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    private List<Restaurant> mRestaurantList=new ArrayList<>();
    private Location mLocation;
    public static final String TAG = "DEBUG_APPLICATION";

    public RestaurantAdapter(List<Restaurant> restaurantList,Location location){
        if (!mRestaurantList.isEmpty()){
            this.mRestaurantList.clear();
        }
        this.mRestaurantList.addAll(restaurantList);
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
        mRestaurantList.get(position).setDistance(this.extrudeDistance(mRestaurantList,position));

        holder.updateRestaurantInfos(mRestaurantList.get(position));
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
