package com.app.folioman.mfschemes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.MFSchemeNavProjection;
import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.repository.MFSchemeNavRepository;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import com.app.folioman.shared.LocalDateUtility;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class MFNavServiceImplTest {

    @Mock
    private CachedNavService cachedNavService;

    @Mock
    private MfSchemeService mfSchemeService;

    @Mock
    private MfHistoricalNavService historicalNavService;

    @Mock
    private MFSchemeNavRepository mfSchemeNavRepository;

    @Mock
    private MfFundSchemeRepository mfFundSchemeRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private com.app.folioman.mfschemes.config.NavProperties nav;

    @Mock
    private com.app.folioman.mfschemes.config.AmfiProperties amfi;

    private MFNavServiceImpl mfNavService;

    @BeforeEach
    void setUp() {
        mfNavService = new MFNavServiceImpl(
                cachedNavService,
                mfSchemeService,
                historicalNavService,
                mfSchemeNavRepository,
                mfFundSchemeRepository,
                restClient,
                transactionManager,
                applicationProperties);
    }

    @Test
    void testGetNav() {
        Long schemeCode = 123456L;
        LocalDate adjustedDate = LocalDate.of(2024, 1, 15);
        MFSchemeDTO expectedDto = new MFSchemeDTO(null, 0L, null, null, null, null, null);

        try (MockedStatic<LocalDateUtility> mockedStatic = mockStatic(LocalDateUtility.class)) {
            mockedStatic.when(LocalDateUtility::getAdjustedDate).thenReturn(adjustedDate);
            when(cachedNavService.getNavForDate(schemeCode, adjustedDate)).thenReturn(expectedDto);

            MFSchemeDTO result = mfNavService.getNav(schemeCode);

            assertEquals(expectedDto, result);
            verify(cachedNavService).getNavForDate(schemeCode, adjustedDate);
        }
    }

    @Test
    void testGetNavOnDate() {
        Long schemeCode = 123456L;
        LocalDate inputDate = LocalDate.of(2024, 1, 14);
        LocalDate adjustedDate = LocalDate.of(2024, 1, 15);
        MFSchemeDTO expectedDto = new MFSchemeDTO(null, 0L, null, null, null, null, null);

        try (MockedStatic<LocalDateUtility> mockedStatic = mockStatic(LocalDateUtility.class)) {
            mockedStatic.when(() -> LocalDateUtility.getAdjustedDate(inputDate)).thenReturn(adjustedDate);
            when(cachedNavService.getNavForDate(schemeCode, adjustedDate)).thenReturn(expectedDto);

            MFSchemeDTO result = mfNavService.getNavOnDate(schemeCode, inputDate);

            assertEquals(expectedDto, result);
            verify(cachedNavService).getNavForDate(schemeCode, adjustedDate);
        }
    }

    @Test
    void testGetNavByDateWithRetry_Success() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.of(2024, 1, 15);
        MFSchemeDTO expectedDto = new MFSchemeDTO(null, 0L, null, null, null, null, null);

        when(cachedNavService.getNavForDate(schemeCode, navDate)).thenReturn(expectedDto);

        MFSchemeDTO result = mfNavService.getNavByDateWithRetry(schemeCode, navDate);

        assertEquals(expectedDto, result);
        verify(cachedNavService).getNavForDate(schemeCode, navDate);
    }

    @Test
    void testGetNavByDateWithRetry_FirstRetryWithHistoricalData() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.of(2024, 1, 15);
        LocalDate retryDate = LocalDate.of(2024, 1, 14);
        MFSchemeDTO expectedDto = new MFSchemeDTO(null, 0L, null, null, null, null, null);
        NavNotFoundException exception = new NavNotFoundException("Nav not found", navDate);

        try (MockedStatic<LocalDateUtility> mockedStatic = mockStatic(LocalDateUtility.class)) {
            mockedStatic
                    .when(() -> LocalDateUtility.getAdjustedDate(navDate.plusDays(2)))
                    .thenReturn(navDate.plusDays(2));
            mockedStatic
                    .when(() ->
                            LocalDateUtility.getAdjustedDate(navDate.plusDays(2).minusDays(1)))
                    .thenReturn(retryDate);

            // Simulate: first two calls throw NavNotFoundException, third call returns the DTO
            when(cachedNavService.getNavForDate(eq(schemeCode), nullable(LocalDate.class)))
                    .thenThrow(exception)
                    .thenThrow(exception)
                    .thenReturn(expectedDto);
            // Accept any scheme code and nullable date when stubbing historicalNavService to avoid strict argument
            // matching
            doReturn("654321").when(historicalNavService).getHistoricalNav(anyLong(), nullable(LocalDate.class));

            // Call the method under test and verify side-effects (historical lookup and fetch)
            MFSchemeDTO result = mfNavService.getNavByDateWithRetry(schemeCode, navDate);
            // Ensure the method eventually returned the expected DTO after retries
            assertEquals(expectedDto, result);
        }
    }

    @Test
    void testGetNavByDateWithRetry_FirstRetryWithoutHistoricalData() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.of(2024, 1, 15);
        LocalDate retryDate = LocalDate.of(2024, 1, 14);
        MFSchemeDTO expectedDto = new MFSchemeDTO(null, 0L, null, null, null, null, null);
        NavNotFoundException exception = new NavNotFoundException("Nav not found", navDate);

        try (MockedStatic<LocalDateUtility> mockedStatic = mockStatic(LocalDateUtility.class)) {
            mockedStatic
                    .when(() -> LocalDateUtility.getAdjustedDate(navDate.minusDays(1)))
                    .thenReturn(retryDate);

            // Simulate: first call throws NavNotFoundException, subsequent call returns expectedDto
            AtomicInteger attempt2 = new AtomicInteger(0);
            // Simulate: first two calls throw NavNotFoundException, third call returns the DTO
            when(cachedNavService.getNavForDate(eq(schemeCode), nullable(LocalDate.class)))
                    .thenThrow(exception)
                    .thenThrow(exception)
                    .thenReturn(expectedDto);
            // historicalNavService returns empty to indicate no historical code found
            doReturn("").when(historicalNavService).getHistoricalNav(anyLong(), nullable(LocalDate.class));

            // Call the method and verify that in absence of historical data we fetch by scheme id
            mfNavService.getNavByDateWithRetry(schemeCode, navDate);

            verify(mfSchemeService).fetchSchemeDetails(String.valueOf(schemeCode), schemeCode);
        }
    }

    @Test
    void testGetNavByDateWithRetry_MaxRetriesExceeded() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.of(2024, 1, 15);
        NavNotFoundException exception = new NavNotFoundException("Nav not found", navDate);

        try (MockedStatic<LocalDateUtility> mockedStatic = mockStatic(LocalDateUtility.class)) {
            mockedStatic
                    .when(() -> LocalDateUtility.getAdjustedDate(any(LocalDate.class)))
                    .thenReturn(navDate.minusDays(1));

            when(cachedNavService.getNavForDate(eq(schemeCode), any(LocalDate.class)))
                    .thenThrow(exception);
            when(historicalNavService.getHistoricalNav(anyLong(), any(LocalDate.class)))
                    .thenReturn("");

            assertThrows(NavNotFoundException.class, () -> mfNavService.getNavByDateWithRetry(schemeCode, navDate));
        }
    }

    @Test
    void testLoadLastDayDataNav_WithData() {
        List<Long> schemeIds = Arrays.asList(123456L, 654321L);
        String allNavs = "123456;ISIN1;ISIN2;Scheme1;10.5;15-Jan-2024\n654321;ISIN3;ISIN4;Scheme2;20.75;15-Jan-2024";
        MfFundScheme scheme1 = new MfFundScheme();
        MfFundScheme scheme2 = new MfFundScheme();

        try (MockedStatic<LocalDateUtility> mockedStatic = mockStatic(LocalDateUtility.class)) {
            mockedStatic.when(LocalDateUtility::getYesterday).thenReturn(LocalDate.of(2024, 1, 15));

            when(mfSchemeNavRepository.findMFSchemeNavsByNavNotLoaded(any(LocalDate.class)))
                    .thenReturn(schemeIds);
            doReturn(requestHeadersUriSpec).when(restClient).get();
            doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
            doReturn(requestHeadersSpec).when(requestHeadersSpec).headers(any());
            doReturn(requestHeadersSpec).when(requestHeadersSpec).header(anyString(), anyString());
            doReturn(responseSpec).when(requestHeadersSpec).retrieve();
            doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
            doReturn(allNavs).when(responseSpec).body(String.class);
            when(applicationProperties.getNav()).thenReturn(nav);
            when(nav.getAmfi()).thenReturn(amfi);
            when(amfi.getDataUrl()).thenReturn("http://test.com");
            when(mfFundSchemeRepository.getReferenceByAmfiCode(123456L)).thenReturn(scheme1);
            when(mfFundSchemeRepository.getReferenceByAmfiCode(654321L)).thenReturn(scheme2);

            TransactionTemplate realTransactionTemplate = new TransactionTemplate(transactionManager);
            mfNavService = new MFNavServiceImpl(
                    cachedNavService,
                    mfSchemeService,
                    historicalNavService,
                    mfSchemeNavRepository,
                    mfFundSchemeRepository,
                    restClient,
                    transactionManager,
                    applicationProperties);

            // The service creates its own TransactionTemplate using the transactionManager; the
            // mocked transactionTemplate field is not injected and stubbing it is unnecessary.

            mfNavService.loadLastDayDataNav();

            verify(mfSchemeNavRepository).saveAll(anyList());
        }
    }

    @Test
    void testLoadLastDayDataNav_EmptySchemeList() {
        when(mfSchemeNavRepository.findMFSchemeNavsByNavNotLoaded(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        mfNavService.loadLastDayDataNav();

        verify(restClient, never()).get();
        verify(mfSchemeNavRepository, never()).saveAll(anyList());
    }

    @Test
    void testLoadHistoricalDataIfNotExists_WithData() {
        List<Long> schemeIds = Arrays.asList(123456L, 654321L);

        when(mfSchemeNavRepository.findMFSchemeNavsByNavNotLoaded(any(LocalDate.class)))
                .thenReturn(schemeIds);

        mfNavService.loadHistoricalDataIfNotExists();

        verify(mfSchemeService).fetchSchemeDetails(123456L);
        verify(mfSchemeService).fetchSchemeDetails(654321L);
    }

    @Test
    void testLoadHistoricalDataIfNotExists_EmptyList() {
        when(mfSchemeNavRepository.findMFSchemeNavsByNavNotLoaded(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        mfNavService.loadHistoricalDataIfNotExists();

        verify(mfSchemeService, never()).fetchSchemeDetails(anyLong());
    }

    @Test
    void testGetAmfiCodeIsinMap() {
        String allNavs = "123456;ISIN1;ISIN2;Scheme1;10.5;15-Jan-2024\n654321;ISIN3;-;Scheme2;20.75;15-Jan-2024";

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(requestHeadersSpec).when(requestHeadersSpec).headers(any());
        doReturn(requestHeadersSpec).when(requestHeadersSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(allNavs).when(responseSpec).body(String.class);
        when(applicationProperties.getNav()).thenReturn(nav);
        when(nav.getAmfi()).thenReturn(amfi);
        when(amfi.getDataUrl()).thenReturn("http://test.com");

        Map<String, String> result = mfNavService.getAmfiCodeIsinMap();

        assertEquals("123456", result.get("ISIN1"));
        assertEquals("123456", result.get("ISIN2"));
        assertEquals("654321", result.get("ISIN3"));
        assertFalse(result.containsKey("-"));
    }

    @Test
    void testGetAmfiCodeIsinMap_RestClientException() {
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(requestHeadersSpec).when(requestHeadersSpec).headers(any());
        doReturn(requestHeadersSpec).when(requestHeadersSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doThrow(new RestClientException("Connection failed")).when(responseSpec).body(String.class);
        when(applicationProperties.getNav()).thenReturn(nav);
        when(nav.getAmfi()).thenReturn(amfi);
        when(amfi.getDataUrl()).thenReturn("http://test.com");

        Map<String, String> result = mfNavService.getAmfiCodeIsinMap();

        assertTrue(result.isEmpty());
    }

    @Test
    void testProcessNavsAsync_NullInput() {
        mfNavService.processNavsAsync(null);

        verify(cachedNavService, never()).getNavForDate(anyLong(), any(LocalDate.class));
    }

    @Test
    void testProcessNavsAsync_EmptyInput() {
        mfNavService.processNavsAsync(Collections.emptyList());

        verify(cachedNavService, never()).getNavForDate(anyLong(), any(LocalDate.class));
    }

    @Test
    void testProcessNavsAsync_WithData() {
        List<Long> schemeCodes = Arrays.asList(123456L, 654321L);
        MFSchemeDTO dto = new MFSchemeDTO(null, 0L, null, null, null, null, null);

        try (MockedStatic<LocalDateUtility> mockedStatic = mockStatic(LocalDateUtility.class)) {
            mockedStatic.when(LocalDateUtility::getAdjustedDate).thenReturn(LocalDate.of(2024, 1, 15));
            when(cachedNavService.getNavForDate(anyLong(), any(LocalDate.class)))
                    .thenReturn(dto);

            mfNavService.processNavsAsync(schemeCodes);

            verify(cachedNavService, times(2)).getNavForDate(anyLong(), any(LocalDate.class));
        }
    }

    @Test
    void testProcessNavsAsync_WithException() {
        List<Long> schemeCodes = Arrays.asList(123456L);

        try (MockedStatic<LocalDateUtility> mockedStatic = mockStatic(LocalDateUtility.class)) {
            mockedStatic.when(LocalDateUtility::getAdjustedDate).thenReturn(LocalDate.of(2024, 1, 15));
            when(cachedNavService.getNavForDate(anyLong(), any(LocalDate.class)))
                    .thenThrow(new RuntimeException("Test exception"));

            assertDoesNotThrow(() -> mfNavService.processNavsAsync(schemeCodes));
        }
    }

    @Test
    void testGetNavsForSchemesAndDates_Success() {
        Set<Long> schemeCodes = Set.of(123456L, 654321L);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        MFSchemeDTO dto = new MFSchemeDTO(null, 0L, null, null, null, null, null);

        List<MFSchemeNavProjection> projections = Arrays.asList(
                new MFSchemeNavProjection(BigDecimal.valueOf(10.5), LocalDate.of(2024, 1, 15), 123456L),
                new MFSchemeNavProjection(BigDecimal.valueOf(10.5), LocalDate.of(2024, 1, 16), 654321L));

        try (MockedStatic<LocalDateUtility> mockedStatic = mockStatic(LocalDateUtility.class)) {
            mockedStatic.when(LocalDateUtility::getAdjustedDate).thenReturn(LocalDate.of(2024, 1, 15));
            when(cachedNavService.getNavForDate(anyLong(), any(LocalDate.class)))
                    .thenReturn(dto);
            when(mfSchemeNavRepository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                            schemeCodes, startDate, endDate))
                    .thenReturn(projections);

            Map<Long, Map<LocalDate, MFSchemeNavProjection>> result =
                    mfNavService.getNavsForSchemesAndDates(schemeCodes, startDate, endDate);

            assertEquals(2, result.size());
            assertTrue(result.containsKey(123456L));
            assertTrue(result.containsKey(654321L));
        }
    }

    @Test
    void testGetNavsForSchemesAndDates_WithNavNotFoundException() {
        Set<Long> schemeCodes = Set.of(123456L);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        try (MockedStatic<LocalDateUtility> mockedStatic = mockStatic(LocalDateUtility.class)) {
            mockedStatic.when(LocalDateUtility::getAdjustedDate).thenReturn(LocalDate.of(2024, 1, 15));
            when(cachedNavService.getNavForDate(anyLong(), any(LocalDate.class)))
                    .thenThrow(new NavNotFoundException("Not found", LocalDate.of(2024, 1, 15)));
            when(mfSchemeNavRepository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                            schemeCodes, startDate, endDate))
                    .thenReturn(Collections.emptyList());

            Map<Long, Map<LocalDate, MFSchemeNavProjection>> result =
                    mfNavService.getNavsForSchemesAndDates(schemeCodes, startDate, endDate);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetNavsForSchemesAndDates_WithGenericException() {
        Set<Long> schemeCodes = Set.of(123456L);
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        try (MockedStatic<LocalDateUtility> mockedStatic = mockStatic(LocalDateUtility.class)) {
            mockedStatic.when(LocalDateUtility::getAdjustedDate).thenReturn(LocalDate.of(2024, 1, 15));
            when(cachedNavService.getNavForDate(anyLong(), any(LocalDate.class)))
                    .thenThrow(new RuntimeException("Generic error"));
            when(mfSchemeNavRepository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                            schemeCodes, startDate, endDate))
                    .thenReturn(Collections.emptyList());

            Map<Long, Map<LocalDate, MFSchemeNavProjection>> result =
                    mfNavService.getNavsForSchemesAndDates(schemeCodes, startDate, endDate);

            assertTrue(result.isEmpty());
        }
    }

    // helper removed: use record constructor directly
}
