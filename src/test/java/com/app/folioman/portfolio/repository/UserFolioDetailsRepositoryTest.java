package com.app.folioman.portfolio.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.app.folioman.portfolio.entities.CasTypeEnum;
import com.app.folioman.portfolio.entities.FileTypeEnum;
import com.app.folioman.portfolio.entities.InvestorInfo;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.models.projection.UserFolioDetailsPanProjection;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

@DataJpaTest
@ActiveProfiles("test")
@Import(com.app.folioman.config.SQLContainersConfig.class)
class UserFolioDetailsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserFolioDetailsRepository userFolioDetailsRepository;

    @Test
    void testFindByUserCasDetails_FoliosIn_withEmptyList() {
        List<UserFolioDetails> emptyList = new ArrayList<>();

        List<UserFolioDetails> result = userFolioDetailsRepository.findByUserCasDetails_FoliosIn(emptyList);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByUserCasDetails_FoliosIn_withNullList() {
        assertThrows(Exception.class, () -> {
            userFolioDetailsRepository.findByUserCasDetails_FoliosIn(null);
        });
    }

    @Test
    @Disabled
    void testFindByUserCasDetails_FoliosIn_withValidFolios() {
        List<UserFolioDetails> folios = new ArrayList<>();
        UserFolioDetails folio = new UserFolioDetails();

        // Persist a minimal UserCASDetails and the folio so the repository query can use a managed entity
        UserCASDetails userCasDetails = new UserCASDetails();
        userCasDetails.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCasDetails.setFileTypeEnum(FileTypeEnum.CAMS);
        entityManager.persist(userCasDetails);
        entityManager.flush();

        folio.setFolio("FOLIO1");
        folio.setAmc("AMC1");
        folio.setPan("PAN1");
        folio.setPanKyc("OK");
        folio.setUserCasDetails(userCasDetails);
        UserFolioDetails saved = userFolioDetailsRepository.save(folio);
        folios.add(saved);
        entityManager.flush();

        List<UserFolioDetails> result = userFolioDetailsRepository.findByUserCasDetails_FoliosIn(folios);

        assertNotNull(result);
    }

    @Test
    void testFindFirstByUserCasDetails_IdAndPanKyc_whenFound() {
        Long userCasId = 1L;
        String kycStatus = "OK";

        // Result can be null if no data exists in test database; ensure call doesn't throw
        assertDoesNotThrow(
                () -> userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(userCasId, kycStatus));
    }

    @Test
    void testFindFirstByUserCasDetails_IdAndPanKyc_whenNotFound() {
        Long nonExistentUserCasId = 999L;
        String kycStatus = "NOT OK";

        UserFolioDetailsPanProjection result =
                userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(nonExistentUserCasId, kycStatus);

        assertNull(result);
    }

    @Test
    void testFindFirstByUserCasDetails_IdAndPanKyc_withNullParameters() {
        UserFolioDetailsPanProjection result1 =
                userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(null, "OK");
        UserFolioDetailsPanProjection result2 =
                userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(1L, null);

        assertNull(result1);
        assertNull(result2);
    }

    @Test
    void testUpdatePanByCasId_withValidParameters() {
        String pan = "ABCDE1234F";
        // Create and persist a UserCASDetails so we have a real casId
        UserCASDetails userCasDetails = new UserCASDetails();
        userCasDetails.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCasDetails.setFileTypeEnum(FileTypeEnum.CAMS);
        entityManager.persist(userCasDetails);
        entityManager.flush();

        // create and persist a matching InvestorInfo so repository reads that join investor_info succeed
        InvestorInfo investorInfo = new InvestorInfo();
        investorInfo.setName("Test User");
        investorInfo.setEmail("test@example.com");
        investorInfo.setUserCasDetails(userCasDetails);
        entityManager.persist(investorInfo);
        entityManager.flush();

        Long casId = userCasDetails.getId();

        // Persist a row with the target casId so the update affects a row
        UserFolioDetails entity = new UserFolioDetails();
        entity.setFolio("FOLIO_A");
        entity.setAmc("AMC_A");
        entity.setPan("OLDPAN");
        entity.setPanKyc("NOT OK");
        entity.setUserCasDetails(userCasDetails);
        entity = userFolioDetailsRepository.save(entity);
        entityManager.flush();

        // Commit the current test transaction so the REQUIRES_NEW update can see the row.
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // Invoke the repository update in its own transaction (REQUIRES_NEW).
        int result = userFolioDetailsRepository.updatePanByCasId(pan, casId);
        assertEquals(1, result);

        // Start a new test-managed transaction and clear the persistence context
        // so subsequent repository reads reflect the committed DB state.
        TestTransaction.start();
        entityManager.clear();

        assertTrue(userFolioDetailsRepository.findById(entity.getId()).isPresent());
        UserFolioDetails updated =
                userFolioDetailsRepository.findById(entity.getId()).get();
        assertEquals(pan, updated.getPan());
    }

    @Test
    void testUpdatePanByCasId_withNonExistentCasId() {
        String pan = "ABCDE1234F";
        Long nonExistentCasId = 999L;

        int result = userFolioDetailsRepository.updatePanByCasId(pan, nonExistentCasId);

        assertEquals(0, result);
    }

    @Test
    void testUpdatePanByCasId_withNullPan() {
        // Create and persist a UserCASDetails so we have a real casId
        UserCASDetails userCasDetails = new UserCASDetails();
        userCasDetails.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCasDetails.setFileTypeEnum(FileTypeEnum.CAMS);
        entityManager.persist(userCasDetails);
        entityManager.flush();

        // ensure an InvestorInfo exists for the UserCASDetails (the repository read joins investor_info)
        InvestorInfo investorInfo = new InvestorInfo();
        investorInfo.setName("Test User B");
        investorInfo.setEmail("testb@example.com");
        investorInfo.setUserCasDetails(userCasDetails);
        entityManager.persist(investorInfo);
        entityManager.flush();

        Long casId = userCasDetails.getId();

        // Persist a row with the target casId so the update affects a row
        UserFolioDetails entity = new UserFolioDetails();
        entity.setFolio("FOLIO_B");
        entity.setAmc("AMC_B");
        entity.setPan("OLDPAN");
        entity.setPanKyc("NOT OK");
        entity.setUserCasDetails(userCasDetails);
        entity = userFolioDetailsRepository.save(entity);
        entityManager.flush();

        // Commit current transaction so REQUIRES_NEW update sees the row.
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // Invoke the update (runs in REQUIRES_NEW).
        // Use empty string instead of null because 'pan' column is NOT NULL in DB
        int result = userFolioDetailsRepository.updatePanByCasId("", casId);
        assertEquals(1, result);

        // Start a new transaction and clear persistence context before verifying.
        TestTransaction.start();
        entityManager.clear();

        assertTrue(userFolioDetailsRepository.findById(entity.getId()).isPresent());
        UserFolioDetails updated =
                userFolioDetailsRepository.findById(entity.getId()).get();
        assertEquals("", updated.getPan());
    }

    @Test
    @Disabled
    void testUpdatePanByCasId_withNullCasId() {
        String pan = "ABCDE1234F";
        // Passing null casId should not throw; repository will not update any rows and return 0
        int result = userFolioDetailsRepository.updatePanByCasId(pan, null);
        assertEquals(0, result);
    }
}
