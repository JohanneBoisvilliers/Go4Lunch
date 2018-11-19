package com.example.jbois.go4lunch.Controllers.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantDetails;
import com.example.jbois.go4lunch.Models.RestaurantListJson;
import com.example.jbois.go4lunch.Utils.GooglePlacesStreams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.example.jbois.go4lunch.Controllers.Activities    .RestaurantProfileActivity;
import com.example.jbois.go4lunch.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class MapFragment extends Fragment
        implements  GoogleMap.OnMyLocationButtonClickListener,
                    GoogleMap.OnMyLocationClickListener,
                    OnMapReadyCallback,
                    GoogleMap.OnMarkerClickListener,
                    LocationListener,
                    GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(-48.874949, 2.350520);
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;
    private final static int MY_PERMISSION_FINE_LOCATION = 101;

    private Disposable mDisposable;

    public MapFragment() {}

    public static MapFragment newInstance() {

        //Create new fragment
        MapFragment frag = new MapFragment();

        return(frag);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disposeWhenDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Get layout of MapFragment
        View result = inflater.inflate(R.layout.fragment_map, container, false);
        //get map from GoogleMaps
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Initialize location objects
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        this.buildGoogleApiClient();

        return result;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap=map;

        checkPermissionToLocation();
        updateLocationUI();
        getDeviceLocation();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }
    //Check permission for location and ask for it if user didn't allowed it
    public void checkPermissionToLocation(){
        //Permission for user's location
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted=true;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);
            }
        }
    }
    //What to do when user allowed or not permission for location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted=true;
                    }
                }
        }
        updateLocationUI();
    }
    //Avoid memory leaks
    private void disposeWhenDestroy(){
        if (this.mDisposable != null && !this.mDisposable.isDisposed()) this.mDisposable.dispose();
    }
    //On click on blue dot (user's location)
    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getActivity(), R.string.user_position, Toast.LENGTH_LONG).show();
    }
    //On click on MyLocationButton
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getActivity(), R.string.user_position, Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
    //Method to open restaurant profile when user click on a marker
    @Override
    public boolean onMarkerClick(final Marker marker) {
        Intent intent = new Intent(getActivity(),RestaurantProfileActivity.class);
        intent.putExtra("TEST",(Restaurant)marker.getTag());
        startActivity(intent);
        return false;
    }
    //Set and add a new marker
    public void createMarker(LatLng latLng,RestaurantDetails restaurant){

        Marker marker=mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant_location_32)));
        marker.setTag(restaurant);
    }
    // Turn on the My Location layer and the related control on the map.
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mLastKnownLocation = null;
                checkPermissionToLocation();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    // Get the current location of the device and set the position of the map.
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                    Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                    locationResult.addOnCompleteListener(getActivity(),new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()) {
                                // Set the map's camera position to the current location of the device.
                                mLastKnownLocation = task.getResult();
                                Log.e("VALEUR_LOCATION",mLastKnownLocation.getLatitude()+","+mLastKnownLocation.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), 15));
                                executeRequestToshowCurrentPlacetest(mLastKnownLocation);
                            } else {
                                mMap.moveCamera(CameraUpdateFactory
                                        .newLatLngZoom(mDefaultLocation, 15));
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        }
                    });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void executeRequestToshowCurrentPlacetest(Location location) {
        Log.e("PAAA","ERROR DE SA MERE");
        this.mDisposable = GooglePlacesStreams.streamFetchRestaurantsWithNeededInfos(location.getLatitude()+","+location.getLongitude())
                .subscribeWith(new DisposableObserver<List<RestaurantDetails>>() {
                    @Override
                    public void onNext(List<RestaurantDetails> restaurantListJson) {
                        Log.e("EEEEEEE",""+restaurantListJson.size());
                        for(int i = 0; i< restaurantListJson.size(); i++){
                            Log.e("MARKER TEST",""+restaurantListJson.size());
                            Double lat = restaurantListJson.get(i).getResult().getGeometry().getLocation().getLat();
                            Double lng = restaurantListJson.get(i).getResult().getGeometry().getLocation().getLng();
                            createMarker(new LatLng(lat,lng), restaurantListJson.get(i));
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e("MARKER TEST","ERROR DE SA MERE");
                    }
                    @Override
                    public void onComplete() {
                        Log.e("MARKER TEST","ERROR DE SA MERE");
                    }
                });
    }
    // Get and show current places with marker
    //private void executeRequestToshowCurrentPlace(Location location) {
    //    this.mDisposable = GooglePlacesStreams.streamFetchRestaurants( location.getLatitude()+","+location.getLongitude())
    //            .subscribeWith(new DisposableObserver<RestaurantListJson>() {
    //        @Override
    //        public void onNext(RestaurantListJson restaurantListJson) {
    //            for(int i = 0; i< restaurantListJson.getResults().size(); i++){
    //                Double lat = restaurantListJson.getResults().get(i).getGeometry().getLocation().getLat();
    //                Double lng = restaurantListJson.getResults().get(i).getGeometry().getLocation().getLng();
    //                createMarker(new LatLng(lat,lng), restaurantListJson.getResults().get(i));
    //            }
    //        }
    //        @Override
    //        public void onError(Throwable e) {
    //        }
    //        @Override
    //        public void onComplete() {
    //        }
    //    });
    //}

    //private Restaurant executeRequestToGetRestaurantInfos(String placeId){
    //    final Restaurant restaurant = new Restaurant();
//
    //    this.mDisposable = GooglePlacesStreams.streamFetchRestaurantDetails(placeId)
    //            .subscribeWith(new DisposableObserver<RestaurantDetails>() {
    //                @Override
    //                public void onNext(RestaurantDetails restaurantDetails) {
    //                    restaurant.setName(restaurantDetails.getResult().getName());
    //                    restaurant.setAdress(restaurantDetails.getResult().getFormattedAddress());
    //                }
    //                @Override
    //                public void onError(Throwable e) {
    //                }
    //                @Override
    //                public void onComplete() {
    //                }
    //            });
    //    return restaurant;
    //}
    //Create google api client
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
