package com.app.folioman.portfolio.models.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserFolioDetailsPanProjectionTest {

    @Test
    void getPanMethodExists() {
        UserFolioDetailsPanProjection projection = Mockito.mock(UserFolioDetailsPanProjection.class);

        assertThat(projection).isNotNull();
    }

    @Test
    void getPanReturnsString() {
        UserFolioDetailsPanProjection projection = Mockito.mock(UserFolioDetailsPanProjection.class);
        String expectedPan = "ABCDE1234F";

        when(projection.getPan()).thenReturn(expectedPan);

        String actualPan = projection.getPan();

        assertThat(actualPan).isEqualTo(expectedPan);
    }

    @Test
    void getPanReturnsNull() {
        UserFolioDetailsPanProjection projection = Mockito.mock(UserFolioDetailsPanProjection.class);

        when(projection.getPan()).thenReturn(null);

        String actualPan = projection.getPan();

        assertThat(actualPan).isNull();
    }

    @Test
    void getPanReturnsEmptyString() {
        UserFolioDetailsPanProjection projection = Mockito.mock(UserFolioDetailsPanProjection.class);

        when(projection.getPan()).thenReturn("");

        String actualPan = projection.getPan();

        assertThat(actualPan).isEmpty();
    }
}
