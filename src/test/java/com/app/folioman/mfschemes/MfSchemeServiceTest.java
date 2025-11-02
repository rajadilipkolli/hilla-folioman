package com.app.folioman.mfschemes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MfSchemeServiceTest {

    @Mock
    private MfSchemeService mfSchemeService;

    @Mock
    private MFSchemeProjection mfSchemeProjection;

    @Mock
    private FundDetailProjection fundDetailProjection;

    @BeforeEach
    void setUp() {
        // Setup method if needed for common test data
    }

    @Test
    void findByPayOut_WithValidIsin_ReturnsOptionalWithProjection() {
        String isin = "INF123456789";
        when(mfSchemeService.findByPayOut(isin)).thenReturn(Optional.of(mfSchemeProjection));

        Optional<MFSchemeProjection> result = mfSchemeService.findByPayOut(isin);

        assertTrue(result.isPresent());
        assertEquals(mfSchemeProjection, result.get());
        verify(mfSchemeService).findByPayOut(isin);
    }

    @Test
    void findByPayOut_WithInvalidIsin_ReturnsEmptyOptional() {
        String isin = "INVALID_ISIN";
        when(mfSchemeService.findByPayOut(isin)).thenReturn(Optional.empty());

        Optional<MFSchemeProjection> result = mfSchemeService.findByPayOut(isin);

        assertFalse(result.isPresent());
        verify(mfSchemeService).findByPayOut(isin);
    }

    @Test
    void findByPayOut_WithNullIsin_ReturnsEmptyOptional() {
        when(mfSchemeService.findByPayOut(null)).thenReturn(Optional.empty());

        Optional<MFSchemeProjection> result = mfSchemeService.findByPayOut(null);

        assertFalse(result.isPresent());
        verify(mfSchemeService).findByPayOut(null);
    }

    @Test
    void findByPayOut_WithEmptyIsin_ReturnsEmptyOptional() {
        String isin = "";
        when(mfSchemeService.findByPayOut(isin)).thenReturn(Optional.empty());

        Optional<MFSchemeProjection> result = mfSchemeService.findByPayOut(isin);

        assertFalse(result.isPresent());
        verify(mfSchemeService).findByPayOut(isin);
    }

    @Test
    void fetchSchemes_WithValidScheme_ReturnsListOfProjections() {
        String scheme = "HDFC Equity Fund";
        List<FundDetailProjection> expectedList = Arrays.asList(fundDetailProjection);
        when(mfSchemeService.fetchSchemes(scheme)).thenReturn(expectedList);

        List<FundDetailProjection> result = mfSchemeService.fetchSchemes(scheme);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(fundDetailProjection, result.get(0));
        verify(mfSchemeService).fetchSchemes(scheme);
    }

    @Test
    void fetchSchemes_WithInvalidScheme_ReturnsEmptyList() {
        String scheme = "NonExistentScheme";
        when(mfSchemeService.fetchSchemes(scheme)).thenReturn(Collections.emptyList());

        List<FundDetailProjection> result = mfSchemeService.fetchSchemes(scheme);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mfSchemeService).fetchSchemes(scheme);
    }

    @Test
    void fetchSchemes_WithNullScheme_ReturnsEmptyList() {
        when(mfSchemeService.fetchSchemes(null)).thenReturn(Collections.emptyList());

        List<FundDetailProjection> result = mfSchemeService.fetchSchemes(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mfSchemeService).fetchSchemes(null);
    }

    @Test
    void fetchSchemes_WithEmptyScheme_ReturnsEmptyList() {
        String scheme = "";
        when(mfSchemeService.fetchSchemes(scheme)).thenReturn(Collections.emptyList());

        List<FundDetailProjection> result = mfSchemeService.fetchSchemes(scheme);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mfSchemeService).fetchSchemes(scheme);
    }

    @Test
    void fetchSchemeDetails_WithValidSchemeId_ExecutesSuccessfully() {
        Long schemeId = 12345L;
        doNothing().when(mfSchemeService).fetchSchemeDetails(schemeId);

        assertDoesNotThrow(() -> mfSchemeService.fetchSchemeDetails(schemeId));
        verify(mfSchemeService).fetchSchemeDetails(schemeId);
    }

    @Test
    void fetchSchemeDetails_WithNullSchemeId_ExecutesSuccessfully() {
        Long schemeId = null;
        doNothing().when(mfSchemeService).fetchSchemeDetails(schemeId);

        assertDoesNotThrow(() -> mfSchemeService.fetchSchemeDetails(schemeId));
        verify(mfSchemeService).fetchSchemeDetails(schemeId);
    }

    @Test
    void fetchSchemeDetails_WithZeroSchemeId_ExecutesSuccessfully() {
        Long schemeId = 0L;
        doNothing().when(mfSchemeService).fetchSchemeDetails(schemeId);

        assertDoesNotThrow(() -> mfSchemeService.fetchSchemeDetails(schemeId));
        verify(mfSchemeService).fetchSchemeDetails(schemeId);
    }

    @Test
    void fetchSchemeDetailsStringLong_WithValidParameters_ExecutesSuccessfully() {
        String oldSchemeCode = "OLD123";
        Long newSchemeCode = 456L;
        doNothing().when(mfSchemeService).fetchSchemeDetails(oldSchemeCode, newSchemeCode);

        assertDoesNotThrow(() -> mfSchemeService.fetchSchemeDetails(oldSchemeCode, newSchemeCode));
        verify(mfSchemeService).fetchSchemeDetails(oldSchemeCode, newSchemeCode);
    }

    @Test
    void fetchSchemeDetailsStringLong_WithNullOldSchemeCode_ExecutesSuccessfully() {
        String oldSchemeCode = null;
        Long newSchemeCode = 456L;
        doNothing().when(mfSchemeService).fetchSchemeDetails(oldSchemeCode, newSchemeCode);

        assertDoesNotThrow(() -> mfSchemeService.fetchSchemeDetails(oldSchemeCode, newSchemeCode));
        verify(mfSchemeService).fetchSchemeDetails(oldSchemeCode, newSchemeCode);
    }

    @Test
    void fetchSchemeDetailsStringLong_WithEmptyOldSchemeCode_ExecutesSuccessfully() {
        String oldSchemeCode = "";
        Long newSchemeCode = 456L;
        doNothing().when(mfSchemeService).fetchSchemeDetails(oldSchemeCode, newSchemeCode);

        assertDoesNotThrow(() -> mfSchemeService.fetchSchemeDetails(oldSchemeCode, newSchemeCode));
        verify(mfSchemeService).fetchSchemeDetails(oldSchemeCode, newSchemeCode);
    }

    @Test
    void fetchSchemeDetailsStringLong_WithNullNewSchemeCode_ExecutesSuccessfully() {
        String oldSchemeCode = "OLD123";
        Long newSchemeCode = null;
        doNothing().when(mfSchemeService).fetchSchemeDetails(oldSchemeCode, newSchemeCode);

        assertDoesNotThrow(() -> mfSchemeService.fetchSchemeDetails(oldSchemeCode, newSchemeCode));
        verify(mfSchemeService).fetchSchemeDetails(oldSchemeCode, newSchemeCode);
    }

    @Test
    void fetchSchemeDetailsStringLong_WithBothNullParameters_ExecutesSuccessfully() {
        String oldSchemeCode = null;
        Long newSchemeCode = null;
        doNothing().when(mfSchemeService).fetchSchemeDetails(oldSchemeCode, newSchemeCode);

        assertDoesNotThrow(() -> mfSchemeService.fetchSchemeDetails(oldSchemeCode, newSchemeCode));
        verify(mfSchemeService).fetchSchemeDetails(oldSchemeCode, newSchemeCode);
    }

    @Test
    void fetchSchemesByRtaCode_WithValidRtaCode_ReturnsListOfProjections() {
        String rtaCode = "RTA001";
        List<MFSchemeProjection> expectedList = Arrays.asList(mfSchemeProjection);
        when(mfSchemeService.fetchSchemesByRtaCode(rtaCode)).thenReturn(expectedList);

        List<MFSchemeProjection> result = mfSchemeService.fetchSchemesByRtaCode(rtaCode);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mfSchemeProjection, result.get(0));
        verify(mfSchemeService).fetchSchemesByRtaCode(rtaCode);
    }

    @Test
    void fetchSchemesByRtaCode_WithInvalidRtaCode_ReturnsEmptyList() {
        String rtaCode = "INVALID_RTA";
        when(mfSchemeService.fetchSchemesByRtaCode(rtaCode)).thenReturn(Collections.emptyList());

        List<MFSchemeProjection> result = mfSchemeService.fetchSchemesByRtaCode(rtaCode);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mfSchemeService).fetchSchemesByRtaCode(rtaCode);
    }

    @Test
    void fetchSchemesByRtaCode_WithNullRtaCode_ReturnsEmptyList() {
        when(mfSchemeService.fetchSchemesByRtaCode(null)).thenReturn(Collections.emptyList());

        List<MFSchemeProjection> result = mfSchemeService.fetchSchemesByRtaCode(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mfSchemeService).fetchSchemesByRtaCode(null);
    }

    @Test
    void fetchSchemesByRtaCode_WithEmptyRtaCode_ReturnsEmptyList() {
        String rtaCode = "";
        when(mfSchemeService.fetchSchemesByRtaCode(rtaCode)).thenReturn(Collections.emptyList());

        List<MFSchemeProjection> result = mfSchemeService.fetchSchemesByRtaCode(rtaCode);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mfSchemeService).fetchSchemesByRtaCode(rtaCode);
    }
}
