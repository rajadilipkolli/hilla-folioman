package com.app.folioman.portfolio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.portfolio.entities.CasTypeEnum;
import com.app.folioman.portfolio.entities.FileTypeEnum;
import com.app.folioman.portfolio.entities.FolioScheme;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(SQLContainersConfig.class)
class FolioSchemeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FolioSchemeRepository folioSchemeRepository;

    @Test
    void findByUserSchemeDetails_Id_ShouldReturnFolioScheme_WhenValidUserSchemeDetailId() {
        UserFolioDetails userFolio = new UserFolioDetails();
        userFolio.setFolio("FOLIO-A");
        userFolio.setAmc("AMC-A");
        userFolio.setPan("PAN-A");
        UserCASDetails cas = new UserCASDetails();
        cas.setCasTypeEnum(CasTypeEnum.DETAILED);
        cas.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        cas = entityManager.persistAndFlush(cas);
        userFolio.setUserCasDetails(cas);
        userFolio = entityManager.persistAndFlush(userFolio);

        UserSchemeDetails userSchemeDetails = new UserSchemeDetails();
        userSchemeDetails.setScheme("SOME SCHEME");
        userSchemeDetails.setUserFolioDetails(userFolio);
        userSchemeDetails = entityManager.persistAndFlush(userSchemeDetails);

        FolioScheme folioScheme = new FolioScheme();
        folioScheme.setUserFolioDetails(userFolio);
        folioScheme.setUserSchemeDetails(userSchemeDetails);
        folioScheme = entityManager.persistAndFlush(folioScheme);

        Long userSchemeDetailId = userSchemeDetails.getId();

        FolioScheme result = folioSchemeRepository.findByUserSchemeDetails_Id(userSchemeDetailId);

        assertThat(result).isNotNull();
    }

    @Test
    void findByUserSchemeDetails_Id_ShouldReturnNull_WhenNullUserSchemeDetailId() {
        FolioScheme result = folioSchemeRepository.findByUserSchemeDetails_Id(null);

        assertThat(result).isNull();
    }

    @Test
    void findByUserSchemeDetails_Id_ShouldReturnNull_WhenNonExistentUserSchemeDetailId() {
        Long nonExistentId = 999L;

        FolioScheme result = folioSchemeRepository.findByUserSchemeDetails_Id(nonExistentId);

        assertThat(result).isNull();
    }

    @Test
    void findByUserFolioDetails_Id_ShouldReturnListOfFolioScheme_WhenValidId() {
        UserFolioDetails userFolio = new UserFolioDetails();
        userFolio.setFolio("FOLIO-B");
        userFolio.setAmc("AMC-B");
        userFolio.setPan("PAN-B");
        UserCASDetails cas2 = new UserCASDetails();
        cas2.setCasTypeEnum(CasTypeEnum.SUMMARY);
        cas2.setFileTypeEnum(FileTypeEnum.UNKNOWN);
        cas2 = entityManager.persistAndFlush(cas2);
        userFolio.setUserCasDetails(cas2);
        userFolio = entityManager.persistAndFlush(userFolio);

        UserSchemeDetails userSchemeDetails = new UserSchemeDetails();
        userSchemeDetails.setScheme("SOME SCHEME");
        userSchemeDetails.setUserFolioDetails(userFolio);
        userSchemeDetails = entityManager.persistAndFlush(userSchemeDetails);

        FolioScheme folioScheme = new FolioScheme();
        folioScheme.setUserFolioDetails(userFolio);
        folioScheme.setUserSchemeDetails(userSchemeDetails);
        folioScheme = entityManager.persistAndFlush(folioScheme);

        Long id = userFolio.getId();

        List<FolioScheme> result = folioSchemeRepository.findByUserFolioDetails_Id(id);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(List.class);
    }

    @Test
    void findByUserFolioDetails_Id_ShouldReturnEmptyList_WhenNullId() {
        List<FolioScheme> result = folioSchemeRepository.findByUserFolioDetails_Id(null);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserFolioDetails_Id_ShouldReturnEmptyList_WhenNonExistentId() {
        Long nonExistentId = 999L;

        List<FolioScheme> result = folioSchemeRepository.findByUserFolioDetails_Id(nonExistentId);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
