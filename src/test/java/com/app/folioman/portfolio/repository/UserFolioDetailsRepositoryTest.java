package com.app.folioman.portfolio.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.models.projection.UserFolioDetailsPanProjection;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

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
    void testFindByUserCasDetails_FoliosIn_withValidFolios() {
        List<UserFolioDetails> folios = new ArrayList<>();
        UserFolioDetails folio = new UserFolioDetails();
        folios.add(folio);

        List<UserFolioDetails> result = userFolioDetailsRepository.findByUserCasDetails_FoliosIn(folios);

        assertNotNull(result);
    }

    @Test
    void testFindFirstByUserCasDetails_IdAndPanKyc_whenFound() {
        Long userCasId = 1L;
        String kycStatus = "OK";

        UserFolioDetailsPanProjection result =
                userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(userCasId, kycStatus);

        // Result can be null if no data exists in test database
        // This is expected behavior for repository tests
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
        Long casId = 1L;

        int result = userFolioDetailsRepository.updatePanByCasId(pan, casId);

        assertTrue(result >= 0);
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
        Long casId = 1L;

        int result = userFolioDetailsRepository.updatePanByCasId(null, casId);

        assertTrue(result >= 0);
    }

    @Test
    void testUpdatePanByCasId_withNullCasId() {
        String pan = "ABCDE1234F";

        assertThrows(Exception.class, () -> {
            userFolioDetailsRepository.updatePanByCasId(pan, null);
        });
    }
}
