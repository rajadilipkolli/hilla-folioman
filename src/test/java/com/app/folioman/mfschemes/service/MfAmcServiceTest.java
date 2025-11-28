package com.app.folioman.mfschemes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.app.folioman.mfschemes.entities.MfAmc;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MfAmcServiceTest {

    @Mock
    private MfAmcCacheService mfAmcCacheService;

    @InjectMocks
    @Spy
    private MfAmcService mfAmcService;

    private MfAmc createMfAmc(String code, String name) {
        MfAmc amc = new MfAmc();
        amc.setCode(code);
        amc.setName(name);
        return amc;
    }

    @Nested
    @DisplayName("findByTextSearch tests")
    class FindByTextSearchTests {

        @Test
        @DisplayName("Should delegate to cache service and return results")
        void findByTextSearch_shouldDelegateToCache() {
            // Arrange
            String searchTerms = "sbi small cap";
            List<MfAmc> expectedResults = Arrays.asList(
                    createMfAmc("SBI", "SBI Funds Management Limited"), createMfAmc("SBI001", "SBI Small Cap Fund"));

            when(mfAmcCacheService.findByTextSearch(eq(searchTerms))).thenReturn(expectedResults);

            // Act
            List<MfAmc> actualResults = mfAmcService.findByTextSearch(searchTerms);

            // Assert
            assertThat(actualResults).isEqualTo(expectedResults);
            verify(mfAmcCacheService).findByTextSearch(searchTerms);
        }

        @Test
        @DisplayName("Should try with first term when combined search returns empty results")
        void findByTextSearch_shouldTrySingleTermWhenNoResults() {
            // Arrange
            String searchTerms = "sbi small cap";
            String firstTerm = "sbi";
            List<MfAmc> expectedResults = Collections.singletonList(createMfAmc("SBI", "SBI Funds Management Limited"));

            when(mfAmcCacheService.findByTextSearch(eq(searchTerms))).thenReturn(Collections.emptyList());
            when(mfAmcCacheService.findByTextSearch(eq(firstTerm))).thenReturn(expectedResults);

            // Act
            List<MfAmc> actualResults = mfAmcService.findByTextSearch(searchTerms);

            // Assert
            assertThat(actualResults).isEqualTo(expectedResults);
            verify(mfAmcCacheService).findByTextSearch(searchTerms);
            verify(mfAmcCacheService).findByTextSearch(firstTerm);
        }

        @Test
        @DisplayName("Should not attempt single term search when input has no spaces")
        void findByTextSearch_shouldNotAttemptSingleTermSearchForSingleWord() {
            // Arrange
            String searchTerm = "hdfc";
            List<MfAmc> expectedResults = Collections.emptyList();

            when(mfAmcCacheService.findByTextSearch(eq(searchTerm))).thenReturn(expectedResults);

            // Act
            List<MfAmc> actualResults = mfAmcService.findByTextSearch(searchTerm);

            // Assert
            assertThat(actualResults).isEmpty();
            verify(mfAmcCacheService, times(1)).findByTextSearch(anyString());
        }
    }

    @Nested
    @DisplayName("findBySearchTerms tests")
    class FindBySearchTermsTests {

        @Test
        @DisplayName("Should return text search results when available")
        void findBySearchTerms_shouldReturnTextSearchResults() {
            // Arrange
            String searchTerms = "sbi funds";
            List<MfAmc> expectedResults = Collections.singletonList(createMfAmc("SBI", "SBI Funds Management Limited"));

            // Use doReturn for spied methods to avoid actual method calls
            doReturn(expectedResults).when(mfAmcService).findByTextSearch(searchTerms);

            // Act
            List<MfAmc> actualResults = mfAmcService.findBySearchTerms(searchTerms);

            // Assert
            assertThat(actualResults).isEqualTo(expectedResults);
            verify(mfAmcCacheService, never()).findAllAmcs();
        }

        @Test
        @DisplayName("Should fall back to fuzzy search when text search returns no results")
        void findBySearchTerms_shouldFallBackToFuzzySearch() {
            // Arrange
            String searchTerms = "sbi funds";
            List<MfAmc> amcList = Arrays.asList(
                    createMfAmc("SBI", "SBI Funds Management Limited"),
                    createMfAmc("HDFC", "HDFC Asset Management"),
                    createMfAmc("ICICI", "ICICI Prudential"));

            // Use doReturn for spied methods
            doReturn(Collections.emptyList()).when(mfAmcService).findByTextSearch(searchTerms);
            when(mfAmcCacheService.findAllAmcs()).thenReturn(amcList);

            // Act
            List<MfAmc> actualResults = mfAmcService.findBySearchTerms(searchTerms);

            // Assert
            assertThat(actualResults).isNotEmpty();
            // Should contain SBI since it has highest fuzzy match score
            assertThat(actualResults.stream().anyMatch(amc -> amc.getCode().equals("SBI")))
                    .isTrue();
            verify(mfAmcCacheService).findAllAmcs();
        }
    }

    @Nested
    @DisplayName("findByName tests")
    class FindByNameTests {

        @Test
        @DisplayName("Should return exact match when found")
        void findByName_shouldReturnExactMatch() {
            // Arrange
            String amcName = "SBI Funds Management";
            MfAmc expectedAmc = createMfAmc("SBI", "SBI Funds Management Limited");

            when(mfAmcCacheService.findByName(amcName)).thenReturn(expectedAmc);

            // Act
            MfAmc result = mfAmcService.findByName(amcName);

            // Assert
            assertThat(result).isEqualTo(expectedAmc);
            verify(mfAmcCacheService).findByName(amcName);
            verify(mfAmcCacheService, never()).findAllAmcs();
        }

        @Test
        @DisplayName("Should find closest match when exact match not found")
        void findByName_shouldFindClosestMatch() {
            // Arrange
            String amcName = "SBI Fund";
            MfAmc sbiAmc = createMfAmc("SBI", "SBI Funds Management Limited");
            MfAmc hdfcAmc = createMfAmc("HDFC", "HDFC Asset Management");
            List<MfAmc> allAmcs = Arrays.asList(sbiAmc, hdfcAmc);

            when(mfAmcCacheService.findByName(amcName)).thenReturn(null);
            when(mfAmcCacheService.findAllAmcs()).thenReturn(allAmcs);

            // Act
            MfAmc result = mfAmcService.findByName(amcName);

            // Assert
            // Should return SBI as it has higher fuzzy match score with "SBI Fund"
            assertThat(result).isEqualTo(sbiAmc);
            verify(mfAmcCacheService).findByName(amcName);
            verify(mfAmcCacheService).findAllAmcs();
        }
    }

    @Nested
    @DisplayName("findOrCreateByName tests")
    class FindOrCreateByNameTests {

        @Test
        @DisplayName("Should return existing AMC when found")
        void findOrCreateByName_shouldReturnExisting() {
            // Arrange
            String amcName = "SBI Funds Management";
            MfAmc expectedAmc = createMfAmc("SBI", "SBI Funds Management Limited");

            // Use doReturn for spied methods
            doReturn(expectedAmc).when(mfAmcService).findByName(amcName);

            // Act
            MfAmc result = mfAmcService.findOrCreateByName(amcName);

            // Assert
            assertThat(result).isEqualTo(expectedAmc);
            verify(mfAmcCacheService, never()).saveMfAmc(any());
        }

        @Test
        @DisplayName("Should create new AMC when not found")
        void findOrCreateByName_shouldCreateNew() {
            // Arrange
            String amcName = "New AMC";
            MfAmc newAmc = createMfAmc(amcName, amcName);
            MfAmc savedAmc = createMfAmc(amcName, amcName);
            savedAmc.setId(1); // Using Integer instead of Long

            // Setup mocks
            doReturn(null).when(mfAmcService).findByName(amcName);
            when(mfAmcCacheService.findByName(amcName)).thenReturn(null);
            when(mfAmcCacheService.saveMfAmc(any())).thenReturn(savedAmc);

            // Act
            MfAmc result = mfAmcService.findOrCreateByName(amcName);

            // Assert
            assertThat(result).isEqualTo(savedAmc);
            verify(mfAmcCacheService).saveMfAmc(any());
        }
    }
}
