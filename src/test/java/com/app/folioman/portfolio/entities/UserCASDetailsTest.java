package com.app.folioman.portfolio.entities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCASDetailsTest {

    private UserCASDetails userCASDetails;

    @Mock
    private InvestorInfo mockInvestorInfo;

    @Mock
    private UserFolioDetails mockUserFolioDetails;

    @BeforeEach
    void setUp() {
        userCASDetails = new UserCASDetails();
    }

    @Test
    void testGetId() {
        assertNull(userCASDetails.getId());
    }

    @Test
    void testSetId() {
        Long id = 123L;
        UserCASDetails result = userCASDetails.setId(id);

        assertEquals(id, userCASDetails.getId());
        assertSame(userCASDetails, result);
    }

    @Test
    void testSetIdWithNull() {
        UserCASDetails result = userCASDetails.setId(null);

        assertNull(userCASDetails.getId());
        assertSame(userCASDetails, result);
    }

    @Test
    void testGetCasTypeEnum() {
        assertNull(userCASDetails.getCasTypeEnum());
    }

    @Test
    void testSetCasTypeEnum() {
        UserCASDetails result = userCASDetails.setCasTypeEnum(CasTypeEnum.DETAILED);
        assertEquals(CasTypeEnum.DETAILED, userCASDetails.getCasTypeEnum());
        assertSame(userCASDetails, result);
    }

    @Test
    void testSetCasTypeEnumWithNull() {
        UserCASDetails result = userCASDetails.setCasTypeEnum(null);

        assertNull(userCASDetails.getCasTypeEnum());
        assertSame(userCASDetails, result);
    }

    @Test
    void testGetFileTypeEnum() {
        assertNull(userCASDetails.getFileTypeEnum());
    }

    @Test
    void testSetFileTypeEnum() {
        UserCASDetails result = userCASDetails.setFileTypeEnum(FileTypeEnum.CAMS);

        assertEquals(FileTypeEnum.CAMS, userCASDetails.getFileTypeEnum());
        assertSame(userCASDetails, result);
    }

    @Test
    void testSetFileTypeEnumWithNull() {
        UserCASDetails result = userCASDetails.setFileTypeEnum(null);

        assertNull(userCASDetails.getFileTypeEnum());
        assertSame(userCASDetails, result);
    }

    @Test
    void testGetFolios() {
        List<UserFolioDetails> folios = userCASDetails.getFolios();

        assertNotNull(folios);
        assertTrue(folios.isEmpty());
    }

    @Test
    void testSetFolios() {
        List<UserFolioDetails> folios = new ArrayList<>();
        folios.add(mockUserFolioDetails);

        UserCASDetails result = userCASDetails.setFolios(folios);

        assertEquals(folios, userCASDetails.getFolios());
        assertSame(userCASDetails, result);
    }

    @Test
    void testSetFoliosWithNull() {
        UserCASDetails result = userCASDetails.setFolios(null);

        assertNull(userCASDetails.getFolios());
        assertSame(userCASDetails, result);
    }

    @Test
    void testGetInvestorInfo() {
        assertNull(userCASDetails.getInvestorInfo());
    }

    @Test
    void testSetInvestorInfoWithNonNull() {
        UserCASDetails result = userCASDetails.setInvestorInfo(mockInvestorInfo);

        assertEquals(mockInvestorInfo, userCASDetails.getInvestorInfo());
        verify(mockInvestorInfo).setUserCasDetails(userCASDetails);
        assertSame(userCASDetails, result);
    }

    @Test
    void testSetInvestorInfoWithNullWhenCurrentIsNull() {
        UserCASDetails result = userCASDetails.setInvestorInfo(null);

        assertNull(userCASDetails.getInvestorInfo());
        assertSame(userCASDetails, result);
    }

    @Test
    void testSetInvestorInfoWithNullWhenCurrentIsNotNull() {
        InvestorInfo currentInvestorInfo = mock(InvestorInfo.class);
        userCASDetails.setInvestorInfo(currentInvestorInfo);

        UserCASDetails result = userCASDetails.setInvestorInfo(null);

        assertNull(userCASDetails.getInvestorInfo());
        verify(currentInvestorInfo).setUserCasDetails(null);
        assertSame(userCASDetails, result);
    }

    @Test
    void testSetInvestorInfoReplaceExisting() {
        InvestorInfo currentInvestorInfo = mock(InvestorInfo.class);
        userCASDetails.setInvestorInfo(currentInvestorInfo);

        UserCASDetails result = userCASDetails.setInvestorInfo(mockInvestorInfo);

        assertEquals(mockInvestorInfo, userCASDetails.getInvestorInfo());
        verify(mockInvestorInfo).setUserCasDetails(userCASDetails);
        assertSame(userCASDetails, result);
    }

    @Test
    void testAddFolioEntity() {
        userCASDetails.addFolioEntity(mockUserFolioDetails);

        assertTrue(userCASDetails.getFolios().contains(mockUserFolioDetails));
        verify(mockUserFolioDetails).setUserCasDetails(userCASDetails);
    }

    @Test
    void testAddMultipleFolioEntities() {
        UserFolioDetails anotherMockFolio = mock(UserFolioDetails.class);

        userCASDetails.addFolioEntity(mockUserFolioDetails);
        userCASDetails.addFolioEntity(anotherMockFolio);

        assertEquals(2, userCASDetails.getFolios().size());
        assertTrue(userCASDetails.getFolios().contains(mockUserFolioDetails));
        assertTrue(userCASDetails.getFolios().contains(anotherMockFolio));
        verify(mockUserFolioDetails).setUserCasDetails(userCASDetails);
        verify(anotherMockFolio).setUserCasDetails(userCASDetails);
    }
}
