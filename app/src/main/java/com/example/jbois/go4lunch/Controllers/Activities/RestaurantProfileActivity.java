package com.example.jbois.go4lunch.Controllers.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.jbois.go4lunch.Controllers.Fragments.WorkmatesFragment;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.GlideApp;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.example.jbois.go4lunch.Views.WorkmatesViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;

public class RestaurantProfileActivity extends BaseUserActivity implements View.OnClickListener {

    @BindView(R.id.activity_restaurant_name)TextView mRestaurantNameInProfile;
    @BindView(R.id.activity_restaurant_adress)TextView mRestaurantAdressInProfile;
    @BindView(R.id.activity_restaurant_website)Button mWebsiteButton;
    @BindView(R.id.activity_restaurant_phoneNumber)Button mPhoneNumberButton;
    @BindView(R.id.restaurant_photo)ImageView mRestaurantPhoto;
    @BindView(R.id.fab)FloatingActionButton mCheckToChoose;

    private String mWebsiteUrl;
    private String mPhoneNumber;
    private String mPhotoReference;
    private String mRestaurantChose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_profile);
        ButterKnife.bind(this);

        getRestaurantFromBundleAndSetProfile();
        this.checkToSetStateButton(mPhoneNumberButton,mPhoneNumber);
        this.checkToSetStateButton(mWebsiteButton,mWebsiteUrl);
        //this.fetchRestaurantPhoto();
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
        mRestaurantChose = restaurant.getId();
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
        GlideApp.with(this)
                .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth="
                        +getScreenWidth(this)
                        +"&photoreference="+mPhotoReference+"&key=AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4")
                .centerInside()
                .error(R.drawable.no_photo_profile)
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
            case R.id.fab:
                UserHelper.updateRestaurantChose(this.getCurrentUser().getUid(), mRestaurantChose);
                break;
        }
    }

    private void checkToSetStateButton(Button button,String buttonName){
        if (buttonName==null || buttonName.equals("")){
            DrawableCompat.setTint(button.getCompoundDrawables()[1], ContextCompat.getColor(this, R.color.deactivated));
            button.setEnabled(false);
        }else{
            button.setOnClickListener(this);
        }
    }
}
