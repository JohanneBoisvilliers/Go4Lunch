package com.example.jbois.go4lunch;

import com.example.jbois.go4lunch.Models.User;
import com.google.firebase.database.ThrowOnExtraProperties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserTest {
    private User mUser;

    @Before
    public void setUp() throws Exception{
        this.mUser = new User();
    }

    @Test
    public void testGetId(){
        String expected = "unTestdId";
        mUser.setUid(expected);
        String actual = mUser.getUid();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetUsername(){
        String expectedUsername="Bobby";
        mUser.setUsername(expectedUsername);
        String actualUsername = mUser.getUsername();
        Assert.assertEquals(expectedUsername,actualUsername);
    }

    @Test
    public void testGetUrlPicture(){
        String expectedUrl = "google.com";
        mUser.setUrlPicture(expectedUrl);
        String actuelUrl = mUser.getUrlPicture();
        Assert.assertEquals(expectedUrl,actuelUrl);
    }

    @Test
    public void testGetRestaurantChose(){
        String expectedRestaurant ="Mcdo";
        mUser.setRestaurantChose(expectedRestaurant);
        String actualRestaurant = mUser.getRestaurantChose();
        Assert.assertEquals(expectedRestaurant,actualRestaurant);
    }
}
