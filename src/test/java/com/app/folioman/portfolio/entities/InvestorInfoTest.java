package com.app.folioman.portfolio.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvestorInfoTest {

    private InvestorInfo investorInfo;
    private UserCASDetails userCasDetails;

    @BeforeEach
    void setUp() {
        investorInfo = new InvestorInfo();
        userCasDetails = new UserCASDetails();
    }

    @Test
    void getId() {
        assertThat(investorInfo.getId()).isNull();
    }

    @Test
    void setId() {
        Long id = 1L;
        InvestorInfo result = investorInfo.setId(id);
        assertThat(investorInfo.getId()).isEqualTo(id);
        assertThat(result).isSameAs(investorInfo);
    }

    @Test
    void getEmail() {
        assertThat(investorInfo.getEmail()).isNull();
    }

    @Test
    void setEmail() {
        String email = "test@example.com";
        InvestorInfo result = investorInfo.setEmail(email);
        assertThat(investorInfo.getEmail()).isEqualTo(email);
        assertThat(result).isSameAs(investorInfo);
    }

    @Test
    void setEmailWithNull() {
        InvestorInfo result = investorInfo.setEmail(null);
        assertThat(investorInfo.getEmail()).isNull();
        assertThat(result).isSameAs(investorInfo);
    }

    @Test
    void getName() {
        assertThat(investorInfo.getName()).isNull();
    }

    @Test
    void setName() {
        String name = "John Doe";
        InvestorInfo result = investorInfo.setName(name);
        assertThat(investorInfo.getName()).isEqualTo(name);
        assertThat(result).isSameAs(investorInfo);
    }

    @Test
    void setNameWithNull() {
        InvestorInfo result = investorInfo.setName(null);
        assertThat(investorInfo.getName()).isNull();
        assertThat(result).isSameAs(investorInfo);
    }

    @Test
    void getMobile() {
        assertThat(investorInfo.getMobile()).isNull();
    }

    @Test
    void setMobile() {
        String mobile = "1234567890";
        InvestorInfo result = investorInfo.setMobile(mobile);
        assertThat(investorInfo.getMobile()).isEqualTo(mobile);
        assertThat(result).isSameAs(investorInfo);
    }

    @Test
    void setMobileWithNull() {
        InvestorInfo result = investorInfo.setMobile(null);
        assertThat(investorInfo.getMobile()).isNull();
        assertThat(result).isSameAs(investorInfo);
    }

    @Test
    void getAddress() {
        assertThat(investorInfo.getAddress()).isNull();
    }

    @Test
    void setAddress() {
        String address = "123 Main St";
        InvestorInfo result = investorInfo.setAddress(address);
        assertThat(investorInfo.getAddress()).isEqualTo(address);
        assertThat(result).isSameAs(investorInfo);
    }

    @Test
    void setAddressWithNull() {
        InvestorInfo result = investorInfo.setAddress(null);
        assertThat(investorInfo.getAddress()).isNull();
        assertThat(result).isSameAs(investorInfo);
    }

    @Test
    void getUserCasDetails() {
        assertThat(investorInfo.getUserCasDetails()).isNull();
    }

    @Test
    void setUserCasDetails() {
        InvestorInfo result = investorInfo.setUserCasDetails(userCasDetails);
        assertThat(investorInfo.getUserCasDetails()).isEqualTo(userCasDetails);
        assertThat(result).isSameAs(investorInfo);
    }

    @Test
    void setUserCasDetailsWithNull() {
        InvestorInfo result = investorInfo.setUserCasDetails(null);
        assertThat(investorInfo.getUserCasDetails()).isNull();
        assertThat(result).isSameAs(investorInfo);
    }

    @Test
    void equalsSameObject() {
        assertThat(investorInfo).isEqualTo(investorInfo);
    }

    @Test
    void equalsWithNull() {
        assertThat(investorInfo).isNotEqualTo(null);
    }

    @Test
    void equalsWithDifferentClass() {
        Object different = "string";
        assertThat(different.getClass()).isNotEqualTo(investorInfo.getClass());
        assertThat(different).isNotEqualTo(investorInfo);
    }

    @Test
    void equalsWithNullId() {
        InvestorInfo other = new InvestorInfo();
        other.setEmail("test@example.com");
        // both have null id -> should not be equal
        assertThat(other).isNotEqualTo(investorInfo);
    }

    @Test
    void equalsWithSameEmail() {
        investorInfo.setId(1L).setEmail("test@example.com");
        InvestorInfo other = new InvestorInfo();
        other.setId(1L).setEmail("test@example.com");
        assertThat(other.getClass()).isEqualTo(investorInfo.getClass());
        assertThat(other).isEqualTo(investorInfo);
    }

    @Test
    void equalsWithDifferentEmail() {
        investorInfo.setId(1L).setEmail("test1@example.com");
        InvestorInfo other = new InvestorInfo();
        other.setId(1L).setEmail("test2@example.com");
        assertThat(other.getClass()).isEqualTo(investorInfo.getClass());
        assertThat(other).isNotEqualTo(investorInfo);
    }

    @Test
    void equalsWithNullEmails() {
        investorInfo.setId(1L);
        InvestorInfo other = new InvestorInfo();
        other.setId(1L);
        assertThat(other.getClass()).isEqualTo(investorInfo.getClass());
        assertThat(other).isEqualTo(investorInfo);
    }

    @Test
    void testHashCode() {
        int hashCode1 = investorInfo.hashCode();
        int hashCode2 = investorInfo.hashCode();
        assertThat(hashCode2).isEqualTo(hashCode1);
        assertThat(hashCode1).isEqualTo(InvestorInfo.class.hashCode());
    }

    @Test
    void hashCodeConsistency() {
        investorInfo.setEmail("test@example.com");
        int hashCode1 = investorInfo.hashCode();
        int hashCode2 = investorInfo.hashCode();
        assertThat(hashCode2).isEqualTo(hashCode1);
    }

    @Test
    void fluentInterfaceChaining() {
        InvestorInfo result = investorInfo
                .setId(1L)
                .setEmail("test@example.com")
                .setName("John Doe")
                .setMobile("1234567890")
                .setAddress("123 Main St")
                .setUserCasDetails(userCasDetails);

        assertThat(result).isSameAs(investorInfo);
        assertThat(investorInfo.getId()).isOne();
        assertThat(investorInfo.getEmail()).isEqualTo("test@example.com");
        assertThat(investorInfo.getName()).isEqualTo("John Doe");
        assertThat(investorInfo.getMobile()).isEqualTo("1234567890");
        assertThat(investorInfo.getAddress()).isEqualTo("123 Main St");
        assertThat(investorInfo.getUserCasDetails()).isEqualTo(userCasDetails);
    }
}
