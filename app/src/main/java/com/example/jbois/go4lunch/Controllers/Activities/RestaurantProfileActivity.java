package com.example.jbois.go4lunch.Controllers.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jbois.go4lunch.Controllers.Fragments.WorkmatesFragment;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;
//import com.example.jbois.go4lunch.Utils.GlideApp;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;
import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.newInstance;

public class RestaurantProfileActivity extends BaseUserActivity implements View.OnClickListener {

    @BindView(R.id.activity_restaurant_name)TextView mRestaurantNameInProfile;
    @BindView(R.id.activity_restaurant_adress)TextView mRestaurantAdressInProfile;
    @BindView(R.id.activity_restaurant_website)Button mWebsiteButton;
    @BindView(R.id.activity_restaurant_phoneNumber)Button mPhoneNumberButton;
    @BindView(R.id.activity_restaurant_like)Button mLikeButton;
    @BindView(R.id.restaurant_photo)ImageView mRestaurantPhoto;
    @BindView(R.id.fab)FloatingActionButton mCheckToChoose;
    @BindView(R.id.workmates_list_recycler_view)RecyclerView mRecyclerView;
    @BindViews({ R.id.stars1, R.id.stars2, R.id.stars3 }) List<ImageView> mStars;

    private String mWebsiteUrl;
    private String mPhoneNumber;
    private String mPhotoReference;
    private String mRestaurantChose;
    private Double mRating;
    private Restaurant mRestaurant;
    private Boolean mIsLiked=false;
    private SharedPreferences mMySharedPreferences;
    private SharedPreferences.Editor mEditor;
    public static final String PREFS_NAME = "MySharedPreferences";
    public static final String RESTAURANT_SAVED = "restaurantInSharedPreferences";
    private FirestoreRecyclerAdapter adapter;

    public static class getRestaurantNameForJoiningUsers{
        public String restaurantName;

        public getRestaurantNameForJoiningUsers(String restName){
            this.restaurantName = restName;
        }
    }
    public static class getRestaurant{
        public Restaurant restaurant;

        public getRestaurant(Restaurant restaurant){
            this.restaurant = restaurant;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_profile);
        ButterKnife.bind(this);

        mMySharedPreferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mEditor = mMySharedPreferences.edit();
        getRestaurantFromBundleAndSetProfile();

        WorkmatesFragment fragment = (WorkmatesFragment)getSupportFragmentManager().findFragmentById(R.id.fooFragment);
        fragment.configureRecyclerView();

        this.checkToSetStateButton(mPhoneNumberButton,mPhoneNumber);
        this.checkToSetStateButton(mWebsiteButton,mWebsiteUrl);
        BaseUserActivity.setStars(mRestaurant.getRating(),mStars);
        this.checkStateOfFAB();
        this.checkIfRestaurantIsLiked();
        //this.fetchRestaurantPhoto();

    }

