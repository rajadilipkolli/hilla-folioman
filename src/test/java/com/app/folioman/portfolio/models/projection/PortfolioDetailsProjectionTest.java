package com.app.folioman.portfolio.models.projection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void testGetSchemeName() {
        String expectedSchemeName = "Test Scheme";
        when(portfolioDetailsProjection.getSchemeName()).thenReturn(expectedSchemeName);

        String actualSchemeName = portfolioDetailsProjection.getSchemeName();

        assertEquals(expectedSchemeName, actualSchemeName);
    }

    @Test
    void testGetSchemeNameNull() {
        when(portfolioDetailsProjection.getSchemeName()).thenReturn(null);

        String actualSchemeName = portfolioDetailsProjection.getSchemeName();

        assertNull(actualSchemeName);
    }

    @Test
    void testGetSchemeNameEmpty() {
        String expectedSchemeName = "";
        when(portfolioDetailsProjection.getSchemeName()).thenReturn(expectedSchemeName);

        String actualSchemeName = portfolioDetailsProjection.getSchemeName();

        assertEquals(expectedSchemeName, actualSchemeName);
    }

    @Test
    void testGetFolioNumber() {
        String expectedFolioNumber = "12345";
        when(portfolioDetailsProjection.getFolioNumber()).thenReturn(expectedFolioNumber);

        String actualFolioNumber = portfolioDetailsProjection.getFolioNumber();

        assertEquals(expectedFolioNumber, actualFolioNumber);
    }

    @Test
    void testGetFolioNumberNull() {
        when(portfolioDetailsProjection.getFolioNumber()).thenReturn(null);

        String actualFolioNumber = portfolioDetailsProjection.getFolioNumber();

        assertNull(actualFolioNumber);
    }

    @Test
    void testGetFolioNumberEmpty() {
        String expectedFolioNumber = "";
        when(portfolioDetailsProjection.getFolioNumber()).thenReturn(expectedFolioNumber);

        String actualFolioNumber = portfolioDetailsProjection.getFolioNumber();

        assertEquals(expectedFolioNumber, actualFolioNumber);
    }

    @Test
    void testGetBalanceUnits() {
        Double expectedBalanceUnits = 100.50;
        when(portfolioDetailsProjection.getBalanceUnits()).thenReturn(expectedBalanceUnits);

        Double actualBalanceUnits = portfolioDetailsProjection.getBalanceUnits();

        assertEquals(expectedBalanceUnits, actualBalanceUnits);
    }

    @Test
    void testGetBalanceUnitsNull() {
        when(portfolioDetailsProjection.getBalanceUnits()).thenReturn(null);

        Double actualBalanceUnits = portfolioDetailsProjection.getBalanceUnits();

        assertNull(actualBalanceUnits);
    }

    @Test
    void testGetBalanceUnitsZero() {
        Double expectedBalanceUnits = 0.0;
        when(portfolioDetailsProjection.getBalanceUnits()).thenReturn(expectedBalanceUnits);

        Double actualBalanceUnits = portfolioDetailsProjection.getBalanceUnits();

        assertEquals(expectedBalanceUnits, actualBalanceUnits);
    }

    @Test
    void testGetBalanceUnitsNegative() {
        Double expectedBalanceUnits = -50.25;
        when(portfolioDetailsProjection.getBalanceUnits()).thenReturn(expectedBalanceUnits);

        Double actualBalanceUnits = portfolioDetailsProjection.getBalanceUnits();

        assertEquals(expectedBalanceUnits, actualBalanceUnits);
    }

    @Test
    void testGetSchemeId() {
        Long expectedSchemeId = 123L;
        when(portfolioDetailsProjection.getSchemeId()).thenReturn(expectedSchemeId);

        Long actualSchemeId = portfolioDetailsProjection.getSchemeId();

        assertEquals(expectedSchemeId, actualSchemeId);
    }

    @Test
    void testGetSchemeIdNull() {
        when(portfolioDetailsProjection.getSchemeId()).thenReturn(null);

        Long actualSchemeId = portfolioDetailsProjection.getSchemeId();

        assertNull(actualSchemeId);
    }

    @Test
    void testGetSchemeIdZero() {
        Long expectedSchemeId = 0L;
        when(portfolioDetailsProjection.getSchemeId()).thenReturn(expectedSchemeId);

        Long actualSchemeId = portfolioDetailsProjection.getSchemeId();

        assertEquals(expectedSchemeId, actualSchemeId);
    }

    @Test
    void testGetSchemeDetailId() {
        Long expectedSchemeDetailId = 456L;
        when(portfolioDetailsProjection.getSchemeDetailId()).thenReturn(expectedSchemeDetailId);

        Long actualSchemeDetailId = portfolioDetailsProjection.getSchemeDetailId();

        assertEquals(expectedSchemeDetailId, actualSchemeDetailId);
    }

    @Test
    void testGetSchemeDetailIdNull() {
        when(portfolioDetailsProjection.getSchemeDetailId()).thenReturn(null);

        Long actualSchemeDetailId = portfolioDetailsProjection.getSchemeDetailId();

        assertNull(actualSchemeDetailId);
    }

    @Test
    void testGetSchemeDetailIdZero() {
        Long expectedSchemeDetailId = 0L;
        when(portfolioDetailsProjection.getSchemeDetailId()).thenReturn(expectedSchemeDetailId);

        Long actualSchemeDetailId = portfolioDetailsProjection.getSchemeDetailId();

        assertEquals(expectedSchemeDetailId, actualSchemeDetailId);
    }
}
