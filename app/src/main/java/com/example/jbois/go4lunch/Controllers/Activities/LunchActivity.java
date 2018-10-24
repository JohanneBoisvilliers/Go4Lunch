package com.example.jbois.go4lunch.Controllers.Activities;

import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenu;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.jbois.go4lunch.Controllers.Adapters.PageAdapter;
import com.example.jbois.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LunchActivity extends AppCompatActivity {

    @BindView(R.id.activity_main_viewpager)ViewPager mViewPager;
    @BindView(R.id.bottom_navigation_view)BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunch);
        ButterKnife.bind(this);
        this.configureToolbar();
        this.configureViewPager();
        this.configureBottomView();
    }


    private void configureToolbar(){
        // Get the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        // Sets the Toolbar
        setSupportActionBar(toolbar);
    }

    private void configureViewPager(){
        // 1 - Get ViewPager from layout
        ViewPager pager = mViewPager;
        // 2 - Set Adapter PageAdapter and glue it together
        pager.setAdapter(new PageAdapter(getSupportFragmentManager()) {
        });
    }

    private void configureBottomView(){
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                updateMainFragment(item.getItemId());
                return false;
            }
        });
    }
    private Boolean updateMainFragment(Integer integer){
        switch (integer) {
            case R.id.navigation_map:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.navigation_list:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.navigation_workmates:
                mViewPager.setCurrentItem(2);
                break;
        }
        return true;
    }
}
