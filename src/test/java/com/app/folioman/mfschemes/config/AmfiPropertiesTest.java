package com.app.folioman.mfschemes.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AmfiPropertiesTest {

    private AmfiProperties amfiProperties;

    @BeforeEach
    void setUp() {
        amfiProperties = new AmfiProperties();
    }

    @Test
    void testGetDataUrlInitiallyNull() {
        assertNull(amfiProperties.getDataUrl());
    }

    @Test
    void testSetAndGetDataUrl() {
        String expectedUrl = "https://www.amfiindia.com/data";
        amfiProperties.setDataUrl(expectedUrl);
        assertEquals(expectedUrl, amfiProperties.getDataUrl());
    }

    @Test
    void testSetDataUrlWithNull() {
        amfiProperties.setDataUrl("https://example.com");
        amfiProperties.setDataUrl(null);
        assertNull(amfiProperties.getDataUrl());
    }

    @Test
    void testSetDataUrlWithEmptyString() {
        String emptyUrl = "";
        amfiProperties.setDataUrl(emptyUrl);
        assertEquals(emptyUrl, amfiProperties.getDataUrl());
    }

    @Test
    void testGetSchemeInitiallyNull() {
        assertNull(amfiProperties.getScheme());
    }

    @Test
    void testSetAndGetScheme() {
        SchemeProperties expectedScheme = new SchemeProperties();
        amfiProperties.setScheme(expectedScheme);
        assertEquals(expectedScheme, amfiProperties.getScheme());
    }

    @Test
    void testSetSchemeWithNull() {
        amfiProperties.setScheme(new SchemeProperties());
        amfiProperties.setScheme(null);
        assertNull(amfiProperties.getScheme());
    }

    @Test
    void testMultipleSettersAndGetters() {
        String testUrl = "https://test.amfi.com";
        SchemeProperties testScheme = new SchemeProperties();

        amfiProperties.setDataUrl(testUrl);
        amfiProperties.setScheme(testScheme);

        assertEquals(testUrl, amfiProperties.getDataUrl());
        assertEquals(testScheme, amfiProperties.getScheme());
    }
}
