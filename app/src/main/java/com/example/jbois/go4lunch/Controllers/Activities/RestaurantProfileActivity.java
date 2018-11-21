package com.example.jbois.go4lunch.Controllers.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;

public class RestaurantProfileActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.activity_restaurant_name)TextView mRestaurantNameInProfile;
    @BindView(R.id.activity_restaurant_adress)TextView mRestaurantAdressInProfile;
    @BindView(R.id.activity_restaurant_website)Button mWebsiteButton;
    @BindView(R.id.activity_restaurant_phoneNumber)Button mPhoneNumberButton;

    private String mWebsiteUrl;
    private String mPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_profile);
        ButterKnife.bind(this);

        getRestaurantFromBundleAndSetProfile();
        mWebsiteButton.setOnClickListener(this);
        mPhoneNumberButton.setOnClickListener(this);
    }
    private void getRestaurantFromBundleAndSetProfile(){
        Bundle data = getIntent().getExtras();
        Restaurant restaurant = (Restaurant) data.getParcelable(RESTAURANT_IN_TAG);
        mRestaurantNameInProfile.setText(restaurant.getName());
        mRestaurantAdressInProfile.setText(restaurant.getAdress());
        mWebsiteUrl = restaurant.getUrl();
        mPhoneNumber = restaurant.getPhoneNumber();
    }

    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
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
