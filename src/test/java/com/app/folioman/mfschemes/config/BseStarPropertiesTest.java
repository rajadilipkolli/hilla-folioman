package com.app.folioman.mfschemes.config;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(bseStarProperties.getScheme()).isNull();
    }

    @Test
    void getScheme_shouldReturnSetValue() {
        SchemeProperties schemeProperties = new SchemeProperties();
        bseStarProperties.setScheme(schemeProperties);

        assertThat(bseStarProperties.getScheme()).isEqualTo(schemeProperties);
    }

    @Test
    void setScheme_shouldSetNullValue() {
        bseStarProperties.setScheme(null);

        assertThat(bseStarProperties.getScheme()).isNull();
    }

    @Test
    void setScheme_shouldSetValidSchemeProperties() {
        SchemeProperties schemeProperties = new SchemeProperties();

        bseStarProperties.setScheme(schemeProperties);

        assertThat(bseStarProperties.getScheme()).isEqualTo(schemeProperties);
    }

    @Test
    void setScheme_shouldReplaceExistingValue() {
        SchemeProperties firstScheme = new SchemeProperties();
        SchemeProperties secondScheme = new SchemeProperties();

        bseStarProperties.setScheme(firstScheme);
        bseStarProperties.setScheme(secondScheme);

        assertThat(bseStarProperties.getScheme()).isEqualTo(secondScheme);
        assertThat(bseStarProperties.getScheme()).isNotEqualTo(firstScheme);
    }
}
