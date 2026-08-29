package com.app.folioman.mfschemes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.config.AmfiProperties;
import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.config.MfSchemesProperties;
import com.app.folioman.mfschemes.config.SchemeProperties;
import java.util.ArrayList;
import java.util.List;
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
    private MfSchemesProperties mfSchemesProperties;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private AmfiService amfiService;

    @BeforeEach
    void setUp() {
        amfiService = new AmfiService(restClient, applicationProperties, mfSchemesProperties);
    }

    @Test
    void fetchAmfiSchemeData_SuccessfulRetrieval() throws Exception {
        String csvContent =
                "Scheme Name,Scheme Code,Net Asset Value,Date\nTest Fund,123,100.50,01-Jan-2024\nAnother Fund,456,200.75,01-Jan-2024";
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(dataUrl);
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        when(responseSpec.body(String.class)).thenReturn(csvContent);
        when(mfSchemesProperties.getCsvProcessingBatchSize()).thenReturn(5000);

        List<Map<Long, Map<String, String>>> batches = new ArrayList<>();
        amfiService.fetchAmfiSchemeData(batches::add);

        assertThat(batches).hasSize(1);
        Map<Long, Map<String, String>> result = batches.get(0);
        assertThat(result).hasSize(2);
        assertThat(result).containsKey(123L).containsKey(456L);
        assertThat(result.get(123L)).containsEntry("Scheme Name", "Test Fund");
        assertThat(result.get(456L)).containsEntry("Scheme Name", "Another Fund");
    }

    @Test
    void fetchAmfiSchemeData_RestClientException() throws Exception {
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(dataUrl);
        doThrow(new RuntimeException("Network error"))
                .when(requestHeadersUriSpec)
                .retrieve();

        List<Map<Long, Map<String, String>>> batches = new ArrayList<>();
        amfiService.fetchAmfiSchemeData(batches::add);

        assertThat(batches).isEmpty();
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

        IllegalStateException exception = assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> amfiService.fetchAmfiSchemeData(batch -> {}))
                .actual();
        assertThat(exception.getMessage()).isEqualTo("Invalid response! No data received.");
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

        IllegalStateException exception = assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> amfiService.fetchAmfiSchemeData(batch -> {}))
                .actual();
        assertThat(exception.getMessage()).isEqualTo("Invalid response! No data received.");
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

        IllegalStateException exception = assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> amfiService.fetchAmfiSchemeData(batch -> {}))
                .actual();
        assertThat(exception.getMessage()).isEqualTo("Invalid response! No data received.");
    }

    @Test
    void fetchAmfiSchemeData_OnlyHeaders() throws Exception {
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
        when(mfSchemesProperties.getCsvProcessingBatchSize()).thenReturn(5000);

        List<Map<Long, Map<String, String>>> batches = new ArrayList<>();
        amfiService.fetchAmfiSchemeData(batches::add);

        assertThat(batches).isEmpty();
    }

    @Test
    void fetchAmfiSchemeData_WithWhitespaceInData() throws Exception {
        String csvContent =
                "Scheme Name,Scheme Code,Net Asset Value,Date\n  Test Fund  ,  123  ,  100.50  ,  01-Jan-2024  ";
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(dataUrl);
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(csvContent).when(responseSpec).body(String.class);
        when(mfSchemesProperties.getCsvProcessingBatchSize()).thenReturn(5000);

        List<Map<Long, Map<String, String>>> batches = new ArrayList<>();
        amfiService.fetchAmfiSchemeData(batches::add);

        assertThat(batches).hasSize(1);
        Map<Long, Map<String, String>> result = batches.get(0);
        assertThat(result).hasSize(1).containsKey(123L);
        assertThat(result.get(123L)).containsEntry("Scheme Name", "Test Fund");
        assertThat(result.get(123L)).containsEntry("Net Asset Value", "100.50");
    }

    @Test
    void fetchAmfiSchemeData_MultipleRowsSameCode() throws Exception {
        String csvContent =
                "Scheme Name,Scheme Code,Net Asset Value,Date\nTest Fund 1,123,100.50,01-Jan-2024\nTest Fund 2,123,200.75,02-Jan-2024";
        String dataUrl = "http://test-url.com/data.csv";

        when(applicationProperties.getAmfi()).thenReturn(amfi);
        when(amfi.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn(dataUrl);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(dataUrl)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(csvContent);
        when(mfSchemesProperties.getCsvProcessingBatchSize()).thenReturn(5000);

        List<Map<Long, Map<String, String>>> batches = new ArrayList<>();
        amfiService.fetchAmfiSchemeData(batches::add);

        assertThat(batches).hasSize(1);
        Map<Long, Map<String, String>> result = batches.get(0);
        assertThat(result).hasSize(1).containsKey(123L);
        assertThat(result.get(123L)).containsEntry("Scheme Name", "Test Fund 2");
    }
}
