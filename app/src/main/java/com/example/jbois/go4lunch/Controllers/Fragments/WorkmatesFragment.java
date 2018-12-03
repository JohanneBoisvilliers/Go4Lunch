package com.example.jbois.go4lunch.Controllers.Fragments;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity;
import com.example.jbois.go4lunch.Controllers.Adapters.RestaurantAdapter;
import com.example.jbois.go4lunch.Controllers.Adapters.WorkmatesAdapter;
import com.example.jbois.go4lunch.Models.Workmates;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.ItemClickSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkmatesFragment extends Fragment {

    @BindView(R.id.workmates_list_recycler_view)RecyclerView mRecyclerView;

    private List<Workmates> mWorkmatesList =new ArrayList<>();
    private WorkmatesAdapter adapter;

    public WorkmatesFragment() {}

    public static WorkmatesFragment newInstance() {

        //Create new fragment
        WorkmatesFragment frag = new WorkmatesFragment();

        return(frag);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        ButterKnife.bind(this,view);
        configureTestList();
        this.configureRecyclerView();
        this.configureOnClickRecyclerView();
        return view;
    }

    private void configureRecyclerView(){
        //Create adapter passing the list of restaurant
        this.adapter = new WorkmatesAdapter(this.mWorkmatesList);
        //Attach the adapter to the recyclerview to populate items
        this.mRecyclerView.setAdapter(this.adapter);
        //Set layout manager to position the items
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureTestList(){
        this.mWorkmatesList = new ArrayList<>();
        for(int i=0;i<10;i++){
            mWorkmatesList.add(new Workmates());
            mWorkmatesList.get(i).setName("Workmate "+i);
        }
    }

    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(mRecyclerView, R.layout.recyclerview_restaurant_list_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    }
                });
    }

}
