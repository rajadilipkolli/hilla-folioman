package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
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

    @Autowired
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
                .updatePortfolioValue(casDetails);

        var schemeValues = schemeValueRepository.findAll();
        assertThat(schemeValues).isNotEmpty();
        // Get the latest value by date
        var value = schemeValues.stream()
                .max(java.util.Comparator.comparing(SchemeValueEntity::getDate))
                .orElseThrow();
        assertThat(value.getBalance()).isEqualByComparingTo(new BigDecimal("145.0"));

        BigDecimal realNav = new BigDecimal(
                mfNavService.getNavOnDate(amfiCode, value.getDate()).nav());
        BigDecimal expectedValue = value.getBalance().multiply(realNav);

        assertThat(value.getValue())
                .isCloseTo(expectedValue, org.assertj.core.data.Offset.offset(new BigDecimal("0.5")));
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

        List<Map<String, Object>> transactions = (List<Map<String, Object>>) fixture.get("transactions");
        for (Map<String, Object> t : transactions) {
            LocalDate date = LocalDate.parse((String) t.get("date"));
            BigDecimal amount = BigDecimal.valueOf((Double) t.get("amount"));
            Double nav = (Double) t.get("nav");
            Double units = (Double) t.get("units");
            TransactionType type = TransactionType.valueOf((String) t.get("type"));
            builder.addTransaction(date, type, amount, units, nav, units);
        }

        UserCasDetailsEntity casDetails = testEntityManager.persistFlushFind(builder.build());
        persistFolioSchemes(casDetails);

        ((PortfolioValueUpdateService) AopTestUtils.getTargetObject(portfolioValueUpdateService))
                .updatePortfolioValue(casDetails);

        var portfolios = userPortfolioValueRepository.findAll();
        assertThat(portfolios).isNotEmpty();
        var userPortfolio = portfolios.stream()
                .max(java.util.Comparator.comparing(UserPortfolioValueEntity::getDate))
                .orElseThrow();
        assertThat(userPortfolio.getXirr()).isNotNull();
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
                .updatePortfolioValue(casDetails);
        assertThat(userPortfolioValueRepository.count()).isGreaterThanOrEqualTo(1);

        long portfoliosBefore = userPortfolioValueRepository.count();
        long schemeValuesBefore = schemeValueRepository.count();

        ((PortfolioValueUpdateService) AopTestUtils.getTargetObject(portfolioValueUpdateService))
                .updatePortfolioValue(casDetails);
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
                .updatePortfolioValue(casDetails);

        var schemeValues = schemeValueRepository.findAll();
        // find the latest scheme value for our specific scheme
        var value = schemeValues.stream()
                .filter(sv -> sv.getUserSchemeDetails().getAmfi().equals(amfiCode))
                .max(java.util.Comparator.comparing(SchemeValueEntity::getDate))
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
