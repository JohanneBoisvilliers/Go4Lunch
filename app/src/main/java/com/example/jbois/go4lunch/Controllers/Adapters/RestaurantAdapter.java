package com.example.jbois.go4lunch.Controllers.Adapters;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jbois.go4lunch.Models.DistanceJson;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Utils.GooglePlacesStreams;
import com.example.jbois.go4lunch.Views.RestaurantViewHolder;
import com.example.jbois.go4lunch.R;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    private List<Restaurant> mRestaurantList;
    private Disposable mDisposable;
    private Location mLocation;
    private int mDistance;

    public RestaurantAdapter(List<Restaurant> restaurantList,Location location){
        this.mRestaurantList = restaurantList;
        this.mLocation = location;
    }
    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CREATE VIEW HOLDER AND INFLATING ITS XML LAYOUT
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_restaurant_list_item, parent, false);

        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        this.executeRequestToComputeDistance(mLocation,mRestaurantList.get(position).getId());
        mRestaurantList.get(position).setDistance(mDistance);
        holder.updateRestaurantInfos(mRestaurantList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.mRestaurantList.size();
    }

    // Get places around user and put marker on this places
    private void executeRequestToComputeDistance(Location location, String placeId) {
        this.mDisposable = GooglePlacesStreams.streamComputeRestaurantDistance(location.getLatitude()+","+location.getLongitude(),"place_id:"+placeId)
                .subscribeWith(new DisposableObserver<DistanceJson>() {
                    @Override
                    public void onNext(DistanceJson distance) {
                        mDistance = distance.getRows().get(0).getElements().get(0).getDistance().getValue();
                    }
                    @Override
                    public void onError(Throwable e) {
                    }
                    @Override
                    public void onComplete() {
                    }
                });

    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        if (this.mDisposable != null && !this.mDisposable.isDisposed()) this.mDisposable.dispose();
    }

}
