package com.app.folioman.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.models.projection.UserFolioDetailsPanProjection;
import com.app.folioman.portfolio.repository.UserFolioDetailsRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserFolioDetailServiceTest {

    @Mock
    private UserFolioDetailsRepository userFolioDetailsRepository;

    @Mock
    private UserFolioDetailsPanProjection panProjection;

    @InjectMocks
    private UserFolioDetailService userFolioDetailService;

    private List<UserFolioDetails> testFolios;
    private UserFolioDetails testFolio;

    @BeforeEach
    void setUp() {
        testFolio = new UserFolioDetails();
        testFolios = new ArrayList<>();
        testFolios.add(testFolio);
    }

    @Test
    void constructor() {
        UserFolioDetailService service = new UserFolioDetailService(userFolioDetailsRepository);
        assertThat(service).isNotNull();
    }

    @Test
    void findByFoliosInWithValidList() {
        List<UserFolioDetails> expectedResult = List.of(testFolio);
        when(userFolioDetailsRepository.findByUserCasDetails_FoliosIn(testFolios))
                .thenReturn(expectedResult);

        List<UserFolioDetails> result = userFolioDetailService.findByFoliosIn(testFolios);

        assertThat(result).isEqualTo(expectedResult);
        verify(userFolioDetailsRepository).findByUserCasDetails_FoliosIn(testFolios);
    }

    @Test
    void findByFoliosInWithEmptyList() {
        List<UserFolioDetails> emptyList = Collections.emptyList();
        List<UserFolioDetails> expectedResult = Collections.emptyList();
        when(userFolioDetailsRepository.findByUserCasDetails_FoliosIn(emptyList))
                .thenReturn(expectedResult);

        List<UserFolioDetails> result = userFolioDetailService.findByFoliosIn(emptyList);

        assertThat(result).isEqualTo(expectedResult);
        verify(userFolioDetailsRepository).findByUserCasDetails_FoliosIn(emptyList);
    }

    @Test
    void findByFoliosInWithNullList() {
        when(userFolioDetailsRepository.findByUserCasDetails_FoliosIn(null))
                .thenThrow(new IllegalArgumentException("Folios list cannot be null"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> userFolioDetailService.findByFoliosIn(null));
    }

    @Test
    void setPANIfNotSetWithValidProjection() {
        Long userCasID = 123L;
        String testPan = "ABCDE1234F";
        int expectedRowsUpdated = 1;

        when(panProjection.getPan()).thenReturn(testPan);
        when(userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(userCasID, "OK"))
                .thenReturn(panProjection);
        when(userFolioDetailsRepository.updatePanByCasId(testPan, userCasID)).thenReturn(expectedRowsUpdated);

        userFolioDetailService.setPANIfNotSet(userCasID);

        verify(userFolioDetailsRepository).findFirstByUserCasDetails_IdAndPanKyc(userCasID, "OK");
        verify(panProjection).getPan();
        verify(userFolioDetailsRepository).updatePanByCasId(testPan, userCasID);
    }

    @Test
    void setPANIfNotSetWithNullProjection() {
        Long userCasID = 456L;

        when(userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(userCasID, "OK"))
                .thenReturn(null);

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> userFolioDetailService.setPANIfNotSet(userCasID));

        verify(userFolioDetailsRepository).findFirstByUserCasDetails_IdAndPanKyc(userCasID, "OK");
    }

    @Test
    void setPANIfNotSetWithZeroRowsUpdated() {
        Long userCasID = 789L;
        String testPan = "XYZ123456Z";
        int expectedRowsUpdated = 0;

        when(panProjection.getPan()).thenReturn(testPan);
        when(userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(userCasID, "OK"))
                .thenReturn(panProjection);
        when(userFolioDetailsRepository.updatePanByCasId(testPan, userCasID)).thenReturn(expectedRowsUpdated);

        userFolioDetailService.setPANIfNotSet(userCasID);

        verify(userFolioDetailsRepository).findFirstByUserCasDetails_IdAndPanKyc(userCasID, "OK");
        verify(panProjection).getPan();
        verify(userFolioDetailsRepository).updatePanByCasId(testPan, userCasID);
    }

    @Test
    void setPANIfNotSetWithNullUserCasID() {
        when(userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(null, "OK"))
                .thenThrow(new IllegalArgumentException("UserCasID cannot be null"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> userFolioDetailService.setPANIfNotSet(null));
    }
}
