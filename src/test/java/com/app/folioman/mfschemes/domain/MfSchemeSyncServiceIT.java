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
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@TestPropertySource(properties = "application.initializer.enabled=false")
class MfSchemeSyncServiceIT extends AbstractIntegrationTest {

    @MockitoBean
    private BSEStarMasterDataService bseStarMasterDataService;

    @MockitoBean
    private AmfiService amfiService;

    @MockitoBean
    private RestClient restClient; // prevent external network calls

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "DELETE FROM mfschemes.mf_fund_scheme where amfi_code =201 or amfi_code =202 or amfi_code =203");
        jdbcTemplate.update("DELETE FROM mfschemes.mf_amc where code='TESTAMC'");
    }

    @Test
    @SuppressWarnings("unchecked")
    void syncAllSchemes_integrationTest() throws Exception {
        // Pre-populate DB with an existing scheme
        transactionTemplate.executeWithoutResult(_ -> {
            jdbcTemplate.update(
                    "INSERT INTO mfschemes.mf_amc (id, name, code, created_at, updated_at, version) VALUES (50001, 'Test AMC', 'TESTAMC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)");
            jdbcTemplate.update(
                    "INSERT INTO mfschemes.mf_fund_scheme (id, amfi_code, name, sid, mf_amc_id, created_at, updated_at, version) VALUES (nextval('mfschemes.mf_fund_scheme_seq'), 201, 'Existing Scheme Old Name', 1, 50001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)");
            jdbcTemplate.update(
                    "INSERT INTO mfschemes.mf_fund_scheme (id, amfi_code, name, sid, mf_amc_id, created_at, updated_at, version) VALUES (nextval('mfschemes.mf_fund_scheme_seq'), 202, 'Unchanged Scheme', 2, 50001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)");
        });

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

        MfAmcEntity amc = new MfAmcEntity();
        amc.setId(50001);
        amc.setVersion((short) 0);

        MfFundSchemeEntity incomingChanged = new MfFundSchemeEntity();
        incomingChanged
                .setAmfiCode(201L)
                .setName("Existing Scheme New Name")
                .setAmc(amc)
                .setSid(1)
                .setVersion((short) 1);

        MfFundSchemeEntity incomingUnchanged = new MfFundSchemeEntity();
        incomingUnchanged
                .setAmfiCode(202L)
                .setName("Unchanged Scheme")
                .setAmc(amc)
                .setSid(2)
                .setVersion((short) 0);

        MfFundSchemeEntity incomingNew = new MfFundSchemeEntity();
        incomingNew
                .setAmfiCode(203L)
                .setName("Brand New Scheme")
                .setAmc(amc)
                .setSid(3)
                .setVersion((short) 0);

        Map<String, MfFundSchemeEntity> incomingMap = Map.of(
                "201", incomingChanged,
                "202", incomingUnchanged,
                "203", incomingNew);
        given(bseStarMasterDataService.processAmfiBatch(eq(mockResult), eq(amfiData), anyMap()))
                .willReturn(incomingMap);

        // Execute sync
        mfSchemeSyncService.syncAllSchemes();

        // Verify Database
        String name201 = jdbcTemplate.queryForObject(
                "SELECT name FROM mfschemes.mf_fund_scheme WHERE amfi_code = 201", String.class);
        assertThat(name201).isEqualTo("Existing Scheme New Name");

        String name203 = jdbcTemplate.queryForObject(
                "SELECT name FROM mfschemes.mf_fund_scheme WHERE amfi_code = 203", String.class);
        assertThat(name203).isEqualTo("Brand New Scheme");

        String name202 = jdbcTemplate.queryForObject(
                "SELECT name FROM mfschemes.mf_fund_scheme WHERE amfi_code = 202", String.class);
        assertThat(name202).isEqualTo("Unchanged Scheme");
    }
}
