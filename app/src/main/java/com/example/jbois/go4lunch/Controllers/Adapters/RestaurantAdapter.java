package com.example.jbois.go4lunch.Controllers.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jbois.go4lunch.Models.RestaurantListJson;
import com.example.jbois.go4lunch.Views.RestaurantViewHolder;
import com.example.jbois.go4lunch.R;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    private List<RestaurantListJson> mRestaurantListJsonList;

    public RestaurantAdapter(List<RestaurantListJson> restaurantListJsonList){
        this.mRestaurantListJsonList = restaurantListJsonList;
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
        holder.updateRestaurantName(mRestaurantListJsonList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.mRestaurantListJsonList.size();
    }
}
