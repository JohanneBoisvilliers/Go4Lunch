package com.example.jbois.go4lunch.Controllers.Fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.jbois.go4lunch.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MapFragment extends Fragment
        implements  GoogleMap.OnMyLocationButtonClickListener,
                    GoogleMap.OnMyLocationClickListener,
                    OnMapReadyCallback{


    private GoogleMap mMap;
    private final static int MY_PERMISSION_FINE_LOCATION = 101;

    public MapFragment() {}

    public static MapFragment newInstance() {

        //Create new fragment
        MapFragment frag = new MapFragment();

        return(frag);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Get layout of MapFragment
        View result = inflater.inflate(R.layout.fragment_map, container, false);
        //get map from GoogleMaps
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return result;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap=map;
        //Check permissions for location and ask for it if user didn't allowed it
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);
            }
        }
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
    }
    //What to do when user allowed or not permission for location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(getContext(), "This app requires location permissions to be granted", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    //On click on blue dot (user's location)
    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getActivity(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }
    //On click on MyLocationButton
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

}
