package com.example.jbois.go4lunch.Views;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jbois.go4lunch.Models.User;
import com.example.jbois.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmatesViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.restaurant_chose)TextView mRestaurantChose;
    @Nullable @BindView(R.id.restaurant_photo)ImageView mRestaurantPhoto;

    public WorkmatesViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void updateRestaurantDestination(User workmates){
        if(mRestaurantPhoto != null){
            mRestaurantChose.setText(mRestaurantChose.getContext().getResources().getString((R.string.workmate_joining),workmates.getUsername()));
        }else{
            mRestaurantChose.setText(mRestaurantChose.getContext().getResources().getString((R.string.restaurant_chose_by_workmate),workmates.getUsername(),"BLA"));
        }

    }
}
