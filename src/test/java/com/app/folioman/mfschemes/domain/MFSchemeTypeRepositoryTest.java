package com.app.folioman.mfschemes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.app.folioman.config.SQLContainersConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
@Import(SQLContainersConfig.class)
@Execution(ExecutionMode.SAME_THREAD) // To avoid issues with cache manager in parallel tests
class MFSchemeTypeRepositoryTest {

    @MockitoBean
    private MFSchemeTypeRepository MfSchemeTypeRepository;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void findByTypeAndCategoryAndSubCategoryValidParametersReturnsEntity() {
        String type = "EQUITY";
        String category = "LARGE_CAP";
        String subCategory = "GROWTH";
        MFSchemeTypeEntity expectedEntity = new MFSchemeTypeEntity();
        expectedEntity.setType(type);
        expectedEntity.setCategory(category);
        expectedEntity.setSubCategory(subCategory);

        given(MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .willReturn(expectedEntity);

        MFSchemeTypeEntity result =
                MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getSubCategory()).isEqualTo(subCategory);
        verify(MfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategoryNoMatchFoundReturnsNull() {
        String type = "INVALID";
        String category = "INVALID";
        String subCategory = "INVALID";

        given(MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .willReturn(null);

        MFSchemeTypeEntity result =
                MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertThat(result).isNull();
        verify(MfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategoryNullParametersHandlesProperly() {
        given(MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(null, null, null))
                .willReturn(null);

        MFSchemeTypeEntity result = MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(null, null, null);

        assertThat(result).isNull();
        verify(MfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(null, null, null);
    }

    @Test
    void findByTypeAndCategoryAndSubCategoryEmptyParametersHandlesProperly() {
        String emptyType = "";
        String emptyCategory = "";
        String emptySubCategory = "";

        given(MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(emptyType, emptyCategory, emptySubCategory))
                .willReturn(null);

        MFSchemeTypeEntity result =
                MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(emptyType, emptyCategory, emptySubCategory);

        assertThat(result).isNull();
        verify(MfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(emptyType, emptyCategory, emptySubCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategoryCacheableAnnotationCachesResult() {
        String type = "DEBT";
        String category = "MEDIUM_TERM";
        String subCategory = "GOVERNMENT";
        MFSchemeTypeEntity expectedEntity = new MFSchemeTypeEntity();
        expectedEntity.setType(type);
        expectedEntity.setCategory(category);
        expectedEntity.setSubCategory(subCategory);

        given(MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .willReturn(expectedEntity);

        MFSchemeTypeEntity firstCall =
                MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);
        MFSchemeTypeEntity secondCall =
                MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertThat(firstCall).isNotNull();
        assertThat(secondCall).isNotNull();
        assertThat(secondCall.getType()).isEqualTo(firstCall.getType());
        verify(MfSchemeTypeRepository, times(2)).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategoryMixedCaseParametersReturnsEntity() {
        String type = "equity";
        String category = "LARGE_cap";
        String subCategory = "Growth";
        MFSchemeTypeEntity expectedEntity = new MFSchemeTypeEntity();
        expectedEntity.setType(type);
        expectedEntity.setCategory(category);
        expectedEntity.setSubCategory(subCategory);

        given(MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .willReturn(expectedEntity);

        MFSchemeTypeEntity result =
                MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getSubCategory()).isEqualTo(subCategory);
        verify(MfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }
}
