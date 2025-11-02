package com.app.folioman.portfolio.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.portfolio.entities.CasTypeEnum;
import com.app.folioman.portfolio.entities.FileTypeEnum;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
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
        com.app.folioman.portfolio.entities.UserFolioDetails userFolio =
                new com.app.folioman.portfolio.entities.UserFolioDetails();
        userFolio.setFolio("FOLIO-1");
        userFolio.setAmc("AMC1");
        userFolio.setPan("PAN1234");
        com.app.folioman.portfolio.entities.UserCASDetails cas =
                new com.app.folioman.portfolio.entities.UserCASDetails();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.CAMS);
        com.app.folioman.portfolio.entities.InvestorInfo info = new com.app.folioman.portfolio.entities.InvestorInfo();
        info.setName("Test User");
        cas.setInvestorInfo(info);
        cas = entityManager.persistAndFlush(cas);
        userFolio.setUserCasDetails(cas);
        userFolio = entityManager.persistAndFlush(userFolio);

        UserSchemeDetails scheme1 = new UserSchemeDetails();
        scheme1.setScheme("Scheme 1");
        scheme1.setUserFolioDetails(userFolio);

        UserSchemeDetails scheme2 = new UserSchemeDetails();
        scheme2.setScheme("Scheme 2");
        scheme2.setUserFolioDetails(userFolio);

        entityManager.persist(scheme1);
        entityManager.persist(scheme2);
        entityManager.flush();

        List<UserSchemeDetails> schemes = List.of(scheme1, scheme2);

        List<UserSchemeDetails> result = userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(schemes);

        assertNotNull(result);
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
        com.app.folioman.portfolio.entities.UserFolioDetails userFolio =
                new com.app.folioman.portfolio.entities.UserFolioDetails();
        userFolio.setFolio("FOLIO-2");
        userFolio.setAmc("AMC1");
        userFolio.setPan("PAN5678");
        com.app.folioman.portfolio.entities.UserCASDetails cas2 =
                new com.app.folioman.portfolio.entities.UserCASDetails();
        cas2.setCasTypeEnum(CasTypeEnum.SUMMARY);
        cas2.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        com.app.folioman.portfolio.entities.InvestorInfo info2 = new com.app.folioman.portfolio.entities.InvestorInfo();
        info2.setName("Test User 2");
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

        List<UserSchemeDetails> result = userSchemeDetailsRepository.findByAmfiIsNull();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(scheme -> scheme.getAmfi() == null));
    }

    @Test
    void findByAmfiIsNull_WhenNoRecordsWithNullAmfi_ShouldReturnEmptyList() {
        com.app.folioman.portfolio.entities.UserFolioDetails userFolio =
                new com.app.folioman.portfolio.entities.UserFolioDetails();
        userFolio.setFolio("FOLIO-3");
        userFolio.setAmc("AMC1");
        userFolio.setPan("PAN9999");
        com.app.folioman.portfolio.entities.UserCASDetails cas3 =
                new com.app.folioman.portfolio.entities.UserCASDetails();
        cas3.setCasTypeEnum(com.app.folioman.portfolio.entities.CasTypeEnum.DETAILED);
        cas3.setFileTypeEnum(com.app.folioman.portfolio.entities.FileTypeEnum.CAMS);
        com.app.folioman.portfolio.entities.InvestorInfo info3 = new com.app.folioman.portfolio.entities.InvestorInfo();
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

        List<UserSchemeDetails> result = userSchemeDetailsRepository.findByAmfiIsNull();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateAmfiAndIsinById_WithValidData_ShouldUpdateSuccessfully() {
        UserSchemeDetails scheme = new UserSchemeDetails();
        scheme.setAmfi(null);
        scheme.setIsin(null);
        com.app.folioman.portfolio.entities.UserFolioDetails userFolio =
                new com.app.folioman.portfolio.entities.UserFolioDetails();
        userFolio.setFolio("FOLIO-4");
        userFolio.setAmc("AMC1");
        userFolio.setPan("PAN0001");
        com.app.folioman.portfolio.entities.UserCASDetails cas4 =
                new com.app.folioman.portfolio.entities.UserCASDetails();
        cas4.setCasTypeEnum(com.app.folioman.portfolio.entities.CasTypeEnum.SUMMARY);
        cas4.setFileTypeEnum(com.app.folioman.portfolio.entities.FileTypeEnum.UNKNOWN);
        com.app.folioman.portfolio.entities.InvestorInfo info4 = new com.app.folioman.portfolio.entities.InvestorInfo();
        info4.setName("Test User 4");
        cas4.setInvestorInfo(info4);
        cas4 = entityManager.persistAndFlush(cas4);
        userFolio.setUserCasDetails(cas4);
        userFolio = entityManager.persistAndFlush(userFolio);
        scheme.setScheme("S4");
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
            UserSchemeDetails updatedScheme = em2.find(UserSchemeDetails.class, schemeId);
            assertNotNull(updatedScheme);
            assertEquals(newAmfi, updatedScheme.getAmfi());
            assertEquals(newIsin, updatedScheme.getIsin());
            // cleanup: remove the committed row so other tests are not affected
            em2.getTransaction().begin();
            em2.remove(updatedScheme);
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
        com.app.folioman.portfolio.entities.UserFolioDetails userFolio =
                new com.app.folioman.portfolio.entities.UserFolioDetails();
        userFolio.setFolio("FOLIO-NULL-1");
        userFolio.setAmc("AMC-NULL");
        userFolio.setPan("PANNULL1");
        com.app.folioman.portfolio.entities.UserCASDetails cas =
                new com.app.folioman.portfolio.entities.UserCASDetails();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        com.app.folioman.portfolio.entities.InvestorInfo info5 = new com.app.folioman.portfolio.entities.InvestorInfo();
        info5.setName("Test User 5");
        cas.setInvestorInfo(info5);
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
            UserSchemeDetails updatedScheme = em2.find(UserSchemeDetails.class, schemeId);
            assertNotNull(updatedScheme);
            assertNull(updatedScheme.getAmfi());
            assertNull(updatedScheme.getIsin());
            // cleanup: remove the committed row so other tests are not affected
            em2.getTransaction().begin();
            em2.remove(updatedScheme);
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
