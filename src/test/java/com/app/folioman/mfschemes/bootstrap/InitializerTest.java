package com.app.folioman.mfschemes.bootstrap;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.config.MfSchemesProperties;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.service.AmfiService;
import com.app.folioman.mfschemes.service.BSEStarMasterDataService;
import com.app.folioman.mfschemes.service.MfFundSchemeService;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationStartedEvent;

@ExtendWith(MockitoExtension.class)
class InitializerTest {

    @Mock
    private AmfiService amfiService;

    @Mock
    private BSEStarMasterDataService bseStarMasterDataService;

    @Mock
    private MfFundSchemeService mfFundSchemeService;

    @Mock
    private MFNavService mfNavService;

    @Mock
    private MfSchemesProperties properties;

    @Mock
    private ApplicationStartedEvent event;

    @InjectMocks
    private Initializer initializer;

    @Test
    void testHandleApplicationStartedEventWithNoNewSchemes() throws IOException, CsvException {
        // Configure properties needed for this test
        given(properties.getRetryAttempts()).willReturn(2);

        // Setup data for this specific test
        given(amfiService.fetchAmfiSchemeData()).willReturn(createAmfiTestData(10));
        given(mfFundSchemeService.getTotalCount()).willReturn(10L);

        // Execute
        initializer.handleApplicationStartedEvent(event);

        // Verify
        verify(amfiService, times(1)).fetchAmfiSchemeData();
        // No data processing should happen
        verify(bseStarMasterDataService, times(0)).fetchBseStarMasterData(anyMap(), anyMap());
    }

    @Test
    void testHandleApplicationStartedEventWithNewSchemes() throws IOException, CsvException {
        // Configure properties needed for this test
        given(properties.getRetryAttempts()).willReturn(2);
        given(properties.getBatchSize()).willReturn(100);

        // Setup data for this specific test
        Map<String, Map<String, String>> amfiData = createAmfiTestData(20);
        given(amfiService.fetchAmfiSchemeData()).willReturn(amfiData);
        given(mfFundSchemeService.getTotalCount()).willReturn(10L);
        given(mfNavService.getAmfiCodeIsinMap()).willReturn(Collections.emptyMap());

        // Mock BSE data
        Map<String, MfFundScheme> bseData = new HashMap<>();
        for (String amfiCode : amfiData.keySet()) {
            bseData.put(amfiCode, new MfFundScheme());
        }
        given(bseStarMasterDataService.fetchBseStarMasterData(anyMap(), anyMap()))
                .willReturn(bseData);

        // Mock DB data
        given(mfFundSchemeService.findDistinctAmfiCode()).willReturn(Collections.emptyList());

        // Execute
        initializer.handleApplicationStartedEvent(event);

        // Verify
        verify(amfiService, times(1)).fetchAmfiSchemeData();
        verify(bseStarMasterDataService, times(1)).fetchBseStarMasterData(anyMap(), anyMap());
        verify(mfFundSchemeService, times(1)).findDistinctAmfiCode();

        // Verify batching is done with the mocked batch size of 100
        verify(mfFundSchemeService, times(1)).saveDataInBatches(anyList(), eq(100));
    }

    @Test
    void testHandleApplicationStartedEventWithRetry() throws IOException, CsvException {
        // Configure properties needed for this test
        given(properties.getRetryAttempts()).willReturn(2);
        given(properties.getRetryDelayMs()).willReturn(10L);

        // Setup - first call throws exception, second succeeds
        given(amfiService.fetchAmfiSchemeData())
                .willThrow(new IOException("Test exception"))
                .willReturn(createAmfiTestData(10));

        given(mfFundSchemeService.getTotalCount()).willReturn(10L);

        // Execute
        initializer.handleApplicationStartedEvent(event);

        // Verify
        verify(amfiService, times(2)).fetchAmfiSchemeData();
    }

    private Map<String, Map<String, String>> createAmfiTestData(int count) {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (int i = 1; i <= count; i++) {
            String amfiCode = String.valueOf(i);
            Map<String, String> schemeData = new HashMap<>();
            schemeData.put(Initializer.ISIN_KEY, "ISIN" + i);
            schemeData.put("NAME", "Test Scheme " + i);
            result.put(amfiCode, schemeData);
        }
        return result;
    }
}
