package com.example.jbois.go4lunch.Controllers.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity;
import com.example.jbois.go4lunch.Controllers.Adapters.RestaurantAdapter;
import com.example.jbois.go4lunch.Models.RestaurantListJson;
import com.example.jbois.go4lunch.Utils.ItemClickSupport;
import com.example.jbois.go4lunch.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantListFragment extends Fragment {

    @BindView(R.id.restaurant_list_recycler_view)RecyclerView mRecyclerView;

    private List<RestaurantListJson> mRestaurantListJsonList;
    private RestaurantAdapter adapter;

    public RestaurantListFragment() {}

    public static RestaurantListFragment newInstance() {

        //Create new fragment
        RestaurantListFragment frag = new RestaurantListFragment();

        return(frag);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);
        ButterKnife.bind(this,view);
        this.configureTestList();
        this.configureRecyclerView();
        this.configureOnClickRecyclerView();
        return view;
    }

    private void configureRecyclerView(){
        // 3.2 - Create adapter passing the list of users
        this.adapter = new RestaurantAdapter(this.mRestaurantListJsonList);
        // 3.3 - Attach the adapter to the recyclerview to populate items
        this.mRecyclerView.setAdapter(this.adapter);
        // 3.4 - Set layout manager to position the items
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureTestList(){
        this.mRestaurantListJsonList = new ArrayList<>();
        for(int i=0;i<10;i++){
            mRestaurantListJsonList.add(new RestaurantListJson());
            //mRestaurantListJsonList.get(i).setName("Ligne "+i);
        }
    }

    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(mRecyclerView, R.layout.recyclerview_restaurant_list_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent intent = new Intent(getActivity(),RestaurantProfileActivity.class);
                        startActivity(intent);
                    }
                });
    }
}
