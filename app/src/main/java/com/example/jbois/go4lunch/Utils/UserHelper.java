package com.example.jbois.go4lunch.Utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import static com.example.jbois.go4lunch.Controllers.Activities.LunchActivity.TAG;

public class UserHelper {

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_RESTAURANT_CHOSEN = "restaurantChosen";
    public static final String COLLECTION_RESTAURANT_LIKED = "restaurantsLiked";
    public static final String COLLECTION_USERS_WHO_CHOSE = "usersWhoChose";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_USERS);
    }
    public static CollectionReference getRestaurantChosen(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_RESTAURANT_CHOSEN);
    }

    public static CollectionReference getRestaurantsLikedCollection(String uid){
        return UserHelper.getUsersCollection().document(uid).collection(COLLECTION_RESTAURANT_LIKED);
    }

    public static CollectionReference getUsersWhoChose(String placeId){
        return UserHelper.getRestaurantChosen().document(placeId).collection(COLLECTION_USERS_WHO_CHOSE);
    }

    // --- CREATE ---

    public static Task<Void> createUser(String uid, String username, String urlPicture) {
        User userToCreate = new User(uid, username, urlPicture);
        return UserHelper.getUsersCollection().document(uid).set(userToCreate);
    }

    public static Task<Void> createRestaurantLiked(String placeId, String uid,Restaurant restaurant) {
        return UserHelper.getRestaurantsLikedCollection(uid).document(placeId).set(restaurant);
    }

    public static Task<Void> createRestaurantChosen(Restaurant restaurant, String uid) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        DocumentReference writingUser = UserHelper.getUsersWhoChose(restaurant.getId()).document(uid);
        DocumentReference writingRestaurantName =UserHelper.getRestaurantChosen().document(restaurant.getId());

        User userToCreate = new User();
        userToCreate.setUid(uid);
        batch.set(writingUser,userToCreate);

        HashMap<String,String> restaurantName = new HashMap<>();
        restaurantName.put("name",restaurant.getName());
        batch.set(writingRestaurantName,restaurantName);
        return batch.commit();
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }
    public static Task<QuerySnapshot> getRestaurantsListLiked(String uid){
        return UserHelper.getRestaurantsLikedCollection(uid).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateUsername(String uid, String username) {
        return UserHelper.getUsersCollection().document(uid).update("username", username);
    }

    public static Task<Void> updateEntireRestaurant(String uid, String restaurant) {
        return UserHelper.getUsersCollection().document(uid).update("restaurantChose", restaurant);
    }


    // --- DELETE ---

    public static Task<Void> UnlikeRestaurant(String uid,String placeId) {
        return UserHelper.getRestaurantsLikedCollection(uid).document(placeId).delete();
    }
    public static Task<Void> deleteRestaurantInCollection(String uid) {
        return UserHelper.getRestaurantChosen().document(uid).delete();
    }
    public static Task<Void> unCheckRestaurantDestination(String placeId, String uid) {
       WriteBatch batch = FirebaseFirestore.getInstance().batch();
       DocumentReference userToDelete = UserHelper.getUsersWhoChose(placeId).document(uid);
       DocumentReference restaurantToDelete = UserHelper.getRestaurantChosen().document(uid);
       CollectionReference restaurantToCheck = UserHelper.getUsersWhoChose(placeId);

       batch.delete(userToDelete);

       return batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
           @Override
           public void onComplete(@NonNull Task<Void> task) {
               UserHelper.getUsersWhoChose(placeId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       if (task.getResult().size()==0) {
                           Log.d(TAG, "delete restaurant size 0");
                           UserHelper.deleteRestaurantInCollection(placeId);
                       }else {
                           Log.d(TAG, "delete restaurant taille plus grand que 0");
                       }
                   }
               });
           }
       });
       //return UserHelper.getUsersWhoChose(placeId).document(uid).delete();
    }

}
