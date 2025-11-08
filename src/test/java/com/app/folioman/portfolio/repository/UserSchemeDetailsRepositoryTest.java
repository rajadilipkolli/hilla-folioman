package com.app.folioman.portfolio.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.portfolio.entities.CasTypeEnum;
import com.app.folioman.portfolio.entities.FileTypeEnum;
import com.app.folioman.portfolio.entities.InvestorInfo;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.models.request.TransactionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

@DataJpaTest
@ActiveProfiles("test")
@Import(SQLContainersConfig.class)
class UserSchemeDetailsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserSchemeDetailsRepository userSchemeDetailsRepository;

    @Autowired
    private EntityManagerFactory emf;

    @Test
    void findByUserFolioDetails_SchemesIn_WithValidSchemes_ShouldReturnMatchingSchemes() {
        UserFolioDetails userFolio = new UserFolioDetails();
        userFolio.setFolio("FOLIO-1");
        userFolio.setAmc("AMC1");
        userFolio.setPan("PAN1234");

        UserCASDetails cas = new UserCASDetails();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.CAMS);
        InvestorInfo info = new InvestorInfo();
        cas.setInvestorInfo(info);
        cas = entityManager.persistAndFlush(cas);

        userFolio.setUserCasDetails(cas);
        userFolio = entityManager.persistAndFlush(userFolio);

        UserSchemeDetails scheme1 = new UserSchemeDetails();
        scheme1.setScheme("Scheme 1");
        scheme1.setIsin("ISIN-1");
        scheme1.setUserFolioDetails(userFolio);
        // add a transaction so the repository's join fetch on transactions returns this row
        var tx1 = new UserTransactionDetails();
        tx1.setType(TransactionType.PURCHASE);
        scheme1.addTransaction(tx1);

        UserSchemeDetails scheme2 = new UserSchemeDetails();
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
        scheme1 = entityManager.find(UserSchemeDetails.class, scheme1.getId());
        scheme2 = entityManager.find(UserSchemeDetails.class, scheme2.getId());

        List<UserSchemeDetails> schemes = List.of(scheme1, scheme2);
        List<UserSchemeDetails> result = userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(schemes);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    void findByUserFolioDetails_SchemesIn_WithEmptyList_ShouldReturnEmptyList() {
        List<UserSchemeDetails> emptySchemes = Collections.emptyList();
        List<UserSchemeDetails> result = userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(emptySchemes);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserFolioDetails_SchemesIn_WithNullList_ShouldReturnEmptyList() {
        List<UserSchemeDetails> result =
                userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserFolioDetails_SchemesIn_WithNonExistentSchemes_ShouldReturnEmptyList() {
        List<UserSchemeDetails> result =
                userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByAmfiIsNull_WhenRecordsExist_ShouldReturnRecordsWithNullAmfi() {
        UserFolioDetails userFolio = new UserFolioDetails();
        userFolio.setFolio("FOLIO-2");
        userFolio.setAmc("AMC1");
        userFolio.setPan("PAN5678");

        UserCASDetails cas2 = new UserCASDetails();
        cas2.setCasTypeEnum(CasTypeEnum.SUMMARY);
        cas2.setFileTypeEnum(FileTypeEnum.CAMS);
        InvestorInfo info2 = new InvestorInfo();
        cas2.setInvestorInfo(info2);
        cas2 = entityManager.persistAndFlush(cas2);

        userFolio.setUserCasDetails(cas2);
        userFolio = entityManager.persistAndFlush(userFolio);

        UserSchemeDetails schemeWithNullAmfi = new UserSchemeDetails();
        schemeWithNullAmfi.setAmfi(null);
        schemeWithNullAmfi.setScheme("S1");
        schemeWithNullAmfi.setUserFolioDetails(userFolio);

        UserSchemeDetails schemeWithAmfi = new UserSchemeDetails();
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

        assertNotNull(count);
        assertTrue(count.longValue() > 0);
    }

    @Test
    void findByAmfiIsNull_WhenNoRecordsWithNullAmfi_ShouldReturnEmptyList() {
        UserFolioDetails userFolio = new UserFolioDetails();
        userFolio.setFolio("FOLIO-3");
        userFolio.setAmc("AMC-3");
        userFolio.setPan("PAN-3");

        UserCASDetails cas3 = new UserCASDetails();
        cas3.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas3.setFileTypeEnum(FileTypeEnum.CAMS);
        InvestorInfo info3 = new InvestorInfo();
        info3.setName("Test User 3");
        cas3.setInvestorInfo(info3);
        cas3 = entityManager.persistAndFlush(cas3);

        userFolio.setUserCasDetails(cas3);
        userFolio = entityManager.persistAndFlush(userFolio);

        UserSchemeDetails schemeWithAmfi = new UserSchemeDetails();
        schemeWithAmfi.setAmfi(12345L);
        schemeWithAmfi.setScheme("S3");
        schemeWithAmfi.setUserFolioDetails(userFolio);

        entityManager.persist(schemeWithAmfi);
        entityManager.flush();

        Number count = (Number) entityManager
                .getEntityManager()
                .createNativeQuery("select count(*) from portfolio.user_scheme_details where amfi is null")
                .getSingleResult();

        assertNotNull(count);
        assertEquals(0L, count.longValue());
    }

    @Test
    void updateAmfiAndIsinById_WithValidData_ShouldUpdateSuccessfully() {
        UserSchemeDetails scheme = new UserSchemeDetails();
        scheme.setAmfi(null);
        scheme.setIsin(null);
        scheme.setScheme("S4");

        UserFolioDetails userFolio = new UserFolioDetails();
        userFolio.setFolio("FOLIO-4");
        userFolio.setAmc("AMC1");
        userFolio.setPan("PAN0001");

        UserCASDetails cas4 = new UserCASDetails();
        cas4.setCasTypeEnum(CasTypeEnum.SUMMARY);
        cas4.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        InvestorInfo info4 = new InvestorInfo();
        info4.setName("Test User 4");
        cas4.setInvestorInfo(info4);
        cas4 = entityManager.persistAndFlush(cas4);

        userFolio.setUserCasDetails(cas4);
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

            assertNotNull(cnt);
            assertEquals(1L, cnt.longValue());

            // cleanup: only remove the committed scheme row so other tests' data is not affected
            em2.getTransaction().begin();
            em2.createQuery("delete from UserSchemeDetails u where u.id = :id")
                    .setParameter("id", schemeId)
                    .executeUpdate();
            em2.getTransaction().commit();
        }

        // restart the test transaction for the rest of the test lifecycle
        TestTransaction.start();
    }

    @Test
    void updateAmfiAndIsinById_WithNullValues_ShouldUpdateToNull() {
        UserSchemeDetails scheme = new UserSchemeDetails();
        scheme.setAmfi(12345L);
        scheme.setIsin("INE456B01023");
        scheme.setScheme("S5");

        UserFolioDetails userFolio = new UserFolioDetails();
        userFolio.setFolio("FOLIO-NULL-1");
        userFolio.setAmc("AMC-NULL");
        userFolio.setPan("PANNULL1");

        UserCASDetails cas = new UserCASDetails();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        cas = entityManager.persistAndFlush(cas);

        userFolio.setUserCasDetails(cas);
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

            assertNotNull(cnt);
            assertEquals(1L, cnt.longValue());

            // cleanup: only remove the committed scheme row so other tests' data is not affected
            em2.getTransaction().begin();
            em2.createQuery("delete from UserSchemeDetails u where u.id = :id")
                    .setParameter("id", schemeId)
                    .executeUpdate();
            em2.getTransaction().commit();
        }

        TestTransaction.start();
    }

    @Test
    void updateAmfiAndIsinById_WithNonExistentId_ShouldNotThrowException() {
        Long nonExistentId = 999L;
        Long newAmfi = 54321L;
        String newIsin = "INE123A01012";

        assertDoesNotThrow(() -> {
            userSchemeDetailsRepository.updateAmfiAndIsinById(newAmfi, newIsin, nonExistentId);
        });
    }
}
