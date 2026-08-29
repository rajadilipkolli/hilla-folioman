package com.app.folioman.mfschemes.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.mfschemes.domain.models.projection.NavDateValueProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeNavProjection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(SQLContainersConfig.class)
class MFSchemeNavRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MfSchemeNavRepository repository;

    private MfFundSchemeEntity testScheme1;
    private MfFundSchemeEntity testScheme2;

    @BeforeEach
    void setUp() {
        MfAmcEntity amc = new MfAmcEntity();
        amc.setCode("AMC001");
        amc.setName("AMC001");
        amc = entityManager.persist(amc);

        testScheme1 = new MfFundSchemeEntity();
        testScheme1.setAmfiCode(12345L);
        testScheme1.setName("Test Scheme 1");
        testScheme1.setAmc(amc);
        entityManager.persist(testScheme1);

        testScheme2 = new MfFundSchemeEntity();
        testScheme2.setAmfiCode(67890L);
        testScheme2.setName("Test Scheme 2");
        testScheme2.setAmc(amc);
        entityManager.persist(testScheme2);

        MFSchemeNavEntity nav1 = new MFSchemeNavEntity();
        nav1.setMfFundSchemeEntity(testScheme1);
        nav1.setNavDate(LocalDate.of(2023, 1, 15));
        nav1.setNav(new BigDecimal("100.50"));
        entityManager.persist(nav1);

        MFSchemeNavEntity nav2 = new MFSchemeNavEntity();
        nav2.setMfFundSchemeEntity(testScheme1);
        nav2.setNavDate(LocalDate.of(2023, 2, 15));
        nav2.setNav(new BigDecimal("105.75"));
        entityManager.persist(nav2);

        MFSchemeNavEntity nav3 = new MFSchemeNavEntity();
        nav3.setMfFundSchemeEntity(testScheme2);
        nav3.setNavDate(LocalDate.of(2023, 1, 10));
        nav3.setNav(new BigDecimal("200.25"));
        entityManager.persist(nav3);

        entityManager.flush();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void findMFSchemeNavsByNavNotLoaded_WithValidDate_ReturnsAmfiCodes() {
        LocalDate asOfDate = LocalDate.of(2023, 3, 1);

        List<Long> result = repository.findMFSchemeNavsByNavNotLoaded(asOfDate);

        assertThat(result).hasSize(2).contains(12345L).contains(67890L);
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void findMFSchemeNavsByNavNotLoaded_WithEarlyDate_ReturnsEmptyList() {
        LocalDate asOfDate = LocalDate.of(2022, 12, 1);

        List<Long> result = repository.findMFSchemeNavsByNavNotLoaded(asOfDate);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void findMFSchemeNavsByNavNotLoaded_WithFutureDate_ReturnsAllAmfiCodes() {
        LocalDate asOfDate = LocalDate.of(2024, 1, 1);

        List<Long> result = repository.findMFSchemeNavsByNavNotLoaded(asOfDate);

        assertThat(result).hasSize(2);
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void
            findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual_WithValidParameters_ReturnsProjections() {
        Set<Long> amfiCodes = Set.of(12345L, 67890L);
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 2, 28);

        List<MFSchemeNavProjection> result =
                repository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                        amfiCodes, startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);

        MFSchemeNavProjection projection1 = result.getFirst();
        assertThat(projection1.amfiCode()).isEqualTo(12345L);
        assertThat(projection1.navDate()).isEqualTo(LocalDate.of(2023, 1, 15));
        assertThat(projection1.nav()).isEqualByComparingTo(new BigDecimal("100.50000"));
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void
            findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual_WithEmptyAmfiCodes_ReturnsEmptyList() {
        Set<Long> amfiCodes = Set.of();
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 2, 28);

        List<MFSchemeNavProjection> result =
                repository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                        amfiCodes, startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void
            findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual_WithNonExistentAmfiCode_ReturnsEmptyList() {
        Set<Long> amfiCodes = Set.of(99999L);
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 2, 28);

        List<MFSchemeNavProjection> result =
                repository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                        amfiCodes, startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void
            findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual_WithDateRangeOutsideData_ReturnsEmptyList() {
        Set<Long> amfiCodes = Set.of(12345L);
        LocalDate startDate = LocalDate.of(2023, 3, 1);
        LocalDate endDate = LocalDate.of(2023, 3, 31);

        List<MFSchemeNavProjection> result =
                repository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                        amfiCodes, startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void findAllNavDateValuesBySchemeId_WithValidSchemeId_ReturnsProjections() {
        List<NavDateValueProjection> result = repository.findAllNavDateValuesBySchemeId(testScheme1.getId());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        NavDateValueProjection projection1 = result.getFirst();
        assertThat(projection1.nav()).isEqualByComparingTo(new BigDecimal("100.50000"));
        assertThat(projection1.navDate()).isEqualTo(LocalDate.of(2023, 1, 15));

        NavDateValueProjection projection2 = result.get(1);
        assertThat(projection2.nav()).isEqualByComparingTo(new BigDecimal("105.75"));
        assertThat(projection2.navDate()).isEqualTo(LocalDate.of(2023, 2, 15));
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void findAllNavDateValuesBySchemeId_WithNonExistentSchemeId_ReturnsEmptyList() {
        List<NavDateValueProjection> result = repository.findAllNavDateValuesBySchemeId(99999L);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void findAllNavDateValuesBySchemeId_WithSchemeHavingOneNav_ReturnsSingleProjection() {
        List<NavDateValueProjection> result = repository.findAllNavDateValuesBySchemeId(testScheme2.getId());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        NavDateValueProjection projection = result.getFirst();
        assertThat(projection.nav()).isEqualByComparingTo(new BigDecimal("200.25000"));
        assertThat(projection.navDate()).isEqualTo(LocalDate.of(2023, 1, 10));
    }
}
