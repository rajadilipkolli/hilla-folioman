package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserPortfolioValueTest {

    private UserPortfolioValueEntity UserPortfolioValueEntity;
    private UserCasDetailsEntity userCasDetailsEntity;

    @BeforeEach
    void setUp() {
        UserPortfolioValueEntity = new UserPortfolioValueEntity();
        userCasDetailsEntity = new UserCasDetailsEntity();
        userCasDetailsEntity.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCasDetailsEntity.setFileTypeEnum(FileTypeEnum.CAMS);
        InvestorInfoEntity ii = new InvestorInfoEntity();
        ii.setEmail("");
        ii.setName("");
        userCasDetailsEntity.setInvestorInfoEntity(ii);
    }

    @Test
    void getAndSetId() {
        Long id = 1L;

        UserPortfolioValueEntity result = UserPortfolioValueEntity.setId(id);

        assertThat(UserPortfolioValueEntity.getId()).isEqualTo(id);
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetIdWithNull() {
        UserPortfolioValueEntity result = UserPortfolioValueEntity.setId(null);

        assertThat(UserPortfolioValueEntity.getId()).isNull();
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetDate() {
        LocalDate date = LocalDate.of(2023, 12, 15);

        UserPortfolioValueEntity result = UserPortfolioValueEntity.setDate(date);

        assertThat(UserPortfolioValueEntity.getDate()).isEqualTo(date);
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetDateWithNull() {
        UserPortfolioValueEntity result = UserPortfolioValueEntity.setDate(null);

        assertThat(UserPortfolioValueEntity.getDate()).isNull();
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetInvested() {
        BigDecimal invested = new BigDecimal("10000.50");

        UserPortfolioValueEntity result = UserPortfolioValueEntity.setInvested(invested);

        assertThat(UserPortfolioValueEntity.getInvested()).isEqualTo(invested);
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetInvestedWithNull() {
        UserPortfolioValueEntity result = UserPortfolioValueEntity.setInvested(null);

        assertThat(UserPortfolioValueEntity.getInvested()).isNull();
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetValue() {
        BigDecimal value = new BigDecimal("12000.75");

        UserPortfolioValueEntity result = UserPortfolioValueEntity.setValue(value);

        assertThat(UserPortfolioValueEntity.getValue()).isEqualTo(value);
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetValueWithNull() {
        UserPortfolioValueEntity result = UserPortfolioValueEntity.setValue(null);

        assertThat(UserPortfolioValueEntity.getValue()).isNull();
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetXirr() {
        BigDecimal xirr = new BigDecimal("15.25");

        UserPortfolioValueEntity result = UserPortfolioValueEntity.setXirr(xirr);

        assertThat(UserPortfolioValueEntity.getXirr()).isEqualTo(xirr);
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetXirrWithNull() {
        UserPortfolioValueEntity result = UserPortfolioValueEntity.setXirr(null);

        assertThat(UserPortfolioValueEntity.getXirr()).isNull();
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetLiveXirr() {
        BigDecimal liveXirr = new BigDecimal("16.75");

        UserPortfolioValueEntity result = UserPortfolioValueEntity.setLiveXirr(liveXirr);

        assertThat(UserPortfolioValueEntity.getLiveXirr()).isEqualTo(liveXirr);
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetLiveXirrWithNull() {
        UserPortfolioValueEntity result = UserPortfolioValueEntity.setLiveXirr(null);

        assertThat(UserPortfolioValueEntity.getLiveXirr()).isNull();
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetUserCasDetails() {
        UserPortfolioValueEntity result = UserPortfolioValueEntity.setUserCasDetails(userCasDetailsEntity);

        assertThat(UserPortfolioValueEntity.getUserCasDetails()).isEqualTo(userCasDetailsEntity);
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void getAndSetUserCasDetailsWithNull() {
        UserPortfolioValueEntity result = UserPortfolioValueEntity.setUserCasDetails(null);

        assertThat(UserPortfolioValueEntity.getUserCasDetails()).isNull();
        assertThat(result).isSameAs(UserPortfolioValueEntity);
    }

    @Test
    void methodChaining() {
        Long id = 1L;
        LocalDate date = LocalDate.of(2023, 12, 15);
        BigDecimal invested = new BigDecimal("10000.00");
        BigDecimal value = new BigDecimal("12000.00");
        BigDecimal xirr = new BigDecimal("15.00");
        BigDecimal liveXirr = new BigDecimal("16.00");

        UserPortfolioValueEntity result = UserPortfolioValueEntity.setId(id)
                .setDate(date)
                .setInvested(invested)
                .setValue(value)
                .setXirr(xirr)
                .setLiveXirr(liveXirr)
                .setUserCasDetails(userCasDetailsEntity);

        assertThat(result).isSameAs(UserPortfolioValueEntity);
        assertThat(UserPortfolioValueEntity.getId()).isEqualTo(id);
        assertThat(UserPortfolioValueEntity.getDate()).isEqualTo(date);
        assertThat(UserPortfolioValueEntity.getInvested()).isEqualTo(invested);
        assertThat(UserPortfolioValueEntity.getValue()).isEqualTo(value);
        assertThat(UserPortfolioValueEntity.getXirr()).isEqualTo(xirr);
        assertThat(UserPortfolioValueEntity.getLiveXirr()).isEqualTo(liveXirr);
        assertThat(UserPortfolioValueEntity.getUserCasDetails()).isEqualTo(userCasDetailsEntity);
    }
}
