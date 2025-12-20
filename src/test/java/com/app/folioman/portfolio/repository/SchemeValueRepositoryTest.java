package com.app.folioman.portfolio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.portfolio.entities.SchemeValue;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(SQLContainersConfig.class)
class SchemeValueRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SchemeValueRepository schemeValueRepository;

    @Test
    void findFirstByUserSchemeDetailsUserFolioDetailsIdOrderByDateDescWithValidId() {
        SchemeValue result = schemeValueRepository.findFirstByUserSchemeDetails_UserFolioDetails_IdOrderByDateDesc(1L);

        assertThat(result).isNull();
    }

    @Test
    void findFirstByUserSchemeDetailsUserFolioDetailsIdOrderByDateDescWithNullId() {
        SchemeValue result =
                schemeValueRepository.findFirstByUserSchemeDetails_UserFolioDetails_IdOrderByDateDesc(null);

        assertThat(result).isNull();
    }

    @Test
    void findFirstByUserSchemeDetailsUserFolioDetailsIdOrderByDateDescWithNonExistentId() {
        SchemeValue result =
                schemeValueRepository.findFirstByUserSchemeDetails_UserFolioDetails_IdOrderByDateDesc(999L);

        assertThat(result).isNull();
    }

    @Test
    void findFirstByUserSchemeDetailsIdAndDateBeforeOrderByDateDescWithValidParameters() {
        LocalDate testDate = LocalDate.now();
        Optional<SchemeValue> result =
                schemeValueRepository.findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(1L, testDate);

        assertThat(result).isEmpty();
    }

    @Test
    void findFirstByUserSchemeDetailsIdAndDateBeforeOrderByDateDescWithNullId() {
        LocalDate testDate = LocalDate.now();
        Optional<SchemeValue> result =
                schemeValueRepository.findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(null, testDate);

        assertThat(result).isEmpty();
    }

    @Test
    void findFirstByUserSchemeDetailsIdAndDateBeforeOrderByDateDescWithNullDate() {
        Optional<SchemeValue> result =
                schemeValueRepository.findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(1L, null);

        assertThat(result).isEmpty();
    }

    @Test
    void findFirstByUserSchemeDetailsIdAndDateBeforeOrderByDateDescWithBothNullParameters() {
        Optional<SchemeValue> result =
                schemeValueRepository.findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void findFirstByUserSchemeDetailsIdAndDateBeforeOrderByDateDescWithNonExistentId() {
        LocalDate testDate = LocalDate.now();
        Optional<SchemeValue> result =
                schemeValueRepository.findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(999L, testDate);

        assertThat(result).isEmpty();
    }
}