    //Browse Bundle sent by OnMarkerClicked to set the RestaurantProfileActivity
    private void getRestaurantFromBundleAndSetProfile(){
        Bundle data = getIntent().getExtras();
        mRestaurant = (Restaurant) data.getParcelable(RESTAURANT_IN_TAG);
        mRestaurantNameInProfile.setText(mRestaurant.getName());
        mRestaurantAdressInProfile.setText(mRestaurant.getAdress());
        mWebsiteUrl = mRestaurant.getUrl();
        mPhoneNumber = mRestaurant.getPhoneNumber();
        mPhotoReference = mRestaurant.getPhotoReference();
        mRestaurantChose = mRestaurant.getName();
        mRating = mRestaurant.getRating();
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
    //private void fetchRestaurantPhoto(){
    //    GlideApp.with(this)
    //            .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth="
    //                    +getScreenWidth(this)
    //                    +"&photoreference="+mPhotoReference+"&key=AIzaSyCSxNwL3bdtJNrZuJEyc6L9yH84QjSjkU4")
    //            .centerInside()
    //            .error(R.drawable.no_photo_profile)
    //            .into(mRestaurantPhoto);
    //}
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.activity_restaurant_phoneNumber:
                dialPhoneNumber(mPhoneNumber);
                break;
            case R.id.activity_restaurant_like:
                this.buttonLikelistener();
                break;
            case R.id.activity_restaurant_website:
                openWebPage(mWebsiteUrl);
                break;
            case R.id.fab:
                this.setStateOfFAB();
                EventBus.getDefault().postSticky(new RestaurantProfileActivity.getRestaurant(mRestaurant));
                break;
        }
    }
    //check if the restaurant has a website/or a phone number and set UI(activate or deactivate button)
    private void checkToSetStateButton(Button button,String buttonName){

        if (TextUtils.isEmpty(buttonName)){
            DrawableCompat.setTint(button.getCompoundDrawables()[1], ContextCompat.getColor(this, R.color.deactivated));
            button.setTextColor(getResources().getColor(R.color.deactivated));
            button.setEnabled(false);
        }else{
            DrawableCompat.setTint(button.getCompoundDrawables()[1], ContextCompat.getColor(this, R.color.colorPrimary));
            button.setOnClickListener(this);
        }
    }
    //check if user has made a choice of a restaurant yet, update UI of FAB and update firebase database
    private void setStateOfFAB(){
        Boolean isChecked = mRestaurant.getFABChecked();
        if(!isChecked){
            mCheckToChoose.setColorFilter(getResources().getColor(R.color.floatingButtonValidate));
            mRestaurant.setFABChecked(true);
            mRestaurantChose = mRestaurant.getName();
            this.serializeRestaurantForNotification(mRestaurant);
        }else{
            mCheckToChoose.setColorFilter(getResources().getColor(R.color.colorPrimary));
            mRestaurantChose="";
            mRestaurant.setFABChecked(false);
            this.serializeRestaurantForNotification(null);
        }
        UserHelper.updateRestaurantChose(this.getCurrentUser().getUid(), mRestaurantChose);
    }
    private void checkStateOfFAB(){
        Gson gson = new Gson();
        try{
            String restaurantToString = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(RESTAURANT_SAVED,"");
            Restaurant restaurant = gson.fromJson(restaurantToString,new TypeToken<Restaurant>(){}.getType());
            mRestaurant.setFABChecked(restaurant.getFABChecked());
        }catch (Exception e){
            Log.e("SP_RESTAURANT","no restaurant available!");
        }

    }
    //save the restaurant into sharedpreferences to set the notification click
    private void serializeRestaurantForNotification(@Nullable Restaurant restaurant){
        Gson gson = new Gson();
        String restaurantToList = gson.toJson(restaurant);
        mEditor.putString(RESTAURANT_SAVED,restaurantToList);
        mEditor.apply();
        UserHelper.updateEntireRestaurant(this.getCurrentUser().getUid(), restaurantToList);
    }
    //save that user like this restaurant
    private void buttonLikelistener(){
        mLikeButton.setOnClickListener(this);
        if(mIsLiked){
            mIsLiked=false;
            UserHelper.UnlikeRestaurant(this.getCurrentUser().getUid(),mRestaurant.getId()).addOnFailureListener(this.onFailureListener());
            checkIfRestaurantIsLiked();
        }else{
            UserHelper.createRestaurantLiked(mRestaurant.getId(),this.getCurrentUser().getUid(),mRestaurant).addOnFailureListener(this.onFailureListener());
            checkIfRestaurantIsLiked();
        }

    }
    //check on database if user liked this restaurant
    private void checkIfRestaurantIsLiked(){
        UserHelper.getRestaurantsListLiked(this.getCurrentUser().getUid())
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        if (mRestaurant.getId().equals(document.getId())){
                            mIsLiked=true;
                        }
                    }
                    EventBus.getDefault().postSticky(new BaseUserActivity.getLikedOrNot(mIsLiked));
                } else {
                    Log.w("LIKEBUTTON","can't receive if restaurant is liked");
                }
            }
        });
    }
    //if user liked this restaurant, change like button color
    private void ifIsLikedChangeLikeButtonColor(){
        if(mIsLiked){
            DrawableCompat.setTint(mLikeButton.getCompoundDrawables()[1], ContextCompat.getColor(this, R.color.stars));
            mLikeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            }else{
            DrawableCompat.setTint(mLikeButton.getCompoundDrawables()[1], ContextCompat.getColor(this, R.color.colorPrimary));
            mLikeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }
    //Callback method to fetch restaurant
    @Subscribe(sticky = true)
    public void onGetIfChecked(BaseUserActivity.getLikedOrNot event) {
        mIsLiked=event.isLiked;
        this.ifIsLikedChangeLikeButtonColor();
    }

}
