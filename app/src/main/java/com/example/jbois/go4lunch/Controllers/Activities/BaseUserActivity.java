package com.example.jbois.go4lunch.Controllers.Activities;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jbois.go4lunch.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class BaseUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_user);
    }

    //@Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ERRORFIREBASE",""+e);
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }
    //set number of stars to show
    public static void setStars(Double rating,List<ImageView> stars) {
        int numberOfStars = rating.intValue();
        switch (numberOfStars){
            case 4 :case 5:numberOfStars = 3;
                break;
            case 3:numberOfStars = 2;
                break;
            case 2 :numberOfStars = 1;
                break;
            case 0 :case 1 :numberOfStars = 0;
                break;
        }
        for (int i = 0; i < numberOfStars; i++) {
            stars.get(i).setVisibility(View.VISIBLE);
        }
    }
    //callback class to get if restaurant is liked
    public static class getLikedOrNot{
        public Boolean isLiked;

        public getLikedOrNot(Boolean isLiked){
            this.isLiked = isLiked;
        }
    }
}
