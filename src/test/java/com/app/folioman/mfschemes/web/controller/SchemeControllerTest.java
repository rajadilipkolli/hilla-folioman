package com.app.folioman.mfschemes.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.FundDetailProjection;
import com.app.folioman.mfschemes.MfSchemeService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemeControllerTest {

    @Mock
    private MfSchemeService mfSchemeService;

    @Mock
    private FundDetailProjection fundDetailProjection;

    private SchemeController schemeController;

    @BeforeEach
    void setUp() {
        schemeController = new SchemeController(mfSchemeService);
    }

    @Test
    void constructor_ShouldInitializeWithMfSchemeService() {
        SchemeController controller = new SchemeController(mfSchemeService);

        assertThat(controller).isNotNull();
    }

    @Test
    void fetchSchemes_WithValidSchemeName_ShouldReturnSchemeList() {
        String schemeName = "HDFC";
        List<FundDetailProjection> expectedSchemes = List.of(fundDetailProjection);
        when(mfSchemeService.fetchSchemes(schemeName)).thenReturn(expectedSchemes);

        List<FundDetailProjection> result = schemeController.fetchSchemes(schemeName);

        assertThat(result).isEqualTo(expectedSchemes);
        verify(mfSchemeService).fetchSchemes(schemeName);
    }

    @Test
    void fetchSchemes_WithEmptyString_ShouldCallServiceAndReturnResult() {
        String schemeName = "";
        List<FundDetailProjection> expectedSchemes = new ArrayList<>();
        when(mfSchemeService.fetchSchemes(schemeName)).thenReturn(expectedSchemes);

        List<FundDetailProjection> result = schemeController.fetchSchemes(schemeName);

        assertThat(result).isEqualTo(expectedSchemes);
        verify(mfSchemeService).fetchSchemes(schemeName);
    }

    @Test
    void fetchSchemes_WithNullSchemeName_ShouldCallServiceAndReturnResult() {
        String schemeName = null;
        List<FundDetailProjection> expectedSchemes = new ArrayList<>();
        when(mfSchemeService.fetchSchemes(schemeName)).thenReturn(expectedSchemes);

        List<FundDetailProjection> result = schemeController.fetchSchemes(schemeName);

        assertThat(result).isEqualTo(expectedSchemes);
        verify(mfSchemeService).fetchSchemes(schemeName);
    }

    @Test
    void fetchSchemes_WhenServiceReturnsEmptyList_ShouldReturnEmptyList() {
        String schemeName = "NonExistentScheme";
        List<FundDetailProjection> emptyList = new ArrayList<>();
        when(mfSchemeService.fetchSchemes(schemeName)).thenReturn(emptyList);

        List<FundDetailProjection> result = schemeController.fetchSchemes(schemeName);

        assertThat(result).isEqualTo(emptyList);
        verify(mfSchemeService).fetchSchemes(schemeName);
    }

    @Test
    void fetchSchemes_WhenServiceThrowsException_ShouldPropagateException() {
        String schemeName = "TestScheme";
        RuntimeException expectedException = new RuntimeException("Service error");
        when(mfSchemeService.fetchSchemes(schemeName)).thenThrow(expectedException);

        RuntimeException thrownException = assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> schemeController.fetchSchemes(schemeName))
                .actual();

        assertThat(thrownException).isEqualTo(expectedException);
        verify(mfSchemeService).fetchSchemes(schemeName);
    }

    @Test
    void fetchSchemes_WithMultipleSchemes_ShouldReturnAllSchemes() {
        String schemeName = "Axis";
        List<FundDetailProjection> expectedSchemes = List.of(fundDetailProjection, fundDetailProjection);
        when(mfSchemeService.fetchSchemes(schemeName)).thenReturn(expectedSchemes);

        List<FundDetailProjection> result = schemeController.fetchSchemes(schemeName);

        assertThat(result).isEqualTo(expectedSchemes);
        assertThat(result).hasSize(2);
        verify(mfSchemeService).fetchSchemes(schemeName);
    }
}
