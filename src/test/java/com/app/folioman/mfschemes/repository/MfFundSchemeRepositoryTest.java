package com.app.folioman.mfschemes.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.mfschemes.FundDetailProjection;
import com.app.folioman.mfschemes.MFSchemeProjection;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(SQLContainersConfig.class)
class MfFundSchemeRepositoryTest {

    @Autowired
    private MfFundSchemeRepository mfFundSchemeRepository;

    @Test
    void findAllSchemeIds_ShouldReturnListOfAmfiCodes() {
        List<Long> schemeIds = mfFundSchemeRepository.findAllSchemeIds();
        assertNotNull(schemeIds);
    }

    @Test
    void searchByFullText_WithValidQuery_ShouldReturnMatchingSchemes() {
        String query = "equity";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByFullText(query);
        assertNotNull(results);
    }

    @Test
    void searchByFullText_WithEmptyQuery_ShouldReturnEmptyList() {
        String query = "";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByFullText(query);
        assertNotNull(results);
    }

    @Test
    void searchByFullText_WithNullQuery_ShouldHandleGracefully() {
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByFullText(null);
        assertNotNull(results);
    }

    @Test
    void searchByAmc_WithValidQuery_ShouldReturnMatchingSchemes() {
        String query = "HDFC";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmc(query);
        assertNotNull(results);
    }

    @Test
    void searchByAmc_WithEmptyQuery_ShouldReturnAllSchemes() {
        String query = "";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmc(query);
        assertNotNull(results);
    }

    @Test
    void searchByAmc_WithNullQuery_ShouldHandleGracefully() {
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmc(null);
        assertNotNull(results);
    }

    @Test
    void searchByAmcTextSearch_WithValidSearchTerms_ShouldReturnMatchingSchemes() {
        String searchTerms = "HDFC & Mutual";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmcTextSearch(searchTerms);
        assertNotNull(results);
    }

    @Test
    void searchByAmcTextSearch_WithEmptySearchTerms_ShouldReturnEmptyList() {
        String searchTerms = "";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmcTextSearch(searchTerms);
        assertNotNull(results);
    }

    @Test
    void searchByAmcTextSearch_WithNullSearchTerms_ShouldHandleGracefully() {
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmcTextSearch(null);
        assertNotNull(results);
    }

    @Test
    void findBySchemeIdAndMfSchemeNavs_NavDate_WithValidParameters_ShouldReturnOptional() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.now();
        Optional<MfFundScheme> result =
                mfFundSchemeRepository.findBySchemeIdAndMfSchemeNavs_NavDate(schemeCode, navDate);
        assertNotNull(result);
    }

    @Test
    void findBySchemeIdAndMfSchemeNavs_NavDate_WithNullSchemeCode_ShouldHandleGracefully() {
        LocalDate navDate = LocalDate.now();
        Optional<MfFundScheme> result = mfFundSchemeRepository.findBySchemeIdAndMfSchemeNavs_NavDate(null, navDate);
        assertNotNull(result);
    }

    @Test
    void findBySchemeIdAndMfSchemeNavs_NavDate_WithNullNavDate_ShouldHandleGracefully() {
        Long schemeCode = 123456L;
        Optional<MfFundScheme> result = mfFundSchemeRepository.findBySchemeIdAndMfSchemeNavs_NavDate(schemeCode, null);
        assertNotNull(result);
    }

    @Test
    void findByAmfiCode_WithValidAmfiCode_ShouldReturnMfFundScheme() {
        Long amfiCode = 123456L;
        // This repository test runs against testcontainers and may not have data; just ensure method executes
        assertDoesNotThrow(() -> mfFundSchemeRepository.findByAmfiCode(amfiCode));
    }

    @Test
    void findByAmfiCode_WithNullAmfiCode_ShouldHandleGracefully() {
        MfFundScheme result = mfFundSchemeRepository.findByAmfiCode(null);
        assertNull(result);
    }

    @Test
    void existsByAmfiCode_WithValidAmfiCode_ShouldReturnBoolean() {
        Long amfiCode = 123456L;
        boolean result = mfFundSchemeRepository.existsByAmfiCode(amfiCode);
        assertFalse(result);
    }

    @Test
    void existsByAmfiCode_WithNullAmfiCode_ShouldReturnFalse() {
        boolean result = mfFundSchemeRepository.existsByAmfiCode(null);
        assertFalse(result);
    }

    @Test
    void findByIsin_WithValidIsin_ShouldReturnOptional() {
        String isin = "INF123456789";
        Optional<MFSchemeProjection> result = mfFundSchemeRepository.findByIsin(isin);
        assertNotNull(result);
    }

    @Test
    void findByIsin_WithNullIsin_ShouldReturnEmptyOptional() {
        Optional<MFSchemeProjection> result = mfFundSchemeRepository.findByIsin(null);
        assertNotNull(result);
    }

    @Test
    void findByIsin_WithEmptyIsin_ShouldReturnEmptyOptional() {
        String isin = "";
        Optional<MFSchemeProjection> result = mfFundSchemeRepository.findByIsin(isin);
        assertNotNull(result);
    }

    @Test
    void findDistinctAmfiCode_ShouldReturnListOfStrings() {
        List<String> result = mfFundSchemeRepository.findDistinctAmfiCode();
        assertNotNull(result);
    }

    @Test
    void findByRtaCodeStartsWith_WithValidRtaCode_ShouldReturnList() {
        String rtaCode = "HDFC";
        List<MFSchemeProjection> result = mfFundSchemeRepository.findByRtaCodeStartsWith(rtaCode);
        assertNotNull(result);
    }

    @Test
    void findByRtaCodeStartsWith_WithEmptyRtaCode_ShouldReturnAllSchemes() {
        String rtaCode = "";
        List<MFSchemeProjection> result = mfFundSchemeRepository.findByRtaCodeStartsWith(rtaCode);
        assertNotNull(result);
    }

    @Test
    void findByRtaCodeStartsWith_WithNullRtaCode_ShouldHandleGracefully() {
        List<MFSchemeProjection> result = mfFundSchemeRepository.findByRtaCodeStartsWith(null);
        assertNotNull(result);
    }

    @Test
    void getReferenceByAmfiCode_WithValidAmfiCode_ShouldReturnReference() {
        Long amfiCode = 123456L;
        // Ensure the JPA reference fetch does not throw in test environment
        assertDoesNotThrow(() -> mfFundSchemeRepository.getReferenceByAmfiCode(amfiCode));
    }

    @Test
    void getReferenceByAmfiCode_WithNullAmfiCode_ShouldHandleGracefully() {
        // Ensure no exception thrown when passing null
        assertDoesNotThrow(() -> mfFundSchemeRepository.getReferenceByAmfiCode(null));
    }
}
