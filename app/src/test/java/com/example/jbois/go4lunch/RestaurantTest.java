package com.example.jbois.go4lunch;

import com.example.jbois.go4lunch.Models.Restaurant;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RestaurantTest {
    private Restaurant mRestaurant;

    @Before
    public void setUp(){
        this.mRestaurant = new Restaurant();
    }

    @Test
    public void testGetName(){
        String expectedName = "mcdo";
        mRestaurant.setName(expectedName);
        String actualName  = mRestaurant.getName();
        Assert.assertEquals(expectedName,actualName);
    }
    @Test
    public void testGetId(){
        String expectedId = "fakeId01";
        mRestaurant.setId(expectedId);
        String actualId  = mRestaurant.getId();
        Assert.assertEquals(expectedId,actualId);
    }
    @Test
    public void testGetAddress(){
        String expected = "pays des bisounours";
        mRestaurant.setAdress(expected);
        String actual  = mRestaurant.getAdress();
        Assert.assertEquals(expected,actual);
    }
    @Test
    public void testGetURL(){
        String expected = "www.google.com";
        mRestaurant.setUrl(expected);
        String actual  = mRestaurant.getUrl();
        Assert.assertEquals(expected,actual);
    }
    @Test
    public void testGetPhoneNumber(){
        String expected = "0606060606";
        mRestaurant.setPhoneNumber(expected);
        String actual  = mRestaurant.getPhoneNumber();
        Assert.assertEquals(expected,actual);
    }    @Test
    public void testGetPhotoReference(){
        String expected = "fakeId01";
        mRestaurant.setPhotoReference(expected);
        String actual  = mRestaurant.getPhotoReference();
        Assert.assertEquals(expected,actual);
    }
    @Test
    public void testGetLat(){
        Double expected = 15.05;
        mRestaurant.setLat(expected);
        Double actual  = mRestaurant.getLat();
        Assert.assertEquals(expected,actual);
    }
    @Test
    public void testGetLong(){
        Double expected = 20.05;
        mRestaurant.setLng(expected);
        Double actual  = mRestaurant.getLng();
        Assert.assertEquals(expected,actual);
    }
    @Test
    public void testGetOpeningHours(){
        String expected = "open";
        mRestaurant.setOpeningHours(expected);
        String actual  = mRestaurant.getOpeningHours();
        Assert.assertEquals(expected,actual);
    }
    @Test
    public void testGetDistance(){
        int expected = 20;
        mRestaurant.setDistance(expected);
        int actual  = mRestaurant.getDistance();
        Assert.assertEquals(expected,actual);
    }
    @Test
    public void testGetFABChecked(){
        Boolean expected = true;
        mRestaurant.setFABChecked(expected);
        Boolean actual  = mRestaurant.getFABChecked();
        Assert.assertEquals(expected,actual);
    }
    @Test
    public void testGetRating(){
        Double expected = 10.5;
        mRestaurant.setRating(expected);
        Double actual  = mRestaurant.getRating();
        Assert.assertEquals(expected,actual);
    }
    @Test
    public void testGetClosingSoon(){
        Boolean expected = false;
        mRestaurant.setClosingSoon(expected);
        Boolean actual  = mRestaurant.getClosingSoon();
        Assert.assertEquals(expected,actual);
    }
    @Test
    public void testGetChooseOrNot(){
        Boolean expected = true;
        mRestaurant.setChoseOrNot(expected);
        Boolean actual  = mRestaurant.getChoseOrNot();
        Assert.assertEquals(expected,actual);
    }
    @Test
    public void testGetNumberOfWorkmates(){
        int expected = 12;
        mRestaurant.setNumberOfWorkmates(expected);
        int actual  = mRestaurant.getNumberOfWorkmates();
        Assert.assertEquals(expected,actual);
    }
}
