package com.example.jbois.go4lunch.Controllers.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.jbois.go4lunch.Controllers.Fragments.MapFragment;
import com.example.jbois.go4lunch.Controllers.Fragments.RestaurantListFragment;
import com.example.jbois.go4lunch.Controllers.Fragments.WorkmatesFragment;

public class PageAdapter extends FragmentPagerAdapter {

    public PageAdapter(FragmentManager mgr) {
        super(mgr);
    }

    @Override
    public int getCount() {
        return(3);
    }

    @Override
    public Fragment getItem(int position) {
        //Page to return
        switch(position){
            case 0: return MapFragment.newInstance();
            case 1: return RestaurantListFragment.newInstance();
            case 2: return WorkmatesFragment.newInstance();
            default: return MapFragment.newInstance();
        }
    }
}
