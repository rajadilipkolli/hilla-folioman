package com.app.folioman.mfschemes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.app.folioman.mfschemes.FundDetailProjection;
import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.mapper.MfSchemeEntityToDtoMapper;
import com.app.folioman.mfschemes.mapper.SchemeNAVDataDtoToEntityMapper;
import com.app.folioman.mfschemes.repository.MFSchemeNavRepository;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
public class MfSchemeServiceImplTest {

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
    private MFSchemeNavRepository mfSchemeNavRepository;

    @InjectMocks
    private MfSchemeServiceImpl mfSchemeService;

    @Test
    void fetchSchemes_byFundName_shouldCallSearchByFullText() {
        // Arrange
        String query = "sbi small cap";
        List<FundDetailProjection> expectedResults =
                Collections.singletonList(Mockito.mock(FundDetailProjection.class));
        when(mfSchemeRepository.searchByFullText(eq("'sbi' & 'small' & 'cap'"))).thenReturn(expectedResults);

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).isEqualTo(expectedResults);
        verify(mfSchemeRepository).searchByFullText(eq("'sbi' & 'small' & 'cap'"));
        verify(mfSchemeRepository, never()).searchByAmc(anyString());
    }

    @Test
    void fetchSchemes_byAmcName_shouldCallSearchByAmc() {
        // Arrange
        String query = "amc sbi funds";
        List<FundDetailProjection> expectedResults =
                Collections.singletonList(Mockito.mock(FundDetailProjection.class));
        when(mfSchemeRepository.searchByAmc(query)).thenReturn(expectedResults);

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).isEqualTo(expectedResults);
        verify(mfSchemeRepository).searchByAmc(query);
        verify(mfSchemeRepository, never()).searchByFullText(anyString());
    }

    @Test
    void fetchSchemes_byAmcFullName_shouldCallSearchByAmc() {
        // Arrange
        String query = "SBI Funds Management Limited";
        List<FundDetailProjection> expectedResults =
                Collections.singletonList(Mockito.mock(FundDetailProjection.class));
        when(mfSchemeRepository.searchByAmc(query)).thenReturn(expectedResults);

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).isEqualTo(expectedResults);
        verify(mfSchemeRepository).searchByAmc(query);
        verify(mfSchemeRepository, never()).searchByFullText(anyString());
    }

    @Test
    void fetchSchemes_bySchemeCode_shouldReturnCorrectScheme() {
        // Arrange
        String query = "125494";
        MfFundScheme scheme =
                createTestScheme(125494L, "SBI Small Cap Fund - Direct Plan - Growth", "SBI Funds Management Limited");
        when(mfSchemeRepository.findByAmfiCode(125494L)).thenReturn(scheme);

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
        assertThat(actualResults).isEqualTo(expectedResults);
        verify(mfSchemeRepository, never()).findByAmfiCode(anyLong());
        verify(mfSchemeRepository).searchByFullText(query);
        verify(mfSchemeRepository, never()).searchByAmc(anyString());
    }

    @Test
    void fetchSchemes_bySchemeCodeWithMissingScheme_shouldReturnEmptyList() {
        // Arrange
        String query = "999999"; // Non-existent scheme code
        when(mfSchemeRepository.findByAmfiCode(999999L)).thenReturn(null);

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
        when(mfSchemeRepository.searchByAmc(query)).thenReturn(expectedResults);

        // Act
        List<FundDetailProjection> actualResults = mfSchemeService.fetchSchemes(query);

        // Assert
        assertThat(actualResults).isEqualTo(expectedResults);
        verify(mfSchemeRepository).searchByAmc(query);
        verify(mfSchemeRepository, never()).searchByFullText(anyString());
        verify(mfSchemeRepository, never()).findByAmfiCode(anyLong());
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
        assertThat(actualResults).isEqualTo(expectedResults);
        verify(mfSchemeRepository).searchByFullText(query);
        verify(mfSchemeRepository, never()).searchByAmc(anyString());
        verify(mfSchemeRepository, never()).findByAmfiCode(anyLong());
    }

    private MfFundScheme createTestScheme(Long amfiCode, String name, String amcName) {
        MfFundScheme scheme = new MfFundScheme();
        scheme.setAmfiCode(amfiCode);
        scheme.setName(name);

        MfAmc amc = new MfAmc();
        amc.setName(amcName);
        scheme.setAmc(amc);

        return scheme;
    }
}
