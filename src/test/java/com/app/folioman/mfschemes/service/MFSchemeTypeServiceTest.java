package com.app.folioman.mfschemes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.entities.MFSchemeType;
import com.app.folioman.mfschemes.repository.MFSchemeTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MFSchemeTypeServiceTest {

    @Mock
    private MFSchemeTypeRepository mfSchemeTypeRepository;

    @InjectMocks
    private MFSchemeTypeService mfSchemeTypeService;

    private MFSchemeType testMFSchemeType;

    @BeforeEach
    void setUp() {
        testMFSchemeType = new MFSchemeType();
    }

    @Test
    void findByTypeAndCategoryAndSubCategory_WithValidParameters_ShouldReturnMFSchemeType() {
        String type = "Equity";
        String category = "Large Cap";
        String subCategory = "Diversified";

        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .thenReturn(testMFSchemeType);

        MFSchemeType result = mfSchemeTypeService.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertEquals(testMFSchemeType, result);
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategory_WithValidParameters_ShouldReturnNull() {
        String type = "Debt";
        String category = "Short Term";
        String subCategory = "Liquid";

        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .thenReturn(null);

        MFSchemeType result = mfSchemeTypeService.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertNull(result);
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategory_WithNullParameters_ShouldCallRepository() {
        when(mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(null, null, null))
                .thenReturn(null);

        MFSchemeType result = mfSchemeTypeService.findByTypeAndCategoryAndSubCategory(null, null, null);

        assertNull(result);
        verify(mfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(null, null, null);
    }

    @Test
    void saveCategory_WithValidMFSchemeType_ShouldReturnSavedMFSchemeType() {
        when(mfSchemeTypeRepository.save(testMFSchemeType)).thenReturn(testMFSchemeType);

        MFSchemeType result = mfSchemeTypeService.saveCategory(testMFSchemeType);

        assertEquals(testMFSchemeType, result);
        verify(mfSchemeTypeRepository).save(testMFSchemeType);
    }

    @Test
    void saveCategory_WithNullParameter_ShouldCallRepository() {
        when(mfSchemeTypeRepository.save(null)).thenReturn(null);

        MFSchemeType result = mfSchemeTypeService.saveCategory(null);

        assertNull(result);
        verify(mfSchemeTypeRepository).save(null);
    }
}
