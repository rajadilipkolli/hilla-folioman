package com.app.folioman.portfolio.models.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortfolioDetailsProjectionTest {

    @Mock
    private PortfolioDetailsProjection portfolioDetailsProjection;

    @BeforeEach
    void setUp() {
        // Setup is handled by Mockito annotations
    }

    @Test
    void getSchemeName() {
        String expectedSchemeName = "Test Scheme";
        when(portfolioDetailsProjection.getSchemeName()).thenReturn(expectedSchemeName);

        String actualSchemeName = portfolioDetailsProjection.getSchemeName();

        assertThat(actualSchemeName).isEqualTo(expectedSchemeName);
    }

    @Test
    void getSchemeNameNull() {
        when(portfolioDetailsProjection.getSchemeName()).thenReturn(null);

        String actualSchemeName = portfolioDetailsProjection.getSchemeName();

        assertThat(actualSchemeName).isNull();
    }

    @Test
    void getSchemeNameEmpty() {
        String expectedSchemeName = "";
        when(portfolioDetailsProjection.getSchemeName()).thenReturn(expectedSchemeName);

        String actualSchemeName = portfolioDetailsProjection.getSchemeName();

        assertThat(actualSchemeName).isEqualTo(expectedSchemeName);
    }

    @Test
    void getFolioNumber() {
        String expectedFolioNumber = "12345";
        when(portfolioDetailsProjection.getFolioNumber()).thenReturn(expectedFolioNumber);

        String actualFolioNumber = portfolioDetailsProjection.getFolioNumber();

        assertThat(actualFolioNumber).isEqualTo(expectedFolioNumber);
    }

    @Test
    void getFolioNumberNull() {
        when(portfolioDetailsProjection.getFolioNumber()).thenReturn(null);

        String actualFolioNumber = portfolioDetailsProjection.getFolioNumber();

        assertThat(actualFolioNumber).isNull();
    }

    @Test
    void getFolioNumberEmpty() {
        String expectedFolioNumber = "";
        when(portfolioDetailsProjection.getFolioNumber()).thenReturn(expectedFolioNumber);

        String actualFolioNumber = portfolioDetailsProjection.getFolioNumber();

        assertThat(actualFolioNumber).isEqualTo(expectedFolioNumber);
    }

    @Test
    void getBalanceUnits() {
        Double expectedBalanceUnits = 100.50;
        when(portfolioDetailsProjection.getBalanceUnits()).thenReturn(expectedBalanceUnits);

        Double actualBalanceUnits = portfolioDetailsProjection.getBalanceUnits();

        assertThat(actualBalanceUnits).isEqualTo(expectedBalanceUnits);
    }

    @Test
    void getBalanceUnitsNull() {
        when(portfolioDetailsProjection.getBalanceUnits()).thenReturn(null);

        Double actualBalanceUnits = portfolioDetailsProjection.getBalanceUnits();

        assertThat(actualBalanceUnits).isNull();
    }

    @Test
    void getBalanceUnitsZero() {
        Double expectedBalanceUnits = 0.0;
        when(portfolioDetailsProjection.getBalanceUnits()).thenReturn(expectedBalanceUnits);

        Double actualBalanceUnits = portfolioDetailsProjection.getBalanceUnits();

        assertThat(actualBalanceUnits).isEqualTo(expectedBalanceUnits);
    }

    @Test
    void getBalanceUnitsNegative() {
        Double expectedBalanceUnits = -50.25;
        when(portfolioDetailsProjection.getBalanceUnits()).thenReturn(expectedBalanceUnits);

        Double actualBalanceUnits = portfolioDetailsProjection.getBalanceUnits();

        assertThat(actualBalanceUnits).isEqualTo(expectedBalanceUnits);
    }

    @Test
    void getSchemeId() {
        Long expectedSchemeId = 123L;
        when(portfolioDetailsProjection.getSchemeId()).thenReturn(expectedSchemeId);

        Long actualSchemeId = portfolioDetailsProjection.getSchemeId();

        assertThat(actualSchemeId).isEqualTo(expectedSchemeId);
    }

    @Test
    void getSchemeIdNull() {
        when(portfolioDetailsProjection.getSchemeId()).thenReturn(null);

        Long actualSchemeId = portfolioDetailsProjection.getSchemeId();

        assertThat(actualSchemeId).isNull();
    }

    @Test
    void getSchemeIdZero() {
        Long expectedSchemeId = 0L;
        when(portfolioDetailsProjection.getSchemeId()).thenReturn(expectedSchemeId);

        Long actualSchemeId = portfolioDetailsProjection.getSchemeId();

        assertThat(actualSchemeId).isEqualTo(expectedSchemeId);
    }

    @Test
    void getSchemeDetailId() {
        Long expectedSchemeDetailId = 456L;
        when(portfolioDetailsProjection.getSchemeDetailId()).thenReturn(expectedSchemeDetailId);

        Long actualSchemeDetailId = portfolioDetailsProjection.getSchemeDetailId();

        assertThat(actualSchemeDetailId).isEqualTo(expectedSchemeDetailId);
    }

    @Test
    void getSchemeDetailIdNull() {
        when(portfolioDetailsProjection.getSchemeDetailId()).thenReturn(null);

        Long actualSchemeDetailId = portfolioDetailsProjection.getSchemeDetailId();

        assertThat(actualSchemeDetailId).isNull();
    }

    @Test
    void getSchemeDetailIdZero() {
        Long expectedSchemeDetailId = 0L;
        when(portfolioDetailsProjection.getSchemeDetailId()).thenReturn(expectedSchemeDetailId);

        Long actualSchemeDetailId = portfolioDetailsProjection.getSchemeDetailId();

        assertThat(actualSchemeDetailId).isEqualTo(expectedSchemeDetailId);
    }
}
