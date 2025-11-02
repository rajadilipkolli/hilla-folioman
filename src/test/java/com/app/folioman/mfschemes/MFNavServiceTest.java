package com.app.folioman.mfschemes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MFNavServiceTest {

    @Mock
    private MFNavService mfNavService;

    @Mock
    private MFSchemeDTO mockSchemeDTO;

    @Mock
    private MFSchemeNavProjection mockNavProjection;

    private Long testSchemeCode;
    private LocalDate testDate;
    private List<Long> testSchemeCodes;
    private Set<Long> testSchemeCodesSet;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        testSchemeCode = 12345L;
        testDate = LocalDate.of(2024, 1, 15);
        testSchemeCodes = List.of(12345L, 67890L);
        testSchemeCodesSet = Set.of(12345L, 67890L);
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 1, 31);
    }

    @Test
    void getNav_ShouldReturnMFSchemeDTO_WhenValidSchemeCodeProvided() {
        when(mfNavService.getNav(testSchemeCode)).thenReturn(mockSchemeDTO);

        MFSchemeDTO result = mfNavService.getNav(testSchemeCode);

        assertThat(result).isEqualTo(mockSchemeDTO);
        verify(mfNavService).getNav(testSchemeCode);
    }

    @Test
    void getNav_ShouldHandleNullSchemeCode() {
        when(mfNavService.getNav(null)).thenReturn(null);

        MFSchemeDTO result = mfNavService.getNav(null);

        assertThat(result).isNull();
        verify(mfNavService).getNav(null);
    }

    @Test
    void getNavOnDate_ShouldReturnMFSchemeDTO_WhenValidParametersProvided() {
        when(mfNavService.getNavOnDate(testSchemeCode, testDate)).thenReturn(mockSchemeDTO);

        MFSchemeDTO result = mfNavService.getNavOnDate(testSchemeCode, testDate);

        assertThat(result).isEqualTo(mockSchemeDTO);
        verify(mfNavService).getNavOnDate(testSchemeCode, testDate);
    }

    @Test
    void getNavOnDate_ShouldHandleNullParameters() {
        when(mfNavService.getNavOnDate(null, null)).thenReturn(null);

        MFSchemeDTO result = mfNavService.getNavOnDate(null, null);

        assertThat(result).isNull();
        verify(mfNavService).getNavOnDate(null, null);
    }

    @Test
    void getNavByDateWithRetry_ShouldReturnMFSchemeDTO_WhenValidParametersProvided() {
        when(mfNavService.getNavByDateWithRetry(testSchemeCode, testDate)).thenReturn(mockSchemeDTO);

        MFSchemeDTO result = mfNavService.getNavByDateWithRetry(testSchemeCode, testDate);

        assertThat(result).isEqualTo(mockSchemeDTO);
        verify(mfNavService).getNavByDateWithRetry(testSchemeCode, testDate);
    }

    @Test
    void getNavByDateWithRetry_ShouldHandleNullParameters() {
        when(mfNavService.getNavByDateWithRetry(null, null)).thenReturn(null);

        MFSchemeDTO result = mfNavService.getNavByDateWithRetry(null, null);

        assertThat(result).isNull();
        verify(mfNavService).getNavByDateWithRetry(null, null);
    }

    @Test
    void loadLastDayDataNav_ShouldExecuteSuccessfully() {
        doNothing().when(mfNavService).loadLastDayDataNav();

        mfNavService.loadLastDayDataNav();

        verify(mfNavService).loadLastDayDataNav();
    }

    @Test
    void loadHistoricalDataIfNotExists_ShouldExecuteSuccessfully() {
        doNothing().when(mfNavService).loadHistoricalDataIfNotExists();

        mfNavService.loadHistoricalDataIfNotExists();

        verify(mfNavService).loadHistoricalDataIfNotExists();
    }

    @Test
    void getAmfiCodeIsinMap_ShouldReturnMap() {
        Map<String, String> expectedMap = Map.of("AMFI123", "ISIN123", "AMFI456", "ISIN456");
        when(mfNavService.getAmfiCodeIsinMap()).thenReturn(expectedMap);

        Map<String, String> result = mfNavService.getAmfiCodeIsinMap();

        assertThat(result).isEqualTo(expectedMap);
        verify(mfNavService).getAmfiCodeIsinMap();
    }

    @Test
    void getAmfiCodeIsinMap_ShouldReturnEmptyMap() {
        when(mfNavService.getAmfiCodeIsinMap()).thenReturn(Collections.emptyMap());

        Map<String, String> result = mfNavService.getAmfiCodeIsinMap();

        assertThat(result).isEmpty();
        verify(mfNavService).getAmfiCodeIsinMap();
    }

    @Test
    void processNavsAsync_ShouldExecuteSuccessfully_WhenValidListProvided() {
        doNothing().when(mfNavService).processNavsAsync(testSchemeCodes);

        mfNavService.processNavsAsync(testSchemeCodes);

        verify(mfNavService).processNavsAsync(testSchemeCodes);
    }

    @Test
    void processNavsAsync_ShouldHandleEmptyList() {
        List<Long> emptyList = Collections.emptyList();
        doNothing().when(mfNavService).processNavsAsync(emptyList);

        mfNavService.processNavsAsync(emptyList);

        verify(mfNavService).processNavsAsync(emptyList);
    }

    @Test
    void processNavsAsync_ShouldHandleNullList() {
        doNothing().when(mfNavService).processNavsAsync(null);

        mfNavService.processNavsAsync(null);

        verify(mfNavService).processNavsAsync(null);
    }

    @Test
    void getNavsForSchemesAndDates_ShouldReturnMap_WhenValidParametersProvided() {
        Map<LocalDate, MFSchemeNavProjection> dateNavMap = Map.of(testDate, mockNavProjection);
        Map<Long, Map<LocalDate, MFSchemeNavProjection>> expectedResult = Map.of(testSchemeCode, dateNavMap);

        when(mfNavService.getNavsForSchemesAndDates(testSchemeCodesSet, startDate, endDate))
                .thenReturn(expectedResult);

        Map<Long, Map<LocalDate, MFSchemeNavProjection>> result =
                mfNavService.getNavsForSchemesAndDates(testSchemeCodesSet, startDate, endDate);

        assertThat(result).isEqualTo(expectedResult);
        verify(mfNavService).getNavsForSchemesAndDates(testSchemeCodesSet, startDate, endDate);
    }

    @Test
    void getNavsForSchemesAndDates_ShouldHandleEmptySet() {
        Set<Long> emptySet = Collections.emptySet();
        Map<Long, Map<LocalDate, MFSchemeNavProjection>> emptyResult = Collections.emptyMap();

        when(mfNavService.getNavsForSchemesAndDates(emptySet, startDate, endDate))
                .thenReturn(emptyResult);

        Map<Long, Map<LocalDate, MFSchemeNavProjection>> result =
                mfNavService.getNavsForSchemesAndDates(emptySet, startDate, endDate);

        assertThat(result).isEmpty();
        verify(mfNavService).getNavsForSchemesAndDates(emptySet, startDate, endDate);
    }

    @Test
    void getNavsForSchemesAndDates_ShouldHandleNullParameters() {
        when(mfNavService.getNavsForSchemesAndDates(null, null, null)).thenReturn(Collections.emptyMap());

        Map<Long, Map<LocalDate, MFSchemeNavProjection>> result =
                mfNavService.getNavsForSchemesAndDates(null, null, null);

        assertThat(result).isEmpty();
        verify(mfNavService).getNavsForSchemesAndDates(null, null, null);
    }

    @Test
    void getNavsForSchemesAndDates_ShouldHandleSameDateRange() {
        when(mfNavService.getNavsForSchemesAndDates(testSchemeCodesSet, testDate, testDate))
                .thenReturn(Collections.emptyMap());

        Map<Long, Map<LocalDate, MFSchemeNavProjection>> result =
                mfNavService.getNavsForSchemesAndDates(testSchemeCodesSet, testDate, testDate);

        assertThat(result).isEmpty();
        verify(mfNavService).getNavsForSchemesAndDates(testSchemeCodesSet, testDate, testDate);
    }
}
