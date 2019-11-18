package com.example.jbois.go4lunch.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.util.Log;


import com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.User;
import com.example.jbois.go4lunch.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import static com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity.PREFS_NAME;
import static com.example.jbois.go4lunch.Controllers.Activities.SettingsActivity.MyPreferenceFragment.NOTIF_UID;
import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;
import static com.example.jbois.go4lunch.Utils.UserHelper.COLLECTION_USERS;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "DEBUG_APPLICATION";
    private Boolean mRestaurantNull = true;
    private Restaurant mRestaurant;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //get User Id for request
        String uid = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(NOTIF_UID,"");

        FirebaseFirestore.getInstance().collection(COLLECTION_USERS).document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                getRestaurantChoseByUser(task);
                //Intent to invoke app when click on notification.
                Intent intentRestaurant = new Intent(getApplication(), RestaurantProfileActivity.class);
                intentRestaurant.putExtra(RESTAURANT_IN_TAG,mRestaurant);
                //Pending intent to handle launch of Activity in intent above
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(), 100, intentRestaurant, PendingIntent.FLAG_UPDATE_CURRENT);
                setNotificationManager(setNotificationBuilder(pendingIntent));
            }
        });

    }
    //set the notification builder depending if user chose a restaurant
    private NotificationCompat.Builder setNotificationBuilder(PendingIntent pendingIntent){
        if (mRestaurantNull) {
            return this.createNotificationBuilder(null,getResources().getString(R.string.notif_without_restaurant));
        }else{
            return this.createNotificationBuilder(pendingIntent,getResources().getString(R.string.notif_with_restaurant));
        }
    }
    //create content of notification builder
    private NotificationCompat.Builder createNotificationBuilder(@Nullable PendingIntent pendingIntent, String notificationText){
        return (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext(),"default")
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.arrow_up_float)
                .setContentTitle("Go4Lunch")
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
    }
    //set notification manager
    private void setNotificationManager(NotificationCompat.Builder notificationBuilder){
        NotificationManager mNotificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "CHANNEL_ID",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("NOTIFICATION_CHANNEL_ID");
            mNotificationManager.createNotificationChannel(channel);
            // notificationId is a unique int for each notification that you must define
            mNotificationManager.notify(0, notificationBuilder.build());
        }
    }
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
    }
    //transform requeest into user to get restaurant chose by user
    private void getRestaurantChoseByUser(@NonNull Task<DocumentSnapshot> task){
        Log.d(TAG, "onComplete: notification received");
        DocumentSnapshot documentSnapshot =task.getResult();
        User user = documentSnapshot.toObject(User.class);
        Gson gson = new Gson();
        String restaurantToString = user.getRestaurantChose();
        Log.d(TAG, "onComplete: "+restaurantToString);
        mRestaurant= gson.fromJson(restaurantToString,new TypeToken<Restaurant>(){}.getType());
        if (mRestaurant!=null) {
            mRestaurantNull = false;
        }
    }
}
