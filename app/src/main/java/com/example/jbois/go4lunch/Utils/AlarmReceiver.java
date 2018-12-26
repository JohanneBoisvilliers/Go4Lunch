package com.example.jbois.go4lunch.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.app.PendingIntent;

import com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity;
import com.example.jbois.go4lunch.Models.Restaurant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import static com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity.PREFS_NAME;
import static com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity.RESTAURANT_SAVED;
import static com.example.jbois.go4lunch.Controllers.Fragments.MapFragment.RESTAURANT_IN_TAG;

public class AlarmReceiver extends BroadcastReceiver{

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("AAAAAAAAAAAAAAAA","alarme recue");

        Gson gson = new Gson();

        String restaurantToString = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(RESTAURANT_SAVED,"");
        Restaurant restaurant = gson.fromJson(restaurantToString,new TypeToken<Restaurant>(){}.getType());
        //Intent to invoke app when click on notification.
        Intent intentRestaurant = new Intent(context, RestaurantProfileActivity.class);
        intentRestaurant.putExtra(RESTAURANT_IN_TAG,restaurant);
        intentRestaurant.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //Pending intent to handle launch of Activity in intent above
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, intentRestaurant, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "CHANNEL_ID",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("NOTIFICATION_CHANNEL_ID");
            mNotificationManager.createNotificationChannel(channel);
        }
        if(TextUtils.isEmpty(restaurantToString)){
            //Build notification
            NotificationCompat.Builder builder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(context,"default")
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(android.R.drawable.arrow_up_float)
                            .setContentTitle("Go4Lunch")
                            .setContentText("You've not chosen any restaurant !")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true);
            // notificationId is a unique int for each notification that you must define
            mNotificationManager.notify(0, builder.build());
        }
        if(restaurant != null){
            //Build notification
            NotificationCompat.Builder builder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(context,"default")
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(android.R.drawable.arrow_up_float)
                            .setContentTitle("Go4Lunch")
                            .setContentText("It's time to eat : see your choice !")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true);
            // notificationId is a unique int for each notification that you must define
            mNotificationManager.notify(1, builder.build());
        }
    }
}
