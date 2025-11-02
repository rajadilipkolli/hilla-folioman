package com.app.folioman.mfschemes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FundDetailProjectionTest {

    @Mock
    private FundDetailProjection fundDetailProjection;

    @BeforeEach
    void setUp() {
        // Setup is handled by @Mock annotation
    }

    @Test
    void testGetSchemeNameReturnsExpectedValue() {
        String expectedSchemeName = "Test Scheme Name";
        when(fundDetailProjection.getSchemeName()).thenReturn(expectedSchemeName);

        String actualSchemeName = fundDetailProjection.getSchemeName();

        assertEquals(expectedSchemeName, actualSchemeName);
    }

    @Test
    void testGetSchemeNameReturnsNull() {
        when(fundDetailProjection.getSchemeName()).thenReturn(null);

        String actualSchemeName = fundDetailProjection.getSchemeName();

        assertNull(actualSchemeName);
    }

    @Test
    void testGetSchemeNameReturnsEmptyString() {
        when(fundDetailProjection.getSchemeName()).thenReturn("");

        String actualSchemeName = fundDetailProjection.getSchemeName();

        assertEquals("", actualSchemeName);
    }

    @Test
    void testGetAmfiCodeReturnsExpectedValue() {
        Long expectedAmfiCode = 123456L;
        when(fundDetailProjection.getAmfiCode()).thenReturn(expectedAmfiCode);

        Long actualAmfiCode = fundDetailProjection.getAmfiCode();

        assertEquals(expectedAmfiCode, actualAmfiCode);
    }

    @Test
    void testGetAmfiCodeReturnsNull() {
        when(fundDetailProjection.getAmfiCode()).thenReturn(null);

        Long actualAmfiCode = fundDetailProjection.getAmfiCode();

        assertNull(actualAmfiCode);
    }

    @Test
    void testGetAmfiCodeReturnsZero() {
        Long expectedAmfiCode = 0L;
        when(fundDetailProjection.getAmfiCode()).thenReturn(expectedAmfiCode);

        Long actualAmfiCode = fundDetailProjection.getAmfiCode();

        assertEquals(expectedAmfiCode, actualAmfiCode);
    }

    @Test
    void testGetAmcNameReturnsExpectedValue() {
        String expectedAmcName = "Test AMC Name";
        when(fundDetailProjection.getAmcName()).thenReturn(expectedAmcName);

        String actualAmcName = fundDetailProjection.getAmcName();

        assertEquals(expectedAmcName, actualAmcName);
    }

    @Test
    void testGetAmcNameReturnsNull() {
        when(fundDetailProjection.getAmcName()).thenReturn(null);

        String actualAmcName = fundDetailProjection.getAmcName();

        assertNull(actualAmcName);
    }

    @Test
    void testGetAmcNameReturnsEmptyString() {
        when(fundDetailProjection.getAmcName()).thenReturn("");

        String actualAmcName = fundDetailProjection.getAmcName();

        assertEquals("", actualAmcName);
    }
}
