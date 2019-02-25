package com.example.jbois.go4lunch.Controllers.Activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jbois.go4lunch.Controllers.Adapters.PageAdapter;
import com.example.jbois.go4lunch.Controllers.Adapters.PlaceAutocompleteAdapter;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.User;
import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.GlideApp;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity.PREFS_NAME;
import static com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity.RESTAURANT_SAVED;
import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;

public class LunchActivity extends BaseUserActivity implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener{

    @BindView(R.id.activity_main_viewpager)ViewPager mViewPager;
    @BindView(R.id.bottom_navigation_view)BottomNavigationView mBottomNavigationView;
    @BindView(R.id.activity_main_toolbar)Toolbar mToolbar;
    @BindView(R.id.activity_main_drawer_layout)DrawerLayout mDrawerLayout;
    @BindView(R.id.activity_main_nav_view)NavigationView mNavigationView;

    private String[] mTitleList = new String[3];
    public static final String USERID="user_id";
    public static final String TAG_ERROR_FIREBASE="LunchActivity_auth";
    public static final String TAG = "DEBUG_APPLICATION";
    private Restaurant mRestaurant;
    private List<Restaurant> mRestaurantList = new ArrayList<>();
    private Location mLocation;
    private MenuItem mItem;
    private GoogleApiClient mGoogleApiClient;
    private GeoDataClient mGeoDataClient;
    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private ListenerRegistration mUsernameListener;
    private ListenerRegistration mRestaurantsChoseListener;


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //callback class to get restaurant List
    public static class refreshRestaurantsList{
        public List<Restaurant> restaurantList;

        public refreshRestaurantsList(List<Restaurant> restlist){
            this.restaurantList = restlist;
        }

    }
    //callback class to get Location
    public static class getLocation{
        public Location location;

        public getLocation(Location location){
            this.location = location;
        }
    }
    //callback class to get user id
    public static class getUid{
        public String uid;

        public getUid(String uid){
            this.uid = uid;
        }
    }
    //callback class to set camera position after clicking in autocomplete research item
    public static class getPlaceLocation{
        public Location location;

        public getPlaceLocation(Location location){
            this.location = location;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        EventBus.getDefault().register(this);
    }
    @Override
    public void onStop() {
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
        EventBus.getDefault().unregister(this);
        mUsernameListener.remove();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setNavigationDrawerHeader();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunch);
        ButterKnife.bind(this);
        //mAutocompleteFragment =(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mTitleList = getResources().getStringArray(R.array.toolbar_title_list);
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();

        this.configureToolbar();
        this.configureViewPager();
        this.configureBottomView();
        this.configureDrawerLayout();
        this.configureNavigationView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        mItem=menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mItem);
        mSearchAutoComplete = (SearchView.SearchAutoComplete) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                String dataName[] = new String[mRestaurantList.size()];
                for (int i = 0; i < mRestaurantList.size(); i++) {
                    dataName[i] = mRestaurantList.get(i).getName();
                }
                ArrayAdapter<String> adapterString = new ArrayAdapter<String>(this,R.layout.searchview_list_item,R.id.search_text,dataName);
                // Construct a GeoDataClient.
                mGeoDataClient = Places.getGeoDataClient(this, null);
                mSearchAutoComplete.setAdapter(adapterString);
                mSearchAutoComplete.setOnItemClickListener(mAutocompleteClickListener);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //Handle back click to close menu
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void configureToolbar(){
        // Sets the Toolbar
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(mTitleList[0]);
    }

