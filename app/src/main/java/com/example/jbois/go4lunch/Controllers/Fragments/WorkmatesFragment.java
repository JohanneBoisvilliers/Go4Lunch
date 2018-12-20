package com.example.jbois.go4lunch.Controllers.Fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jbois.go4lunch.Controllers.Activities.LunchActivity;
import com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.User;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.ItemClickSupport;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.example.jbois.go4lunch.Views.WorkmatesViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.twitter.sdk.android.core.internal.ActivityLifecycleManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WorkmatesFragment extends Fragment {

    @BindView(R.id.workmates_list_recycler_view)RecyclerView mRecyclerView;
    @BindView(R.id.no_workmate)TextView mNoWorkmate;

    private FirestoreRecyclerAdapter adapter;
    private Activity mActivity;
    private List<User> users = new ArrayList<>();
    private String mRestaurantInRestaurantProfile="";

    public WorkmatesFragment() {}

    public static WorkmatesFragment newInstance() {

        //Create new fragment
        WorkmatesFragment frag = new WorkmatesFragment();

        return(frag);
    }
    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        EventBus.getDefault().register(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        ButterKnife.bind(this,view);
        this.configureRecyclerView();
        this.configureOnClickRecyclerView();
        mActivity = this.getActivity();
        return view;
    }

    public void configureRecyclerView(){
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(UserHelper.getUsersCollection(), new SnapshotParser<User>() {
                    @NonNull
                    @Override
                    public User parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        User user = snapshot.toObject(User.class);
                        if(!TextUtils.isEmpty(user.getRestaurantChose())){
                            if(user.getRestaurantChose().equals(mRestaurantInRestaurantProfile)){
                                users.add(user);
                                Log.e("LISTSIZE",users.size()+"");
                            }
                        }
                        return user;
                    }
                })
                .build();

        adapter = new FirestoreRecyclerAdapter<User, WorkmatesViewHolder>(options) {
            @Override
            public void onBindViewHolder(WorkmatesViewHolder holder, int position, User model) {
                if(mActivity.getClass().getSimpleName().equals("RestaurantProfileActivity")){
                    if(users.size()==0){
                        mRecyclerView.setVisibility(View.INVISIBLE);
                        mNoWorkmate.setVisibility(View.VISIBLE);
                    }else{
                        holder.createListOfUserJoining(users.get(position));
                    }
                }else{
                    holder.updateRestaurantDestination(model);
                }
            }

            @Override
            public WorkmatesViewHolder onCreateViewHolder(ViewGroup group, int i) {
                Context context = getContext();
                LayoutInflater inflater = LayoutInflater.from(context);
                View view = inflater.inflate(R.layout.recyclerview_workmates_list_item, group, false);

                return new WorkmatesViewHolder(view);
            }
        };
        //Attach the adapter to the recyclerview to populate items
        this.mRecyclerView.setAdapter(adapter);
        //Set layout manager to position the items
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(mRecyclerView, R.layout.recyclerview_restaurant_list_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    }
                });
    }

    //Callback method to fetch restaurant list
    @Subscribe(sticky = true)
    public void ongetRestaurantNameForJoiningUsers(RestaurantProfileActivity.getRestaurantNameForJoiningUsers event) {
        mRestaurantInRestaurantProfile=event.restaurantName;
    }

    public FirestoreRecyclerAdapter getAdapter() {
        return adapter;
    }
}
