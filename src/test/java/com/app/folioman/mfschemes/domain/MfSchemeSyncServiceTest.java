package com.app.folioman.mfschemes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.app.folioman.mfschemes.config.MfSchemesProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MfSchemeSyncServiceTest {

    @Mock
    private BSEStarMasterDataService bseStarMasterDataService;

    @Mock
    private AmfiService amfiService;

    @Mock
    private MfFundSchemeService mfFundSchemeService;

    @Mock
    private MfFundSchemeRepository mfFundSchemeRepository;

    @Mock
    private MfSchemesProperties properties;

    @InjectMocks
    private MfSchemeSyncService mfSchemeSyncService;

    @Captor
    private ArgumentCaptor<List<MfFundSchemeEntity>> savedSchemesCaptor;

    @Test
    @SuppressWarnings("unchecked")
    void syncAllSchemes_allNewSchemes() throws Exception {
        given(properties.getBatchSize()).willReturn(100);
        given(bseStarMasterDataService.downloadBseMasterData()).willReturn("bseData");

        BSEStarMasterDataService.BseMasterDataResult mockResult =
                new BSEStarMasterDataService.BseMasterDataResult(new HashMap<>(), new HashMap<>());
        given(bseStarMasterDataService.parseBseMasterData("bseData")).willReturn(mockResult);

        Map<String, Map<String, String>> amfiData =
                Map.of("101", Map.of("ISIN Div Payout/ ISIN GrowthISIN Div Reinvestment", "ISIN101"));

        doAnswer(invocation -> {
                    Consumer<Map<String, Map<String, String>>> consumer = invocation.getArgument(0);
                    consumer.accept(amfiData);
                    return null;
                })
                .when(amfiService)
                .fetchAmfiSchemeData(any(Consumer.class));

        MfFundSchemeEntity scheme1 = new MfFundSchemeEntity();
        scheme1.setAmfiCode(101L);
        scheme1.setName("New Scheme 1");

        Map<String, MfFundSchemeEntity> incomingMap = Map.of("101", scheme1);
        given(bseStarMasterDataService.processAmfiBatch(eq(mockResult), eq(amfiData), anyMap()))
                .willReturn(incomingMap);

        // No existing schemes
        given(mfFundSchemeRepository.findByAmfiCodeIn(anyCollection())).willReturn(List.of());

        given(mfFundSchemeService.saveDataInBatches(anyList(), anyInt())).willReturn(1);

        mfSchemeSyncService.syncAllSchemes();

        verify(mfFundSchemeService).saveDataInBatches(savedSchemesCaptor.capture(), eq(100));
        List<MfFundSchemeEntity> saved = savedSchemesCaptor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getName()).isEqualTo("New Scheme 1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void syncAllSchemes_mixOfNewChangedUnchanged() throws Exception {
        given(properties.getBatchSize()).willReturn(100);
        given(bseStarMasterDataService.downloadBseMasterData()).willReturn("bseData");

        BSEStarMasterDataService.BseMasterDataResult mockResult =
                new BSEStarMasterDataService.BseMasterDataResult(new HashMap<>(), new HashMap<>());
        given(bseStarMasterDataService.parseBseMasterData("bseData")).willReturn(mockResult);

        Map<String, Map<String, String>> amfiData = Map.of(
                "101", Map.of(),
                "102", Map.of(),
                "103", Map.of());

        doAnswer(invocation -> {
                    Consumer<Map<String, Map<String, String>>> consumer = invocation.getArgument(0);
                    consumer.accept(amfiData);
                    return null;
                })
                .when(amfiService)
                .fetchAmfiSchemeData(any(Consumer.class));

        MfFundSchemeEntity incomingNew =
                new MfFundSchemeEntity().setAmfiCode(101L).setName("New Scheme");
        MfFundSchemeEntity incomingChanged =
                new MfFundSchemeEntity().setAmfiCode(102L).setName("Changed Scheme Updated");
        MfFundSchemeEntity incomingUnchanged =
                new MfFundSchemeEntity().setAmfiCode(103L).setName("Unchanged Scheme");

        Map<String, MfFundSchemeEntity> incomingMap = Map.of(
                "101", incomingNew,
                "102", incomingChanged,
                "103", incomingUnchanged);
        given(bseStarMasterDataService.processAmfiBatch(eq(mockResult), eq(amfiData), anyMap()))
                .willReturn(incomingMap);

        MfFundSchemeEntity existingChanged =
                new MfFundSchemeEntity().setAmfiCode(102L).setName("Changed Scheme Old");
        MfFundSchemeEntity existingUnchanged =
                new MfFundSchemeEntity().setAmfiCode(103L).setName("Unchanged Scheme");

        given(mfFundSchemeRepository.findByAmfiCodeIn(anyCollection()))
                .willReturn(List.of(existingChanged, existingUnchanged));

        given(mfFundSchemeService.saveDataInBatches(anyList(), anyInt()))
                .willReturn(1); // 1 call for new, 1 call for update

        mfSchemeSyncService.syncAllSchemes();

        verify(mfFundSchemeService, times(2)).saveDataInBatches(anyList(), anyInt());
        assertThat(existingChanged.getName()).isEqualTo("Changed Scheme Updated");
    }

    @Test
    void syncAllSchemes_downloadFails() throws Exception {
        given(bseStarMasterDataService.downloadBseMasterData()).willThrow(new IOException("Network error"));

        try {
            mfSchemeSyncService.syncAllSchemes();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Failed to download or parse BSE Master Data");
            assertThat(e.getCause()).isInstanceOf(IOException.class);
        }

        verify(amfiService, times(0)).fetchAmfiSchemeData(any());
    }
}
