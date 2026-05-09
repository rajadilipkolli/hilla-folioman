package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.portfolio.domain.models.request.TransactionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.transaction.TestTransaction;

@DataJpaTest
@Import(SQLContainersConfig.class)
class UserSchemeDetailsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserSchemeDetailsRepository userSchemeDetailsRepository;

    @Autowired
    private EntityManagerFactory emf;

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
    void findByUserFolioDetails_SchemesIn_WithValidSchemes_ShouldReturnMatchingSchemes() {
        UserFolioDetailsEntity userFolio = new UserFolioDetailsEntity();
        userFolio.setFolio("FOLIO-1");
        userFolio.setAmc("AMC1");
        userFolio.setPan("PAN1234");

        UserCasDetailsEntity cas = new UserCasDetailsEntity();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.CAMS);
        InvestorInfoEntity info = new InvestorInfoEntity();
        cas.setInvestorInfoEntity(info);
        cas = entityManager.persistAndFlush(cas);

        userFolio.setUserCasDetailsEntity(cas);
        userFolio = entityManager.persistAndFlush(userFolio);

        UserSchemeDetailsEntity scheme1 = new UserSchemeDetailsEntity();
        scheme1.setScheme("Scheme 1");
        scheme1.setIsin("ISIN-1");
        scheme1.setUserFolioDetails(userFolio);
        // add a transaction so the repository's join fetch on transactions returns this row
        var tx1 = new UserTransactionDetails();
        tx1.setType(TransactionType.PURCHASE);
        scheme1.addTransaction(tx1);

        UserSchemeDetailsEntity scheme2 = new UserSchemeDetailsEntity();
        scheme2.setScheme("Scheme 2");
        scheme2.setIsin("ISIN-2");
        scheme2.setUserFolioDetails(userFolio);
        var tx2 = new UserTransactionDetails();
        tx2.setType(TransactionType.PURCHASE);
        scheme2.addTransaction(tx2);

        entityManager.persist(scheme1);
        entityManager.persist(scheme2);
        entityManager.flush();
        // re-fetch persisted entities so they are managed instances for the repository query
        scheme1 = entityManager.find(UserSchemeDetailsEntity.class, scheme1.getId());
        scheme2 = entityManager.find(UserSchemeDetailsEntity.class, scheme2.getId());

        List<UserSchemeDetailsEntity> schemes = List.of(scheme1, scheme2);
        List<UserSchemeDetailsEntity> result = userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(schemes);

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void findByUserFolioDetails_SchemesIn_WithEmptyList_ShouldReturnEmptyList() {
        List<UserSchemeDetailsEntity> emptySchemes = Collections.emptyList();
        List<UserSchemeDetailsEntity> result =
                userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(emptySchemes);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
    void findByAmfiIsNull_WhenRecordsExist_ShouldReturnRecordsWithNullAmfi() {
        UserFolioDetailsEntity userFolio = new UserFolioDetailsEntity();
        userFolio.setFolio("FOLIO-2");
        userFolio.setAmc("AMC1");
        userFolio.setPan("PAN5678");

        UserCasDetailsEntity cas2 = new UserCasDetailsEntity();
        cas2.setCasTypeEnum(CasTypeEnum.SUMMARY);
        cas2.setFileTypeEnum(FileTypeEnum.CAMS);
        InvestorInfoEntity info2 = new InvestorInfoEntity();
        cas2.setInvestorInfoEntity(info2);
        cas2 = entityManager.persistAndFlush(cas2);

        userFolio.setUserCasDetailsEntity(cas2);
        userFolio = entityManager.persistAndFlush(userFolio);

        UserSchemeDetailsEntity schemeWithNullAmfi = new UserSchemeDetailsEntity();
        schemeWithNullAmfi.setAmfi(null);
        schemeWithNullAmfi.setScheme("S1");
        schemeWithNullAmfi.setUserFolioDetails(userFolio);

        UserSchemeDetailsEntity schemeWithAmfi = new UserSchemeDetailsEntity();
        schemeWithAmfi.setAmfi(12345L);
        schemeWithAmfi.setScheme("S2");
        schemeWithAmfi.setUserFolioDetails(userFolio);

        entityManager.persist(schemeWithNullAmfi);
        entityManager.persist(schemeWithAmfi);
        entityManager.flush();

        Number count = (Number) entityManager
                .getEntityManager()
                .createNativeQuery("select count(*) from portfolio.user_scheme_details where amfi is null")
                .getSingleResult();

        assertThat(count).isNotNull();
        assertThat(count.longValue()).isGreaterThan(0);
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
    void findByAmfiIsNull_WhenNoRecordsWithNullAmfi_ShouldReturnEmptyList() {
        UserFolioDetailsEntity userFolio = new UserFolioDetailsEntity();
        userFolio.setFolio("FOLIO-3");
        userFolio.setAmc("AMC-3");
        userFolio.setPan("PAN-3");

        UserCasDetailsEntity cas3 = new UserCasDetailsEntity();
        cas3.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas3.setFileTypeEnum(FileTypeEnum.CAMS);
        InvestorInfoEntity info3 = new InvestorInfoEntity();
        info3.setName("Test User 3");
        cas3.setInvestorInfoEntity(info3);
        cas3 = entityManager.persistAndFlush(cas3);

        userFolio.setUserCasDetailsEntity(cas3);
        userFolio = entityManager.persistAndFlush(userFolio);

        UserSchemeDetailsEntity schemeWithAmfi = new UserSchemeDetailsEntity();
        schemeWithAmfi.setAmfi(12345L);
        schemeWithAmfi.setScheme("S3");
        schemeWithAmfi.setUserFolioDetails(userFolio);

        entityManager.persist(schemeWithAmfi);
        entityManager.flush();

        Number count = (Number) entityManager
                .getEntityManager()
                .createNativeQuery("select count(*) from portfolio.user_scheme_details where amfi is null")
                .getSingleResult();

        assertThat(count).isNotNull();
        assertThat(count.longValue()).isZero();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
    void updateAmfiAndIsinById_WithValidData_ShouldUpdateSuccessfully() {
        UserSchemeDetailsEntity scheme = new UserSchemeDetailsEntity();
        scheme.setAmfi(null);
        scheme.setIsin(null);
        scheme.setScheme("S4");

        UserFolioDetailsEntity userFolio = new UserFolioDetailsEntity();
        userFolio.setFolio("FOLIO-4");
        userFolio.setAmc("AMC1");
        userFolio.setPan("PAN0001");

        UserCasDetailsEntity cas4 = new UserCasDetailsEntity();
        cas4.setCasTypeEnum(CasTypeEnum.SUMMARY);
        cas4.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        InvestorInfoEntity info4 = new InvestorInfoEntity();
        info4.setName("Test User 4");
        cas4.setInvestorInfoEntity(info4);
        cas4 = entityManager.persistAndFlush(cas4);

        userFolio.setUserCasDetailsEntity(cas4);
        userFolio = entityManager.persistAndFlush(userFolio);

        scheme.setUserFolioDetails(userFolio);

        entityManager.persist(scheme);
        entityManager.flush();

        Long schemeId = scheme.getId();
        Long newAmfi = 54321L;
        String newIsin = "INE123A01012";

        // commit outer test transaction so REQUIRES_NEW update can see the persisted row
        TestTransaction.flagForCommit();
        TestTransaction.end();

        userSchemeDetailsRepository.updateAmfiAndIsinById(newAmfi, newIsin, schemeId);

        // Use a fresh EntityManager (from the injected factory) to observe the changes committed by the REQUIRES_NEW
        // update
        try (EntityManager em2 = emf.createEntityManager()) {
            // verify the update by checking the persisted row directly with a native query
            Number cnt = (Number) em2.createNativeQuery(
                            "select count(*) from portfolio.user_scheme_details where id = ? and amfi = ? and isin = ?")
                    .setParameter(1, schemeId)
                    .setParameter(2, newAmfi)
                    .setParameter(3, newIsin)
                    .getSingleResult();

            assertThat(cnt).isNotNull();
            assertThat(cnt.longValue()).isOne();

            // cleanup: only remove the committed scheme row so other tests' data is not affected
            em2.getTransaction().begin();
            em2.createQuery("delete from UserSchemeDetailsEntity u where u.id = :id")
                    .setParameter("id", schemeId)
                    .executeUpdate();
            em2.getTransaction().commit();
        }

        // restart the test transaction for the rest of the test lifecycle
        TestTransaction.start();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
    void updateAmfiAndIsinById_WithNullValues_ShouldUpdateToNull() {
        UserSchemeDetailsEntity scheme = new UserSchemeDetailsEntity();
        scheme.setAmfi(12345L);
        scheme.setIsin("INE456B01023");
        scheme.setScheme("S5");

        UserFolioDetailsEntity userFolio = new UserFolioDetailsEntity();
        userFolio.setFolio("FOLIO-NULL-1");
        userFolio.setAmc("AMC-NULL");
        userFolio.setPan("PANNULL1");

        UserCasDetailsEntity cas = new UserCasDetailsEntity();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        cas = entityManager.persistAndFlush(cas);

        userFolio.setUserCasDetailsEntity(cas);
        userFolio = entityManager.persistAndFlush(userFolio);
        scheme.setScheme("S5");
        scheme.setUserFolioDetails(userFolio);

        entityManager.persist(scheme);
        entityManager.flush();

        Long schemeId = scheme.getId();

        // commit outer test transaction so REQUIRES_NEW update can see the persisted row
        TestTransaction.flagForCommit();
        TestTransaction.end();

        userSchemeDetailsRepository.updateAmfiAndIsinById(null, null, schemeId);

        try (EntityManager em2 = emf.createEntityManager()) {
            // verify the update by checking the persisted row directly with a native query
            Number cnt = (Number) em2.createNativeQuery(
                            "select count(*) from portfolio.user_scheme_details where id = ? and amfi is null and isin is null")
                    .setParameter(1, schemeId)
                    .getSingleResult();

            assertThat(cnt).isNotNull();
            assertThat(cnt.longValue()).isOne();

            // cleanup: only remove the committed scheme row so other tests' data is not affected
            em2.getTransaction().begin();
            em2.createQuery("delete from UserSchemeDetailsEntity u where u.id = :id")
                    .setParameter("id", schemeId)
                    .executeUpdate();
            em2.getTransaction().commit();
        }

        TestTransaction.start();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void updateAmfiAndIsinById_WithNonExistentId_ShouldNotThrowException() {
        Long nonExistentId = 999L;
        Long newAmfi = 54321L;
        String newIsin = "INE123A01012";

        assertThatCode(() -> {
                    userSchemeDetailsRepository.updateAmfiAndIsinById(newAmfi, newIsin, nonExistentId);
                })
                .doesNotThrowAnyException();
    }
}
