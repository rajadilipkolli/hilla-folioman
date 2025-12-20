package com.app.folioman.mfschemes.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.mfschemes.entities.MFSchemeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
@Import(SQLContainersConfig.class)
class MFSchemeTypeRepositoryTest {

    @MockitoBean
    private MFSchemeTypeRepository mfSchemeTypeRepository;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void findByTypeAndCategoryAndSubCategoryValidParametersReturnsEntity() {
        String type = "EQUITY";
        String category = "LARGE_CAP";
        String subCategory = "GROWTH";
        MFSchemeType expectedEntity = new MFSchemeType();
        expectedEntity.setType(type);
        expectedEntity.setCategory(category);
        expectedEntity.setSubCategory(subCategory);

        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .thenReturn(expectedEntity);

        MFSchemeType result = mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getSubCategory()).isEqualTo(subCategory);
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategoryNoMatchFoundReturnsNull() {
        String type = "INVALID";
        String category = "INVALID";
        String subCategory = "INVALID";

        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .thenReturn(null);

        MFSchemeType result = mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertThat(result).isNull();
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategoryNullParametersHandlesProperly() {
        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(null, null, null))
                .thenReturn(null);

        MFSchemeType result = mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(null, null, null);

        assertThat(result).isNull();
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(null, null, null);
    }

    @Test
    void findByTypeAndCategoryAndSubCategoryEmptyParametersHandlesProperly() {
        String emptyType = "";
        String emptyCategory = "";
        String emptySubCategory = "";

        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(emptyType, emptyCategory, emptySubCategory))
                .thenReturn(null);

        MFSchemeType result =
                mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(emptyType, emptyCategory, emptySubCategory);

        assertThat(result).isNull();
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(emptyType, emptyCategory, emptySubCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategoryCacheableAnnotationCachesResult() {
        String type = "DEBT";
        String category = "MEDIUM_TERM";
        String subCategory = "GOVERNMENT";
        MFSchemeType expectedEntity = new MFSchemeType();
        expectedEntity.setType(type);
        expectedEntity.setCategory(category);
        expectedEntity.setSubCategory(subCategory);

        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .thenReturn(expectedEntity);

        MFSchemeType firstCall =
                mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);
        MFSchemeType secondCall =
                mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertThat(firstCall).isNotNull();
        assertThat(secondCall).isNotNull();
        assertThat(secondCall.getType()).isEqualTo(firstCall.getType());
        verify(mfSchemeTypeRepository, times(2)).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategoryMixedCaseParametersReturnsEntity() {
        String type = "equity";
        String category = "LARGE_cap";
        String subCategory = "Growth";
        MFSchemeType expectedEntity = new MFSchemeType();
        expectedEntity.setType(type);
        expectedEntity.setCategory(category);
        expectedEntity.setSubCategory(subCategory);

        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .thenReturn(expectedEntity);

        MFSchemeType result = mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getSubCategory()).isEqualTo(subCategory);
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }
}
