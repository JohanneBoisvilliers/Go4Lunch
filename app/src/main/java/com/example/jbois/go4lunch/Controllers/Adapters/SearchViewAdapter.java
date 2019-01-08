package com.example.jbois.go4lunch.Controllers.Adapters;

import android.content.Context;
import android.content.RestrictionEntry;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class SearchViewAdapter extends ArrayAdapter {

    private int mContainer;

    public SearchViewAdapter(@NonNull Context context,int resource ,Restaurant[]restaurantList) {
        super(context,resource, restaurantList);

        mContainer = resource;
    }

    //@NonNull
    //@Override
    //public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    //    LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
    //    View listItem = inflater.inflate(mContainer,parent,false);
//
    //    Restaurant currentRestaurant = (Restaurant)getItem(position);
//
    //    TextView restaurantName = (TextView) listItem.findViewById(R.id.search_text);
    //    //TextView restaurantAdress = (TextView) listItem.findViewById(R.id.search_adress);
//
    //    restaurantName.setText(currentRestaurant.getName());
    //    //restaurantAdress.setText(currentRestaurant.getAdress());
//
    //    return listItem;
    //}
}
