package com.app.folioman.mfschemes.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.config.MfSchemesProperties;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
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
    @SuppressWarnings("unchecked")
    void handleApplicationStartedEventWithNoNewSchemes() throws Exception {
        // Configure properties needed for this test
        given(properties.getRetryAttempts()).willReturn(2);
        given(bseStarMasterDataService.downloadBseMasterData()).willReturn("bseData");

        // Setup data for this specific test
        doAnswer(invocation -> {
                    Consumer<Map<String, Map<String, String>>> consumer = invocation.getArgument(0);
                    consumer.accept(Collections.emptyMap());
                    return null;
                })
                .when(amfiService)
                .fetchAmfiSchemeData(any(Consumer.class));

        // Execute
        initializer.handleApplicationStartedEvent(event);

        // Verify
        verify(amfiService, times(1)).fetchAmfiSchemeData(any(Consumer.class));
        // No data processing should happen
        verify(bseStarMasterDataService, times(0)).fetchBseStarMasterData(anyString(), anyMap(), anyMap());
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleApplicationStartedEventWithNewSchemes() throws Exception {
        // Configure properties needed for this test
        given(properties.getRetryAttempts()).willReturn(2);
        given(properties.getBatchSize()).willReturn(100);
        given(bseStarMasterDataService.downloadBseMasterData()).willReturn("bseRawData");

        // Setup data for this specific test
        Map<String, Map<String, String>> amfiData = createAmfiTestData(20);
        doAnswer(invocation -> {
                    Consumer<Map<String, Map<String, String>>> consumer = invocation.getArgument(0);
                    consumer.accept(amfiData);
                    return null;
                })
                .when(amfiService)
                .fetchAmfiSchemeData(any(Consumer.class));

        given(mfNavService.getAmfiCodeIsinMap()).willReturn(Collections.emptyMap());

        // Mock BSE data
        Map<String, MfFundScheme> bseData = new HashMap<>();
        for (String amfiCode : amfiData.keySet()) {
            bseData.put(amfiCode, new MfFundScheme());
        }
        given(bseStarMasterDataService.fetchBseStarMasterData(eq("bseRawData"), anyMap(), anyMap()))
                .willReturn(bseData);

        // Mock DB data
        given(mfFundSchemeService.findDistinctAmfiCode()).willReturn(Collections.emptyList());

        // Execute
        initializer.handleApplicationStartedEvent(event);

        // Verify
        verify(amfiService, times(1)).fetchAmfiSchemeData(any(Consumer.class));
        verify(bseStarMasterDataService, times(1)).fetchBseStarMasterData(eq("bseRawData"), anyMap(), anyMap());
        verify(mfFundSchemeService, times(1)).findDistinctAmfiCode();

        // Verify batching is done with the mocked batch size of 100
        verify(mfFundSchemeService, times(1)).saveDataInBatches(anyList(), eq(100));
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleApplicationStartedEventWithRetry() throws Exception {
        // Configure properties needed for this test
        given(properties.getRetryAttempts()).willReturn(2);
        given(properties.getRetryDelayMs()).willReturn(10L);
        given(bseStarMasterDataService.downloadBseMasterData()).willReturn("bseData");

        // Setup - first call throws exception, second succeeds
        doThrow(new IOException("Test exception"))
                .doAnswer(invocation -> {
                    Consumer<Map<String, Map<String, String>>> consumer = invocation.getArgument(0);
                    consumer.accept(createAmfiTestData(10));
                    return null;
                })
                .when(amfiService)
                .fetchAmfiSchemeData(any(Consumer.class));

        // Execute
        initializer.handleApplicationStartedEvent(event);

        // Verify
        verify(amfiService, times(2)).fetchAmfiSchemeData(any(Consumer.class));
    }

    private Map<String, Map<String, String>> createAmfiTestData(int count) {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (int i = 1; i <= count; i++) {
            String amfiCode = String.valueOf(i);
            Map<String, String> schemeData = new HashMap<>();
            schemeData.put("ISIN Div Payout/ ISIN GrowthISIN Div Reinvestment", "ISIN" + i);
            schemeData.put("NAME", "Test Scheme " + i);
            result.put(amfiCode, schemeData);
        }
        return result;
    }
}
