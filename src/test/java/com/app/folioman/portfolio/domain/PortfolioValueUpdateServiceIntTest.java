package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;

@AutoConfigureTestEntityManager
@Transactional
@TestMethodOrder(OrderAnnotation.class)
class PortfolioValueUpdateServiceIntTest extends AbstractIntegrationTest {

    @Autowired
    private PortfolioValueUpdateService portfolioValueUpdateService;

    @Autowired
    private UserPortfolioValueRepository userPortfolioValueRepository;

    @Autowired
    private SchemeValueRepository schemeValueRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @MockitoSpyBean
    private MFNavService mfNavService;

    @Test
    @Order(1)
    void shouldCalculateCorrectBalanceAndValueAfterBuysAndSells() {
        Long amfiCode = 120684L;

        UserCasDetailsEntity casDetails = TransactionTestDataBuilder.builder()
                .withInvestor("test1@example.com", "Test User 1")
                .withFolio("91095687154 / 0", "AXIS Mutual Fund", "PAN123")
                .withScheme("Axis ELSS Tax Saver Fund - Direct Growth - ISIN: INF846K01EW2", "INF846K01EW2", amfiCode)
                .addTransaction(
                        LocalDate.of(2023, 1, 2), TransactionType.PURCHASE, new BigDecimal("1000"), 100.0, 10.0, 100.0)
                .addTransaction(
                        LocalDate.of(2023, 2, 1), TransactionType.PURCHASE, new BigDecimal("600"), 50.0, 12.0, 150.0)
                .addTransaction(
                        LocalDate.of(2023, 3, 1), TransactionType.PURCHASE, new BigDecimal("825"), 75.0, 11.0, 225.0)
                .addTransaction(
                        LocalDate.of(2023, 4, 3),
                        TransactionType.REDEMPTION,
                        new BigDecimal("1200"),
                        -80.0,
                        15.0,
                        145.0)
                .build();

        casDetails = testEntityManager.persistFlushFind(casDetails);
        persistFolioSchemes(casDetails);

        ((PortfolioValueUpdateService) AopTestUtils.getTargetObject(portfolioValueUpdateService))
                .updatePortfolioValue(casDetails.getId());

        final Long casId = casDetails.getId();
        var schemeValues = schemeValueRepository.findAll();
        assertThat(schemeValues).isNotEmpty();
        // Get the latest value by date
        var value = schemeValues.stream()
                .filter(sv -> sv.getUserSchemeDetails()
                        .getUserFolioDetails()
                        .getUserCasDetailsEntity()
                        .getId()
                        .equals(casId))
                .max(Comparator.comparing(SchemeValueEntity::getDate))
                .orElseThrow();
        assertThat(value.getBalance()).isEqualByComparingTo(new BigDecimal("145.0"));

        BigDecimal realNav = new BigDecimal(
                mfNavService.getNavOnDate(amfiCode, value.getDate()).nav());
        BigDecimal expectedValue = value.getBalance().multiply(realNav);

        assertThat(value.getValue()).isCloseTo(expectedValue, offset(new BigDecimal("0.5")));
    }

    @Test
    @Order(2)
    void shouldCalculateXirrForMonthlySipScenario() throws Exception {
        Long amfiCode = 120684L;
        String fixtureJson = Files.readString(Path.of("src/test/resources/fixtures/sip_xirr_fixture.json"));
        Map<String, Object> fixture = jsonMapper.readValue(fixtureJson, new TypeReference<Map<String, Object>>() {});

        TransactionTestDataBuilder builder = TransactionTestDataBuilder.builder()
                .withInvestor("sip@example.com", "SIP User")
                .withFolio("91095687154 / 0", "AXIS Mutual Fund", "PANSIP")
                .withScheme("Axis ELSS Tax Saver Fund - Direct Growth - ISIN: INF846K01EW2", "INF846K01EW2", amfiCode);

        Double runningBalance = 0.0;
        List<Map<String, Object>> transactions = (List<Map<String, Object>>) fixture.get("transactions");
        for (Map<String, Object> t : transactions) {
            LocalDate date = LocalDate.parse((String) t.get("date"));
            BigDecimal amount = BigDecimal.valueOf((Double) t.get("amount"));
            Double nav = (Double) t.get("nav");
            Double units = (Double) t.get("units");
            TransactionType type = TransactionType.valueOf((String) t.get("type"));
            runningBalance += units;
            builder.addTransaction(date, type, amount, units, nav, runningBalance);
        }

        UserCasDetailsEntity casDetails = testEntityManager.persistFlushFind(builder.build());
        persistFolioSchemes(casDetails);

        Map<String, Object> navsJson = (Map<String, Object>) fixture.get("navs");
        Map<LocalDate, com.app.folioman.mfschemes.rest.dtos.MFSchemeNavProjection> mockNavMap =
                new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : navsJson.entrySet()) {
            LocalDate d = LocalDate.parse(entry.getKey());
            Double v = ((Number) entry.getValue()).doubleValue();
            mockNavMap.put(
                    d,
                    new com.app.folioman.mfschemes.rest.dtos.MFSchemeNavProjection(BigDecimal.valueOf(v), d, amfiCode));
        }

