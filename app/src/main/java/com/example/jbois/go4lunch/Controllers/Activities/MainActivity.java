package com.example.jbois.go4lunch.Controllers.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.multidex.MultiDex;

import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.ApplicationContext;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.DocumentSnapshot;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

import butterknife.BindView;

import static com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity.PREFS_NAME;
import static com.example.jbois.go4lunch.Controllers.Activities.SettingsActivity.MyPreferenceFragment.NOTIF_UID;

public class MainActivity extends BaseUserActivity {

    @BindView(R.id.main_activity_coordinator_layout) CoordinatorLayout mCoordinatorLayout;

    private static final int RC_SIGN_IN = 123;
    private SharedPreferences mMySharedPreferences;
    private SharedPreferences.Editor mEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.startSignInActivity();
       // this.startLunchActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle SignIn Activity response on activity result
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
        EventBus.getDefault().postSticky(new LunchActivity.getUid(this.getCurrentUser().getUid()));
        mMySharedPreferences = ApplicationContext.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mEditor = mMySharedPreferences.edit();
        mEditor.putString(NOTIF_UID,this.getCurrentUser().getUid());
        mEditor.apply();
        this.startLunchActivity();

    }
    //Start an activity that let user choose between two ways for connection
    private void startSignInActivity(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.FacebookBuilder().build(),
                                        new AuthUI.IdpConfig.TwitterBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build()
                                ) // FACEBOOK
                        )
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.logo_go4lunch)
                        .build(),
                RC_SIGN_IN);
    }
    //show a snackbar into the bottom of screen to say that the connection work well
    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message){
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }
    //catch if there are any problems when user try to connect
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                this.createUserInFirestore();
            } else { // ERRORS
                if (response == null) {
                    showSnackBar(this.mCoordinatorLayout, getString(R.string.error_authentication_canceled));
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(this.mCoordinatorLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(this.mCoordinatorLayout, getString(R.string.error_unknown_error));
                }
            }
        }
    }
    //start the LunchActivity after user's connection
    private void startLunchActivity(){
        Intent intent = new Intent(this, LunchActivity.class);
        startActivity(intent);
    }

    private void createUserInFirestore(){
        UserHelper.getUser(this.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (!documentSnapshot.exists()) {

                        String urlPicture = (getCurrentUser().getPhotoUrl() != null) ? getCurrentUser().getPhotoUrl().toString() : null;
                        String username = getCurrentUser().getDisplayName();
                        String uid = getCurrentUser().getUid();

                        UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(onFailureListener());
                }
            }
        });



    }
}