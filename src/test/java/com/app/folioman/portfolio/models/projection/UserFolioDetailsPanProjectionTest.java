package com.app.folioman.portfolio.models.projection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserFolioDetailsPanProjectionTest {

    @Test
    void testGetPanMethodExists() {
        UserFolioDetailsPanProjection projection = Mockito.mock(UserFolioDetailsPanProjection.class);

        assertNotNull(projection);
    }

    @Test
    void testGetPanReturnsString() {
        UserFolioDetailsPanProjection projection = Mockito.mock(UserFolioDetailsPanProjection.class);
        String expectedPan = "ABCDE1234F";

        when(projection.getPan()).thenReturn(expectedPan);

        String actualPan = projection.getPan();

        assertEquals(expectedPan, actualPan);
    }

    @Test
    void testGetPanReturnsNull() {
        UserFolioDetailsPanProjection projection = Mockito.mock(UserFolioDetailsPanProjection.class);

        when(projection.getPan()).thenReturn(null);

        String actualPan = projection.getPan();

        assertEquals(null, actualPan);
    }

    @Test
    void testGetPanReturnsEmptyString() {
        UserFolioDetailsPanProjection projection = Mockito.mock(UserFolioDetailsPanProjection.class);

        when(projection.getPan()).thenReturn("");

        String actualPan = projection.getPan();

        assertEquals("", actualPan);
    }
}