    private void configureViewPager(){
        // Get ViewPager from layout
        ViewPager pager = mViewPager;
        // Set Adapter PageAdapter and glue it together
        pager.setAdapter(new PageAdapter(getSupportFragmentManager()) {
        });

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mBottomNavigationView.getMenu().getItem(position).setChecked(true);
                mToolbar.setTitle(mTitleList[position]);
                switch (position){
                    case 0: case 1:mItem.setVisible(true);
                        break;
                    case 2:mItem.setVisible(false);
                }
                if (position==2){
                    mSearchView.setQuery("",false);
                    MenuItemCompat.collapseActionView(mItem);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void configureBottomView(){
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                updateMainFragment(item.getItemId());
                item.setChecked(true);
                return false;
            }
        });
    }

    //Configure NavigationView
    private void configureNavigationView(){
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private void updateMainFragment(Integer integer){
        switch (integer) {
            case R.id.navigation_map:
                mViewPager.setCurrentItem(0);
                mToolbar.setTitle(mTitleList[0]);
                break;
            case R.id.navigation_list:
                mViewPager.setCurrentItem(1);
                mToolbar.setTitle(mTitleList[0]);
                break;
            case R.id.navigation_workmates:
                mViewPager.setCurrentItem(2);
                mToolbar.setTitle(mTitleList[2]);
                break;
        }
    }

    /*
        -------------------------- navigationDrawer settings -----------------------------------
     */
    //Configure Drawer Layout
    private void configureDrawerLayout(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.floatingButton));
        toggle.syncState();
        setNavigationDrawerHeader();
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle Navigation Item Click
        int id = item.getItemId();

        switch (id) {
            case R.id.drawer_your_lunch:
                this.yourLunchButton();
                break;
            case R.id.drawer_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.drawer_logout:
                this.returntoMainActivity();
                this.signOutUserFromFirebase();
                break;
            default:
                break;
        }
        this.mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
    //SignOut user
    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this);
    }
    //When user logout, return to MainActivity to let user log again
    private void returntoMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    //set the navigationDrawer
    private void setNavigationDrawerHeader(){
        View header = mNavigationView.getHeaderView(0);
        ImageView userPhoto = header.findViewById(R.id.user_profile_photo);
        TextView userNameContainer = header.findViewById(R.id.user_profile_name);
        TextView userMail =header.findViewById(R.id.user_profile_mail);

        GlideApp.with(this)
                .load(this.getCurrentUser().getPhotoUrl())
                .circleCrop()
                .error(R.drawable.no_image_small_icon)
                .into(userPhoto);
        String userName = TextUtils.isEmpty(this.getCurrentUser().getDisplayName())?
                getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();
        String email = TextUtils.isEmpty(this.getCurrentUser().getEmail())?
                getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();
        this.usernameListener(this.getCurrentUser().getUid(), userNameContainer);
        userNameContainer.setText(userName);
        userMail.setText(email);
    }
    //show the restaurant chosen by user
    private void yourLunchButton(){
        Restaurant restaurant = this.getRestaurantInSharedPreferences();
        if (restaurant!=null){
            Intent intentRestaurant = new Intent(this,RestaurantProfileActivity.class);
            intentRestaurant.putExtra(RESTAURANT_IN_TAG, restaurant);
            startActivity(intentRestaurant);
        }else{
            Toast.makeText(this, getString(R.string.no_restaurant_chose_yet), Toast.LENGTH_SHORT).show();
        }
    }
    private Restaurant getRestaurantInSharedPreferences(){
        Gson gson = new Gson();
        String restaurantToString = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(RESTAURANT_SAVED,"");
        return gson.fromJson(restaurantToString,new TypeToken<Restaurant>(){}.getType());
    }
    //listener to know when user change his name to refresh the textview in navigation header
    private void usernameListener(String uid,TextView textView){
        mUsernameListener = UserHelper.getUsersCollection().document(uid)
        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if(snapshot!=null){
                    User user = snapshot.toObject(User.class);
                    textView.setText(user.getUsername());
                }
            }
        });
    }
     /*
        ------------------ google places API autocomplete suggestions -------------------------
     */
     private void hideSoftKeyboard(View view){
         InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         if (in != null) {
             in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
         }
     }
    //on item click listener for searchview
    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard(view);
            //final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String itemName =(String) mSearchAutoComplete.getAdapter().getItem(i);

            //final String placeID = item.getPlaceId();
            if (mViewPager.getCurrentItem()==0){
                for (int j = 0; j < mRestaurantList.size(); j++) {
                    if (itemName.equals(mRestaurantList.get(j).getName())){
                        Location location = new Location("");
                        location.setLatitude(mRestaurantList.get(j).getLat());
                        location.setLongitude(mRestaurantList.get(j).getLng());
                        EventBus.getDefault().post(new LunchActivity.getPlaceLocation(location));
                    }
                }
            }
            if (mViewPager.getCurrentItem()==1){
                for (int j = 0; j < mRestaurantList.size(); j++) {
                    if (itemName.equals(mRestaurantList.get(j).getName())){
                        Intent intentRestaurant = new Intent(getBaseContext(),RestaurantProfileActivity.class);
                        intentRestaurant.putExtra(RESTAURANT_IN_TAG, mRestaurantList.get(j));
                        startActivity(intentRestaurant);
                    }
                }
            }
        }
    };

    /*
        --------------------------- Callbacks Methods -----------------------------------------
     */
    //Callback method to fetch restaurant
    @Subscribe
    public void ongetRestaurant(RestaurantProfileActivity.getRestaurant event) {
        mRestaurant=event.restaurant;
    }
    //Callback method to fetch Location
    //@Subscribe(sticky = true)
    //public void onGetLocation(LunchActivity.getLocation event) {
    //    mLocation=event.location;
    //   // this.configureSearchToolbar();
    //}
    //Callback method to fetch restaurant list
    @Subscribe
    public void onRefreshingRestaurantList(LunchActivity.refreshRestaurantsList event) {
        mRestaurantList.clear();
        mRestaurantList.addAll(event.restaurantList);
    }
}

