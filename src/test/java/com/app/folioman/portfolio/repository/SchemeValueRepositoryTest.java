package com.app.folioman.portfolio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.portfolio.entities.SchemeValue;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(com.app.folioman.config.SQLContainersConfig.class)
class SchemeValueRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SchemeValueRepository schemeValueRepository;

    @Test
    void testFindFirstByUserSchemeDetails_UserFolioDetails_IdOrderByDateDesc_WithValidId() {
        SchemeValue result = schemeValueRepository.findFirstByUserSchemeDetails_UserFolioDetails_IdOrderByDateDesc(1L);

        assertThat(result).isNull();
    }

    @Test
    void testFindFirstByUserSchemeDetails_UserFolioDetails_IdOrderByDateDesc_WithNullId() {
        SchemeValue result =
                schemeValueRepository.findFirstByUserSchemeDetails_UserFolioDetails_IdOrderByDateDesc(null);

        assertThat(result).isNull();
    }

    @Test
    void testFindFirstByUserSchemeDetails_UserFolioDetails_IdOrderByDateDesc_WithNonExistentId() {
        SchemeValue result =
                schemeValueRepository.findFirstByUserSchemeDetails_UserFolioDetails_IdOrderByDateDesc(999L);

        assertThat(result).isNull();
    }

    @Test
    void testFindFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc_WithValidParameters() {
        LocalDate testDate = LocalDate.now();
        Optional<SchemeValue> result =
                schemeValueRepository.findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(1L, testDate);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc_WithNullId() {
        LocalDate testDate = LocalDate.now();
        Optional<SchemeValue> result =
                schemeValueRepository.findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(null, testDate);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc_WithNullDate() {
        Optional<SchemeValue> result =
                schemeValueRepository.findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(1L, null);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc_WithBothNullParameters() {
        Optional<SchemeValue> result =
                schemeValueRepository.findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc_WithNonExistentId() {
        LocalDate testDate = LocalDate.now();
        Optional<SchemeValue> result =
                schemeValueRepository.findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(999L, testDate);

        assertThat(result).isEmpty();
    }
}
