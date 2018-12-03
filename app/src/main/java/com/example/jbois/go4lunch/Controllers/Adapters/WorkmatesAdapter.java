package com.example.jbois.go4lunch.Controllers.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jbois.go4lunch.Models.Workmates;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Views.RestaurantViewHolder;
import com.example.jbois.go4lunch.Views.WorkmatesViewHolder;

import java.util.List;

public class WorkmatesAdapter extends RecyclerView.Adapter<WorkmatesViewHolder> {

    private List<Workmates> mWorkmatesList;

    public WorkmatesAdapter(List<Workmates> workmatesList) {
        this.mWorkmatesList = workmatesList;
    }

    @NonNull
    @Override
    public WorkmatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CREATE VIEW HOLDER AND INFLATING ITS XML LAYOUT
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_workmates_list_item, parent, false);

        return new WorkmatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmatesViewHolder holder, int position) {
        holder.updateRestaurantDestination(mWorkmatesList.get(position));
    }

    @Override
    public int getItemCount() {
        return mWorkmatesList.size();
    }
}
