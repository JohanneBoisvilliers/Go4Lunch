package com.example.jbois.go4lunch.Controllers.Fragments;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jbois.go4lunch.Controllers.Activities.LunchActivity;
import com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity;
import com.example.jbois.go4lunch.Controllers.Adapters.RestaurantAdapter;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.ItemClickSupport;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;

public class RestaurantListFragment extends Fragment {

    @BindView(R.id.restaurant_list_recycler_view)RecyclerView mRecyclerView;

    private List<Restaurant> mRestaurantList=new ArrayList<>();
    private RestaurantAdapter adapter;
    private Location mLocation;
    private ListenerRegistration mRestaurantsChoseListener;
    private HashMap<String,Integer> mFinalRestaurantsChosen = new HashMap<>();
    public static final String TAG = "DEBUG_APPLICATION";

    public RestaurantListFragment() {}

    public static RestaurantListFragment newInstance() {

        //Create new fragment
        RestaurantListFragment frag = new RestaurantListFragment();

        return(frag);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        EventBus.getDefault().register(this);
        //this.addRestaurantChosenListener();
        this.RestaurantsChosenListener();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        EventBus.getDefault().unregister(this);
        if (mRestaurantsChoseListener!=null) {
            mRestaurantsChoseListener.remove();
        }
        super.onStop();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);
        ButterKnife.bind(this,view);
        this.getRestaurantsChosen();
       // this.addRestaurantChosenListener();
        this.configureRecyclerView();
        this.configureOnClickRecyclerView();
        return view;
    }

    private void configureRecyclerView(){
        //Create adapter passing the list of restaurant
        this.adapter = new RestaurantAdapter(this.mRestaurantList,mLocation);
        //Attach the adapter to the recyclerview to populate items
        this.mRecyclerView.setAdapter(this.adapter);
        //Set layout manager to position the items
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
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
    private void getRestaurantsChosen(){
        UserHelper.getRestaurantChosen().get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            mFinalRestaurantsChosen.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "récupération restaurants");
                                getNumberOfUsers(document.getId());
                            }
                        } else {
                            Log.w(TAG,"error when receiving users");
                        }
                    }
                });
    }

    private void getNumberOfUsers(String placeId){
        UserHelper.getUsersWhoChose(placeId).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Récupération nombre d'utilisateur dans chaque resto");
                            mFinalRestaurantsChosen.put(placeId,task.getResult().size());
                        } else {
                            Log.w("RESTAURANTADPTR","error when receiving users");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
            }
        });
    }
    //check on database if restaurant is chose by someone
    //private void restaurantsChosenListener(String placeId){
    //    mRestaurantsChoseListener = UserHelper.getUsersWhoChose(placeId)
    //            .addSnapshotListener(new EventListener<QuerySnapshot>() {
    //                @Override
    //                public void onEvent(@Nullable QuerySnapshot value,
    //                                    @Nullable FirebaseFirestoreException e) {
    //                    if (e != null) { Log.w(TAG, "Listen failed.", e); }
//
    //                    if (value!=null){
    //                            mFinalRestaurantsChosen.clear();
    //                            getRestaurantsChosen();
    //                            setNumberOfWorkmates();
//
    //                        }
    //                }
    //            });
    //}
    private void RestaurantsChosenListener(){
        mRestaurantsChoseListener = UserHelper.getRestaurantChosen()
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        Log.d(TAG, "Changement dans base de données");
                        if (e != null) { Log.w(TAG, "Listen failed.", e); }

                        if (value!=null){
                            Log.d(TAG, "entrer dans condition pour rafraichir");
                            mFinalRestaurantsChosen.clear();
                            getRestaurantsChosen();
                            setNumberOfWorkmates();
                            
                        }
                    }
                });
    }
    //compare restaurant list with restaurants chosen by users and set number of users in each restaurant of recycler view
    private void setNumberOfWorkmates(){
        for (int i = 0; i < mRestaurantList.size(); i++) {
            for(Map.Entry<String, Integer> entry : mFinalRestaurantsChosen.entrySet()) {
                if (entry.getKey().equals(mRestaurantList.get(i).getId())) {
                    mRestaurantList.get(i).setNumberOfWorkmates(entry.getValue());
                }
            }
        }
        this.mRecyclerView.setAdapter(new RestaurantAdapter(this.mRestaurantList,mLocation));
    }

    //private void addRestaurantChosenListener(){
    //    for(Map.Entry<String, Integer> entry : mFinalRestaurantsChosen.entrySet()) {
    //        restaurantsChosenListener(entry.getKey());
    //    }
    //}
    /*
        --------------------------- Callbacks Methods -----------------------------------------
    */
    //Callback method to fetch restaurant list
    @Subscribe
    public void onRefreshingRestaurantList(LunchActivity.refreshRestaurantsList event) {
        mRestaurantList.clear();
        mRestaurantList.addAll(event.restaurantList);
        this.setNumberOfWorkmates();
    }
    //Callback method to fetch user's position
    @Subscribe
    public void onLocationFetch(LunchActivity.getLocation event) {
        mLocation = event.location;
        //mRecyclerView.getAdapter().notifyDataSetChanged();
    }

}
