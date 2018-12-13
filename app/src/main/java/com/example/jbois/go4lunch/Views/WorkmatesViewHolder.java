package com.example.jbois.go4lunch.Views;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.User;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.GlideApp;

import java.net.URI;

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
        if(mRestaurantPhoto != null){
            mRestaurantChose.setText(mRestaurantChose.getContext().getResources().getString((R.string.workmate_joining),workmates.getUsername()));
        }else{
            if(workmates.getRestaurantChose() == null){
                this.glideRequest(workmates.getUrlPicture());
                mRestaurantChose.setText(mRestaurantChose.getContext().getResources().getString((R.string.workmate_no_choice_yet),workmates.getUsername()));
            }else{
                this.glideRequest(workmates.getUrlPicture());
                mRestaurantChose.setText(mRestaurantChose.getContext().getResources().getString((R.string.restaurant_chose_by_workmate),workmates.getUsername(),workmates.getRestaurantChose()));
            }

        }
    }

    private void glideRequest(String url){
        GlideApp.with(this.mRestaurantChose.getContext())
                .load(url)
                .circleCrop()
                .error(R.drawable.no_image_small_icon)
                .into(mWorkmatesPhoto);
    }
}
