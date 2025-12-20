package com.app.folioman.portfolio.entities;

import static org.assertj.core.api.Assertions.assertThat;
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
    void getId() {
        assertThat(userCASDetails.getId()).isNull();
    }

    @Test
    void setId() {
        Long id = 123L;
        UserCASDetails result = userCASDetails.setId(id);

        assertThat(userCASDetails.getId()).isEqualTo(id);
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void setIdWithNull() {
        UserCASDetails result = userCASDetails.setId(null);

        assertThat(userCASDetails.getId()).isNull();
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void getCasTypeEnum() {
        assertThat(userCASDetails.getCasTypeEnum()).isNull();
    }

    @Test
    void setCasTypeEnum() {
        UserCASDetails result = userCASDetails.setCasTypeEnum(CasTypeEnum.DETAILED);
        assertThat(userCASDetails.getCasTypeEnum()).isEqualTo(CasTypeEnum.DETAILED);
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void setCasTypeEnumWithNull() {
        UserCASDetails result = userCASDetails.setCasTypeEnum(null);

        assertThat(userCASDetails.getCasTypeEnum()).isNull();
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void getFileTypeEnum() {
        assertThat(userCASDetails.getFileTypeEnum()).isNull();
    }

    @Test
    void setFileTypeEnum() {
        UserCASDetails result = userCASDetails.setFileTypeEnum(FileTypeEnum.CAMS);

        assertThat(userCASDetails.getFileTypeEnum()).isEqualTo(FileTypeEnum.CAMS);
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void setFileTypeEnumWithNull() {
        UserCASDetails result = userCASDetails.setFileTypeEnum(null);

        assertThat(userCASDetails.getFileTypeEnum()).isNull();
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void getFolios() {
        List<UserFolioDetails> folios = userCASDetails.getFolios();

        assertThat(folios).isNotNull();
        assertThat(folios).isEmpty();
    }

    @Test
    void setFolios() {
        List<UserFolioDetails> folios = new ArrayList<>();
        folios.add(mockUserFolioDetails);

        UserCASDetails result = userCASDetails.setFolios(folios);

        assertThat(userCASDetails.getFolios()).isEqualTo(folios);
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void setFoliosWithNull() {
        UserCASDetails result = userCASDetails.setFolios(null);

        assertThat(userCASDetails.getFolios()).isNull();
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void getInvestorInfo() {
        assertThat(userCASDetails.getInvestorInfo()).isNull();
    }

    @Test
    void setInvestorInfoWithNonNull() {
        UserCASDetails result = userCASDetails.setInvestorInfo(mockInvestorInfo);

        assertThat(userCASDetails.getInvestorInfo()).isEqualTo(mockInvestorInfo);
        verify(mockInvestorInfo).setUserCasDetails(userCASDetails);
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void setInvestorInfoWithNullWhenCurrentIsNull() {
        UserCASDetails result = userCASDetails.setInvestorInfo(null);

        assertThat(userCASDetails.getInvestorInfo()).isNull();
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void setInvestorInfoWithNullWhenCurrentIsNotNull() {
        InvestorInfo currentInvestorInfo = mock(InvestorInfo.class);
        userCASDetails.setInvestorInfo(currentInvestorInfo);

        UserCASDetails result = userCASDetails.setInvestorInfo(null);

        assertThat(userCASDetails.getInvestorInfo()).isNull();
        verify(currentInvestorInfo).setUserCasDetails(null);
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void setInvestorInfoReplaceExisting() {
        InvestorInfo currentInvestorInfo = mock(InvestorInfo.class);
        userCASDetails.setInvestorInfo(currentInvestorInfo);

        UserCASDetails result = userCASDetails.setInvestorInfo(mockInvestorInfo);

        assertThat(userCASDetails.getInvestorInfo()).isEqualTo(mockInvestorInfo);
        verify(mockInvestorInfo).setUserCasDetails(userCASDetails);
        assertThat(result).isSameAs(userCASDetails);
    }

    @Test
    void addFolioEntity() {
        userCASDetails.addFolioEntity(mockUserFolioDetails);

        assertThat(userCASDetails.getFolios()).contains(mockUserFolioDetails);
        verify(mockUserFolioDetails).setUserCasDetails(userCASDetails);
    }

    @Test
    void addMultipleFolioEntities() {
        UserFolioDetails anotherMockFolio = mock(UserFolioDetails.class);

        userCASDetails.addFolioEntity(mockUserFolioDetails);
        userCASDetails.addFolioEntity(anotherMockFolio);

        assertThat(userCASDetails.getFolios()).hasSize(2);
        assertThat(userCASDetails.getFolios()).contains(mockUserFolioDetails);
        assertThat(userCASDetails.getFolios()).contains(anotherMockFolio);
        verify(mockUserFolioDetails).setUserCasDetails(userCASDetails);
        verify(anotherMockFolio).setUserCasDetails(userCASDetails);
    }
}
