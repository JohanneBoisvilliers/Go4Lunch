package com.example.jbois.go4lunch;

import android.util.Log;


import com.example.jbois.go4lunch.Models.Restaurant;
import com.example.jbois.go4lunch.Models.RestaurantDetailsJson;
import com.example.jbois.go4lunch.Models.RestaurantListJson;
import com.example.jbois.go4lunch.Utils.GooglePlaceServices;
import com.example.jbois.go4lunch.Utils.GooglePlacesStreams;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;


public class GooglePlacesStreamsTest {
    List<Restaurant> restaurantList = new ArrayList<>();
    List<RestaurantDetailsJson> restaurantDetailJsonList = new ArrayList<>();
    Restaurant mRestaurant;
    RestaurantListJson restaurantListJson;
    RestaurantDetailsJson tesorioDitaliaDetails;
    RestaurantDetailsJson camouflageDetails;
    @Mock
    GooglePlacesStreams mGooglePlacesStreams = new GooglePlacesStreams();
    @Before
    public void setUp() throws IOException {
        mRestaurant = new Restaurant();
        Gson gson = new Gson();
        restaurantListJson = gson
                .fromJson(mGooglePlacesStreams.serializeJson(R.raw.listjsontest),
                        new TypeToken<RestaurantListJson>() {
                        }
                                .getType());
        tesorioDitaliaDetails = gson
                .fromJson(mGooglePlacesStreams.serializeJson(R.raw.tesoroditalia),
                        new TypeToken<RestaurantDetailsJson>() {
                        }
                                .getType());
        camouflageDetails = gson
                .fromJson(mGooglePlacesStreams.serializeJson(R.raw.camouflageveggie),
                        new TypeToken<RestaurantDetailsJson>() {
                        }
                                .getType());
        restaurantDetailJsonList.add(tesorioDitaliaDetails);
        restaurantDetailJsonList.add(camouflageDetails);
    }

    @Test
    public void extrudePlaceInfosTest(){
        GooglePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);

        assertEquals(restaurantList.size(),2);
        assertEquals(restaurantList.get(0).getId(),"restaurantId1");
        assertEquals(restaurantList.get(1).getId(),"restaurantId2");
    }
    @Test
    public void checkOpeningHoursTest(){
        String openingHoursCamouflage = GooglePlacesStreams.checkOpeningHours(camouflageDetails,mRestaurant);
        String openinghoursTesorio = GooglePlacesStreams.checkOpeningHours(tesorioDitaliaDetails,mRestaurant);
        assertEquals(openingHoursCamouflage,"Closed");
        assertEquals(openinghoursTesorio,"Open 24/7");
    }

    @Test
    public void convertStringIntoDateTimeTest(){
        DateTime testDate = GooglePlacesStreams.convertHoursInDateTime("1254");
        assertNotNull(testDate);
        assertEquals(12,testDate.getHourOfDay());
        assertEquals(54,testDate.getMinuteOfHour());
    }

    @Test
    public void convertDateIntoStringTest(){
        DateTime dateTime = new DateTime(2019,12,24,1,50);
        String dateConvert = GooglePlacesStreams.convertHoursInString(dateTime);
        assertEquals("1.50AM",dateConvert);
        Log.d("testdebug", "convertDateIntoStringTest: "+dateTime);
    }

    @Test
    public void extrudeAddressTest(){
        String testAddress = GooglePlacesStreams.extrudeAdressFromJson(tesorioDitaliaDetails);
        assertEquals("41 Rue de Paradis",testAddress);
    }

    @Test
    public void compareAndSetListTest(){
        GooglePlacesStreams.extrudePlaceInfo(restaurantListJson,restaurantList);
        GooglePlacesStreams.compareAndSetList(restaurantList,restaurantDetailJsonList);
        assertEquals("Tesoro d'Italia",restaurantList.get(0).getName());
        assertEquals("41 Rue de Paradis",restaurantList.get(0).getAdress());
        assertEquals("http://tesoroditalia.com/",restaurantList.get(0).getUrl());
        assertEquals("01 53 34 00 64",restaurantList.get(0).getPhoneNumber());
        assertEquals("Open 24/7",restaurantList.get(0).getOpeningHours());

        assertEquals("Camouflage vegie",restaurantList.get(1).getName());
        assertEquals("51 Rue de Paradis",restaurantList.get(1).getAdress());
        assertNull(restaurantList.get(1).getUrl());
        assertNull(restaurantList.get(1).getPhoneNumber());
        assertEquals("Closed",restaurantList.get(1).getOpeningHours());

    }
}
