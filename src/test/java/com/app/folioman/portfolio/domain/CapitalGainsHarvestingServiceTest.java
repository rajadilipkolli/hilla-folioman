package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeTypeProjection;
import com.app.folioman.portfolio.config.CapitalGainsTaxProperties;
import com.app.folioman.portfolio.config.ExitLoadProperties;
import com.app.folioman.portfolio.domain.models.CapitalGainsHarvestingRequest;
import com.app.folioman.portfolio.domain.models.CapitalGainsHarvestingResponse;
import com.app.folioman.portfolio.domain.models.projection.PortfolioDetailsProjection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CapitalGainsHarvestingServiceTest {

    @Mock
    private UserCASDetailsRepository userCASDetailsRepository;

    @Mock
    private UserTransactionDetailsRepository userTransactionDetailsRepository;

    @Mock
    private MfSchemeService mfSchemeService;

    @Mock
    private MFNavService mfNavService;

    @Mock
    private CapitalGainsTaxProperties taxProperties;

    @Mock
    private ExitLoadProperties exitLoadProperties;

    @InjectMocks
    private CapitalGainsHarvestingService service;

    @BeforeEach
    void setUp() {
        CapitalGainsTaxProperties.TaxRuleSet equityRule = new CapitalGainsTaxProperties.TaxRuleSet();
        equityRule.setLongTermThresholdMonths(12);
        equityRule.setStcgTaxRate(new BigDecimal("0.15"));
        equityRule.setLtcgTaxRate(new BigDecimal("0.10"));
        equityRule.setAnnualLtcgExemptionLimit(new BigDecimal("100000"));

        org.mockito.Mockito.lenient().when(taxProperties.getEquity()).thenReturn(equityRule);
    }

    private PortfolioDetailsProjection createHolding(String schemeName, String folio, Long schemeId, Long detailId) {
        return new PortfolioDetailsProjection() {
            @Override
            public String getSchemeName() {
                return schemeName;
            }

            @Override
            public String getFolioNumber() {
                return folio;
            }

            @Override
            public Double getBalanceUnits() {
                return 100.0;
            }

            @Override
            public Long getSchemeId() {
                return schemeId;
            }

            @Override
            public Long getSchemeDetailId() {
                return detailId;
            }
        };
    }

    private UserTransactionDetailsEntity createTxn(BigDecimal amount, Double units, Double nav, LocalDate date) {
        UserTransactionDetailsEntity txn = new UserTransactionDetailsEntity();
        txn.setAmount(amount);
        txn.setUnits(units);
        txn.setNav(nav);
        txn.setTransactionDate(date);
        txn.setType(TransactionType.PURCHASE_SIP);
        return txn;
    }

    private void mockSchemeAsEquity(Long amfiCode) {
        MFSchemeTypeProjection typeProj = new MFSchemeTypeProjection() {
            @Override
            public String getCategory() {
                return "Equity Scheme";
            }
        };
        MFSchemeProjection schemeProj = new MFSchemeProjection() {
            @Override
            public Long getAmfiCode() {
                return amfiCode;
            }

            @Override
            public @Nullable String getIsin() {
                return null;
            }

            @Override
            public @Nullable MFSchemeTypeProjection getMfSchemeTypeEntity() {
                return typeProj;
            }
        };
        when(mfSchemeService.findByAmfiCode(amfiCode)).thenReturn(Optional.of(schemeProj));
    }

    @Test
    void shouldReturnEmptyWhenNoEligibleHoldings() {
        CapitalGainsHarvestingRequest request =
                new CapitalGainsHarvestingRequest("ABCDE1234F", null, true, true, false, null, null, null, null);

        when(userCASDetailsRepository.getPortfolioDetails(eq("ABCDE1234F"), any()))
                .thenReturn(List.of());

        CapitalGainsHarvestingResponse response = service.generateHarvestingPlan(request);
        assertThat(response.recommendations()).isEmpty();
    }

    @Test
    void shouldPrioritizeLtcgExemptionFirst() {
        String pan = "ABCDE1234F";
        PortfolioDetailsProjection holding = createHolding("Equity Fund", "FOLIO1", 123456L, 1L);

        // 14 months ago
        LocalDate buyDate = LocalDate.now().minusMonths(14);
        UserTransactionDetailsEntity buy = createTxn(new BigDecimal("10000"), 1000.0, 10.0, buyDate);

        when(userCASDetailsRepository.getPortfolioDetails(eq(pan), any())).thenReturn(List.of(holding));
        when(userTransactionDetailsRepository.findByUserSchemeDetails_IdOrderByTransactionDateAsc(eq(1L)))
                .thenReturn(List.of(buy));

        mockSchemeAsEquity(123456L);

        // Mock NAV
        when(mfNavService.getNav(123456L))
                .thenReturn(new MFSchemeDTO(
                        "AMC", 123456L, "INE123", "Equity Fund", "50.0", "2023-10-10", "Equity Scheme"));

        org.mockito.Mockito.lenient()
                .when(exitLoadProperties.getApplicabilityWindowDays())
                .thenReturn(365);
        org.mockito.Mockito.lenient()
                .when(exitLoadProperties.getDefaultPercentage())
                .thenReturn(new BigDecimal("1.0"));

        CapitalGainsHarvestingRequest request =
                new CapitalGainsHarvestingRequest(pan, null, true, true, false, null, null, null, null);

        CapitalGainsHarvestingResponse response = service.generateHarvestingPlan(request);

        assertThat(response.recommendations()).hasSize(1);
        var rec = response.recommendations().get(0);
        // Exemption covers 1L, since profit is 40k, estimated tax is 0.
        assertThat(rec.estimatedTax()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
