package com.example.jbois.go4lunch.Controllers.Adapters;

import android.content.Context;
import android.content.RestrictionEntry;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;

import java.util.ArrayList;
import java.util.List;

public class SearchViewAdapter extends ArrayAdapter<Restaurant> {

    private Context mContext;
    private List<Restaurant> mRestaurantList =new  ArrayList<>();

    public SearchViewAdapter(@NonNull Context context, ArrayList<Restaurant> restaurantList) {
        super(context,0, restaurantList);
        mContext = context;
        mRestaurantList = restaurantList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.searchview_list_item,parent,false);

        Restaurant currentRestaurant = mRestaurantList.get(position);

        TextView restaurantName = (TextView) listItem.findViewById(R.id.search_text);
        restaurantName.setText(currentRestaurant.getName());

        TextView restaurantAdress = (TextView) listItem.findViewById(R.id.search_adress);
        restaurantAdress.setText(currentRestaurant.getAdress());

        return listItem;
    }
}
