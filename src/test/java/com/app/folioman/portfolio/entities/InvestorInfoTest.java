package com.app.folioman.portfolio.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testGetId() {
        assertNull(investorInfo.getId());
    }

    @Test
    void testSetId() {
        Long id = 1L;
        InvestorInfo result = investorInfo.setId(id);
        assertEquals(id, investorInfo.getId());
        assertSame(investorInfo, result);
    }

    @Test
    void testGetEmail() {
        assertNull(investorInfo.getEmail());
    }

    @Test
    void testSetEmail() {
        String email = "test@example.com";
        InvestorInfo result = investorInfo.setEmail(email);
        assertEquals(email, investorInfo.getEmail());
        assertSame(investorInfo, result);
    }

    @Test
    void testSetEmailWithNull() {
        InvestorInfo result = investorInfo.setEmail(null);
        assertNull(investorInfo.getEmail());
        assertSame(investorInfo, result);
    }

    @Test
    void testGetName() {
        assertNull(investorInfo.getName());
    }

    @Test
    void testSetName() {
        String name = "John Doe";
        InvestorInfo result = investorInfo.setName(name);
        assertEquals(name, investorInfo.getName());
        assertSame(investorInfo, result);
    }

    @Test
    void testSetNameWithNull() {
        InvestorInfo result = investorInfo.setName(null);
        assertNull(investorInfo.getName());
        assertSame(investorInfo, result);
    }

    @Test
    void testGetMobile() {
        assertNull(investorInfo.getMobile());
    }

    @Test
    void testSetMobile() {
        String mobile = "1234567890";
        InvestorInfo result = investorInfo.setMobile(mobile);
        assertEquals(mobile, investorInfo.getMobile());
        assertSame(investorInfo, result);
    }

    @Test
    void testSetMobileWithNull() {
        InvestorInfo result = investorInfo.setMobile(null);
        assertNull(investorInfo.getMobile());
        assertSame(investorInfo, result);
    }

    @Test
    void testGetAddress() {
        assertNull(investorInfo.getAddress());
    }

    @Test
    void testSetAddress() {
        String address = "123 Main St";
        InvestorInfo result = investorInfo.setAddress(address);
        assertEquals(address, investorInfo.getAddress());
        assertSame(investorInfo, result);
    }

    @Test
    void testSetAddressWithNull() {
        InvestorInfo result = investorInfo.setAddress(null);
        assertNull(investorInfo.getAddress());
        assertSame(investorInfo, result);
    }

    @Test
    void testGetUserCasDetails() {
        assertNull(investorInfo.getUserCasDetails());
    }

    @Test
    void testSetUserCasDetails() {
        InvestorInfo result = investorInfo.setUserCasDetails(userCasDetails);
        assertEquals(userCasDetails, investorInfo.getUserCasDetails());
        assertSame(investorInfo, result);
    }

    @Test
    void testSetUserCasDetailsWithNull() {
        InvestorInfo result = investorInfo.setUserCasDetails(null);
        assertNull(investorInfo.getUserCasDetails());
        assertSame(investorInfo, result);
    }

    @Test
    void testEqualsSameObject() {
        assertTrue(investorInfo.equals(investorInfo));
    }

    @Test
    void testEqualsWithNull() {
        assertFalse(investorInfo.equals(null));
    }

    @Test
    void testEqualsWithDifferentClass() {
        Object different = "string";
        assertFalse(investorInfo.getClass().equals(different.getClass()));
        assertFalse(investorInfo.equals(different));
    }

    @Test
    void testEqualsWithNullId() {
        InvestorInfo other = new InvestorInfo();
        other.setEmail("test@example.com");
        // both have null id -> should not be equal
        assertFalse(investorInfo.equals(other));
    }

    @Test
    void testEqualsWithSameEmail() {
        investorInfo.setId(1L).setEmail("test@example.com");
        InvestorInfo other = new InvestorInfo();
        other.setId(1L).setEmail("test@example.com");
        assertEquals(investorInfo.getClass(), other.getClass());
        assertTrue(investorInfo.equals(other));
    }

    @Test
    void testEqualsWithDifferentEmail() {
        investorInfo.setId(1L).setEmail("test1@example.com");
        InvestorInfo other = new InvestorInfo();
        other.setId(1L).setEmail("test2@example.com");
        assertEquals(investorInfo.getClass(), other.getClass());
        assertFalse(investorInfo.equals(other));
    }

    @Test
    void testEqualsWithNullEmails() {
        investorInfo.setId(1L);
        InvestorInfo other = new InvestorInfo();
        other.setId(1L);
        assertEquals(investorInfo.getClass(), other.getClass());
        assertTrue(investorInfo.equals(other));
    }

    @Test
    void testHashCode() {
        int hashCode1 = investorInfo.hashCode();
        int hashCode2 = investorInfo.hashCode();
        assertEquals(hashCode1, hashCode2);
        assertEquals(InvestorInfo.class.hashCode(), hashCode1);
    }

    @Test
    void testHashCodeConsistency() {
        investorInfo.setEmail("test@example.com");
        int hashCode1 = investorInfo.hashCode();
        int hashCode2 = investorInfo.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testFluentInterfaceChaining() {
        InvestorInfo result = investorInfo
                .setId(1L)
                .setEmail("test@example.com")
                .setName("John Doe")
                .setMobile("1234567890")
                .setAddress("123 Main St")
                .setUserCasDetails(userCasDetails);

        assertSame(investorInfo, result);
        assertEquals(1L, investorInfo.getId());
        assertEquals("test@example.com", investorInfo.getEmail());
        assertEquals("John Doe", investorInfo.getName());
        assertEquals("1234567890", investorInfo.getMobile());
        assertEquals("123 Main St", investorInfo.getAddress());
        assertEquals(userCasDetails, investorInfo.getUserCasDetails());
    }
}
