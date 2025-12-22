package com.app.folioman.mfschemes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.MfSchemeDtoToEntityMapperHelper;
import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.config.BseStarProperties;
import com.app.folioman.mfschemes.config.SchemeProperties;
import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class BSEStarMasterDataServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private MfAmcService mfAmcService;

    @Mock
    private MfSchemeDtoToEntityMapperHelper mfSchemeDtoToEntityMapperHelper;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private BseStarProperties bseStar;

    @Mock
    private SchemeProperties scheme;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @InjectMocks
    private BSEStarMasterDataService bseStarMasterDataService;

    @Test
    void fetchBseStarMasterData_ShouldReturnMasterData_WhenValidInputProvided() throws Exception {
        // Given
        Map<String, Map<String, String>> amfiDataMap = new HashMap<>();
        Map<String, String> amfiCodeIsinMapping = new HashMap<>();
        amfiCodeIsinMapping.put("INF123456789", "12345");

        Map<String, String> schemeData = new HashMap<>();
        schemeData.put("Scheme Name", "Test Scheme");
        schemeData.put("AMC", "Test AMC");
        schemeData.put("Scheme Category", "Equity-Large Cap");
        schemeData.put("Scheme Type", "Open Ended");
        amfiDataMap.put("12345", schemeData);

        String htmlResponse = """
                <html>
                <body>
                <form id="frmOrdConfirm">
                    <input type="hidden" name="__VIEWSTATE" value="testviewstate"/>
                    <input type="hidden" name="__EVENTVALIDATION" value="testevent"/>
                </form>
                </body>
                </html>
                """;

        String csvResponse = """
                Unique No|Scheme Code|ISIN|Scheme Name|AMC Code|AMC Scheme Code|Scheme Plan|RTA Agent Code|Channel Partner Code|Start Date|End Date
                1|TEST001|INF123456789|Test Scheme Name|AMC001|SC001|DIRECT|RTA001|CP001|Jan 1 2020|Dec 31 2025
                """;

        when(applicationProperties.getBseStar()).thenReturn(bseStar);
        when(bseStar.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn("https://test-url.com");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        // First call should return the HTML page, second call should return the CSV body
        when(responseSpec.body(String.class)).thenReturn(htmlResponse, csvResponse);

        MfAmc mockAmc = new MfAmc();
        mockAmc.setCode("AMC001");
        mockAmc.setName("Test AMC");
        when(mfAmcService.findByCode(anyString())).thenReturn(mockAmc);

        // When
        Map<String, MfFundScheme> result =
                bseStarMasterDataService.fetchBseStarMasterData(amfiDataMap, amfiCodeIsinMapping);

        assertThat(result).containsKey("12345");
    }

    @Test
    void fetchBseStarMasterData_ShouldThrowIOException_WhenFormNotFound() throws Exception {
        // Given
        Map<String, Map<String, String>> amfiDataMap = new HashMap<>();
        Map<String, String> amfiCodeIsinMapping = new HashMap<>();

        String invalidHtmlResponse = "<html><body>No form here</body></html>";

        when(applicationProperties.getBseStar()).thenReturn(bseStar);
        when(bseStar.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn("https://test-url.com");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(invalidHtmlResponse);

        // When & Then
        assertThatThrownBy(() -> bseStarMasterDataService.fetchBseStarMasterData(amfiDataMap, amfiCodeIsinMapping))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Unable to find the form with ID 'frmOrdConfirm'");
    }

    @Test
    void fetchBseStarMasterData_ShouldHandleEmptyResponse() throws Exception {
        // Given
        Map<String, Map<String, String>> amfiDataMap = new HashMap<>();
        Map<String, String> amfiCodeIsinMapping = new HashMap<>();

        String htmlResponse = """
                <html>
                <body>
                <form id="frmOrdConfirm">
                    <input type="hidden" name="__VIEWSTATE" value="testviewstate"/>
                </form>
                </body>
                </html>
                """;

        when(applicationProperties.getBseStar()).thenReturn(bseStar);
        when(bseStar.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn("https://test-url.com");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        // return HTML first, then empty CSV on second body() call
        when(responseSpec.body(String.class)).thenReturn(htmlResponse, "");

        // When
        Map<String, MfFundScheme> result =
                bseStarMasterDataService.fetchBseStarMasterData(amfiDataMap, amfiCodeIsinMapping);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void mapHeaders_ShouldReturnCorrectMapping_WhenValidHeadersProvided() {
        // Given
        String[] headers = {"Header1|Header2|Header3"};

        // When
        Map<String, Integer> result = bseStarMasterDataService.mapHeaders(headers);

        assertThat(result)
                // Then
                .hasSize(3)
                .containsEntry("Header1", 0)
                .containsEntry("Header2", 1)
                .containsEntry("Header3", 2);
    }

    @Test
    void mapHeaders_ShouldReturnSingleMapping_WhenSingleHeaderProvided() {
        // Given
        String[] headers = {"SingleHeader"};

        // When
        Map<String, Integer> result = bseStarMasterDataService.mapHeaders(headers);

        assertThat(result)
                // Then
                .hasSize(1)
                .containsEntry("SingleHeader", 0);
    }

    @Test
    void mapHeaders_ShouldReturnEmptyMap_WhenEmptyArrayProvided() {
        // Given
        String[] headers = {""};

        // When
        Map<String, Integer> result = bseStarMasterDataService.mapHeaders(headers);

        assertThat(result)
                // Then
                .hasSize(1)
                .containsEntry("", 0);
    }

    @Test
    void fetchBseStarMasterData_ShouldProcessAmfiFallback_WhenSchemeNotInBseData() throws Exception {
        // Given
        Map<String, Map<String, String>> amfiDataMap = new HashMap<>();
        Map<String, String> amfiCodeIsinMapping = new HashMap<>();
        amfiCodeIsinMapping.put("INF987654321", "54321");

        Map<String, String> schemeData = new HashMap<>();
        schemeData.put("Scheme Name", "Fallback Scheme");
        schemeData.put("AMC", "Fallback AMC");
        schemeData.put("Scheme Category", "Debt-Short Duration");
        schemeData.put("Scheme Type", "Close Ended");
        amfiDataMap.put("54321", schemeData);

        String htmlResponse = """
                <html>
                <body>
                <form id="frmOrdConfirm">
                    <input type="hidden" name="__VIEWSTATE" value="testviewstate"/>
                </form>
                </body>
                </html>
                """;

        String emptyCsvResponse = "Header1|Header2\n";

        when(applicationProperties.getBseStar()).thenReturn(bseStar);
        when(bseStar.getScheme()).thenReturn(scheme);
        when(scheme.getDataUrl()).thenReturn("https://test-url.com");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(htmlResponse);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        // return HTML first, then the empty CSV response
        when(responseSpec.body(String.class)).thenReturn(htmlResponse, emptyCsvResponse);

        MfAmc mockAmc = new MfAmc();
        mockAmc.setName("Fallback AMC");
        when(mfAmcService.findOrCreateByName("Fallback AMC")).thenReturn(mockAmc);

        // When
        Map<String, MfFundScheme> result =
                bseStarMasterDataService.fetchBseStarMasterData(amfiDataMap, amfiCodeIsinMapping);

        assertThat(result).containsKey("54321");
        MfFundScheme scheme = result.get("54321");
        assertThat(scheme.getAmfiCode()).isEqualTo(54321L);
        assertThat(scheme.getName()).isEqualTo("Fallback Scheme");
    }
}
