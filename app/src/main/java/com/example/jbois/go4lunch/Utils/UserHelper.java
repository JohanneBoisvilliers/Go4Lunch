package com.example.jbois.go4lunch.Utils;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

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

    public static Task<Void> createRestaurantChosen(String placeId, String uid) {
        User userToCreate = new User();
        userToCreate.setUid(uid);
        return UserHelper.getUsersWhoChose(placeId).document(uid).set(userToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }
    public static Task<QuerySnapshot> getRestaurantsListLiked(String uid){
        return UserHelper.getRestaurantsLikedCollection(uid).get();
    }
    public static Task<QuerySnapshot> getRestaurantListChosen(){
        return UserHelper.getRestaurantChosen().get();
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
    public static Task<Void> unCheckRestaurantDestination(String placeId, String uid) {
        return UserHelper.getUsersWhoChose(placeId).document(uid).delete();
    }

}
