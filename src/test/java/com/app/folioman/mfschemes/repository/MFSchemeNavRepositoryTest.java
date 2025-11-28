package com.app.folioman.mfschemes.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.mfschemes.MFSchemeNavProjection;
import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.models.projection.NavDateValueProjection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(SQLContainersConfig.class)
class MFSchemeNavRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MFSchemeNavRepository repository;

    private MfFundScheme testScheme1;
    private MfFundScheme testScheme2;

    @BeforeEach
    void setUp() {
        MfAmc amc = new MfAmc();
        amc.setCode("AMC001");
        amc = entityManager.persist(amc);

        testScheme1 = new MfFundScheme();
        testScheme1.setAmfiCode(12345L);
        testScheme1.setName("Test Scheme 1");
        testScheme1.setAmc(amc);
        entityManager.persist(testScheme1);

        testScheme2 = new MfFundScheme();
        testScheme2.setAmfiCode(67890L);
        testScheme2.setName("Test Scheme 2");
        testScheme2.setAmc(amc);
        entityManager.persist(testScheme2);

        MFSchemeNav nav1 = new MFSchemeNav();
        nav1.setMfScheme(testScheme1);
        nav1.setNavDate(LocalDate.of(2023, 1, 15));
        nav1.setNav(new BigDecimal("100.50"));
        entityManager.persist(nav1);

        MFSchemeNav nav2 = new MFSchemeNav();
        nav2.setMfScheme(testScheme1);
        nav2.setNavDate(LocalDate.of(2023, 2, 15));
        nav2.setNav(new BigDecimal("105.75"));
        entityManager.persist(nav2);

        MFSchemeNav nav3 = new MFSchemeNav();
        nav3.setMfScheme(testScheme2);
        nav3.setNavDate(LocalDate.of(2023, 1, 10));
        nav3.setNav(new BigDecimal("200.25"));
        entityManager.persist(nav3);

        entityManager.flush();
    }

    @Test
    void findMFSchemeNavsByNavNotLoaded_WithValidDate_ReturnsAmfiCodes() {
        LocalDate asOfDate = LocalDate.of(2023, 3, 1);

        List<Long> result = repository.findMFSchemeNavsByNavNotLoaded(asOfDate);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(12345L));
        assertTrue(result.contains(67890L));
    }

    @Test
    void findMFSchemeNavsByNavNotLoaded_WithEarlyDate_ReturnsEmptyList() {
        LocalDate asOfDate = LocalDate.of(2022, 12, 1);

        List<Long> result = repository.findMFSchemeNavsByNavNotLoaded(asOfDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findMFSchemeNavsByNavNotLoaded_WithFutureDate_ReturnsAllAmfiCodes() {
        LocalDate asOfDate = LocalDate.of(2024, 1, 1);

        List<Long> result = repository.findMFSchemeNavsByNavNotLoaded(asOfDate);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void
            findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual_WithValidParameters_ReturnsProjections() {
        Set<Long> amfiCodes = Set.of(12345L, 67890L);
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 2, 28);

        List<MFSchemeNavProjection> result =
                repository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                        amfiCodes, startDate, endDate);

        assertNotNull(result);
        assertEquals(3, result.size());

        MFSchemeNavProjection projection1 = result.getFirst();
        assertEquals(12345L, projection1.amfiCode());
        assertEquals(LocalDate.of(2023, 1, 15), projection1.navDate());
        assertEquals(0, projection1.nav().compareTo(new BigDecimal("100.50000")));
    }

    @Test
    void
            findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual_WithEmptyAmfiCodes_ReturnsEmptyList() {
        Set<Long> amfiCodes = Set.of();
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 2, 28);

        List<MFSchemeNavProjection> result =
                repository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                        amfiCodes, startDate, endDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void
            findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual_WithNonExistentAmfiCode_ReturnsEmptyList() {
        Set<Long> amfiCodes = Set.of(99999L);
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 2, 28);

        List<MFSchemeNavProjection> result =
                repository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                        amfiCodes, startDate, endDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void
            findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual_WithDateRangeOutsideData_ReturnsEmptyList() {
        Set<Long> amfiCodes = Set.of(12345L);
        LocalDate startDate = LocalDate.of(2023, 3, 1);
        LocalDate endDate = LocalDate.of(2023, 3, 31);

        List<MFSchemeNavProjection> result =
                repository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                        amfiCodes, startDate, endDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllNavDateValuesBySchemeId_WithValidSchemeId_ReturnsProjections() {
        List<NavDateValueProjection> result = repository.findAllNavDateValuesBySchemeId(testScheme1.getId());

        assertNotNull(result);
        assertEquals(2, result.size());

        NavDateValueProjection projection1 = result.getFirst();
        assertEquals(0, projection1.nav().compareTo(new BigDecimal("100.50000")));
        assertEquals(LocalDate.of(2023, 1, 15), projection1.navDate());

        NavDateValueProjection projection2 = result.get(1);
        assertEquals(0, projection2.nav().compareTo(new BigDecimal("105.75")));
        assertEquals(LocalDate.of(2023, 2, 15), projection2.navDate());
    }

    @Test
    void findAllNavDateValuesBySchemeId_WithNonExistentSchemeId_ReturnsEmptyList() {
        List<NavDateValueProjection> result = repository.findAllNavDateValuesBySchemeId(99999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllNavDateValuesBySchemeId_WithSchemeHavingOneNav_ReturnsSingleProjection() {
        List<NavDateValueProjection> result = repository.findAllNavDateValuesBySchemeId(testScheme2.getId());

        assertNotNull(result);
        assertEquals(1, result.size());

        NavDateValueProjection projection = result.getFirst();
        assertEquals(0, projection.nav().compareTo(new BigDecimal("200.25000")));
        assertEquals(LocalDate.of(2023, 1, 10), projection.navDate());
    }
}
