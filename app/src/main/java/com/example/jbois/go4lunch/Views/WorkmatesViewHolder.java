package com.example.jbois.go4lunch.Views;

import android.graphics.Typeface;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.User;
import com.example.jbois.go4lunch.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmatesViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.restaurant_chose)TextView mRestaurantChose;
    @Nullable @BindView(R.id.restaurant_photo)ImageView mRestaurantPhoto;
    @BindView(R.id.workmate_photo)ImageView mWorkmatesPhoto;

    public WorkmatesViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void updateRestaurantDestination(User workmates){
        Gson gson = new Gson();
        String restaurantToString = workmates.getRestaurantChose();
        Restaurant restaurant= gson.fromJson(restaurantToString,new TypeToken<Restaurant>(){}.getType());
        if(restaurant==null){
            this.glideRequest(workmates.getUrlPicture());
            mRestaurantChose.setText(mRestaurantChose.getContext().getResources().getString((R.string.workmate_no_choice_yet),workmates.getUsername()));
            mRestaurantChose.setTextColor(mRestaurantChose.getContext().getResources().getColor(R.color.deactivated));
            mRestaurantChose.setTypeface(null, Typeface.ITALIC);
        }else{
            this.glideRequest(workmates.getUrlPicture());
            mRestaurantChose.setText(mRestaurantChose.getContext().getResources().getString((R.string.restaurant_chose_by_workmate),workmates.getUsername(),restaurant.getName()));
            mRestaurantChose.setTextColor(mRestaurantChose.getContext().getResources().getColor(R.color.black));
            mRestaurantChose.setTypeface(null, Typeface.NORMAL);
        }
    }

    public void createListOfUserJoining(User user){
        this.glideRequest(user.getUrlPicture());
        mRestaurantChose.setText(mRestaurantChose.getContext().getResources().getString((R.string.workmate_joining),user.getUsername()));
    }

    private void glideRequest(String url){
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        options.error(R.drawable.no_image_small_icon);

        Glide.with(this.mRestaurantChose.getContext())
                .load(url)
                .apply(options)
                .into(mWorkmatesPhoto);
    }
}
