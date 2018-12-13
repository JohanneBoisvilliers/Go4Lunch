package com.example.jbois.go4lunch.Controllers.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jbois.go4lunch.Controllers.Adapters.WorkmatesAdapter;
import com.example.jbois.go4lunch.Models.User;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.ItemClickSupport;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.example.jbois.go4lunch.Views.WorkmatesViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;



/**
 * A simple {@link Fragment} subclass.
 */
public class WorkmatesFragment extends Fragment {

    @BindView(R.id.workmates_list_recycler_view)RecyclerView mRecyclerView;

    private FirestoreRecyclerAdapter adapter;

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
    }
    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        ButterKnife.bind(this,view);
        //configureTestList();
        this.configureRecyclerView();
        this.configureOnClickRecyclerView();

        return view;
    }

    public void configureRecyclerView(){
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(UserHelper.getUsersCollection(), User.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<User, WorkmatesViewHolder>(options) {
            @Override
            public void onBindViewHolder(WorkmatesViewHolder holder, int position, User model) {

                    holder.updateRestaurantDestination(model);
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

}
