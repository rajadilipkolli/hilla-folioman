package com.app.folioman.mfschemes.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.entities.MFSchemeType;
import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MfSchemeEntityToDtoMapperTest {

    private final MfSchemeEntityToDtoMapper mapper = Mappers.getMapper(MfSchemeEntityToDtoMapper.class);

    @Test
    void testConvertEntityToDto_WithBasicMapping() {
        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setName("Test Scheme");
        mfScheme.setAmfiCode(123456L);
        MfAmc amc = new MfAmc();
        amc.setName("Test AMC");
        mfScheme.setAmc(amc);
        mfScheme.setMfSchemeNavs(new ArrayList<>());
        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Equity");
        schemeType.setCategory("Large Cap");
        schemeType.setSubCategory("Growth");
        mfScheme.setMfSchemeType(schemeType);
        MFSchemeDTO result = mapper.convertEntityToDto(mfScheme);

        assertNotNull(result);
        assertEquals("Test Scheme", result.schemeName());
        assertEquals(123456L, result.schemeCode());
        assertEquals("Test AMC", result.amc());
    }

    @Test
    void testUpdateMFScheme_WithEmptyNavList() {
        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setMfSchemeNavs(new ArrayList<>());
        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Equity");
        schemeType.setCategory("Large Cap");
        schemeType.setSubCategory("Growth");
        mfScheme.setMfSchemeType(schemeType);

        MFSchemeDTO inputDto = new MFSchemeDTO("Test", 123L, "", "Test", null, null, null);
        MFSchemeDTO result = mapper.updateMFScheme(mfScheme, inputDto);

        assertNotNull(result);
        assertEquals("Equity(Large Cap - Growth)", result.schemeType());
        assertNull(result.nav());
        assertNull(result.date());
    }

    @Test
    void testUpdateMFScheme_WithNavListAndValidDate() {
        List<MFSchemeNav> navList = new ArrayList<>();
        MFSchemeNav nav = new MFSchemeNav();
        nav.setNavDate(LocalDate.of(2023, 12, 1));
        nav.setNav(new BigDecimal("100.50"));
        navList.add(nav);

        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setMfSchemeNavs(navList);
        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Debt");
        schemeType.setCategory("Corporate Bond");
        schemeType.setSubCategory("Short Duration");
        mfScheme.setMfSchemeType(schemeType);

        MFSchemeDTO inputDto = new MFSchemeDTO("Test", 123L, "", "Test", null, null, null);
        MFSchemeDTO result = mapper.updateMFScheme(mfScheme, inputDto);

        assertNotNull(result);
        assertEquals("Debt(Corporate Bond - Short Duration)", result.schemeType());
        assertEquals("100.50", result.nav());
        assertEquals("2023-12-01", result.date());
    }

    @Test
    void testUpdateMFScheme_WithNavListAndNullDate() {
        List<MFSchemeNav> navList = new ArrayList<>();
        MFSchemeNav nav = new MFSchemeNav();
        nav.setNavDate(null);
        nav.setNav(new BigDecimal("75.25"));
        navList.add(nav);

        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setMfSchemeNavs(navList);
        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Hybrid");
        schemeType.setCategory("Balanced");
        schemeType.setSubCategory("Conservative");
        mfScheme.setMfSchemeType(schemeType);

        MFSchemeDTO inputDto = new MFSchemeDTO("Test", 123L, "", "Test", null, null, null);
        MFSchemeDTO result = mapper.updateMFScheme(mfScheme, inputDto);

        assertNotNull(result);
        assertEquals("Hybrid(Balanced - Conservative)", result.schemeType());
        assertEquals("75.25", result.nav());
        assertNull(result.date());
    }

    @Test
    void testUpdateMFScheme_WithEmptySubCategory() {
        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setMfSchemeNavs(new ArrayList<>());
        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Equity");
        schemeType.setCategory("Large Cap");
        schemeType.setSubCategory("");
        mfScheme.setMfSchemeType(schemeType);

        MFSchemeDTO inputDto = new MFSchemeDTO("Test", 123L, "", "Test", null, null, null);
        MFSchemeDTO result = mapper.updateMFScheme(mfScheme, inputDto);

        assertNotNull(result);
        assertEquals("Equity(Large Cap)", result.schemeType());
    }

    @Test
    void testUpdateMFScheme_WithNullSubCategory() {
        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setMfSchemeNavs(new ArrayList<>());
        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Equity");
        schemeType.setCategory("Large Cap");
        schemeType.setSubCategory(null);
        mfScheme.setMfSchemeType(schemeType);

        MFSchemeDTO inputDto = new MFSchemeDTO("Test", 123L, "", "Test", null, null, null);
        MFSchemeDTO result = mapper.updateMFScheme(mfScheme, inputDto);

        assertNotNull(result);
        assertEquals("Equity(Large Cap)", result.schemeType());
    }

    @Test
    void testUpdateMFScheme_WithWhitespaceOnlySubCategory() {
        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setMfSchemeNavs(new ArrayList<>());
        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Equity");
        schemeType.setCategory("Mid Cap");
        schemeType.setSubCategory("   ");
        mfScheme.setMfSchemeType(schemeType);

        MFSchemeDTO inputDto = new MFSchemeDTO("Test", 123L, "", "Test", null, null, null);
        MFSchemeDTO result = mapper.updateMFScheme(mfScheme, inputDto);

        assertNotNull(result);
        assertEquals("Equity(Mid Cap)", result.schemeType());
    }

    @Test
    void testUpdateMFScheme_CompleteScenario() {
        List<MFSchemeNav> navList = new ArrayList<>();

        MFSchemeNav nav = new MFSchemeNav();
        nav.setNavDate(LocalDate.of(2024, 1, 15));
        nav.setNav(new BigDecimal("250.75"));
        navList.add(nav);

        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setMfSchemeNavs(navList);

        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Equity");
        schemeType.setCategory("Small Cap");
        schemeType.setSubCategory("Value");
        mfScheme.setMfSchemeType(schemeType);

        MFSchemeDTO inputDto = new MFSchemeDTO("Complete Test", 789L, "", "Complete Test", null, null, null);
        MFSchemeDTO result = mapper.updateMFScheme(mfScheme, inputDto);

        assertNotNull(result);
        assertEquals("Equity(Small Cap - Value)", result.schemeType());
        assertEquals("250.75", result.nav());
        assertEquals("2024-01-15", result.date());
    }
}
