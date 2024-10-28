package com.app.folioman.mfschemes.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ApplicationPropertiesIT extends AbstractIntegrationTest {

    @Test
    void whenValidPropertiesProvided_thenBindingSucceeds() {
        assertThat(properties.getAmfi()).isNotNull();
        assertThat(properties.getBseStar()).isNotNull();
        assertThat(properties.getNav()).isNotNull();
    }
}
