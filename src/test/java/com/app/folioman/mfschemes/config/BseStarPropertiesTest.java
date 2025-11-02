package com.app.folioman.mfschemes.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BseStarPropertiesTest {

    private BseStarProperties bseStarProperties;

    @BeforeEach
    void setUp() {
        bseStarProperties = new BseStarProperties();
    }

    @Test
    void getScheme_shouldReturnNullInitially() {
        assertNull(bseStarProperties.getScheme());
    }

    @Test
    void getScheme_shouldReturnSetValue() {
        SchemeProperties schemeProperties = new SchemeProperties();
        bseStarProperties.setScheme(schemeProperties);

        assertEquals(schemeProperties, bseStarProperties.getScheme());
    }

    @Test
    void setScheme_shouldSetNullValue() {
        bseStarProperties.setScheme(null);

        assertNull(bseStarProperties.getScheme());
    }

    @Test
    void setScheme_shouldSetValidSchemeProperties() {
        SchemeProperties schemeProperties = new SchemeProperties();

        bseStarProperties.setScheme(schemeProperties);

        assertEquals(schemeProperties, bseStarProperties.getScheme());
    }

    @Test
    void setScheme_shouldReplaceExistingValue() {
        SchemeProperties firstScheme = new SchemeProperties();
        SchemeProperties secondScheme = new SchemeProperties();

        bseStarProperties.setScheme(firstScheme);
        bseStarProperties.setScheme(secondScheme);

        assertEquals(secondScheme, bseStarProperties.getScheme());
        assertNotEquals(firstScheme, bseStarProperties.getScheme());
    }
}
