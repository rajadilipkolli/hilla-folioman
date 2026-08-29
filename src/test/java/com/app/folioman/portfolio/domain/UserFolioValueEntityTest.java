package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class UserFolioValueEntityTest {

    @Test
    void testGettersAndSetters() {
        UserFolioValueEntity entity = new UserFolioValueEntity();
        UserFolioDetailsEntity folioDetails = new UserFolioDetailsEntity();
        folioDetails.setId(10L);

        entity.setId(1L)
                .setDate(LocalDate.of(2022, 1, 1))
                .setInvested(new BigDecimal("1000"))
                .setValue(new BigDecimal("1100"))
                .setUserFolioDetailsEntity(folioDetails);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2022, 1, 1));
        assertThat(entity.getInvested()).isEqualTo(new BigDecimal("1000"));
        assertThat(entity.getValue()).isEqualTo(new BigDecimal("1100"));
        assertThat(entity.getUserFolioDetailsEntity()).isEqualTo(folioDetails);
    }

    @Test
    void testEqualsAndHashCode() {
        UserFolioDetailsEntity folioDetails1 = new UserFolioDetailsEntity();
        folioDetails1.setId(10L);

        UserFolioDetailsEntity folioDetails2 = new UserFolioDetailsEntity();
        folioDetails2.setId(20L);

        UserFolioValueEntity entity1 =
                new UserFolioValueEntity().setDate(LocalDate.of(2022, 1, 1)).setUserFolioDetailsEntity(folioDetails1);

        UserFolioValueEntity entity2 =
                new UserFolioValueEntity().setDate(LocalDate.of(2022, 1, 1)).setUserFolioDetailsEntity(folioDetails1);

        UserFolioValueEntity entity3 =
                new UserFolioValueEntity().setDate(LocalDate.of(2022, 1, 2)).setUserFolioDetailsEntity(folioDetails1);

        UserFolioValueEntity entity4 =
                new UserFolioValueEntity().setDate(LocalDate.of(2022, 1, 1)).setUserFolioDetailsEntity(folioDetails2);

        assertThat(entity1).isEqualTo(entity1);
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1).isNotEqualTo(entity3);
        assertThat(entity1).isNotEqualTo(entity4);
        assertThat(entity1).isNotEqualTo(new Object());
        assertThat(entity1).isNotEqualTo(null);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        assertThat(entity1.hashCode()).isNotEqualTo(entity3.hashCode());
    }

    @Test
    void testEqualsAndHashCodeWithNullDetails() {
        UserFolioValueEntity entity1 = new UserFolioValueEntity().setDate(LocalDate.of(2022, 1, 1));

        UserFolioValueEntity entity2 = new UserFolioValueEntity().setDate(LocalDate.of(2022, 1, 1));

        assertThat(entity1).isNotEqualTo(entity2); // because getUserFolioDetailsEntity is null

        UserFolioDetailsEntity folioDetails1 = new UserFolioDetailsEntity();
        folioDetails1.setId(10L);
        entity2.setUserFolioDetailsEntity(folioDetails1);

        assertThat(entity1).isNotEqualTo(entity2);
        assertThat(entity2).isNotEqualTo(entity1);
    }
}
