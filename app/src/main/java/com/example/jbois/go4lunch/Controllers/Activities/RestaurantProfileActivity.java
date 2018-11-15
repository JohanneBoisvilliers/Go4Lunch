package com.example.jbois.go4lunch.Controllers.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantProfileActivity extends AppCompatActivity {

    @BindView(R.id.activity_restaurant_name)TextView mRestaurantNameInProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_profile);
        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras();
        Restaurant restaurant = (Restaurant) data.getParcelable("TEST");
        mRestaurantNameInProfile.setText(restaurant.getName());

    }
}
