package com.app.folioman.mfschemes.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.mfschemes.entities.MFSchemeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
    void testFindByTypeAndCategoryAndSubCategory_ValidParameters_ReturnsEntity() {
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

        assertNotNull(result);
        assertEquals(type, result.getType());
        assertEquals(category, result.getCategory());
        assertEquals(subCategory, result.getSubCategory());
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void testFindByTypeAndCategoryAndSubCategory_NoMatchFound_ReturnsNull() {
        String type = "INVALID";
        String category = "INVALID";
        String subCategory = "INVALID";

        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .thenReturn(null);

        MFSchemeType result = mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertNull(result);
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void testFindByTypeAndCategoryAndSubCategory_NullParameters_HandlesProperly() {
        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(null, null, null))
                .thenReturn(null);

        MFSchemeType result = mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(null, null, null);

        assertNull(result);
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(null, null, null);
    }

    @Test
    void testFindByTypeAndCategoryAndSubCategory_EmptyParameters_HandlesProperly() {
        String emptyType = "";
        String emptyCategory = "";
        String emptySubCategory = "";

        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(emptyType, emptyCategory, emptySubCategory))
                .thenReturn(null);

        MFSchemeType result =
                mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(emptyType, emptyCategory, emptySubCategory);

        assertNull(result);
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(emptyType, emptyCategory, emptySubCategory);
    }

    @Test
    void testFindByTypeAndCategoryAndSubCategory_CacheableAnnotation_CachesResult() {
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

        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertEquals(firstCall.getType(), secondCall.getType());
        verify(mfSchemeTypeRepository, times(2)).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void testFindByTypeAndCategoryAndSubCategory_MixedCaseParameters_ReturnsEntity() {
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

        assertNotNull(result);
        assertEquals(type, result.getType());
        assertEquals(category, result.getCategory());
        assertEquals(subCategory, result.getSubCategory());
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }
}
