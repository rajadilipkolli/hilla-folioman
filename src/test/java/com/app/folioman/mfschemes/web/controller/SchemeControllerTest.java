package com.app.folioman.mfschemes.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        assertNotNull(controller);
    }

    @Test
    void fetchSchemes_WithValidSchemeName_ShouldReturnSchemeList() {
        String schemeName = "HDFC";
        List<FundDetailProjection> expectedSchemes = List.of(fundDetailProjection);
        when(mfSchemeService.fetchSchemes(schemeName)).thenReturn(expectedSchemes);

        List<FundDetailProjection> result = schemeController.fetchSchemes(schemeName);

        assertEquals(expectedSchemes, result);
        verify(mfSchemeService).fetchSchemes(schemeName);
    }

    @Test
    void fetchSchemes_WithEmptyString_ShouldCallServiceAndReturnResult() {
        String schemeName = "";
        List<FundDetailProjection> expectedSchemes = new ArrayList<>();
        when(mfSchemeService.fetchSchemes(schemeName)).thenReturn(expectedSchemes);

        List<FundDetailProjection> result = schemeController.fetchSchemes(schemeName);

        assertEquals(expectedSchemes, result);
        verify(mfSchemeService).fetchSchemes(schemeName);
    }

    @Test
    void fetchSchemes_WithNullSchemeName_ShouldCallServiceAndReturnResult() {
        String schemeName = null;
        List<FundDetailProjection> expectedSchemes = new ArrayList<>();
        when(mfSchemeService.fetchSchemes(schemeName)).thenReturn(expectedSchemes);

        List<FundDetailProjection> result = schemeController.fetchSchemes(schemeName);

        assertEquals(expectedSchemes, result);
        verify(mfSchemeService).fetchSchemes(schemeName);
    }

    @Test
    void fetchSchemes_WhenServiceReturnsEmptyList_ShouldReturnEmptyList() {
        String schemeName = "NonExistentScheme";
        List<FundDetailProjection> emptyList = new ArrayList<>();
        when(mfSchemeService.fetchSchemes(schemeName)).thenReturn(emptyList);

        List<FundDetailProjection> result = schemeController.fetchSchemes(schemeName);

        assertEquals(emptyList, result);
        verify(mfSchemeService).fetchSchemes(schemeName);
    }

    @Test
    void fetchSchemes_WhenServiceThrowsException_ShouldPropagateException() {
        String schemeName = "TestScheme";
        RuntimeException expectedException = new RuntimeException("Service error");
        when(mfSchemeService.fetchSchemes(schemeName)).thenThrow(expectedException);

        RuntimeException thrownException =
                assertThrows(RuntimeException.class, () -> schemeController.fetchSchemes(schemeName));

        assertEquals(expectedException, thrownException);
        verify(mfSchemeService).fetchSchemes(schemeName);
    }

    @Test
    void fetchSchemes_WithMultipleSchemes_ShouldReturnAllSchemes() {
        String schemeName = "Axis";
        List<FundDetailProjection> expectedSchemes = List.of(fundDetailProjection, fundDetailProjection);
        when(mfSchemeService.fetchSchemes(schemeName)).thenReturn(expectedSchemes);

        List<FundDetailProjection> result = schemeController.fetchSchemes(schemeName);

        assertEquals(expectedSchemes, result);
        assertEquals(2, result.size());
        verify(mfSchemeService).fetchSchemes(schemeName);
    }
}
