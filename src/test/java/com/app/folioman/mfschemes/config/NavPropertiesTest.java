package com.app.folioman.mfschemes.config;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(navProperties.getAmfi()).isNull();
    }

    @Test
    void setAmfi_shouldSetAmfiProperties() {
        navProperties.setAmfi(mockAmfiProperties);

        assertThat(navProperties.getAmfi()).isEqualTo(mockAmfiProperties);
    }

    @Test
    void setAmfi_shouldAcceptNull() {
        navProperties.setAmfi(mockAmfiProperties);
        navProperties.setAmfi(null);

        assertThat(navProperties.getAmfi()).isNull();
    }

    @Test
    void getMfApi_shouldReturnNull_whenNotSet() {
        assertThat(navProperties.getMfApi()).isNull();
    }

    @Test
    void setMfApi_shouldSetMfApiProperties() {
        navProperties.setMfApi(mockMfApiProperties);

        assertThat(navProperties.getMfApi()).isEqualTo(mockMfApiProperties);
    }

    @Test
    void setMfApi_shouldAcceptNull() {
        navProperties.setMfApi(mockMfApiProperties);
        navProperties.setMfApi(null);

        assertThat(navProperties.getMfApi()).isNull();
    }

    @Test
    void getAmfi_shouldReturnSetValue_afterSettingAmfiProperties() {
        navProperties.setAmfi(mockAmfiProperties);

        assertThat(navProperties.getAmfi()).isSameAs(mockAmfiProperties);
    }

    @Test
    void getMfApi_shouldReturnSetValue_afterSettingMfApiProperties() {
        navProperties.setMfApi(mockMfApiProperties);

        assertThat(navProperties.getMfApi()).isSameAs(mockMfApiProperties);
    }
}
