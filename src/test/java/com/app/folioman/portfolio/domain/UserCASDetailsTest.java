package com.app.folioman.portfolio.domain;

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

    private UserCasDetailsEntity userCasDetailsEntity;

    @Mock
    private InvestorInfoEntity mockInvestorInfo;

    @Mock
    private UserFolioDetailsEntity mockUserFolioDetails;

    @BeforeEach
    void setUp() {
        userCasDetailsEntity = new UserCasDetailsEntity();
    }

    @Test
    void getId() {
        assertThat(userCasDetailsEntity.getId()).isNull();
    }

    @Test
    void setId() {
        Long id = 123L;
        UserCasDetailsEntity result = userCasDetailsEntity.setId(id);

        assertThat(userCasDetailsEntity.getId()).isEqualTo(id);
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void setIdWithNull() {
        UserCasDetailsEntity result = userCasDetailsEntity.setId(null);

        assertThat(userCasDetailsEntity.getId()).isNull();
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void getCasTypeEnum() {
        assertThat(userCasDetailsEntity.getCasTypeEnum()).isNull();
    }

    @Test
    void setCasTypeEnum() {
        UserCasDetailsEntity result = userCasDetailsEntity.setCasTypeEnum(CasTypeEnum.DETAILED);
        assertThat(userCasDetailsEntity.getCasTypeEnum()).isEqualTo(CasTypeEnum.DETAILED);
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void setCasTypeEnumWithNull() {
        UserCasDetailsEntity result = userCasDetailsEntity.setCasTypeEnum(null);

        assertThat(userCasDetailsEntity.getCasTypeEnum()).isNull();
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void getFileTypeEnum() {
        assertThat(userCasDetailsEntity.getFileTypeEnum()).isNull();
    }

    @Test
    void setFileTypeEnum() {
        UserCasDetailsEntity result = userCasDetailsEntity.setFileTypeEnum(FileTypeEnum.CAMS);

        assertThat(userCasDetailsEntity.getFileTypeEnum()).isEqualTo(FileTypeEnum.CAMS);
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void setFileTypeEnumWithNull() {
        UserCasDetailsEntity result = userCasDetailsEntity.setFileTypeEnum(null);

        assertThat(userCasDetailsEntity.getFileTypeEnum()).isNull();
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void getFolios() {
        List<UserFolioDetailsEntity> folios = userCasDetailsEntity.getFolios();

        assertThat(folios).isNotNull();
        assertThat(folios).isEmpty();
    }

    @Test
    void setFolios() {
        List<UserFolioDetailsEntity> folios = new ArrayList<>();
        folios.add(mockUserFolioDetails);

        UserCasDetailsEntity result = userCasDetailsEntity.setFolios(folios);

        assertThat(userCasDetailsEntity.getFolios()).isEqualTo(folios);
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void setFoliosWithNull() {
        UserCasDetailsEntity result = userCasDetailsEntity.setFolios(null);

        assertThat(userCasDetailsEntity.getFolios()).isNull();
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void getInvestorInfo() {
        assertThat(userCasDetailsEntity.getInvestorInfoEntity()).isNull();
    }

    @Test
    void setInvestorInfoWithNonNull() {
        UserCasDetailsEntity result = userCasDetailsEntity.setInvestorInfoEntity(mockInvestorInfo);

        assertThat(userCasDetailsEntity.getInvestorInfoEntity()).isEqualTo(mockInvestorInfo);
        verify(mockInvestorInfo).setUserCasDetailsEntity(userCasDetailsEntity);
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void setInvestorInfoWithNullWhenCurrentIsNull() {
        UserCasDetailsEntity result = userCasDetailsEntity.setInvestorInfoEntity(null);

        assertThat(userCasDetailsEntity.getInvestorInfoEntity()).isNull();
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void setInvestorInfoWithNullWhenCurrentIsNotNull() {
        InvestorInfoEntity currentInvestorInfo = mock(InvestorInfoEntity.class);
        userCasDetailsEntity.setInvestorInfoEntity(currentInvestorInfo);

        UserCasDetailsEntity result = userCasDetailsEntity.setInvestorInfoEntity(null);

        assertThat(userCasDetailsEntity.getInvestorInfoEntity()).isNull();
        verify(currentInvestorInfo).setUserCasDetailsEntity(null);
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void setInvestorInfoReplaceExisting() {
        InvestorInfoEntity currentInvestorInfo = mock(InvestorInfoEntity.class);
        userCasDetailsEntity.setInvestorInfoEntity(currentInvestorInfo);

        UserCasDetailsEntity result = userCasDetailsEntity.setInvestorInfoEntity(mockInvestorInfo);

        assertThat(userCasDetailsEntity.getInvestorInfoEntity()).isEqualTo(mockInvestorInfo);
        verify(mockInvestorInfo).setUserCasDetailsEntity(userCasDetailsEntity);
        assertThat(result).isSameAs(userCasDetailsEntity);
    }

    @Test
    void addFolioEntity() {
        userCasDetailsEntity.addFolioEntity(mockUserFolioDetails);

        assertThat(userCasDetailsEntity.getFolios()).contains(mockUserFolioDetails);
        verify(mockUserFolioDetails).setUserCasDetailsEntity(userCasDetailsEntity);
    }

    @Test
    void addMultipleFolioEntities() {
        UserFolioDetailsEntity anotherMockFolio = mock(UserFolioDetailsEntity.class);

        userCasDetailsEntity.addFolioEntity(mockUserFolioDetails);
        userCasDetailsEntity.addFolioEntity(anotherMockFolio);

        assertThat(userCasDetailsEntity.getFolios()).hasSize(2);
        assertThat(userCasDetailsEntity.getFolios()).contains(mockUserFolioDetails);
        assertThat(userCasDetailsEntity.getFolios()).contains(anotherMockFolio);
        verify(mockUserFolioDetails).setUserCasDetailsEntity(userCasDetailsEntity);
        verify(anotherMockFolio).setUserCasDetailsEntity(userCasDetailsEntity);
    }
}
