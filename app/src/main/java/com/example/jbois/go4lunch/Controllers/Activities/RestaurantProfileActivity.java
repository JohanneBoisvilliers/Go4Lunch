package com.example.jbois.go4lunch.Controllers.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantDetails;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.GooglePlacesStreams;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static com.bumptech.glide.request.RequestOptions.fitCenterTransform;
import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;

public class RestaurantProfileActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.activity_restaurant_name)TextView mRestaurantNameInProfile;
    @BindView(R.id.activity_restaurant_adress)TextView mRestaurantAdressInProfile;
    @BindView(R.id.activity_restaurant_website)Button mWebsiteButton;
    @BindView(R.id.activity_restaurant_phoneNumber)Button mPhoneNumberButton;
    @BindView(R.id.restaurant_photo)ImageView mRestaurantPhoto;

    private String mWebsiteUrl;
    private String mPhoneNumber;
    private String mPhotoReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_profile);
        ButterKnife.bind(this);

        getRestaurantFromBundleAndSetProfile();
        mWebsiteButton.setOnClickListener(this);
        mPhoneNumberButton.setOnClickListener(this);
        this.fetchRestaurantPhoto();
    }
    //Browse Bundle sent by OnMarkerClicked to set the RestaurantProfileActivity
    private void getRestaurantFromBundleAndSetProfile(){
        Bundle data = getIntent().getExtras();
        Restaurant restaurant = (Restaurant) data.getParcelable(RESTAURANT_IN_TAG);
        mRestaurantNameInProfile.setText(restaurant.getName());
        mRestaurantAdressInProfile.setText(restaurant.getAdress());
        mWebsiteUrl = restaurant.getUrl();
        mPhoneNumber = restaurant.getPhoneNumber();
        mPhotoReference = restaurant.getPhotoReference();
    }
    //Open the restaurant website in browser
    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    //Open the dial page with the restaurant phone number
    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    //Get the screen width to optimize the google photo request
    private int getScreenWidth(Context context){
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }
    //Use Glide to fetch restaurant's photo and set it into the imageview on top of view
    private void fetchRestaurantPhoto(){
        Glide
                .with(this)
                .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth="
                        +getScreenWidth(this)
                        +"&photoreference="+mPhotoReference+"&key=AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4")
                .into(mRestaurantPhoto);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.activity_restaurant_phoneNumber:
                dialPhoneNumber(mPhoneNumber);
                break;
            case R.id.activity_restaurant_like:
                break;
            case R.id.activity_restaurant_website:
                openWebPage(mWebsiteUrl);
                break;
        }
    }
}
