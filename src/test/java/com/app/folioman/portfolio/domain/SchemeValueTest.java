package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemeValueTest {

    private SchemeValueEntity SchemeValueEntity;
    private final LocalDate date = LocalDate.of(2025, 5, 25);
    private final BigDecimal invested = BigDecimal.valueOf(10000);
    private final BigDecimal value = BigDecimal.valueOf(12000);
    private final BigDecimal avgNav = BigDecimal.valueOf(55.5678);
    private final BigDecimal nav = BigDecimal.valueOf(60.1234);
    private final BigDecimal balance = BigDecimal.valueOf(210.555);
    private final UserSchemeDetailsEntity userSchemeDetailsEntity = new UserSchemeDetailsEntity();

    @BeforeEach
    void setUp() {
        SchemeValueEntity = new SchemeValueEntity();
        userSchemeDetailsEntity.setId(1L);
    }

    @Test
    void shouldSetAndGetId() {
        Long id = 42L;
        SchemeValueEntity.setId(id);
        assertThat(SchemeValueEntity.getId()).isEqualTo(id);
    }

    @Test
    void shouldSetAndGetDate() {
        SchemeValueEntity.setDate(date);
        assertThat(SchemeValueEntity.getDate()).isEqualTo(date);
    }

    @Test
    void shouldSetAndGetInvested() {
        SchemeValueEntity.setInvested(invested);
        assertThat(SchemeValueEntity.getInvested()).isEqualTo(invested);
    }

    @Test
    void shouldSetAndGetValue() {
        SchemeValueEntity.setValue(value);
        assertThat(SchemeValueEntity.getValue()).isEqualTo(value);
    }

    @Test
    void shouldSetAndGetAvgNav() {
        SchemeValueEntity.setAvgNav(avgNav);
        assertThat(SchemeValueEntity.getAvgNav()).isEqualTo(avgNav);
    }

    @Test
    void shouldSetAndGetNav() {
        SchemeValueEntity.setNav(nav);
        assertThat(SchemeValueEntity.getNav()).isEqualTo(nav);
    }

    @Test
    void shouldSetAndGetBalance() {
        SchemeValueEntity.setBalance(balance);
        assertThat(SchemeValueEntity.getBalance()).isEqualTo(balance);
    }

    @Test
    void shouldSetAndGetUserSchemeDetails() {
        SchemeValueEntity.setUserSchemeDetails(userSchemeDetailsEntity);
        assertThat(SchemeValueEntity.getUserSchemeDetails()).isEqualTo(userSchemeDetailsEntity);
    }
}
