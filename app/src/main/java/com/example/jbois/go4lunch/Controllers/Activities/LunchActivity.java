package com.example.jbois.go4lunch.Controllers.Activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jbois.go4lunch.Controllers.Adapters.PageAdapter;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.User;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.GlideApp;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Optional;

import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;

public class LunchActivity extends BaseUserActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.activity_main_viewpager)ViewPager mViewPager;
    @BindView(R.id.bottom_navigation_view)BottomNavigationView mBottomNavigationView;
    @BindView(R.id.activity_main_toolbar)Toolbar mToolbar;
    @BindView(R.id.activity_main_drawer_layout)DrawerLayout mDrawerLayout;
    @BindView(R.id.activity_main_nav_view)NavigationView mNavigationView;

    private String[] mTitleList = new String[3];
    public static final String USERID="user_id";
    private Restaurant mRestaurant;

    public static class refreshRestaurantsList{
        public List<Restaurant> restaurantList;

        public refreshRestaurantsList(List<Restaurant> restlist){
            this.restaurantList = restlist;
        }

    }

    public static class getLocation{
        public Location location;

        public getLocation(Location location){
            this.location = location;
        }
    }

    public static class getUid{
        public String uid;

        public getUid(String uid){
            this.uid = uid;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setNavigationDrawerHeader();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunch);
        ButterKnife.bind(this);
        mTitleList = getResources().getStringArray(R.array.toolbar_title_list);
        this.configureToolbar();
        this.configureViewPager();
        this.configureBottomView();
        this.configureDrawerLayout();
        this.configureNavigationView();
    }

    @Override
    public void onBackPressed() {
        //Handle back click to close menu
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void configureToolbar(){
        // Sets the Toolbar
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(mTitleList[0]);
    }

    private void configureViewPager(){
        // Get ViewPager from layout
        ViewPager pager = mViewPager;
        // Set Adapter PageAdapter and glue it together
        pager.setAdapter(new PageAdapter(getSupportFragmentManager()) {
        });

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mBottomNavigationView.getMenu().getItem(position).setChecked(true);
                mToolbar.setTitle(mTitleList[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void configureBottomView(){
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                updateMainFragment(item.getItemId());
                item.setChecked(true);
                return false;
            }
        });
    }

    //Configure Drawer Layout
    private void configureDrawerLayout(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        setNavigationDrawerHeader();
    }

    //Configure NavigationView
    private void configureNavigationView(){
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private void updateMainFragment(Integer integer){
        switch (integer) {
            case R.id.navigation_map:
                mViewPager.setCurrentItem(0);
                mToolbar.setTitle(mTitleList[0]);
                break;
            case R.id.navigation_list:
                mViewPager.setCurrentItem(1);
                mToolbar.setTitle(mTitleList[0]);
                break;
            case R.id.navigation_workmates:
                mViewPager.setCurrentItem(2);
                mToolbar.setTitle(mTitleList[2]);
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle Navigation Item Click
        int id = item.getItemId();

        switch (id) {
            case R.id.drawer_your_lunch:
                Intent intentRestaurant = new Intent(this,RestaurantProfileActivity.class);
                intentRestaurant.putExtra(RESTAURANT_IN_TAG, mRestaurant);
                startActivity(intentRestaurant);
                break;
            case R.id.drawer_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.drawer_logout:
                this.signOutUserFromFirebase();
                this.returntoMainActivity();
                break;
            default:
                break;
        }
        this.mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
    //SignOut user
    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this);
    }
    //When user logout, return to MainActivity to let user log again
    private void returntoMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void setNavigationDrawerHeader(){
        View header = mNavigationView.getHeaderView(0);
        ImageView userPhoto = header.findViewById(R.id.user_profile_photo);
        TextView userNameContainer = header.findViewById(R.id.user_profile_name);
        TextView userMail =header.findViewById(R.id.user_profile_mail);

                GlideApp.with(this)
                .load(this.getCurrentUser().getPhotoUrl())
                .circleCrop()
                .error(R.drawable.no_image_small_icon)
                .into(userPhoto);

        String userName = TextUtils.isEmpty(this.getCurrentUser().getDisplayName())?
                getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();
        String email = TextUtils.isEmpty(this.getCurrentUser().getEmail())?
                getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();

        this.usernameListener(this.getCurrentUser().getUid(), userNameContainer);
        userNameContainer.setText(userName);
        userMail.setText(email);
    }

    private void usernameListener(String uid,TextView textView){
        DocumentReference docRef = UserHelper.getUsersCollection().document(uid);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                User user = snapshot.toObject(User.class);
                textView.setText(user.getUsername());
            }
        });
    }
    //Callback method to fetch restaurant
    @Subscribe(sticky = true)
    public void ongetRestaurant(RestaurantProfileActivity.getRestaurant event) {
        mRestaurant=event.restaurant;
    }
}

