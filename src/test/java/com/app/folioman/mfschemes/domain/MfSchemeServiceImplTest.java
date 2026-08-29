package com.app.folioman.mfschemes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.rest.dtos.FundDetailProjection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class MfSchemeServiceImplTest {

    @Mock
    private RestClient restClient;

    @Mock
    private MfFundSchemeRepository mfSchemeRepository;

    @Mock
    private MfSchemeEntityToDtoMapper mfSchemeEntityToDtoMapper;

    @Mock
    private SchemeNAVDataDtoToEntityMapper schemeNavDataDtoToEntityMapper;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private MfSchemeNavRepository MfSchemeNavRepository;

    @Mock
    private MfAmcService mfAmcService;

    @InjectMocks
    private MfSchemeServiceImpl mfSchemeService;

    @Test
    void fetchSchemes_byFundName_shouldCallSearchByFullText() {
        // Arrange
        String query = "sbi small cap";
        List<FundDetailProjection> expectedResults =
                Collections.singletonList(Mockito.mock(FundDetailProjection.class));
        when(mfSchemeRepository.searchByFullText(eq("sbi & small & cap"))).thenReturn(expectedResults);

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).containsExactlyElementsOf(expectedResults);
        verify(mfSchemeRepository).searchByFullText(eq("sbi & small & cap"));
        verify(mfSchemeRepository, never()).searchByAmc(anyString());
    }

    @Test
    void fetchSchemes_byAmcName_shouldCallSearchByAmc() {
        // Arrange
        String query = "amc sbi funds";
        List<FundDetailProjection> expectedResults =
                Collections.singletonList(Mockito.mock(FundDetailProjection.class));

        // Mock searchByFullText to return empty results so that searchByAmc gets called
        when(mfSchemeRepository.searchByFullText(anyString())).thenReturn(Collections.emptyList());
        when(mfSchemeRepository.searchByAmc(query)).thenReturn(expectedResults);

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).containsExactlyElementsOf(expectedResults);
        verify(mfSchemeRepository).searchByAmc(query);
    }

    @Test
    void fetchSchemes_byAmcFullName_shouldCallSearchByAmc() {
        // Arrange
        String query = "SBI Funds Management Limited";
        List<FundDetailProjection> expectedResults =
                Collections.singletonList(Mockito.mock(FundDetailProjection.class));

        // Mock searchByFullText to return empty results so searchByAmc gets called
        when(mfSchemeRepository.searchByFullText(anyString())).thenReturn(Collections.emptyList());
        when(mfSchemeRepository.searchByAmc(query)).thenReturn(expectedResults);

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).containsExactlyElementsOf(expectedResults);
        verify(mfSchemeRepository).searchByAmc(query);
    }

    @Test
    void fetchSchemes_bySchemeCode_shouldReturnCorrectScheme() {
        // Arrange
        String query = "125494";
        MfFundSchemeEntity scheme =
                createTestScheme(125494L, "SBI Small Cap Fund - Direct Plan - Growth", "SBI Funds Management Limited");
        when(mfSchemeRepository.findByAmfiCode(125494L)).thenReturn(Optional.of(scheme));

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).hasSize(1);
        assertThat(actualResults.getFirst().getAmfiCode()).isEqualTo(125494L);
        assertThat(actualResults.getFirst().getSchemeName()).isEqualTo("SBI Small Cap Fund - Direct Plan - Growth");
        assertThat(actualResults.getFirst().getAmcName()).isEqualTo("SBI Funds Management Limited");
        verify(mfSchemeRepository).findByAmfiCode(125494L);
        verify(mfSchemeRepository, never()).searchByFullText(anyString());
        verify(mfSchemeRepository, never()).searchByAmc(anyString());
    }

    @Test
    void fetchSchemes_byInvalidSchemeCode_shouldFallBackToFullTextSearch() {
        // Arrange
        String query = "125494abc"; // Not a valid AMFI code format
        List<FundDetailProjection> expectedResults =
                Collections.singletonList(Mockito.mock(FundDetailProjection.class));
        when(mfSchemeRepository.searchByFullText(query)).thenReturn(expectedResults);

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).containsExactlyElementsOf(expectedResults);
        verify(mfSchemeRepository, never()).findByAmfiCode(anyLong());
        verify(mfSchemeRepository).searchByFullText(query);
        verify(mfSchemeRepository, never()).searchByAmc(anyString());
    }

    @Test
    void fetchSchemes_bySchemeCodeWithMissingScheme_shouldReturnEmptyList() {
        // Arrange
        String query = "999999"; // Non-existent scheme code
        when(mfSchemeRepository.findByAmfiCode(999999L)).thenReturn(Optional.empty());

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).isEmpty();
        verify(mfSchemeRepository).findByAmfiCode(999999L);
        verify(mfSchemeRepository, never()).searchByFullText(anyString());
        verify(mfSchemeRepository, never()).searchByAmc(anyString());
    }

    @Test
    void fetchSchemes_byAmcQueryWithManagement_shouldCallSearchByAmc() {
        // Arrange
        String query = "asset management hdfc";
        List<FundDetailProjection> expectedResults =
                Collections.singletonList(Mockito.mock(FundDetailProjection.class));

        // Mock searchByFullText to return empty results so searchByAmc gets called
        when(mfSchemeRepository.searchByFullText(anyString())).thenReturn(Collections.emptyList());
        when(mfSchemeRepository.searchByAmc(query)).thenReturn(expectedResults);

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).containsExactlyElementsOf(expectedResults);
        verify(mfSchemeRepository).searchByAmc(query);
    }

    @Test
    void fetchSchemes_withSingleKeyword_shouldPassAsIs() {
        // Arrange
        String query = "hdfc";
        List<FundDetailProjection> expectedResults =
                Collections.singletonList(Mockito.mock(FundDetailProjection.class));
        when(mfSchemeRepository.searchByFullText(query)).thenReturn(expectedResults);

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).containsExactlyElementsOf(expectedResults);
        verify(mfSchemeRepository).searchByFullText(query);
        verify(mfSchemeRepository, never()).searchByAmc(anyString());
        verify(mfSchemeRepository, never()).findByAmfiCode(anyLong());
    }

    private MfFundSchemeEntity createTestScheme(Long amfiCode, String name, String amcName) {
        MfFundSchemeEntity scheme = new MfFundSchemeEntity();
        scheme.setAmfiCode(amfiCode);
        scheme.setName(name);

        MfAmcEntity amc = new MfAmcEntity();
        amc.setName(amcName);
        scheme.setAmc(amc);

        return scheme;
    }
}
