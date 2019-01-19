package com.example.jbois.go4lunch.Controllers.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.jbois.go4lunch.Controllers.Activities.LunchActivity;
import com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.User;
import com.example.jbois.go4lunch.R;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(-48.874949, 2.350520);
    private Location mLastKnownLocation;
    private List<Marker> mMarkers = new ArrayList<>();
    private List<Restaurant> mRestaurantsChosenList = new ArrayList<>();
    private List<String> mCollectionRestaurantsChosenList = new ArrayList<>();
    private boolean mLocationPermissionGranted;
    private Disposable mDisposable;
    private ListenerRegistration mRestaurantsChoseListener;
    private final static int MY_PERMISSION_FINE_LOCATION = 101;
    public final static String RESTAURANT_IN_TAG = "restaurant";
    private final static String TAG = "debug";

    private List<Restaurant> mRestaurantsAroundUser = new ArrayList<>();
    //Set onLocationChanged method to know what to do when user is moving
    private LocationListener mLocationListenerGPS = new LocationListener() {

        @Override
        public void onLocationChanged(android.location.Location location) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(),
                            location.getLongitude()), 15));
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

        //Create new fragment
        MapFragment frag = new MapFragment();

        return (frag);
    }

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
        mapFragment.getMapAsync(this);
        //Initialize location objects
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        mLocationManager=(LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        // this.setLocationManager();
        //this.buildGoogleApiClient();
        return result;
    }
    //Set location Manager which use onLocationChanged method
    @SuppressLint("MissingPermission")
    private void setLocationManager(){
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5*60*1000, 10, mLocationListenerGPS);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        this.settingsForMap(map);
        Log.d(TAG, "Restaurants choisis par users : "+mRestaurantsChosenList.size());
        checkPermissionToLocation();
        updateLocationUI();
        getDeviceLocation();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerClickListener(this);
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
    //Check permission for location and ask for it if user didn't allowed it
    public void checkPermissionToLocation() {
        //Permission for user's location
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
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
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = true;
                        }
                    }
                }
        }
        updateLocationUI();
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
        marker.hideInfoWindow();
        Intent intent = new Intent(getActivity(), RestaurantProfileActivity.class);
        intent.putExtra(RESTAURANT_IN_TAG, (Restaurant) marker.getTag());
        startActivity(intent);
        return false;
    }
    //Set and add a new marker
    public void createMarker(LatLng latLng, Restaurant restaurant) {
        String color = getResources().getString(0 + R.color.colorPrimary);
        for (Restaurant restaurantChosenId : mRestaurantsChosenList) {
            if (restaurant.getId().equals(restaurantChosenId.getId())) {
                color = getResources().getString(0 + R.color.floatingButtonValidate);
            }
        }
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(restaurant.getId())
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
                mLastKnownLocation = null;
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
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            moveCamera(mLastKnownLocation,15);
                            EventBus.getDefault().post(new LunchActivity.getLocation(mLastKnownLocation));
                            //executeRequestToShowCurrentPlace(mLastKnownLocation);
                            try {
                                fakeRequest();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, 15));
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
    //check on database if restaurant is chose by someone
    private void RestaurantsChosenListener(){
        mRestaurantsChoseListener = UserHelper.getRestaurantChosen()
                .addSnapshotListener(MetadataChanges.INCLUDE,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        getRestaurantChosenFromUsers();
                        if (e != null) { Log.w(TAG, "Listen failed.", e); }
                        if(!value.isEmpty()){
                            for (QueryDocumentSnapshot document : value) {
                                Log.e(TAG, "onEvent: id de resto dans liste" + document.getId());
                                browseMarkersList(document.getId());
                            }
                        }
                    }
                });
    }
    private void browseMarkersList(String placeId){
        for (Marker marker : mMarkers) {
            if (marker.getTitle().equals(placeId)){
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(setMarkerColor(getResources().getString(0 + R.color.floatingButtonValidate))));
            }else{
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(setMarkerColor(getResources().getString(0 + R.color.colorPrimary))));
            }
        }
    }
    //check on database if user liked this restaurant
    private void getRestaurantChosenFromUsers(){
        mRestaurantsChosenList.clear();
        UserHelper.getUsersCollection().get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);
                                Gson gson = new Gson();
                                String restaurantToString = user.getRestaurantChose();
                                Restaurant restaurant = gson.fromJson(restaurantToString,new TypeToken<Restaurant>(){}.getType());
                                if (restaurant!=null) {
                                    mRestaurantsChosenList.add(restaurant);
                                    Log.e(TAG, "RESTAURANTCHOISIS : "+restaurant.getName());
                                    Log.e("liste", "liste de restaurants choisis : "+mRestaurantsChosenList.size());
                                }
                            }
                        } else {
                            Log.w("LIKEBUTTON","can't receive if restaurant is liked");
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
