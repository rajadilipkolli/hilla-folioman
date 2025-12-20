package com.app.folioman.mfschemes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.repository.MfAmcRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MfAmcCacheServiceTest {

    @Mock
    private MfAmcRepository mfAmcRepository;

    @InjectMocks
    private MfAmcCacheService mfAmcCacheService;

    @Nested
    @DisplayName("findByTextSearch tests")
    class FindByTextSearchTests {

        private MfAmc createMfAmc(String code, String name) {
            MfAmc amc = new MfAmc();
            amc.setCode(code);
            amc.setName(name);
            return amc;
        }

        @Test
        @DisplayName("Should format search terms correctly and return matching AMCs")
        void findByTextSearch_shouldFormatSearchTermsCorrectly() {
            // Arrange
            String searchTerms = "sbi small cap";
            String expectedTsQueryFormat = "'sbi' & 'small' & 'cap'";
            List<MfAmc> expectedResults = Arrays.asList(
                    createMfAmc("SBI", "SBI Funds Management Limited"), createMfAmc("SBI001", "SBI Small Cap Fund"));

            when(mfAmcRepository.findByTextSearch(eq(expectedTsQueryFormat))).thenReturn(expectedResults);

            // Act
            List<MfAmc> actualResults = mfAmcCacheService.findByTextSearch(searchTerms);

            // Assert
            assertThat(actualResults).containsExactlyElementsOf(expectedResults);
            verify(mfAmcRepository).findByTextSearch(expectedTsQueryFormat);
        }

        @Test
        @DisplayName("Should handle null search terms by returning empty list")
        void findByTextSearch_withNullSearchTerms_shouldReturnEmptyList() {
            // Act
            List<MfAmc> results = mfAmcCacheService.findByTextSearch(null);

            // Assert
            assertThat(results).isEmpty();
            verify(mfAmcRepository, never()).findByTextSearch(anyString());
        }

        @Test
        @DisplayName("Should handle empty search terms by returning empty list")
        void findByTextSearch_withEmptySearchTerms_shouldReturnEmptyList() {
            // Act
            List<MfAmc> results = mfAmcCacheService.findByTextSearch("   ");

            // Assert
            assertThat(results).isEmpty();
            verify(mfAmcRepository, never()).findByTextSearch(anyString());
        }

        @Test
        @DisplayName("Should clean and normalize search terms")
        void findByTextSearch_shouldCleanAndNormalizeSearchTerms() {
            // Arrange
            String searchTerms = "  SBI   SMALL-CAP  ";
            String expectedTsQueryFormat = "'sbi' & 'smallcap'";
            List<MfAmc> expectedResults = Collections.singletonList(createMfAmc("SBI001", "SBI Small Cap Fund"));

            when(mfAmcRepository.findByTextSearch(eq(expectedTsQueryFormat))).thenReturn(expectedResults);

            // Act
            List<MfAmc> actualResults = mfAmcCacheService.findByTextSearch(searchTerms);

            // Assert
            assertThat(actualResults).containsExactlyElementsOf(expectedResults);
            verify(mfAmcRepository).findByTextSearch(expectedTsQueryFormat);
        }

        @Test
        @DisplayName("Should handle special characters in search terms")
        void findByTextSearch_shouldHandleSpecialCharacters() {
            // Arrange
            String searchTerms = "SBI#Fund$ (HDFC)";
            String expectedTsQueryFormat = "'sbifund' & 'hdfc'";
            List<MfAmc> expectedResults = Collections.singletonList(createMfAmc("SBI001", "SBI Fund HDFC"));

            when(mfAmcRepository.findByTextSearch(eq(expectedTsQueryFormat))).thenReturn(expectedResults);

            // Act
            List<MfAmc> actualResults = mfAmcCacheService.findByTextSearch(searchTerms);

            // Assert
            assertThat(actualResults).containsExactlyElementsOf(expectedResults);
            verify(mfAmcRepository).findByTextSearch(expectedTsQueryFormat);
        }

        @Test
        @DisplayName("Should handle terms with only special characters returning empty list")
        void findByTextSearch_withOnlySpecialCharacters_shouldReturnEmptyList() {
            // Arrange
            String searchTerms = "###$$$";

            // Act
            List<MfAmc> results = mfAmcCacheService.findByTextSearch(searchTerms);

            // Assert
            assertThat(results).isEmpty();
            verify(mfAmcRepository, never()).findByTextSearch(anyString());
        }

        @Test
        @DisplayName("Should maintain proper single quotes around search terms")
        void findByTextSearch_shouldFormatWithSingleQuotes() {
            // Arrange
            String searchTerms = "hdfc amc fund";
            String expectedTsQueryFormat = "'hdfc' & 'amc' & 'fund'";
            List<MfAmc> expectedResults =
                    Collections.singletonList(createMfAmc("HDFC", "HDFC Asset Management Company"));

            when(mfAmcRepository.findByTextSearch(eq(expectedTsQueryFormat))).thenReturn(expectedResults);

            // Act
            List<MfAmc> actualResults = mfAmcCacheService.findByTextSearch(searchTerms);

            // Assert
            assertThat(actualResults).containsExactlyElementsOf(expectedResults);
            verify(mfAmcRepository).findByTextSearch(expectedTsQueryFormat);
        }

        @Test
        @DisplayName("Should handle mixed case input")
        void findByTextSearch_shouldHandleMixedCase() {
            // Arrange
            String searchTerms = "SbI FuNdS";
            String expectedTsQueryFormat = "'sbi' & 'funds'";
            List<MfAmc> expectedResults = Collections.singletonList(createMfAmc("SBI", "SBI Funds Management Limited"));

            when(mfAmcRepository.findByTextSearch(eq(expectedTsQueryFormat))).thenReturn(expectedResults);

            // Act
            List<MfAmc> actualResults = mfAmcCacheService.findByTextSearch(searchTerms);

            // Assert
            assertThat(actualResults).containsExactlyElementsOf(expectedResults);
            verify(mfAmcRepository).findByTextSearch(expectedTsQueryFormat);
        }
    }

    @Nested
    @DisplayName("findByName tests")
    class FindByNameTests {

        @Test
        @DisplayName("Should convert name to uppercase and call repository")
        void findByName_shouldConvertToUpperCase() {
            // Arrange
            String amcName = "sbi funds";
            String expectedUpperCase = "SBI FUNDS";
            MfAmc expectedAmc = new MfAmc();
            expectedAmc.setName("SBI Funds Management Limited");

            when(mfAmcRepository.findByNameIgnoreCase(eq(expectedUpperCase))).thenReturn(expectedAmc);

            // Act
            MfAmc result = mfAmcCacheService.findByName(amcName);

            // Assert
            assertThat(result).isEqualTo(expectedAmc);
            verify(mfAmcRepository).findByNameIgnoreCase(expectedUpperCase);
        }
    }

    @Nested
    @DisplayName("findByCode tests")
    class FindByCodeTests {

        @Test
        @DisplayName("Should call repository with correct code")
        void findByCode_shouldCallRepository() {
            // Arrange
            String code = "SBI001";
            MfAmc expectedAmc = new MfAmc();
            expectedAmc.setCode(code);

            when(mfAmcRepository.findByCode(eq(code))).thenReturn(expectedAmc);

            // Act
            MfAmc result = mfAmcCacheService.findByCode(code);

            // Assert
            assertThat(result).isEqualTo(expectedAmc);
            verify(mfAmcRepository).findByCode(code);
        }
    }

    @Nested
    @DisplayName("findAllAmcs tests")
    class FindAllAmcsTests {

        @Test
        @DisplayName("Should call repository findAll method")
        void findAllAmcs_shouldCallFindAll() {
            // Arrange
            List<MfAmc> expectedList = Arrays.asList(new MfAmc(), new MfAmc());
            when(mfAmcRepository.findAll()).thenReturn(expectedList);

            // Act
            List<MfAmc> result = mfAmcCacheService.findAllAmcs();

            // Assert
            assertThat(result).containsExactlyElementsOf(expectedList);
            verify(mfAmcRepository).findAll();
        }
    }
}
