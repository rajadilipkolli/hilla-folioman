package com.app.folioman.mfschemes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MFSchemeProjectionTest {

    @Mock
    private MFSchemeProjection mfSchemeProjection;

    @Test
    void testGetAmfiCode() {
        Long expectedAmfiCode = 123456L;
        when(mfSchemeProjection.getAmfiCode()).thenReturn(expectedAmfiCode);

        Long actualAmfiCode = mfSchemeProjection.getAmfiCode();

        assertEquals(expectedAmfiCode, actualAmfiCode);
    }

    @Test
    void testGetAmfiCodeReturnsNull() {
        when(mfSchemeProjection.getAmfiCode()).thenReturn(null);

        Long actualAmfiCode = mfSchemeProjection.getAmfiCode();

        assertNull(actualAmfiCode);
    }

    @Test
    void testGetIsin() {
        String expectedIsin = "INF846K01EW9";
        when(mfSchemeProjection.getIsin()).thenReturn(expectedIsin);

        String actualIsin = mfSchemeProjection.getIsin();

        assertEquals(expectedIsin, actualIsin);
    }

    @Test
    void testGetIsinReturnsNull() {
        when(mfSchemeProjection.getIsin()).thenReturn(null);

        String actualIsin = mfSchemeProjection.getIsin();

        assertNull(actualIsin);
    }

    @Test
    void testGetIsinReturnsEmptyString() {
        String expectedIsin = "";
        when(mfSchemeProjection.getIsin()).thenReturn(expectedIsin);

        String actualIsin = mfSchemeProjection.getIsin();

        assertEquals(expectedIsin, actualIsin);
    }
}
