package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InvestorInfoEntityTest {

    @Test
    void testGettersAndSetters() {
        InvestorInfoEntity entity = new InvestorInfoEntity();
        UserCasDetailsEntity casDetails = new UserCasDetailsEntity();
        casDetails.setId(100L);

        entity.setId(1L)
                .setEmail("test@example.com")
                .setName("John Doe")
                .setMobile("1234567890")
                .setAddress("123 Main St")
                .setUserCasDetailsEntity(casDetails);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getEmail()).isEqualTo("test@example.com");
        assertThat(entity.getName()).isEqualTo("John Doe");
        assertThat(entity.getMobile()).isEqualTo("1234567890");
        assertThat(entity.getAddress()).isEqualTo("123 Main St");
        assertThat(entity.getUserCasDetailsEntity()).isEqualTo(casDetails);
    }

    @Test
    void testEqualsAndHashCode() {
        InvestorInfoEntity entity1 = new InvestorInfoEntity().setId(1L).setEmail("test@example.com");
        InvestorInfoEntity entity2 = new InvestorInfoEntity().setId(2L).setEmail("test@example.com");
        InvestorInfoEntity entity3 = new InvestorInfoEntity().setId(3L).setEmail("different@example.com");

        assertThat(entity1).isEqualTo(entity1);
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1).isNotEqualTo(entity3);
        assertThat(entity1).isNotEqualTo(new Object());
        assertThat(entity1).isNotEqualTo(null);

        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        assertThat(entity1.hashCode()).isEqualTo(entity3.hashCode()); // Due to getClass().hashCode()
    }

    @Test
    void testEqualsWithNullId() {
        InvestorInfoEntity entity1 = new InvestorInfoEntity().setEmail("test@example.com");
        InvestorInfoEntity entity2 = new InvestorInfoEntity().setEmail("test@example.com");

        // As per implementation: return id != null && Objects.equals(email, that.email);
        assertThat(entity1).isNotEqualTo(entity2);
    }
}
