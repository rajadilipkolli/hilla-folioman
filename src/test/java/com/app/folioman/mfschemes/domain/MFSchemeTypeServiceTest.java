package com.app.folioman.mfschemes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MFSchemeTypeServiceTest {

    @Mock
    private MFSchemeTypeRepository MfSchemeTypeRepository;

    @InjectMocks
    private MFSchemeTypeService mfSchemeTypeService;

    private MFSchemeTypeEntity testMFSchemeType;

    @BeforeEach
    void setUp() {
        testMFSchemeType = new MFSchemeTypeEntity();
    }

    @Test
    void findByTypeAndCategoryAndSubCategory_WithValidParameters_ShouldReturnMFSchemeType() {
        String type = "Equity";
        String category = "Large Cap";
        String subCategory = "Diversified";

        when(MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .thenReturn(testMFSchemeType);

        MFSchemeTypeEntity result =
                mfSchemeTypeService.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertThat(result).isEqualTo(testMFSchemeType);
        verify(MfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategory_WithValidParameters_ShouldReturnNull() {
        String type = "Debt";
        String category = "Short Term";
        String subCategory = "Liquid";

        when(MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory))
                .thenReturn(null);

        MFSchemeTypeEntity result =
                mfSchemeTypeService.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

        assertThat(result).isNull();
        verify(MfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Test
    void findByTypeAndCategoryAndSubCategory_WithNullParameters_ShouldCallRepository() {
        when(MfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(null, null, null))
                .thenReturn(null);

        MFSchemeTypeEntity result = mfSchemeTypeService.findByTypeAndCategoryAndSubCategory(null, null, null);

        assertThat(result).isNull();
        verify(MfSchemeTypeRepository).findByTypeAndCategoryAndSubCategory(null, null, null);
    }

    @Test
    void saveCategory_WithValidMFSchemeType_ShouldReturnSavedMFSchemeType() {
        when(MfSchemeTypeRepository.save(testMFSchemeType)).thenReturn(testMFSchemeType);

        MFSchemeTypeEntity result = mfSchemeTypeService.saveCategory(testMFSchemeType);

        assertThat(result).isEqualTo(testMFSchemeType);
        verify(MfSchemeTypeRepository).save(testMFSchemeType);
    }

    @Test
    void saveCategory_WithNullParameter_ShouldCallRepository() {
        when(MfSchemeTypeRepository.save(null)).thenReturn(null);

        MFSchemeTypeEntity result = mfSchemeTypeService.saveCategory(null);

        assertThat(result).isNull();
        verify(MfSchemeTypeRepository).save(null);
    }
}
