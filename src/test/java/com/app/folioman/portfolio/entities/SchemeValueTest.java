package com.app.folioman.portfolio.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemeValueTest {

    private SchemeValue schemeValue;
    private final LocalDate date = LocalDate.of(2025, 5, 25);
    private final BigDecimal invested = BigDecimal.valueOf(10000);
    private final BigDecimal value = BigDecimal.valueOf(12000);
    private final BigDecimal avgNav = BigDecimal.valueOf(55.5678);
    private final BigDecimal nav = BigDecimal.valueOf(60.1234);
    private final BigDecimal balance = BigDecimal.valueOf(210.555);
    private final UserSchemeDetails userSchemeDetails = new UserSchemeDetails();

    @BeforeEach
    void setUp() {
        schemeValue = new SchemeValue();
        userSchemeDetails.setId(1L);
    }

    @Test
    void shouldSetAndGetId() {
        Long id = 42L;
        schemeValue.setId(id);
        assertThat(schemeValue.getId()).isEqualTo(id);
    }

    @Test
    void shouldSetAndGetDate() {
        schemeValue.setDate(date);
        assertThat(schemeValue.getDate()).isEqualTo(date);
    }

    @Test
    void shouldSetAndGetInvested() {
        schemeValue.setInvested(invested);
        assertThat(schemeValue.getInvested()).isEqualTo(invested);
    }

    @Test
    void shouldSetAndGetValue() {
        schemeValue.setValue(value);
        assertThat(schemeValue.getValue()).isEqualTo(value);
    }

    @Test
    void shouldSetAndGetAvgNav() {
        schemeValue.setAvgNav(avgNav);
        assertThat(schemeValue.getAvgNav()).isEqualTo(avgNav);
    }

    @Test
    void shouldSetAndGetNav() {
        schemeValue.setNav(nav);
        assertThat(schemeValue.getNav()).isEqualTo(nav);
    }

    @Test
    void shouldSetAndGetBalance() {
        schemeValue.setBalance(balance);
        assertThat(schemeValue.getBalance()).isEqualTo(balance);
    }

    @Test
    void shouldSetAndGetUserSchemeDetails() {
        schemeValue.setUserSchemeDetails(userSchemeDetails);
        assertThat(schemeValue.getUserSchemeDetails()).isEqualTo(userSchemeDetails);
    }
}
