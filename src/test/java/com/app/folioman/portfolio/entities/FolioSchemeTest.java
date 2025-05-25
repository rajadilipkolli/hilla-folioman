package com.app.folioman.portfolio.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FolioSchemeTest {

    private FolioScheme folioScheme;
    private final LocalDate valuationDate = LocalDate.of(2025, 5, 25);
    private final BigDecimal valuation = BigDecimal.valueOf(1234.56);
    private final BigDecimal xirr = BigDecimal.valueOf(12.5);
    private final UserFolioDetails userFolioDetails = new UserFolioDetails();
    private final UserSchemeDetails userSchemeDetails = new UserSchemeDetails();

    @BeforeEach
    void setUp() {
        folioScheme = new FolioScheme();
        userFolioDetails.setId(1L);
        userSchemeDetails.setId(2L);
    }

    @Test
    void shouldSetAndGetId() {
        Long id = 42L;
        folioScheme.setId(id);
        assertThat(folioScheme.getId()).isEqualTo(id);
    }

    @Test
    void shouldSetAndGetValuation() {
        folioScheme.setValuation(valuation);
        assertThat(folioScheme.getValuation()).isEqualTo(valuation);
    }

    @Test
    void shouldSetAndGetXirr() {
        folioScheme.setXirr(xirr);
        assertThat(folioScheme.getXirr()).isEqualTo(xirr);
    }

    @Test
    void shouldSetAndGetValuationDate() {
        folioScheme.setValuationDate(valuationDate);
        assertThat(folioScheme.getValuationDate()).isEqualTo(valuationDate);
    }

    @Test
    void shouldSetAndGetUserFolioDetails() {
        folioScheme.setUserFolioDetails(userFolioDetails);
        assertThat(folioScheme.getUserFolioDetails()).isEqualTo(userFolioDetails);
    }

    @Test
    void shouldSetAndGetUserSchemeDetails() {
        folioScheme.setUserSchemeDetails(userSchemeDetails);
        assertThat(folioScheme.getUserSchemeDetails()).isEqualTo(userSchemeDetails);
    }
}
