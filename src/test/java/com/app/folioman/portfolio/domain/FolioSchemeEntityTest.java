package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FolioSchemeEntityTest {

    private FolioSchemeEntity FolioSchemeEntity;
    private final LocalDate valuationDate = LocalDate.of(2025, 5, 25);
    private final BigDecimal valuation = BigDecimal.valueOf(1234.56);
    private final BigDecimal xirr = BigDecimal.valueOf(12.5);
    private final UserFolioDetailsEntity userFolioDetailsEntity = new UserFolioDetailsEntity();
    private final UserSchemeDetailsEntity userSchemeDetailsEntity = new UserSchemeDetailsEntity();

    @BeforeEach
    void setUp() {
        FolioSchemeEntity = new FolioSchemeEntity();
        userFolioDetailsEntity.setId(1L);
        userSchemeDetailsEntity.setId(2L);
    }

    @Test
    void shouldSetAndGetId() {
        Long id = 42L;
        FolioSchemeEntity.setId(id);
        assertThat(FolioSchemeEntity.getId()).isEqualTo(id);
    }

    @Test
    void shouldSetAndGetValuation() {
        FolioSchemeEntity.setValuation(valuation);
        assertThat(FolioSchemeEntity.getValuation()).isEqualTo(valuation);
    }

    @Test
    void shouldSetAndGetXirr() {
        FolioSchemeEntity.setXirr(xirr);
        assertThat(FolioSchemeEntity.getXirr()).isEqualTo(xirr);
    }

    @Test
    void shouldSetAndGetValuationDate() {
        FolioSchemeEntity.setValuationDate(valuationDate);
        assertThat(FolioSchemeEntity.getValuationDate()).isEqualTo(valuationDate);
    }

    @Test
    void shouldSetAndGetUserFolioDetails() {
        FolioSchemeEntity.setUserFolioDetailsEntity(userFolioDetailsEntity);
        assertThat(FolioSchemeEntity.getUserFolioDetails()).isEqualTo(userFolioDetailsEntity);
    }

    @Test
    void shouldSetAndGetUserSchemeDetails() {
        FolioSchemeEntity.setUserSchemeDetailsEntity(userSchemeDetailsEntity);
        assertThat(FolioSchemeEntity.getUserSchemeDetails()).isEqualTo(userSchemeDetailsEntity);
    }
}
