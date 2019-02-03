package com.example.jbois.go4lunch;

import android.app.Activity;
import android.content.Context;

import com.example.jbois.go4lunch.Controllers.Activities.LunchActivity;
import com.example.jbois.go4lunch.Controllers.Activities.MainActivity;
import com.example.jbois.go4lunch.Utils.ApplicationContext;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class ExampleUnitTest {
    Activity lunchActivity;
    Context context = ApplicationContext.getContext();

    @Before
    public void setUp() throws Exception {
        FirebaseApp.initializeApp(context);

        lunchActivity = Robolectric.buildActivity(LunchActivity.class)
                .create()
                .start()
                .resume().visible().get();
    }


    @Test
    public void shouldNotBeNull() throws Exception {
        assertNotNull(lunchActivity);
    }


}