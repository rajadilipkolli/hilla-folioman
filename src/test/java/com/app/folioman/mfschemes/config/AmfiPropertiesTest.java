package com.app.folioman.mfschemes.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AmfiPropertiesTest {

    private AmfiProperties amfiProperties;

    @BeforeEach
    void setUp() {
        amfiProperties = new AmfiProperties();
    }

    @Test
    void getDataUrlInitiallyNull() {
        assertThat(amfiProperties.getDataUrl()).isNull();
    }

    @Test
    void setAndGetDataUrl() {
        String expectedUrl = "https://www.amfiindia.com/data";
        amfiProperties.setDataUrl(expectedUrl);
        assertThat(amfiProperties.getDataUrl()).isEqualTo(expectedUrl);
    }

    @Test
    void setDataUrlWithNull() {
        amfiProperties.setDataUrl("https://example.com");
        amfiProperties.setDataUrl(null);
        assertThat(amfiProperties.getDataUrl()).isNull();
    }

    @Test
    void setDataUrlWithEmptyString() {
        String emptyUrl = "";
        amfiProperties.setDataUrl(emptyUrl);
        assertThat(amfiProperties.getDataUrl()).isEqualTo(emptyUrl);
    }

    @Test
    void getSchemeInitiallyNull() {
        assertThat(amfiProperties.getScheme()).isNull();
    }

    @Test
    void setAndGetScheme() {
        SchemeProperties expectedScheme = new SchemeProperties();
        amfiProperties.setScheme(expectedScheme);
        assertThat(amfiProperties.getScheme()).isEqualTo(expectedScheme);
    }

    @Test
    void setSchemeWithNull() {
        amfiProperties.setScheme(new SchemeProperties());
        amfiProperties.setScheme(null);
        assertThat(amfiProperties.getScheme()).isNull();
    }

    @Test
    void multipleSettersAndGetters() {
        String testUrl = "https://test.amfi.com";
        SchemeProperties testScheme = new SchemeProperties();

        amfiProperties.setDataUrl(testUrl);
        amfiProperties.setScheme(testScheme);

        assertThat(amfiProperties.getDataUrl()).isEqualTo(testUrl);
        assertThat(amfiProperties.getScheme()).isEqualTo(testScheme);
    }
}
