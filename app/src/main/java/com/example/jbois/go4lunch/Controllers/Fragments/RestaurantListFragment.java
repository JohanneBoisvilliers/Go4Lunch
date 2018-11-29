package com.example.jbois.go4lunch.Controllers.Fragments;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.jbois.go4lunch.Controllers.Activities.LunchActivity;
import com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity;
import com.example.jbois.go4lunch.Controllers.Adapters.RestaurantAdapter;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantListJson;
import com.example.jbois.go4lunch.Utils.ItemClickSupport;
import com.example.jbois.go4lunch.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;

public class RestaurantListFragment extends Fragment {

    @BindView(R.id.restaurant_list_recycler_view)RecyclerView mRecyclerView;

    private List<Restaurant> mRestaurantList=new ArrayList<>();
    private RestaurantAdapter adapter;
    private Location mLocation;

    public RestaurantListFragment() {}

    public static RestaurantListFragment newInstance() {

        //Create new fragment
        RestaurantListFragment frag = new RestaurantListFragment();

        return(frag);
    }
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);
        ButterKnife.bind(this,view);
        this.configureRecyclerView();
        this.configureOnClickRecyclerView();
        return view;
    }

    private void configureRecyclerView(){
        //Create adapter passing the list of users
        this.adapter = new RestaurantAdapter(this.mRestaurantList,Glide.with(this),mLocation);
        //Attach the adapter to the recyclerview to populate items
        this.mRecyclerView.setAdapter(this.adapter);
        //Set layout manager to position the items
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(mRecyclerView, R.layout.recyclerview_restaurant_list_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent intent = new Intent(getActivity(),RestaurantProfileActivity.class);
                        intent.putExtra(RESTAURANT_IN_TAG,mRestaurantList.get(position));
                        startActivity(intent);
                    }
                });
    }
    //Callback method to fetch restaurant list
    @Subscribe
    public void onRefreshingRestaurantList(LunchActivity.refreshRestaurantsList event) {
        mRestaurantList=event.restaurantList;
        configureRecyclerView();
    }
    //Callback method to fetch user's position
    @Subscribe
    public void onLocationFetch(LunchActivity.getLocation event) {
        mLocation = event.location;
        configureRecyclerView();
    }
}
