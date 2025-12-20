package com.app.folioman.mfschemes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

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
    void convertEntityToDtoWithBasicMapping() {
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

        assertThat(result).isNotNull();
        assertThat(result.schemeName()).isEqualTo("Test Scheme");
        assertThat(result.schemeCode()).isEqualTo(123456L);
        assertThat(result.amc()).isEqualTo("Test AMC");
    }

    @Test
    void updateMFSchemeWithEmptyNavList() {
        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setMfSchemeNavs(new ArrayList<>());
        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Equity");
        schemeType.setCategory("Large Cap");
        schemeType.setSubCategory("Growth");
        mfScheme.setMfSchemeType(schemeType);

        MFSchemeDTO inputDto = new MFSchemeDTO("Test", 123L, "", "Test", null, null, null);
        MFSchemeDTO result = mapper.updateMFScheme(mfScheme, inputDto);

        assertThat(result).isNotNull();
        assertThat(result.schemeType()).isEqualTo("Equity(Large Cap - Growth)");
        assertThat(result.nav()).isNull();
        assertThat(result.date()).isNull();
    }

    @Test
    void updateMFSchemeWithNavListAndValidDate() {
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

        assertThat(result).isNotNull();
        assertThat(result.schemeType()).isEqualTo("Debt(Corporate Bond - Short Duration)");
        assertThat(result.nav()).isEqualTo("100.50");
        assertThat(result.date()).isEqualTo("2023-12-01");
    }

    @Test
    void updateMFSchemeWithNavListAndNullDate() {
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

        assertThat(result).isNotNull();
        assertThat(result.schemeType()).isEqualTo("Hybrid(Balanced - Conservative)");
        assertThat(result.nav()).isEqualTo("75.25");
        assertThat(result.date()).isNull();
    }

    @Test
    void updateMFSchemeWithEmptySubCategory() {
        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setMfSchemeNavs(new ArrayList<>());
        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Equity");
        schemeType.setCategory("Large Cap");
        schemeType.setSubCategory("");
        mfScheme.setMfSchemeType(schemeType);

        MFSchemeDTO inputDto = new MFSchemeDTO("Test", 123L, "", "Test", null, null, null);
        MFSchemeDTO result = mapper.updateMFScheme(mfScheme, inputDto);

        assertThat(result).isNotNull();
        assertThat(result.schemeType()).isEqualTo("Equity(Large Cap)");
    }

    @Test
    void updateMFSchemeWithNullSubCategory() {
        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setMfSchemeNavs(new ArrayList<>());
        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Equity");
        schemeType.setCategory("Large Cap");
        schemeType.setSubCategory(null);
        mfScheme.setMfSchemeType(schemeType);

        MFSchemeDTO inputDto = new MFSchemeDTO("Test", 123L, "", "Test", null, null, null);
        MFSchemeDTO result = mapper.updateMFScheme(mfScheme, inputDto);

        assertThat(result).isNotNull();
        assertThat(result.schemeType()).isEqualTo("Equity(Large Cap)");
    }

    @Test
    void updateMFSchemeWithWhitespaceOnlySubCategory() {
        MfFundScheme mfScheme = new MfFundScheme();
        mfScheme.setMfSchemeNavs(new ArrayList<>());
        MFSchemeType schemeType = new MFSchemeType();
        schemeType.setType("Equity");
        schemeType.setCategory("Mid Cap");
        schemeType.setSubCategory("   ");
        mfScheme.setMfSchemeType(schemeType);

        MFSchemeDTO inputDto = new MFSchemeDTO("Test", 123L, "", "Test", null, null, null);
        MFSchemeDTO result = mapper.updateMFScheme(mfScheme, inputDto);

        assertThat(result).isNotNull();
        assertThat(result.schemeType()).isEqualTo("Equity(Mid Cap)");
    }

    @Test
    void updateMFSchemeCompleteScenario() {
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

        assertThat(result).isNotNull();
        assertThat(result.schemeType()).isEqualTo("Equity(Small Cap - Value)");
        assertThat(result.nav()).isEqualTo("250.75");
        assertThat(result.date()).isEqualTo("2024-01-15");
    }
}
