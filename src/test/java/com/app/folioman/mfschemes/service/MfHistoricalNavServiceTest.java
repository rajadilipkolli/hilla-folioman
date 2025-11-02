package com.app.folioman.mfschemes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.mapper.MfSchemeDtoToEntityMapper;
import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class MfHistoricalNavServiceTest {

    @Mock
    private MfSchemeServiceImpl mfSchemeService;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper;

    @Mock
    private MfFundScheme mfFundScheme;

    private MfHistoricalNavService mfHistoricalNavService;

    @BeforeEach
    void setUp() {
        mfHistoricalNavService = new MfHistoricalNavService(mfSchemeService, restClient, mfSchemeDtoToEntityMapper);
    }

    @Test
    void getHistoricalNav_WhenSchemeExists_ShouldReturnNavData() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.of(2024, 1, 3);
        String isin = "INF123456789";

        when(mfFundScheme.getIsin()).thenReturn(isin);
        when(mfSchemeService.findBySchemeCode(schemeCode)).thenReturn(Optional.of(mfFundScheme));
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(URI.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();

        String mockResponse = createMockNavResponse(schemeCode, isin);
        when(responseSpec.body(String.class)).thenReturn(mockResponse);

        String result = mfHistoricalNavService.getHistoricalNav(schemeCode, navDate);

        assertEquals("123456", result);
        verify(mfSchemeService).findBySchemeCode(schemeCode);
    }

    @Test
    void getHistoricalNav_WhenSchemeDoesNotExist_ShouldHandleDiscontinuedScheme() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.of(2024, 1, 3);

        when(mfSchemeService.findBySchemeCode(schemeCode)).thenReturn(Optional.empty());
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(URI.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();

        String mockResponse = createMockNavResponse(schemeCode, null);
        when(responseSpec.body(String.class)).thenReturn(mockResponse);
        when(mfSchemeDtoToEntityMapper.mapMFSchemeDTOToMfFundScheme(any())).thenReturn(mfFundScheme);

        String result = mfHistoricalNavService.getHistoricalNav(schemeCode, navDate);

        assertEquals("123456", result);
        verify(mfSchemeService).findBySchemeCode(schemeCode);
        verify(mfSchemeService).saveEntity(mfFundScheme);
    }

    @Test
    void getHistoricalNav_WhenResourceAccessException_ShouldReturnNull() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.of(2024, 1, 3);
        String isin = "INF123456789";

        when(mfFundScheme.getIsin()).thenReturn(isin);
        when(mfSchemeService.findBySchemeCode(schemeCode)).thenReturn(Optional.of(mfFundScheme));
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(URI.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.body(String.class)).thenThrow(new ResourceAccessException("Service unavailable"));

        String result = mfHistoricalNavService.getHistoricalNav(schemeCode, navDate);

        assertNull(result);
        verify(mfSchemeService).findBySchemeCode(schemeCode);
    }

    @Test
    void getHistoricalNav_WhenMalformedData_ShouldThrowNavNotFoundException() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.of(2024, 1, 3);
        String isin = "INF123456789";

        when(mfFundScheme.getIsin()).thenReturn(isin);
        when(mfSchemeService.findBySchemeCode(schemeCode)).thenReturn(Optional.of(mfFundScheme));
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(URI.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.body(String.class)).thenReturn("malformed data");

        NavNotFoundException exception = assertThrows(
                NavNotFoundException.class, () -> mfHistoricalNavService.getHistoricalNav(schemeCode, navDate));

        String expectedPrefix = "Unable to parse for " + schemeCode;
        assertTrue(exception.getMessage().startsWith(expectedPrefix));
        assertEquals(navDate, exception.getNavDate());
        verify(mfSchemeService).findBySchemeCode(schemeCode);
    }

    @Test
    void getHistoricalNav_WhenEmptyResponse_ShouldThrowNavNotFoundException() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.of(2024, 1, 3);
        String isin = "INF123456789";

        when(mfFundScheme.getIsin()).thenReturn(isin);
        when(mfSchemeService.findBySchemeCode(schemeCode)).thenReturn(Optional.of(mfFundScheme));
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(URI.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.body(String.class)).thenReturn("");

        NavNotFoundException exception = assertThrows(
                NavNotFoundException.class, () -> mfHistoricalNavService.getHistoricalNav(schemeCode, navDate));

        String expectedPrefix = "Unable to parse for " + schemeCode;
        assertTrue(exception.getMessage().startsWith(expectedPrefix));
        verify(mfSchemeService).findBySchemeCode(schemeCode);
    }

    @Test
    void getHistoricalNav_WhenNullResponse_ShouldThrowNavNotFoundException() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.of(2024, 1, 3);
        String isin = "INF123456789";

        when(mfFundScheme.getIsin()).thenReturn(isin);
        when(mfSchemeService.findBySchemeCode(schemeCode)).thenReturn(Optional.of(mfFundScheme));
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(URI.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(null).when(responseSpec).body(String.class);

        NavNotFoundException exception = assertThrows(
                NavNotFoundException.class, () -> mfHistoricalNavService.getHistoricalNav(schemeCode, navDate));

        String expectedPrefix = "Unable to parse for " + schemeCode;
        assertTrue(exception.getMessage().startsWith(expectedPrefix));
        verify(mfSchemeService).findBySchemeCode(schemeCode);
    }

    @Test
    void getHistoricalNav_WithComplexNavData_ShouldParseCorrectly() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.of(2024, 1, 3);
        String isin = "INF123456789";

        when(mfFundScheme.getIsin()).thenReturn(isin);
        when(mfSchemeService.findBySchemeCode(schemeCode)).thenReturn(Optional.of(mfFundScheme));
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(URI.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();

        String complexResponse = createComplexMockNavResponse(schemeCode, isin);
        when(responseSpec.body(String.class)).thenReturn(complexResponse);

        String result = mfHistoricalNavService.getHistoricalNav(schemeCode, navDate);

        assertEquals("123456", result);
        verify(mfSchemeService).findBySchemeCode(schemeCode);
        verify(mfSchemeService, never()).saveEntity(any());
    }

    @Test
    void getHistoricalNav_WhenSchemeNotFoundInData_ShouldThrowNavNotFoundException() {
        Long schemeCode = 999999L;
        LocalDate navDate = LocalDate.of(2024, 1, 3);
        String isin = "INF123456789";

        when(mfFundScheme.getIsin()).thenReturn(isin);
        when(mfSchemeService.findBySchemeCode(schemeCode)).thenReturn(Optional.of(mfFundScheme));
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(URI.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();

        String mockResponse = createMockNavResponse(123456L, "INF987654321");
        when(responseSpec.body(String.class)).thenReturn(mockResponse);

        NavNotFoundException exception = assertThrows(
                NavNotFoundException.class, () -> mfHistoricalNavService.getHistoricalNav(schemeCode, navDate));

        String expectedPrefix = "Unable to parse for " + schemeCode;
        assertTrue(exception.getMessage().startsWith(expectedPrefix));
        verify(mfSchemeService).findBySchemeCode(schemeCode);
    }

    private String createMockNavResponse(Long schemeCode, String isin) {
        return String.format(
                """
            ;
            ;
            Open Ended Schemes ( All Schemes )
            HDFC Asset Management Company Limited

            123456;Test Scheme;%s;Growth;10.5000;;INF123456789;03-Jan-2024
            """,
                isin != null ? isin : "INF123456789");
    }

    private String createComplexMockNavResponse(Long schemeCode, String isin) {
        return String.format(
                """
            ;
            ;
            Open Ended Schemes ( All Schemes )
            HDFC Asset Management Company Limited


            Close Ended Schemes ( Income )
            SBI Mutual Fund
            123456;Test Scheme;%s;Growth;10.5000;;INF123456789;03-Jan-2024
            789012;Another Scheme;INF987654321;Dividend;12.3000;;INF987654321;03-Jan-2024
            """,
                isin);
    }
}
