package com.app.folioman.portfolio.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for capital gains tax rules.
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "app.tax.capital-gains")
@Validated
@SuppressWarnings("NullAway.Init")
public class CapitalGainsTaxProperties {

    @Valid
    @NotNull
    private TaxRuleSet equity = new TaxRuleSet();

    @Valid
    @NotNull
    private TaxRuleSet nonEquity = new TaxRuleSet();

    public TaxRuleSet getEquity() {
        return equity;
    }

    public void setEquity(TaxRuleSet equity) {
        this.equity = equity;
    }

    public TaxRuleSet getNonEquity() {
        return nonEquity;
    }

    public void setNonEquity(TaxRuleSet nonEquity) {
        this.nonEquity = nonEquity;
    }

    public static class TaxRuleSet {

        @PositiveOrZero
        private int longTermThresholdMonths;

        @NotNull
        @PositiveOrZero
        private BigDecimal stcgTaxRate;

        @NotNull
        @PositiveOrZero
        private BigDecimal ltcgTaxRate;

        @NotNull
        @PositiveOrZero
        private BigDecimal annualLtcgExemptionLimit;

        public int getLongTermThresholdMonths() {
            return longTermThresholdMonths;
        }

        public void setLongTermThresholdMonths(int longTermThresholdMonths) {
            this.longTermThresholdMonths = longTermThresholdMonths;
        }

        public BigDecimal getStcgTaxRate() {
            return stcgTaxRate;
        }

        public void setStcgTaxRate(BigDecimal stcgTaxRate) {
            this.stcgTaxRate = stcgTaxRate;
        }

        public BigDecimal getLtcgTaxRate() {
            return ltcgTaxRate;
        }

        public void setLtcgTaxRate(BigDecimal ltcgTaxRate) {
            this.ltcgTaxRate = ltcgTaxRate;
        }

        public BigDecimal getAnnualLtcgExemptionLimit() {
            return annualLtcgExemptionLimit;
        }

        public void setAnnualLtcgExemptionLimit(BigDecimal annualLtcgExemptionLimit) {
            this.annualLtcgExemptionLimit = annualLtcgExemptionLimit;
        }
    }
}
