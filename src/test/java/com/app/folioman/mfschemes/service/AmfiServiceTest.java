package com.app.folioman.mfschemes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.app.folioman.mfschemes.config.AmfiProperties;
import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.config.SchemeProperties;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class AmfiServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private AmfiProperties amfi;

    @Mock
    private SchemeProperties scheme;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private AmfiService amfiService;

    @BeforeEach
    void setUp() {
        amfiService = new AmfiService(restClient, applicationProperties);
    }

    @Test
    void fetchAmfiSchemeData_SuccessfulRetrieval() throws IOException, CsvException {
        String csvContent =
                "Scheme Code,Scheme Name,Net Asset Value,Date\n123,Test Fund,100.50,01-Jan-2024\n456,Another Fund,200.75,01-Jan-2024";
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(dataUrl);
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(csvContent).when(responseSpec).body(String.class);

        Map<String, Map<String, String>> result = amfiService.fetchAmfiSchemeData();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("123"));
        assertTrue(result.containsKey("456"));
        assertEquals("Test Fund", result.get("123").get("Scheme Name"));
        assertEquals("Another Fund", result.get("456").get("Scheme Name"));
    }

    @Test
    void fetchAmfiSchemeData_RestClientException() throws IOException, CsvException {
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(dataUrl);
        doThrow(new RuntimeException("Network error"))
                .when(requestHeadersUriSpec)
                .retrieve();

        Map<String, Map<String, String>> result = amfiService.fetchAmfiSchemeData();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchAmfiSchemeData_NullResponse() {
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(dataUrl);
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(null).when(responseSpec).body(String.class);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> amfiService.fetchAmfiSchemeData());
        assertEquals("Invalid response! No data received.", exception.getMessage());
    }

    @Test
    void fetchAmfiSchemeData_BlankResponse() {
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(dataUrl);
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn("   ").when(responseSpec).body(String.class);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> amfiService.fetchAmfiSchemeData());
        assertEquals("Invalid response! No data received.", exception.getMessage());
    }

    @Test
    void fetchAmfiSchemeData_EmptyResponse() {
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(dataUrl);
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn("").when(responseSpec).body(String.class);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> amfiService.fetchAmfiSchemeData());
        assertEquals("Invalid response! No data received.", exception.getMessage());
    }

    @Test
    void fetchAmfiSchemeData_OnlyHeaders() throws IOException, CsvException {
        String csvContent = "Scheme Code,Scheme Name,Net Asset Value,Date";
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(dataUrl);
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(csvContent).when(responseSpec).body(String.class);

        Map<String, Map<String, String>> result = amfiService.fetchAmfiSchemeData();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchAmfiSchemeData_WithWhitespaceInData() throws IOException, CsvException {
        String csvContent =
                "Scheme Code,Scheme Name,Net Asset Value,Date\n  123  ,  Test Fund  ,  100.50  ,  01-Jan-2024  ";
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(dataUrl);
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(csvContent).when(responseSpec).body(String.class);

        Map<String, Map<String, String>> result = amfiService.fetchAmfiSchemeData();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("123"));
        assertEquals("Test Fund", result.get("123").get("Scheme Name"));
        assertEquals("100.50", result.get("123").get("Net Asset Value"));
    }

    @Test
    void fetchAmfiSchemeData_MultipleRowsSameCode() throws IOException, CsvException {
        String csvContent =
                "Scheme Code,Scheme Name,Net Asset Value,Date\n123,Test Fund 1,100.50,01-Jan-2024\n123,Test Fund 2,200.75,02-Jan-2024";
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(dataUrl)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(csvContent);

        Map<String, Map<String, String>> result = amfiService.fetchAmfiSchemeData();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("123"));
        assertEquals("Test Fund 2", result.get("123").get("Scheme Name"));
    }
}
