package com.app.folioman.portfolio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.portfolio.entities.CasTypeEnum;
import com.app.folioman.portfolio.entities.FileTypeEnum;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserPortfolioValue;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Disabled
@Import(SQLContainersConfig.class)
class UserPortfolioValueRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserPortfolioValueRepository userPortfolioValueRepository;

    @Test
    void testSave() {
        UserPortfolioValue userPortfolioValue = new UserPortfolioValue();
        userPortfolioValue.setDate(java.time.LocalDate.now());
        userPortfolioValue.setInvested(new java.math.BigDecimal("0"));
        userPortfolioValue.setValue(new java.math.BigDecimal("0"));
        UserCASDetails cas = new UserCASDetails();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        cas = entityManager.persistAndFlush(cas);
        userPortfolioValue.setUserCasDetails(cas);

        UserPortfolioValue saved = userPortfolioValueRepository.save(userPortfolioValue);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void testFindById_ExistingId() {
        UserPortfolioValue userPortfolioValue = new UserPortfolioValue();
        userPortfolioValue.setDate(java.time.LocalDate.now());
        userPortfolioValue.setInvested(new java.math.BigDecimal("0"));
        userPortfolioValue.setValue(new java.math.BigDecimal("0"));
        UserCASDetails cas = new UserCASDetails();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.CAMS);
        cas = entityManager.persistAndFlush(cas);
        userPortfolioValue.setUserCasDetails(cas);
        UserPortfolioValue saved = entityManager.persistAndFlush(userPortfolioValue);

        Optional<UserPortfolioValue> found = userPortfolioValueRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void testFindById_NonExistentId() {
        Optional<UserPortfolioValue> found = userPortfolioValueRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void testFindAll() {
        UserCASDetails cas = new UserCASDetails();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        cas = entityManager.persistAndFlush(cas);

        UserPortfolioValue userPortfolioValue1 = new UserPortfolioValue();
        userPortfolioValue1.setDate(java.time.LocalDate.now());
        userPortfolioValue1.setInvested(new java.math.BigDecimal("0"));
        userPortfolioValue1.setValue(new java.math.BigDecimal("0"));
        userPortfolioValue1.setUserCasDetails(cas);

        UserPortfolioValue userPortfolioValue2 = new UserPortfolioValue();
        userPortfolioValue2.setDate(java.time.LocalDate.now().plusDays(1));
        userPortfolioValue2.setInvested(new java.math.BigDecimal("0"));
        userPortfolioValue2.setValue(new java.math.BigDecimal("0"));
        userPortfolioValue2.setUserCasDetails(cas);

        entityManager.persistAndFlush(userPortfolioValue1);
        entityManager.persistAndFlush(userPortfolioValue2);

        List<UserPortfolioValue> all = userPortfolioValueRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void testDeleteById() {
        UserPortfolioValue userPortfolioValue = new UserPortfolioValue();
        userPortfolioValue.setDate(java.time.LocalDate.now());
        userPortfolioValue.setInvested(new java.math.BigDecimal("0"));
        userPortfolioValue.setValue(new java.math.BigDecimal("0"));
        UserCASDetails cas = new UserCASDetails();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        cas = entityManager.persistAndFlush(cas);
        userPortfolioValue.setUserCasDetails(cas);
        UserPortfolioValue saved = entityManager.persistAndFlush(userPortfolioValue);

        userPortfolioValueRepository.deleteById(saved.getId());

        Optional<UserPortfolioValue> found = userPortfolioValueRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testCount() {
        UserPortfolioValue userPortfolioValue = new UserPortfolioValue();
        userPortfolioValue.setDate(java.time.LocalDate.now());
        userPortfolioValue.setInvested(new java.math.BigDecimal("0"));
        userPortfolioValue.setValue(new java.math.BigDecimal("0"));
        UserCASDetails cas = new UserCASDetails();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        cas = entityManager.persistAndFlush(cas);
        userPortfolioValue.setUserCasDetails(cas);
        entityManager.persistAndFlush(userPortfolioValue);

        long count = userPortfolioValueRepository.count();

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void testExistsById() {
        UserPortfolioValue userPortfolioValue = new UserPortfolioValue();
        userPortfolioValue.setDate(java.time.LocalDate.now());
        userPortfolioValue.setInvested(new java.math.BigDecimal("0"));
        userPortfolioValue.setValue(new java.math.BigDecimal("0"));
        UserCASDetails cas = new UserCASDetails();
        // ensure required enum fields are set before persisting
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        cas = entityManager.persistAndFlush(cas);
        userPortfolioValue.setUserCasDetails(cas);
        UserPortfolioValue saved = entityManager.persistAndFlush(userPortfolioValue);

        boolean exists = userPortfolioValueRepository.existsById(saved.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsById_NonExistentId() {
        boolean exists = userPortfolioValueRepository.existsById(999L);

        assertThat(exists).isFalse();
    }
}
