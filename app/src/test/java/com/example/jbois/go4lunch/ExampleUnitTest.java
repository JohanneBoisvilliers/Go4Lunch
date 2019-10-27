package com.example.jbois.go4lunch;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Context;

import com.example.jbois.go4lunch.Controllers.Activities.LunchActivity;
import com.example.jbois.go4lunch.Controllers.Activities.MainActivity;
import com.example.jbois.go4lunch.Utils.ApplicationContext;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.Resource;
import com.firebase.ui.auth.viewmodel.idp.SocialProviderResponseHandler;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ExampleUnitTest {
    Activity lunchActivity;
    Context context = ApplicationContext.getContext();
   @Mock
   FirebaseAuth mMockAuth;
   //@Mock
   //FirebaseUser mUser = "gxV6l27p9CXthiCVpMenVR1RJHh1" ;
   //@Mock
   //Observer<Resource<IdpResponse>> mResultObserver;

    private SocialProviderResponseHandler mHandler;

    @Before
    public void setUp() throws Exception {
        //MockitoAnnotations.initMocks(this);
        FirebaseApp.initializeApp(context);
        //mMockAuth  = FirebaseAuth.getInstance();
        //when(mMockAuth.getCurrentUser()).thenReturn(mUser);
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