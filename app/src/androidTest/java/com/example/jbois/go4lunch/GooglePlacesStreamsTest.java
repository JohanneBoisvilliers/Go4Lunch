package com.example.jbois.go4lunch;

import com.example.jbois.go4lunch.Utils.GooglePlaceServices;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class GooglePlacesStreamsTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    GooglePlaceServices mGooglePlaceServices;

    @Test
    public void emptyTest() {
    }
}