        try (org.mockito.MockedStatic<com.app.folioman.shared.LocalDateUtility> mockedStatic =
                org.mockito.Mockito.mockStatic(
                        com.app.folioman.shared.LocalDateUtility.class, org.mockito.Mockito.CALLS_REAL_METHODS)) {
            mockedStatic
                    .when(com.app.folioman.shared.LocalDateUtility::getYesterday)
                    .thenReturn(LocalDate.of(2023, 12, 29));
            org.mockito.Mockito.doReturn(Map.of(amfiCode, mockNavMap))
                    .when(mfNavService)
                    .getNavsForSchemesAndDates(
                            org.mockito.Mockito.anySet(), org.mockito.Mockito.any(), org.mockito.Mockito.any());

            ((PortfolioValueUpdateService) AopTestUtils.getTargetObject(portfolioValueUpdateService))
                    .updatePortfolioValue(casDetails.getId());
        }

        final Long casId = casDetails.getId();
        var portfolios = userPortfolioValueRepository.findAll();
        assertThat(portfolios).isNotEmpty();
        var userPortfolio = portfolios.stream()
                .filter(upv -> upv.getUserCasDetails().getId().equals(casId))
                .max(Comparator.comparing(UserPortfolioValueEntity::getDate))
                .orElseThrow();
        Double expectedXirr = ((Number) fixture.get("expectedXirr")).doubleValue();
        BigDecimal actualXirrPercent = userPortfolio.getXirr().multiply(new BigDecimal("100"));
        assertThat(actualXirrPercent).isCloseTo(BigDecimal.valueOf(expectedXirr), offset(new BigDecimal("0.5")));
    }

    @Test
    @Order(3)
    void shouldNotCreateDuplicatesOnReprocessing() {
        Long amfiCode = 120684L;

        UserCasDetailsEntity casDetails = TransactionTestDataBuilder.builder()
                .withInvestor("idemp@example.com", "Idemp User")
                .withFolio("91095687154 / 0", "AXIS Mutual Fund", "PAN333")
                .withScheme("Axis ELSS Tax Saver Fund - Direct Growth - ISIN: INF846K01EW2", "INF846K01EW2", amfiCode)
                .addTransaction(
                        LocalDate.of(2023, 1, 2), TransactionType.PURCHASE, new BigDecimal("1000"), 100.0, 10.0, 100.0)
                .build();

        casDetails = testEntityManager.persistFlushFind(casDetails);
        persistFolioSchemes(casDetails);

        ((PortfolioValueUpdateService) AopTestUtils.getTargetObject(portfolioValueUpdateService))
                .updatePortfolioValue(casDetails.getId());
        assertThat(userPortfolioValueRepository.count()).isGreaterThanOrEqualTo(1);

        long portfoliosBefore = userPortfolioValueRepository.count();
        long schemeValuesBefore = schemeValueRepository.count();

        ((PortfolioValueUpdateService) AopTestUtils.getTargetObject(portfolioValueUpdateService))
                .updatePortfolioValue(casDetails.getId());
        // Processing should finish quickly
        assertThat(userPortfolioValueRepository.count()).isEqualTo(portfoliosBefore);
        assertThat(schemeValueRepository.count()).isEqualTo(schemeValuesBefore);
    }

    @Test
    @Order(4)
    void shouldHandleSchemeWithZeroBalanceAfterFullRedemption() {
        Long amfiCode = 120684L;

        UserCasDetailsEntity casDetails = TransactionTestDataBuilder.builder()
                .withInvestor("zero@example.com", "Zero User")
                .withFolio("91095687154 / 0", "AXIS Mutual Fund", "PAN444")
                .withScheme("Axis ELSS Tax Saver Fund - Direct Growth - ISIN: INF846K01EW2", "INF846K01EW2", amfiCode)
                .addTransaction(
                        LocalDate.of(2023, 1, 2), TransactionType.PURCHASE, new BigDecimal("1000"), 100.0, 10.0, 100.0)
                .addTransaction(
                        LocalDate.of(2023, 2, 1), TransactionType.REDEMPTION, new BigDecimal("1500"), -100.0, 15.0, 0.0)
                .build();

        casDetails = testEntityManager.persistFlushFind(casDetails);
        persistFolioSchemes(casDetails);

        ((PortfolioValueUpdateService) AopTestUtils.getTargetObject(portfolioValueUpdateService))
                .updatePortfolioValue(casDetails.getId());

        final Long casId = casDetails.getId();
        var schemeValues = schemeValueRepository.findAll();
        // find the latest scheme value for our specific scheme
        var value = schemeValues.stream()
                .filter(sv -> sv.getUserSchemeDetails()
                        .getUserFolioDetails()
                        .getUserCasDetailsEntity()
                        .getId()
                        .equals(casId))
                .max(Comparator.comparing(SchemeValueEntity::getDate))
                .orElse(null);
        assertThat(value).isNotNull();
        assertThat(value.getBalance()).isEqualByComparingTo(new BigDecimal("0.0"));
        assertThat(value.getValue()).isEqualByComparingTo(new BigDecimal("0.0"));
    }

    private void persistFolioSchemes(UserCasDetailsEntity casDetails) {
        for (var folio : casDetails.getFolios()) {
            for (var scheme : folio.getSchemes()) {
                FolioSchemeEntity folioScheme = new FolioSchemeEntity();
                folioScheme.setUserFolioDetailsEntity(folio);
                folioScheme.setUserSchemeDetailsEntity(scheme);
                testEntityManager.persist(folioScheme);
            }
        }
        testEntityManager.flush();
    }
}
