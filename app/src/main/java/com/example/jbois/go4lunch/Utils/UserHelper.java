package com.example.jbois.go4lunch.Utils;

import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UserHelper {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_RESTAURANT_LIKED = "restaurantsLiked";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_USERS);
    }

    public static CollectionReference getRestaurantsLikedCollection(String uid){
        return UserHelper.getUsersCollection().document(uid).collection(COLLECTION_RESTAURANT_LIKED);
    }

    // --- CREATE ---

    public static Task<Void> createUser(String uid, String username, String urlPicture) {
        User userToCreate = new User(uid, username, urlPicture);
        return UserHelper.getUsersCollection().document(uid).set(userToCreate);
    }
    public static Task<Void> createRestaurantLiked(String placeId, String uid,Restaurant restaurant) {
        return UserHelper.getRestaurantsLikedCollection(uid).document(placeId).set(restaurant);
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

    public static Task<Void> updateRestaurantChose(String uid, String restaurantChose) {
        return UserHelper.getUsersCollection().document(uid).update("restaurantChoseName", restaurantChose);
    }

    public static Task<Void> updateEntireRestaurant(String uid, String restaurant) {
        return UserHelper.getUsersCollection().document(uid).update("restaurant", restaurant);
    }


    // --- DELETE ---

    public static Task<Void> deleteUser(String uid) {
        return UserHelper.getUsersCollection().document(uid).delete();
    }
    public static Task<Void> UnlikeRestaurant(String uid,String placeId) {
        return UserHelper.getRestaurantsLikedCollection(uid).document(placeId).delete();
    }

}
