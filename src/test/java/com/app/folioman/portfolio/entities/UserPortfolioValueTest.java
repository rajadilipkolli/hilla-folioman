package com.app.folioman.portfolio.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserPortfolioValueTest {

    private UserPortfolioValue userPortfolioValue;
    private UserCASDetails userCasDetails;

    @BeforeEach
    void setUp() {
        userPortfolioValue = new UserPortfolioValue();
        userCasDetails = new UserCASDetails();
        userCasDetails.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCasDetails.setFileTypeEnum(FileTypeEnum.CAMS);
        InvestorInfo ii = new InvestorInfo();
        ii.setEmail("");
        ii.setName("");
        userCasDetails.setInvestorInfo(ii);
    }

    @Test
    void getAndSetId() {
        Long id = 1L;

        UserPortfolioValue result = userPortfolioValue.setId(id);

        assertThat(userPortfolioValue.getId()).isEqualTo(id);
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetIdWithNull() {
        UserPortfolioValue result = userPortfolioValue.setId(null);

        assertThat(userPortfolioValue.getId()).isNull();
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetDate() {
        LocalDate date = LocalDate.of(2023, 12, 15);

        UserPortfolioValue result = userPortfolioValue.setDate(date);

        assertThat(userPortfolioValue.getDate()).isEqualTo(date);
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetDateWithNull() {
        UserPortfolioValue result = userPortfolioValue.setDate(null);

        assertThat(userPortfolioValue.getDate()).isNull();
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetInvested() {
        BigDecimal invested = new BigDecimal("10000.50");

        UserPortfolioValue result = userPortfolioValue.setInvested(invested);

        assertThat(userPortfolioValue.getInvested()).isEqualTo(invested);
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetInvestedWithNull() {
        UserPortfolioValue result = userPortfolioValue.setInvested(null);

        assertThat(userPortfolioValue.getInvested()).isNull();
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetValue() {
        BigDecimal value = new BigDecimal("12000.75");

        UserPortfolioValue result = userPortfolioValue.setValue(value);

        assertThat(userPortfolioValue.getValue()).isEqualTo(value);
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetValueWithNull() {
        UserPortfolioValue result = userPortfolioValue.setValue(null);

        assertThat(userPortfolioValue.getValue()).isNull();
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetXirr() {
        BigDecimal xirr = new BigDecimal("15.25");

        UserPortfolioValue result = userPortfolioValue.setXirr(xirr);

        assertThat(userPortfolioValue.getXirr()).isEqualTo(xirr);
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetXirrWithNull() {
        UserPortfolioValue result = userPortfolioValue.setXirr(null);

        assertThat(userPortfolioValue.getXirr()).isNull();
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetLiveXirr() {
        BigDecimal liveXirr = new BigDecimal("16.75");

        UserPortfolioValue result = userPortfolioValue.setLiveXirr(liveXirr);

        assertThat(userPortfolioValue.getLiveXirr()).isEqualTo(liveXirr);
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetLiveXirrWithNull() {
        UserPortfolioValue result = userPortfolioValue.setLiveXirr(null);

        assertThat(userPortfolioValue.getLiveXirr()).isNull();
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetUserCasDetails() {
        UserPortfolioValue result = userPortfolioValue.setUserCasDetails(userCasDetails);

        assertThat(userPortfolioValue.getUserCasDetails()).isEqualTo(userCasDetails);
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void getAndSetUserCasDetailsWithNull() {
        UserPortfolioValue result = userPortfolioValue.setUserCasDetails(null);

        assertThat(userPortfolioValue.getUserCasDetails()).isNull();
        assertThat(result).isSameAs(userPortfolioValue);
    }

    @Test
    void methodChaining() {
        Long id = 1L;
        LocalDate date = LocalDate.of(2023, 12, 15);
        BigDecimal invested = new BigDecimal("10000.00");
        BigDecimal value = new BigDecimal("12000.00");
        BigDecimal xirr = new BigDecimal("15.00");
        BigDecimal liveXirr = new BigDecimal("16.00");

        UserPortfolioValue result = userPortfolioValue
                .setId(id)
                .setDate(date)
                .setInvested(invested)
                .setValue(value)
                .setXirr(xirr)
                .setLiveXirr(liveXirr)
                .setUserCasDetails(userCasDetails);

        assertThat(result).isSameAs(userPortfolioValue);
        assertThat(userPortfolioValue.getId()).isEqualTo(id);
        assertThat(userPortfolioValue.getDate()).isEqualTo(date);
        assertThat(userPortfolioValue.getInvested()).isEqualTo(invested);
        assertThat(userPortfolioValue.getValue()).isEqualTo(value);
        assertThat(userPortfolioValue.getXirr()).isEqualTo(xirr);
        assertThat(userPortfolioValue.getLiveXirr()).isEqualTo(liveXirr);
        assertThat(userPortfolioValue.getUserCasDetails()).isEqualTo(userCasDetails);
    }
}
