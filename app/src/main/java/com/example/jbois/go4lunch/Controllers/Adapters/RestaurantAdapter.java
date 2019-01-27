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
import com.example.jbois.go4lunch.Models.User;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.example.jbois.go4lunch.Views.RestaurantViewHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;

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
        try{
            holder.updateRestaurantInfos(mRestaurantList.get(position));
        }catch (Exception e){
            Log.e(TAG, "onBindViewHolder: "+e.getMessage()+"on :"+mRestaurantList.get(position).getName());
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
