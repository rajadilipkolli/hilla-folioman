package com.app.folioman.mfschemes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.NavNotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachedNavServiceTest {

    @Mock
    private MfSchemeServiceImpl mfSchemeService;

    @InjectMocks
    private CachedNavService cachedNavService;

    private Long schemeCode;
    private LocalDate navDate;
    private MFSchemeDTO mfSchemeDTO;

    @BeforeEach
    void setUp() {
        schemeCode = 12345L;
        navDate = LocalDate.of(2023, 12, 1);
        mfSchemeDTO = new MFSchemeDTO(null, 0L, null, null, null, null, null);
    }

    @Test
    void getNavForDate_WhenSchemeExistsInDatabase_ReturnsScheme() {
        when(mfSchemeService.getMfSchemeDTO(schemeCode, navDate)).thenReturn(Optional.of(mfSchemeDTO));

        MFSchemeDTO result = cachedNavService.getNavForDate(schemeCode, navDate);

        assertThat(result).isEqualTo(mfSchemeDTO);
        verify(mfSchemeService, times(1)).getMfSchemeDTO(schemeCode, navDate);
    }

    @Test
    void getNavForDate_WhenSchemeNotInDatabase_CallsFetchAndGetSchemeDetails() {
        when(mfSchemeService.getMfSchemeDTO(schemeCode, navDate))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(mfSchemeDTO));

        MFSchemeDTO result = cachedNavService.getNavForDate(schemeCode, navDate);

        assertThat(result).isEqualTo(mfSchemeDTO);
        verify(mfSchemeService, times(2)).getMfSchemeDTO(schemeCode, navDate);
        verify(mfSchemeService, times(1)).fetchSchemeDetails(schemeCode);
    }

    @Test
    void fetchAndGetSchemeDetails_WhenSchemeFoundAfterFetch_ReturnsScheme() {
        when(mfSchemeService.getMfSchemeDTO(schemeCode, navDate)).thenReturn(Optional.of(mfSchemeDTO));

        MFSchemeDTO result = cachedNavService.fetchAndGetSchemeDetails(schemeCode, navDate);

        assertThat(result).isEqualTo(mfSchemeDTO);
        verify(mfSchemeService, times(1)).fetchSchemeDetails(schemeCode);
        verify(mfSchemeService, times(1)).getMfSchemeDTO(schemeCode, navDate);
    }

    @Test
    void fetchAndGetSchemeDetails_WhenSchemeNotFoundAfterFetch_ThrowsNavNotFoundException() {
        when(mfSchemeService.getMfSchemeDTO(schemeCode, navDate)).thenReturn(Optional.empty());

        NavNotFoundException exception = assertThatExceptionOfType(NavNotFoundException.class)
                .isThrownBy(() -> cachedNavService.fetchAndGetSchemeDetails(schemeCode, navDate))
                .actual();

        String expectedPrefix = "Nav Not Found for schemeCode - " + schemeCode;
        assertThat(exception.getMessage()).startsWith(expectedPrefix);
        assertThat(exception.getNavDate()).isEqualTo(navDate);
        verify(mfSchemeService, times(1)).fetchSchemeDetails(schemeCode);
        verify(mfSchemeService, times(1)).getMfSchemeDTO(schemeCode, navDate);
    }
}
