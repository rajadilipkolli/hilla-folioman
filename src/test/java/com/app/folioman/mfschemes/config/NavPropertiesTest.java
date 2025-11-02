package com.app.folioman.mfschemes.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NavPropertiesTest {

    @Mock
    private AmfiProperties mockAmfiProperties;

    @Mock
    private MfApiProperties mockMfApiProperties;

    private NavProperties navProperties;

    @BeforeEach
    void setUp() {
        navProperties = new NavProperties();
    }

    @Test
    void getAmfi_shouldReturnNull_whenNotSet() {
        assertNull(navProperties.getAmfi());
    }

    @Test
    void setAmfi_shouldSetAmfiProperties() {
        navProperties.setAmfi(mockAmfiProperties);

        assertEquals(mockAmfiProperties, navProperties.getAmfi());
    }

    @Test
    void setAmfi_shouldAcceptNull() {
        navProperties.setAmfi(mockAmfiProperties);
        navProperties.setAmfi(null);

        assertNull(navProperties.getAmfi());
    }

    @Test
    void getMfApi_shouldReturnNull_whenNotSet() {
        assertNull(navProperties.getMfApi());
    }

    @Test
    void setMfApi_shouldSetMfApiProperties() {
        navProperties.setMfApi(mockMfApiProperties);

        assertEquals(mockMfApiProperties, navProperties.getMfApi());
    }

    @Test
    void setMfApi_shouldAcceptNull() {
        navProperties.setMfApi(mockMfApiProperties);
        navProperties.setMfApi(null);

        assertNull(navProperties.getMfApi());
    }

    @Test
    void getAmfi_shouldReturnSetValue_afterSettingAmfiProperties() {
        navProperties.setAmfi(mockAmfiProperties);

        assertSame(mockAmfiProperties, navProperties.getAmfi());
    }

    @Test
    void getMfApi_shouldReturnSetValue_afterSettingMfApiProperties() {
        navProperties.setMfApi(mockMfApiProperties);

        assertSame(mockMfApiProperties, navProperties.getMfApi());
    }
}
