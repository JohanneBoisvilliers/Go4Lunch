package com.example.jbois.go4lunch;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import com.example.jbois.go4lunch.Controllers.Activities.LunchActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LunchActivityUITest{ //extends ActivityInstrumentationTestCase2<LunchActivity>

    //public LunchActivityUITest() {
    //    super(LunchActivity.class);
    //}
    ////Get LunchActivity
    //@Before
    //@Rule
    //public void setUp() throws Exception {
    //    super.setUp();
    //    getActivity();
    //}
//
    //@Test
    //public void testContainsIntialViews() {
    //    onView(withId(R.id.bottom_navigation_view)).check(matches(isDisplayed()));
    //    onView(withId(R.id.activity_main_viewpager)).check(matches(isDisplayed()));
    //    onView(withId(R.id.activity_main_toolbar)).check(matches(isDisplayed()));
    //}
}
