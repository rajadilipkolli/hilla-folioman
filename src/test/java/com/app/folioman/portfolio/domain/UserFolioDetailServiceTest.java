package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.portfolio.domain.models.projection.UserFolioDetailsPanProjection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    private List<UserFolioDetailsEntity> testFolios;
    private UserFolioDetailsEntity testFolio;

    @BeforeEach
    void setUp() {
        testFolio = new UserFolioDetailsEntity();
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
        List<UserFolioDetailsEntity> expectedResult = List.of(testFolio);
        when(userFolioDetailsRepository.findByUserCasDetails_FoliosIn(testFolios))
                .thenReturn(expectedResult);

        List<UserFolioDetailsEntity> result = userFolioDetailService.findByFoliosIn(testFolios);

        assertThat(result).isEqualTo(expectedResult);
        verify(userFolioDetailsRepository).findByUserCasDetails_FoliosIn(testFolios);
    }

    @Test
    void findByFoliosInWithEmptyList() {
        List<UserFolioDetailsEntity> emptyList = Collections.emptyList();
        List<UserFolioDetailsEntity> expectedResult = Collections.emptyList();
        when(userFolioDetailsRepository.findByUserCasDetails_FoliosIn(emptyList))
                .thenReturn(expectedResult);

        List<UserFolioDetailsEntity> result = userFolioDetailService.findByFoliosIn(emptyList);

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
        when(userFolioDetailsRepository.findFirstByUserCasDetailsEntity_IdAndPanKyc(userCasID, "OK"))
                .thenReturn(Optional.of(panProjection));
        when(userFolioDetailsRepository.updatePanByCasId(testPan, userCasID)).thenReturn(expectedRowsUpdated);

        userFolioDetailService.setPANIfNotSet(userCasID);

        verify(userFolioDetailsRepository).findFirstByUserCasDetailsEntity_IdAndPanKyc(userCasID, "OK");
        verify(panProjection).getPan();
        verify(userFolioDetailsRepository).updatePanByCasId(testPan, userCasID);
    }

    @Test
    void setPANIfNotSetWithEmptyProjection() {
        Long userCasID = 456L;

        when(userFolioDetailsRepository.findFirstByUserCasDetailsEntity_IdAndPanKyc(userCasID, "OK"))
                .thenReturn(Optional.empty());

        userFolioDetailService.setPANIfNotSet(userCasID);

        verify(userFolioDetailsRepository).findFirstByUserCasDetailsEntity_IdAndPanKyc(userCasID, "OK");
        verify(userFolioDetailsRepository, never()).updatePanByCasId(anyString(), eq(userCasID));
    }

    @Test
    void setPANIfNotSetWithZeroRowsUpdated() {
        Long userCasID = 789L;
        String testPan = "XYZ123456Z";
        int expectedRowsUpdated = 0;

        when(panProjection.getPan()).thenReturn(testPan);
        when(userFolioDetailsRepository.findFirstByUserCasDetailsEntity_IdAndPanKyc(userCasID, "OK"))
                .thenReturn(Optional.of(panProjection));
        when(userFolioDetailsRepository.updatePanByCasId(testPan, userCasID)).thenReturn(expectedRowsUpdated);

        userFolioDetailService.setPANIfNotSet(userCasID);

        verify(userFolioDetailsRepository).findFirstByUserCasDetailsEntity_IdAndPanKyc(userCasID, "OK");
        verify(panProjection).getPan();
        verify(userFolioDetailsRepository).updatePanByCasId(testPan, userCasID);
    }

    @Test
    void setPANIfNotSetWithNullUserCasID() {
        when(userFolioDetailsRepository.findFirstByUserCasDetailsEntity_IdAndPanKyc(null, "OK"))
                .thenThrow(new IllegalArgumentException("UserCasID cannot be null"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> userFolioDetailService.setPANIfNotSet(null));
    }
}
