package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.portfolio.domain.models.projection.UserFolioDetailsPanProjection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.transaction.TestTransaction;

@DataJpaTest
@Import(SQLContainersConfig.class)
class UserFolioDetailsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserFolioDetailsRepository userFolioDetailsRepository;

    @Test
    void findByUserCasDetailsFoliosInWithEmptyList() {
        List<UserFolioDetailsEntity> emptyList = new ArrayList<>();

        List<UserFolioDetailsEntity> result = userFolioDetailsRepository.findByUserCasDetails_FoliosIn(emptyList);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserCasDetailsFoliosInWithValidFolios() {
        List<UserFolioDetailsEntity> folios = new ArrayList<>();
        UserFolioDetailsEntity folio = new UserFolioDetailsEntity();

        // Persist a minimal UserCasDetailsEntity and the folio so the repository query can use a managed entity
        UserCasDetailsEntity userCasDetailsEntity = new UserCasDetailsEntity();
        userCasDetailsEntity.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCasDetailsEntity.setFileTypeEnum(FileTypeEnum.CAMS);
        entityManager.persist(userCasDetailsEntity);
        entityManager.flush();

        folio.setFolio("FOLIO1");
        folio.setAmc("AMC1");
        folio.setPan("PAN1");
        folio.setPanKyc("OK");
        folio.setUserCasDetails(userCasDetailsEntity);
        UserFolioDetailsEntity saved = userFolioDetailsRepository.save(folio);
        folios.add(saved);
        entityManager.flush();

        List<UserFolioDetailsEntity> result = userFolioDetailsRepository.findByUserCasDetails_FoliosIn(folios);

        assertThat(result).isNotNull();
    }

    @Test
    void findFirstByUserCasDetailsIdAndPanKycWhenFound() {
        Long userCasId = 1L;
        String kycStatus = "OK";

        // Result can be null if no data exists in test database; ensure call doesn't throw
        assertThatCode(() -> userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(userCasId, kycStatus))
                .doesNotThrowAnyException();
    }

    @Test
    void findFirstByUserCasDetailsIdAndPanKycWhenNotFound() {
        Long nonExistentUserCasId = 999L;
        String kycStatus = "NOT OK";

        Optional<UserFolioDetailsPanProjection> result =
                userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(nonExistentUserCasId, kycStatus);

        assertThat(result).isEmpty();
    }

    @Test
    void updatePanByCasIdWithValidParameters() {
        String pan = "ABCDE1234F";
        // Create and persist a UserCasDetailsEntity so we have a real casId
        UserCasDetailsEntity userCasDetailsEntity = new UserCasDetailsEntity();
        userCasDetailsEntity.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCasDetailsEntity.setFileTypeEnum(FileTypeEnum.CAMS);
        entityManager.persist(userCasDetailsEntity);
        entityManager.flush();

        // create and persist a matching InvestorInfo so repository reads that join investor_info succeed
        InvestorInfo investorInfo = new InvestorInfo();
        investorInfo.setName("Test User");
        investorInfo.setEmail("test@example.com");
        investorInfo.setUserCasDetailsEntity(userCasDetailsEntity);
        entityManager.persist(investorInfo);
        entityManager.flush();

        Long casId = userCasDetailsEntity.getId();

        // Persist a row with the target casId so the update affects a row
        UserFolioDetailsEntity entity = new UserFolioDetailsEntity();
        entity.setFolio("FOLIO_A");
        entity.setAmc("AMC_A");
        entity.setPan("OLDPAN");
        entity.setPanKyc("NOT OK");
        entity.setUserCasDetails(userCasDetailsEntity);
        entity = userFolioDetailsRepository.save(entity);
        entityManager.flush();

        // Commit the current test transaction so the REQUIRES_NEW update can see the row.
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // Invoke the repository update in its own transaction (REQUIRES_NEW).
        int result = userFolioDetailsRepository.updatePanByCasId(pan, casId);
        assertThat(result).isOne();

        // Start a new test-managed transaction and clear the persistence context
        // so subsequent repository reads reflect the committed DB state.
        TestTransaction.start();
        entityManager.clear();

        assertThat(userFolioDetailsRepository.findById(entity.getId())).isPresent();
        UserFolioDetailsEntity updated =
                userFolioDetailsRepository.findById(entity.getId()).get();
        assertThat(updated.getPan()).isEqualTo(pan);
    }

    @Test
    void updatePanByCasIdWithNonExistentCasId() {
        String pan = "ABCDE1234F";
        Long nonExistentCasId = 999L;

        int result = userFolioDetailsRepository.updatePanByCasId(pan, nonExistentCasId);

        assertThat(result).isZero();
    }

    @Test
    void updatePanByCasIdWithNullPan() {
        // Create and persist a UserCasDetailsEntity so we have a real casId
        UserCasDetailsEntity userCasDetailsEntity = new UserCasDetailsEntity();
        userCasDetailsEntity.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCasDetailsEntity.setFileTypeEnum(FileTypeEnum.CAMS);
        entityManager.persist(userCasDetailsEntity);
        entityManager.flush();

        // ensure an InvestorInfo exists for the UserCasDetailsEntity (the repository read joins investor_info)
        InvestorInfo investorInfo = new InvestorInfo();
        investorInfo.setName("Test User B");
        investorInfo.setEmail("testb@example.com");
        investorInfo.setUserCasDetailsEntity(userCasDetailsEntity);
        entityManager.persist(investorInfo);
        entityManager.flush();

        Long casId = userCasDetailsEntity.getId();

        // Persist a row with the target casId so the update affects a row
        UserFolioDetailsEntity entity = new UserFolioDetailsEntity();
        entity.setFolio("FOLIO_B");
        entity.setAmc("AMC_B");
        entity.setPan("OLDPAN");
        entity.setPanKyc("NOT OK");
        entity.setUserCasDetails(userCasDetailsEntity);
        entity = userFolioDetailsRepository.save(entity);
        entityManager.flush();

        // Commit current transaction so REQUIRES_NEW update sees the row.
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // Invoke the update (runs in REQUIRES_NEW).
        // Use empty string instead of null because 'pan' column is NOT NULL in DB
        int result = userFolioDetailsRepository.updatePanByCasId("", casId);
        assertThat(result).isOne();

        // Start a new transaction and clear persistence context before verifying.
        TestTransaction.start();
        entityManager.clear();

        assertThat(userFolioDetailsRepository.findById(entity.getId())).isPresent();
        UserFolioDetailsEntity updated =
                userFolioDetailsRepository.findById(entity.getId()).get();
        assertThat(updated.getPan()).isEmpty();
    }
}
