package com.example.jbois.go4lunch.Controllers.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.jbois.go4lunch.Controllers.Activities.LunchActivity;
import com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.ApplicationContext;
import com.example.jbois.go4lunch.Utils.GooglePlacesStreams;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MapFragment extends Fragment
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private GoogleApiClient mGoogleApiClient;
    private View mMapView;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(48.874949, 2.350520);
    private Location mLastKnownLocation;
    private List<Marker> mMarkers = new ArrayList<>();
    private List<Restaurant> mRestaurantsChosenList = new ArrayList<>();
    private List<String> mRestaurantsChosenId = new ArrayList<>();
    private List<String> mCollectionRestaurantsChosenList = new ArrayList<>();
    private boolean mLocationPermissionGranted;
    private Disposable mDisposable;
    private ListenerRegistration mRestaurantsChoseListener;
    private final static int MY_PERMISSION_FINE_LOCATION = 101;
    public final static String RESTAURANT_IN_TAG = "restaurant";
    private final static String TAG = "debug";
    private LocationListener mCurrentLocation;
    private List<Restaurant> mRestaurantsAroundUser = new ArrayList<>();
    //Set onLocationChanged method to know what to do when user is moving
    private LocationListener mLocationListenerGPS = new LocationListener() {

        @Override
        public void onLocationChanged(android.location.Location location) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(),
                            location.getLongitude()), 15));

            mMarkers.clear();
            mMap.clear();
            executeRequestToShowCurrentPlace(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public MapFragment() {}

    public static MapFragment newInstance() {
        return  new MapFragment();
    }

    // -------------------------------- LIFE CYCLE --------------------------------

    @Override
    public void onStart() {
        super.onStart();
        this.RestaurantsChosenListener();
        EventBus.getDefault().register(this);
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onStop() {
        super.onStop();
        mRestaurantsChoseListener.remove();
        mLocationManager.removeUpdates(mLocationListenerGPS);
        EventBus.getDefault().unregister(this);
    }
    @Override
    public void onDestroy() {
        this.disposeWhenDestroy();
        super.onDestroy();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Get layout of MapFragment
        View result = inflater.inflate(R.layout.fragment_map, container, false);
        //get map from GoogleMaps
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mMapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
        //Initialize location objects
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        mLocationManager=(LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        return result;
    }
    //Set location Manager which use onLocationChanged method
    @SuppressLint("MissingPermission")
    private void setLocationManager(){
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 200, mLocationListenerGPS);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        this.settingsForMap(map);
        this.getRestaurantsChosen();
        checkPermissionToLocation();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerClickListener(this);
        this.settingsForLocationButton();
    }
    //set style for map
    private void settingsForMap(GoogleMap map){
        map.setBuildingsEnabled(false);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }
    private void settingsForLocationButton(){
        if (mMapView != null &&
                mMapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
    }
    //if GPS is off, then ask user to activate it
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    //Check permission for location and ask for it if user didn't allowed it
    public void checkPermissionToLocation() {
        //Permission for user's location

        if (ActivityCompat.checkSelfPermission(ApplicationContext.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationPermissionGranted = true;
            updateLocationUI();
            this.setLocationManager();
            getDeviceLocation();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);
            }
        }

    }
    //What to do when user allowed or not permission for location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)  {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = true;
                            updateLocationUI();
                            this.setLocationManager();
                            getDeviceLocation();
                        }
                }else{
                    Log.d(TAG, "DEBUG: REQUETE NON");
                }

        }

    }
    //Avoid memory leaks
    private void disposeWhenDestroy() {
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
        return false;
    }
    //Method to open restaurant profile when user click on a marker
    @Override
    public boolean onMarkerClick(final Marker marker) {
        Intent intent = new Intent(getActivity(), RestaurantProfileActivity.class);
        intent.putExtra(RESTAURANT_IN_TAG, (Restaurant) marker.getTag());
        startActivity(intent);
        return false;
    }
    //Set and add a new marker
    public void createMarker(LatLng latLng, Restaurant restaurant) {
        String color = getResources().getString(0 + R.color.colorPrimary);
        for (String restaurantChosenId : mRestaurantsChosenId) {
            if (restaurant.getId().equals(restaurantChosenId)) {
                color = getResources().getString(0 + R.color.floatingButtonValidate);
            }
        }
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(this.setMarkerColor(color))));
        marker.setTag(restaurant);
        mMarkers.add(marker);
    }
    //set the color of markers
    private Bitmap setMarkerColor(String color){

        Bitmap ob = BitmapFactory.decodeResource(this.getResources(),R.drawable.restaurant_location_32);
        Bitmap obm = Bitmap.createBitmap(ob.getWidth(), ob.getHeight(), ob.getConfig());
        Canvas canvas = new Canvas(obm);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(Color.parseColor(color),PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(ob, 0f, 0f, paint);

        return obm;
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
                mLastKnownLocation=null;
                checkPermissionToLocation();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    // Get the current location of the device and set the position of the map.
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()&& mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation==null) {
                                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    mLastKnownLocation = new Location("currentLocation");
                                    mLastKnownLocation.setLatitude(location.getLatitude());
                                    mLastKnownLocation.setLongitude(location.getLongitude());
                                }
                            }else{
                                moveCamera(mLastKnownLocation,15);
                            }
                            EventBus.getDefault().post(new LunchActivity.getLocation(mLastKnownLocation));
                            executeRequestToShowCurrentPlace(mLastKnownLocation);
                            //try {
                            //    fakeRequest();
                            //} catch (IOException e) {
                            //    e.printStackTrace();
                            //}
                        } else {
                            buildAlertMessageNoGps();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 1));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    //center the map on selected item and zoom
    private void moveCamera(Location location,int zoomLevel){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(),
                        location.getLongitude()), zoomLevel));
    }
    // Get places around user and put marker on this places
    private void executeRequestToShowCurrentPlace(Location location) {
        this.mDisposable = GooglePlacesStreams.streamFetchRestaurantsWithNeededInfos(location.getLatitude() + "," + location.getLongitude())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Restaurant>>() {
                    @Override
                    public void onNext(List<Restaurant> restaurantList) {
                        mRestaurantsAroundUser.clear();
                        mRestaurantsAroundUser.addAll(restaurantList);
                        for (Restaurant restaurant : restaurantList) {
                            Double lat = restaurant.getLat();
                            Double lng = restaurant.getLng();
                            createMarker(new LatLng(lat, lng), restaurant);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error on main stream(in Mapfragment): " + e);
                    }

                    @Override
                    public void onComplete() {
                        EventBus.getDefault().post(new LunchActivity.refreshRestaurantsList(mRestaurantsAroundUser));
                    }
                });

    }
    private void fakeRequest() throws IOException {
        this.mDisposable = GooglePlacesStreams.fakeStream()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Restaurant>>() {
                    @Override
                    public void onNext(List<Restaurant> restaurantList) {
                        mMarkers.clear();
                        mRestaurantsAroundUser.clear();
                        mRestaurantsAroundUser.addAll(restaurantList);
                        for (Restaurant restaurant : restaurantList) {
                            Double lat = restaurant.getLat();
                            Double lng = restaurant.getLng();
                            createMarker(new LatLng(lat, lng), restaurant);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error on main stream(in Mapfragment): " + e);
                    }

                    @Override
                    public void onComplete() {
                        EventBus.getDefault().post(new LunchActivity.refreshRestaurantsList(mRestaurantsAroundUser));
                    }
                });

    }
    private void RestaurantsChosenListener(){
        mRestaurantsChoseListener = UserHelper.getRestaurantChosen()
                .addSnapshotListener(MetadataChanges.INCLUDE,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (value!=null){
                            resetMarkersColors();
                            for (QueryDocumentSnapshot document : value) {
                                browseMarkersList(document.getId());
                            }
                        }
                    }
                });
    }
    //reset marker's colors when there is an event
    private void resetMarkersColors(){
        for (Marker marker : mMarkers) {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(setMarkerColor(getResources().getString(0 + R.color.colorPrimary))));
        }
    }
    //browse marker's list to change only marker's colors where there is at least one user
    private void browseMarkersList(String placeId){
        for (Marker marker : mMarkers) {
            Restaurant restaurant = (Restaurant)marker.getTag();
            if (restaurant.getId().equals(placeId)){
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(setMarkerColor(getResources().getString(0 + R.color.floatingButtonValidate))));
            }
        }
    }
    //get restaurants chosen by users to change marker's colors
    private void getRestaurantsChosen(){
        UserHelper.getRestaurantChosen().get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mRestaurantsChosenId.clear();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mRestaurantsChosenId.add(document.getId());
                            }
                        } else {
                            Log.w(TAG,"error when receiving users");
                        }
                    }
                });
    }

    //Callback method to fetch place position into autocomplete widget
    @Subscribe
    public void onGetLocation(LunchActivity.getPlaceLocation event) {
        mLastKnownLocation=event.location;
        this.moveCamera(mLastKnownLocation,20);
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

}
