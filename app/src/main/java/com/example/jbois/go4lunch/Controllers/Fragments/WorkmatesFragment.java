package com.example.jbois.go4lunch.Controllers.Fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.User;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.DividerDecoration;
import com.example.jbois.go4lunch.Utils.ItemClickSupport;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.example.jbois.go4lunch.Views.WorkmatesViewHolder;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.jbois.go4lunch.Controllers.Activities.LunchActivity.TAG;
import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;


public class WorkmatesFragment extends Fragment {

    @BindView(R.id.workmates_list_recycler_view)RecyclerView mRecyclerView;

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
        EventBus.getDefault().register(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        ButterKnife.bind(this,view);

        this.configureRecyclerView();
        configureOnClickRecyclerView();
        mActivity = this.getActivity();
        return view;
    }

    public void configureRecyclerView(){
        users.clear();
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(UserHelper.getUsersCollection().orderBy("restaurantChose", Query.Direction.DESCENDING), new SnapshotParser<User>() {
                    @NonNull
                    @Override
                    public User parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        User user = snapshot.toObject(User.class);
                        users.add(user);
                        Log.d(TAG, "parseSnapshot: " + users.size());
                        return user;
                    }
                })
                .build();

        adapter = new FirestoreRecyclerAdapter<User, WorkmatesViewHolder>(options) {

            @Override
            public void onBindViewHolder(WorkmatesViewHolder holder, int position, User model) {
                Gson gson = new Gson();
                String restaurantToString = model.getRestaurantChose();
                Restaurant restaurant= gson.fromJson(restaurantToString,new TypeToken<Restaurant>(){}.getType());

                if(mActivity.getClass().getSimpleName().equals("RestaurantProfileActivity")){
                    mRestaurantInRestaurantProfile = ((RestaurantProfileActivity)getActivity()).getcurrentRestaurant().getId();
                    if (TextUtils.isEmpty(restaurantToString)||!restaurant.getId().equals(mRestaurantInRestaurantProfile)) {

                            holder.itemView.setVisibility(View.GONE);
                            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.height=0;
                            params.setMargins(0,0,0,0);
                            holder.itemView.setLayoutParams(params);

                    }else{
                        holder.createListOfUserJoining(model);
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

            @NonNull
            @Override
            public User getItem(int position) {
                return super.getItem(position);
            }

            @Override
            public void onChildChanged(@NonNull ChangeEventType type,
                                       @NonNull DocumentSnapshot snapshot,
                                       int newIndex,
                                       int oldIndex) {
                switch (type) {
                    case ADDED:
                        notifyItemInserted(newIndex);
                        break;
                    case CHANGED:
                        notifyItemChanged(newIndex);
                        break;
                    case REMOVED:
                        notifyItemRemoved(oldIndex);
                        break;
                    case MOVED:
                        notifyItemMoved(oldIndex, newIndex);
                        break;
                    default:
                        throw new IllegalStateException("Incomplete case statement");
                }
            }
        };
        //Attach the adapter to the recyclerview to populate items
        this.mRecyclerView.setAdapter(adapter);
        //Set layout manager to position the items
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerDecoration divider= new DividerDecoration(getContext());
        this.mRecyclerView.addItemDecoration(divider);
    }

    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(mRecyclerView, R.layout.recyclerview_restaurant_list_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Gson gson = new Gson();
                        User user = (User)adapter.getItem(position);
                        String restaurantToString =user.getRestaurantChose() ;
                        //String restaurantToString = users.get(position).getRestaurantChose();
                        Restaurant restaurant= gson.fromJson(restaurantToString,new TypeToken<Restaurant>(){}.getType());
                        if (!TextUtils.isEmpty(restaurantToString)) {
                            Intent intent = new Intent(getActivity(),RestaurantProfileActivity.class);
                            intent.putExtra(RESTAURANT_IN_TAG,restaurant);
                            startActivity(intent);
                        }else {
                            Toast.makeText(getContext(), getResources().getString(R.string.workmate_no_choice_yet), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Callback method to fetch restaurant list
    @Subscribe(sticky = true)
    public void ongetRestaurantNameForJoiningUsers(RestaurantProfileActivity.getRestaurantNameForJoiningUsers event) {
        mRestaurantInRestaurantProfile=event.restaurantId;
        this.configureRecyclerView();
    }

}
