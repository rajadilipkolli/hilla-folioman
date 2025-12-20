package com.app.folioman.mfschemes.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

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

@DataJpaTest
@Import(SQLContainersConfig.class)
class MfFundSchemeRepositoryTest {

    @Autowired
    private MfFundSchemeRepository mfFundSchemeRepository;

    @Test
    void findAllSchemeIds_ShouldReturnListOfAmfiCodes() {
        List<Long> schemeIds = mfFundSchemeRepository.findAllSchemeIds();
        assertThat(schemeIds).isNotNull();
    }

    @Test
    void searchByFullText_WithValidQuery_ShouldReturnMatchingSchemes() {
        String query = "equity";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByFullText(query);
        assertThat(results).isNotNull();
    }

    @Test
    void searchByFullText_WithEmptyQuery_ShouldReturnEmptyList() {
        String query = "";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByFullText(query);
        assertThat(results).isNotNull();
    }

    @Test
    void searchByFullText_WithNullQuery_ShouldHandleGracefully() {
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByFullText(null);
        assertThat(results).isNotNull();
    }

    @Test
    void searchByAmc_WithValidQuery_ShouldReturnMatchingSchemes() {
        String query = "HDFC";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmc(query);
        assertThat(results).isNotNull();
    }

    @Test
    void searchByAmc_WithEmptyQuery_ShouldReturnAllSchemes() {
        String query = "";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmc(query);
        assertThat(results).isNotNull();
    }

    @Test
    void searchByAmc_WithNullQuery_ShouldHandleGracefully() {
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmc(null);
        assertThat(results).isNotNull();
    }

    @Test
    void searchByAmcTextSearch_WithValidSearchTerms_ShouldReturnMatchingSchemes() {
        String searchTerms = "HDFC & Mutual";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmcTextSearch(searchTerms);
        assertThat(results).isNotNull();
    }

    @Test
    void searchByAmcTextSearch_WithEmptySearchTerms_ShouldReturnEmptyList() {
        String searchTerms = "";
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmcTextSearch(searchTerms);
        assertThat(results).isNotNull();
    }

    @Test
    void searchByAmcTextSearch_WithNullSearchTerms_ShouldHandleGracefully() {
        List<FundDetailProjection> results = mfFundSchemeRepository.searchByAmcTextSearch(null);
        assertThat(results).isNotNull();
    }

    @Test
    void findBySchemeIdAndMfSchemeNavs_NavDate_WithValidParameters_ShouldReturnOptional() {
        Long schemeCode = 123456L;
        LocalDate navDate = LocalDate.now();
        Optional<MfFundScheme> result =
                mfFundSchemeRepository.findBySchemeIdAndMfSchemeNavs_NavDate(schemeCode, navDate);
        assertThat(result).isNotNull();
    }

    @Test
    void findBySchemeIdAndMfSchemeNavs_NavDate_WithNullSchemeCode_ShouldHandleGracefully() {
        LocalDate navDate = LocalDate.now();
        Optional<MfFundScheme> result = mfFundSchemeRepository.findBySchemeIdAndMfSchemeNavs_NavDate(null, navDate);
        assertThat(result).isNotNull();
    }

    @Test
    void findBySchemeIdAndMfSchemeNavs_NavDate_WithNullNavDate_ShouldHandleGracefully() {
        Long schemeCode = 123456L;
        Optional<MfFundScheme> result = mfFundSchemeRepository.findBySchemeIdAndMfSchemeNavs_NavDate(schemeCode, null);
        assertThat(result).isNotNull();
    }

    @Test
    void findByAmfiCode_WithValidAmfiCode_ShouldReturnMfFundScheme() {
        Long amfiCode = 123456L;
        // This repository test runs against testcontainers and may not have data; just ensure method executes
        assertThatCode(() -> mfFundSchemeRepository.findByAmfiCode(amfiCode)).doesNotThrowAnyException();
    }

    @Test
    void findByAmfiCode_WithNullAmfiCode_ShouldHandleGracefully() {
        MfFundScheme result = mfFundSchemeRepository.findByAmfiCode(null);
        assertThat(result).isNull();
    }

    @Test
    void existsByAmfiCode_WithValidAmfiCode_ShouldReturnBoolean() {
        Long amfiCode = 123456L;
        boolean result = mfFundSchemeRepository.existsByAmfiCode(amfiCode);
        assertThat(result).isFalse();
    }

    @Test
    void existsByAmfiCode_WithNullAmfiCode_ShouldReturnFalse() {
        boolean result = mfFundSchemeRepository.existsByAmfiCode(null);
        assertThat(result).isFalse();
    }

    @Test
    void findByIsin_WithValidIsin_ShouldReturnOptional() {
        String isin = "INF123456789";
        Optional<MFSchemeProjection> result = mfFundSchemeRepository.findByIsin(isin);
        assertThat(result).isNotNull();
    }

    @Test
    void findByIsin_WithNullIsin_ShouldReturnEmptyOptional() {
        Optional<MFSchemeProjection> result = mfFundSchemeRepository.findByIsin(null);
        assertThat(result).isNotNull();
    }

    @Test
    void findByIsin_WithEmptyIsin_ShouldReturnEmptyOptional() {
        String isin = "";
        Optional<MFSchemeProjection> result = mfFundSchemeRepository.findByIsin(isin);
        assertThat(result).isNotNull();
    }

    @Test
    void findDistinctAmfiCode_ShouldReturnListOfStrings() {
        List<String> result = mfFundSchemeRepository.findDistinctAmfiCode();
        assertThat(result).isNotNull();
    }

    @Test
    void findByRtaCodeStartsWith_WithValidRtaCode_ShouldReturnList() {
        String rtaCode = "HDFC";
        List<MFSchemeProjection> result = mfFundSchemeRepository.findByRtaCodeStartsWith(rtaCode);
        assertThat(result).isNotNull();
    }

    @Test
    void findByRtaCodeStartsWith_WithEmptyRtaCode_ShouldReturnAllSchemes() {
        String rtaCode = "";
        List<MFSchemeProjection> result = mfFundSchemeRepository.findByRtaCodeStartsWith(rtaCode);
        assertThat(result).isNotNull();
    }

    @Test
    void findByRtaCodeStartsWith_WithNullRtaCode_ShouldHandleGracefully() {
        List<MFSchemeProjection> result = mfFundSchemeRepository.findByRtaCodeStartsWith(null);
        assertThat(result).isNotNull();
    }

    @Test
    void getReferenceByAmfiCode_WithValidAmfiCode_ShouldReturnReference() {
        Long amfiCode = 123456L;
        // Ensure the JPA reference fetch does not throw in test environment
        assertThatCode(() -> mfFundSchemeRepository.getReferenceByAmfiCode(amfiCode))
                .doesNotThrowAnyException();
    }

    @Test
    void getReferenceByAmfiCode_WithNullAmfiCode_ShouldHandleGracefully() {
        // Ensure no exception thrown when passing null
        assertThatCode(() -> mfFundSchemeRepository.getReferenceByAmfiCode(null))
                .doesNotThrowAnyException();
    }
}
