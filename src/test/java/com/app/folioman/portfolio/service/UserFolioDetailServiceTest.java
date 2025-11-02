package com.app.folioman.portfolio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void testConstructor() {
        UserFolioDetailService service = new UserFolioDetailService(userFolioDetailsRepository);
        assertNotNull(service);
    }

    @Test
    void testFindByFoliosIn_WithValidList() {
        List<UserFolioDetails> expectedResult = List.of(testFolio);
        when(userFolioDetailsRepository.findByUserCasDetails_FoliosIn(testFolios))
                .thenReturn(expectedResult);

        List<UserFolioDetails> result = userFolioDetailService.findByFoliosIn(testFolios);

        assertEquals(expectedResult, result);
        verify(userFolioDetailsRepository).findByUserCasDetails_FoliosIn(testFolios);
    }

    @Test
    void testFindByFoliosIn_WithEmptyList() {
        List<UserFolioDetails> emptyList = Collections.emptyList();
        List<UserFolioDetails> expectedResult = Collections.emptyList();
        when(userFolioDetailsRepository.findByUserCasDetails_FoliosIn(emptyList))
                .thenReturn(expectedResult);

        List<UserFolioDetails> result = userFolioDetailService.findByFoliosIn(emptyList);

        assertEquals(expectedResult, result);
        verify(userFolioDetailsRepository).findByUserCasDetails_FoliosIn(emptyList);
    }

    @Test
    void testFindByFoliosIn_WithNullList() {
        when(userFolioDetailsRepository.findByUserCasDetails_FoliosIn(null))
                .thenThrow(new IllegalArgumentException("Folios list cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> userFolioDetailService.findByFoliosIn(null));
    }

    @Test
    void testSetPANIfNotSet_WithValidProjection() {
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
    void testSetPANIfNotSet_WithNullProjection() {
        Long userCasID = 456L;

        when(userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(userCasID, "OK"))
                .thenReturn(null);

        assertThrows(NullPointerException.class, () -> userFolioDetailService.setPANIfNotSet(userCasID));

        verify(userFolioDetailsRepository).findFirstByUserCasDetails_IdAndPanKyc(userCasID, "OK");
    }

    @Test
    void testSetPANIfNotSet_WithZeroRowsUpdated() {
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
    void testSetPANIfNotSet_WithNullUserCasID() {
        when(userFolioDetailsRepository.findFirstByUserCasDetails_IdAndPanKyc(null, "OK"))
                .thenThrow(new IllegalArgumentException("UserCasID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> userFolioDetailService.setPANIfNotSet(null));
    }
}
