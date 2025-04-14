package com.app.folioman.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.mfschemes.FundDetailProjection;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserSchemeDetailsServiceTest {

    private UserSchemeDetailServiceImpl userSchemeDetailsService;

    @BeforeEach
    public void setUp() {
        // Initialize UserSchemeDetailsService with mock dependencies or nulls
        userSchemeDetailsService = new UserSchemeDetailServiceImpl(null, null);
    }

    @Test
    public void testIsMatchingScheme() throws Exception {
        // Access the private isMatchingScheme method using reflection
        Method method = UserSchemeDetailServiceImpl.class.getDeclaredMethod(
                "isMatchingScheme", String.class, FundDetailProjection.class);
        method.setAccessible(true);

        // Test case 1: Both conditions are true
        boolean result1 = (boolean) method.invoke(
                userSchemeDetailsService,
                "Some Income Scheme",
                new MockFundDetailProjection("Scheme Name IDCW", 180451L, "AmC Name"));
        assertThat(result1).isTrue();

        // Test case 2: Both conditions are false
        boolean result2 = (boolean) method.invoke(
                userSchemeDetailsService,
                "Some Growth Scheme",
                new MockFundDetailProjection("Scheme Name Growth", 180452L, "AmC Name"));
        assertThat(result2).isTrue();

        // Test case 3: First condition true, second false
        boolean result3 = (boolean) method.invoke(
                userSchemeDetailsService,
                "Some Income Scheme",
                new MockFundDetailProjection("Scheme Name Growth", 180453L, "AmC Name"));
        assertThat(result3).isFalse();

        // Test case 4: First condition false, second true
        boolean result4 = (boolean) method.invoke(
                userSchemeDetailsService,
                "Some Growth Scheme",
                new MockFundDetailProjection("Scheme Name IDCW", 180454L, "AmC Name"));
        assertThat(result4).isFalse();
    }

    // Mock implementation of FundDetailProjection
    private record MockFundDetailProjection(String schemeName, Long amfiCode, String amcName)
            implements FundDetailProjection {
        @Override
        public String getSchemeName() {
            return schemeName;
        }

        @Override
        public Long getAmfiCode() {
            return amfiCode;
        }

        @Override
        public String getAmcName() {
            return amcName;
        }
    }
}
