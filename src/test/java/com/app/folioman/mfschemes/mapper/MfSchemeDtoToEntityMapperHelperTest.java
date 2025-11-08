package com.app.folioman.mfschemes.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.entities.MFSchemeType;
import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.service.MFSchemeTypeService;
import com.app.folioman.mfschemes.service.MfAmcService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MfSchemeDtoToEntityMapperHelperTest {

    @Mock
    private MFSchemeTypeService mFSchemeTypeService;

    @Mock
    private MfAmcService mfAmcService;

    @InjectMocks
    private MfSchemeDtoToEntityMapperHelper mapperHelper;

    private MFSchemeDTO mfSchemeDTO;
    private MfFundScheme mfScheme;
    private MfAmc mockAmc;
    private MFSchemeType mockSchemeType;

    @BeforeEach
    void setUp() {
        mockAmc = new MfAmc();
        mockAmc.setId(1);
        mockAmc.setName("Test AMC");

        mockSchemeType = new MFSchemeType();
        mockSchemeType.setSchemeTypeId(1);
        mockSchemeType.setType("Equity");
        mockSchemeType.setCategory("Large Cap");
        mockSchemeType.setSubCategory("Growth");

        mfScheme = new MfFundScheme();
        mfScheme.setAmc(new MfAmc());
    }

    @Test
    void updateMFScheme_WithValidNavAndComplexSchemeType_ShouldMapCorrectly() {
        mfSchemeDTO = new MFSchemeDTO(
                "Test AMC", 1L, null, "Test Scheme", "25.50", "01-Jan-2024", "Equity Fund (Large Cap - Growth)");

        // Accept either exact match or a trimmed version; helper may strip last word for multi-word types
        when(mFSchemeTypeService.findByTypeAndCategoryAndSubCategory(anyString(), eq("Large Cap"), eq("Growth")))
                .thenAnswer(invocation -> {
                    String typeArg = ((String) invocation.getArgument(0)).trim();
                    if (typeArg.startsWith("Equity")) {
                        return mockSchemeType;
                    }
                    return null;
                });
        when(mfAmcService.findOrCreateByName("Test AMC")).thenReturn(mockAmc);

        mapperHelper.updateMFScheme(mfSchemeDTO, mfScheme);

        assertEquals(mockSchemeType, mfScheme.getMfSchemeType());
        assertEquals(mockAmc, mfScheme.getAmc());
        assertFalse(mfScheme.getMfSchemeNavs().isEmpty());
        MFSchemeNav nav = mfScheme.getMfSchemeNavs().get(0);
        assertEquals(new BigDecimal("25.50"), nav.getNav());
        assertEquals(LocalDate.of(2024, 1, 1), nav.getNavDate());
    }

    @Test
    void updateMFScheme_WithNANavValue_ShouldSetNavToZero() {
        mfSchemeDTO =
                new MFSchemeDTO("Test AMC", 1L, null, "Test Scheme", "N.A.", "01-Jan-2024", "Equity Fund (Large Cap)");

        when(mFSchemeTypeService.findByTypeAndCategoryAndSubCategory(eq("Equity Fund"), eq("Large Cap"), isNull()))
                .thenReturn(mockSchemeType);
        when(mfAmcService.findOrCreateByName("Test AMC")).thenReturn(mockAmc);

        mapperHelper.updateMFScheme(mfSchemeDTO, mfScheme);

        MFSchemeNav nav = mfScheme.getMfSchemeNavs().get(0);
        assertEquals(BigDecimal.ZERO, nav.getNav());
    }

    @Test
    void updateMFScheme_WithSimpleSchemeTypeFormat_ShouldParseCorrectly() {
        mfSchemeDTO =
                new MFSchemeDTO("Test AMC", 1L, null, "Test Scheme", "10.25", "15-Feb-2024", "Debt Fund (Short Term)");

        when(mFSchemeTypeService.findByTypeAndCategoryAndSubCategory(anyString(), eq("Short Term"), isNull()))
                .thenReturn(mockSchemeType);
        when(mfAmcService.findOrCreateByName("Test AMC")).thenReturn(mockAmc);

        mapperHelper.updateMFScheme(mfSchemeDTO, mfScheme);

        assertEquals(mockSchemeType, mfScheme.getMfSchemeType());
        assertEquals(
                LocalDate.of(2024, 2, 15), mfScheme.getMfSchemeNavs().get(0).getNavDate());
    }

    @Test
    void updateMFScheme_WithMultiWordTypeAndComplexPattern_ShouldTrimLastWord() {
        mfSchemeDTO = new MFSchemeDTO(
                "Test AMC",
                1L,
                null,
                "Test Scheme",
                "15.75",
                "01-Mar-2024",
                "Long Term Equity Fund (Large Cap - Growth)");

        when(mFSchemeTypeService.findByTypeAndCategoryAndSubCategory(anyString(), eq("Large Cap"), eq("Growth")))
                .thenReturn(mockSchemeType);
        when(mfAmcService.findOrCreateByName("Test AMC")).thenReturn(mockAmc);

        mapperHelper.updateMFScheme(mfSchemeDTO, mfScheme);

        verify(mFSchemeTypeService).findByTypeAndCategoryAndSubCategory("Long Term Equity", "Large Cap", "Growth");
    }

    @Test
    void updateMFScheme_WithInvalidSchemeTypeContainingDash_ShouldLogError() {
        mfSchemeDTO =
                new MFSchemeDTO("Test AMC", 1L, null, "Test Scheme", "12.50", "01-Apr-2024", "Invalid-Type-Format");

        when(mfAmcService.findOrCreateByName("Test AMC")).thenReturn(mockAmc);

        mapperHelper.updateMFScheme(mfSchemeDTO, mfScheme);

        assertNull(mfScheme.getMfSchemeType());
        verify(mFSchemeTypeService, never()).findByTypeAndCategoryAndSubCategory(anyString(), anyString(), anyString());
    }

    @Test
    void updateMFScheme_WithAmcIdNull_ShouldCreateNewAmc() {
        mfScheme.getAmc().setId(null);

        mfSchemeDTO =
                new MFSchemeDTO("New AMC", 1L, null, "Test Scheme", "20.00", "01-May-2024", "Equity Fund (Large Cap)");

        when(mFSchemeTypeService.findByTypeAndCategoryAndSubCategory(eq("Equity Fund"), eq("Large Cap"), isNull()))
                .thenReturn(mockSchemeType);
        when(mfAmcService.findOrCreateByName("New AMC")).thenReturn(mockAmc);

        mapperHelper.updateMFScheme(mfSchemeDTO, mfScheme);

        verify(mfAmcService).findOrCreateByName("New AMC");
        assertEquals(mockAmc, mfScheme.getAmc());
    }

    @Test
    void updateMFScheme_WithAmcIdNotNull_ShouldNotCreateNewAmc() {
        mfScheme.getAmc().setId(2);

        mfSchemeDTO = new MFSchemeDTO(
                "Existing AMC", 1L, null, "Test Scheme", "30.00", "01-Jun-2024", "Equity Fund (Large Cap)");

        when(mFSchemeTypeService.findByTypeAndCategoryAndSubCategory("Equity Fund", "Large Cap", null))
                .thenReturn(mockSchemeType);

        mapperHelper.updateMFScheme(mfSchemeDTO, mfScheme);

        verify(mfAmcService, never()).findOrCreateByName(anyString());
    }

    @Test
    void findOrCreateMFSchemeTypeEntity_WhenExistsInDatabase_ShouldReturnExisting() {
        when(mFSchemeTypeService.findByTypeAndCategoryAndSubCategory(eq("Equity"), eq("Large Cap"), eq("Growth")))
                .thenReturn(mockSchemeType);

        MFSchemeType result = mapperHelper.findOrCreateMFSchemeTypeEntity("Equity", "Large Cap", "Growth");

        assertEquals(mockSchemeType, result);
        verify(mFSchemeTypeService).findByTypeAndCategoryAndSubCategory("Equity", "Large Cap", "Growth");
        verify(mFSchemeTypeService, never()).saveCategory(any());
    }

    @Test
    void findOrCreateMFSchemeTypeEntity_WhenNotExistsInDatabase_ShouldCreateNew() {
        when(mFSchemeTypeService.findByTypeAndCategoryAndSubCategory(eq("Equity"), eq("Mid Cap"), eq("Value")))
                .thenReturn(null);
        when(mFSchemeTypeService.saveCategory(any(MFSchemeType.class))).thenReturn(mockSchemeType);

        MFSchemeType result = mapperHelper.findOrCreateMFSchemeTypeEntity("Equity", "Mid Cap", "Value");

        assertEquals(mockSchemeType, result);
        verify(mFSchemeTypeService).findByTypeAndCategoryAndSubCategory("Equity", "Mid Cap", "Value");
        verify(mFSchemeTypeService).saveCategory(any(MFSchemeType.class));
    }

    @Test
    void findOrCreateMFSchemeTypeEntity_WithNullSubCategory_ShouldHandleCorrectly() {
        when(mFSchemeTypeService.findByTypeAndCategoryAndSubCategory(eq("Debt"), eq("Long Term"), isNull()))
                .thenReturn(mockSchemeType);

        MFSchemeType result = mapperHelper.findOrCreateMFSchemeTypeEntity("Debt", "Long Term", null);

        assertEquals(mockSchemeType, result);
        verify(mFSchemeTypeService).findByTypeAndCategoryAndSubCategory("Debt", "Long Term", null);
    }

    @Test
    void findOrCreateMFSchemeTypeEntity_ShouldUseCache_OnSecondCall() {
        when(mFSchemeTypeService.findByTypeAndCategoryAndSubCategory("Equity", "Large Cap", "Growth"))
                .thenReturn(mockSchemeType);

        MFSchemeType result1 = mapperHelper.findOrCreateMFSchemeTypeEntity("Equity", "Large Cap", "Growth");
        MFSchemeType result2 = mapperHelper.findOrCreateMFSchemeTypeEntity("Equity", "Large Cap", "Growth");

        assertEquals(mockSchemeType, result1);
        assertEquals(mockSchemeType, result2);
        assertSame(result1, result2);
        verify(mFSchemeTypeService, times(1)).findByTypeAndCategoryAndSubCategory("Equity", "Large Cap", "Growth");
    }
}
