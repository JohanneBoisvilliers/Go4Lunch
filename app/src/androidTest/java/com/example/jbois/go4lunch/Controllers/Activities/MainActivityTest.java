package com.example.jbois.go4lunch.Controllers.Activities;


import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.android.dx.command.Main;
import com.example.jbois.go4lunch.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;


@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void LoginUITest() {
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.email_button), withText("Sign in with email"),
                        withParent(withId(R.id.btn_holder)),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.email),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("test.espresso@mail.com"));

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.button_next), withText("Next"), isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.password),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("testespresso"));

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.button_done), withText("SIGN IN"), isDisplayed()));
        appCompatButton5.perform(click());
    }
}
