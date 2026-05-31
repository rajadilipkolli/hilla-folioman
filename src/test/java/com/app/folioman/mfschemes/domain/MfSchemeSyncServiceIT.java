package com.app.folioman.mfschemes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;

import com.app.folioman.shared.AbstractIntegrationTest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

class MfSchemeSyncServiceIT extends AbstractIntegrationTest {

    @MockitoBean
    private BSEStarMasterDataService bseStarMasterDataService;

    @MockitoBean
    private AmfiService amfiService;

    @MockitoBean
    private RestClient restClient; // prevent external network calls

    @Autowired
    private MfFundSchemeRepository mfFundSchemeRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM mfschemes.mf_fund_scheme");
    }

    @Test
    @SuppressWarnings("unchecked")
    void syncAllSchemes_integrationTest() throws Exception {
        // Pre-populate DB with an existing scheme
        MfFundSchemeEntity existing = new MfFundSchemeEntity();
        existing.setAmfiCode(201L);
        existing.setName("Existing Scheme Old Name");
        mfFundSchemeRepository.save(existing);

        MfFundSchemeEntity unchanged = new MfFundSchemeEntity();
        unchanged.setAmfiCode(202L);
        unchanged.setName("Unchanged Scheme");
        mfFundSchemeRepository.save(unchanged);

        // Setup mock inputs
        given(bseStarMasterDataService.downloadBseMasterData()).willReturn("mock-bse-data");
        BSEStarMasterDataService.BseMasterDataResult mockResult =
                new BSEStarMasterDataService.BseMasterDataResult(new HashMap<>(), new HashMap<>());
        given(bseStarMasterDataService.parseBseMasterData("mock-bse-data")).willReturn(mockResult);

        Map<String, Map<String, String>> amfiData = Map.of(
                "201", Map.of(),
                "202", Map.of(),
                "203", Map.of());

        doAnswer(invocation -> {
                    Consumer<Map<String, Map<String, String>>> consumer = invocation.getArgument(0);
                    consumer.accept(amfiData);
                    return null;
                })
                .when(amfiService)
                .fetchAmfiSchemeData(any(Consumer.class));

        MfFundSchemeEntity incomingChanged = new MfFundSchemeEntity();
        incomingChanged.setAmfiCode(201L).setName("Existing Scheme New Name");

        MfFundSchemeEntity incomingUnchanged = new MfFundSchemeEntity();
        incomingUnchanged.setAmfiCode(202L).setName("Unchanged Scheme");

        MfFundSchemeEntity incomingNew = new MfFundSchemeEntity();
        incomingNew.setAmfiCode(203L).setName("Brand New Scheme");

        Map<String, MfFundSchemeEntity> incomingMap = Map.of(
                "201", incomingChanged,
                "202", incomingUnchanged,
                "203", incomingNew);
        given(bseStarMasterDataService.processAmfiBatch(eq(mockResult), eq(amfiData), anyMap()))
                .willReturn(incomingMap);

        // Execute sync
        mfSchemeSyncService.syncAllSchemes();

        // Verify Database
        Optional<MfFundSchemeEntity> updatedScheme = mfFundSchemeRepository.findByAmfiCode(201L);
        assertThat(updatedScheme).isPresent();
        assertThat(updatedScheme.get().getName()).isEqualTo("Existing Scheme New Name");

        Optional<MfFundSchemeEntity> brandNewScheme = mfFundSchemeRepository.findByAmfiCode(203L);
        assertThat(brandNewScheme).isPresent();
        assertThat(brandNewScheme.get().getName()).isEqualTo("Brand New Scheme");

        Optional<MfFundSchemeEntity> keptScheme = mfFundSchemeRepository.findByAmfiCode(202L);
        assertThat(keptScheme).isPresent();
        assertThat(keptScheme.get().getName()).isEqualTo("Unchanged Scheme");
    }
}
