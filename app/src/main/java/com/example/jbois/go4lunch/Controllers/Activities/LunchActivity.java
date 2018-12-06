package com.example.jbois.go4lunch.Controllers.Activities;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.jbois.go4lunch.Controllers.Adapters.PageAdapter;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantListJson;
import com.example.jbois.go4lunch.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LunchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.activity_main_viewpager)ViewPager mViewPager;
    @BindView(R.id.bottom_navigation_view)BottomNavigationView mBottomNavigationView;
    @BindView(R.id.activity_main_toolbar)Toolbar mToolbar;
    @BindView(R.id.activity_main_drawer_layout)DrawerLayout mDrawerLayout;
    @BindView(R.id.activity_main_nav_view)NavigationView mNavigationView;

    private String[] mTitleList = new String[3];
    private Context mContext;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunch);
        ButterKnife.bind(this);
        mContext = this;
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
                break;
            case R.id.drawer_settings:
                break;
            case R.id.drawer_logout:
                break;
            default:
                break;
        }
        this.mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    public Context getContext() {
        return mContext;
    }
}
