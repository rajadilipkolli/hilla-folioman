package com.app.folioman.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.FundDetailProjection;
import com.app.folioman.mfschemes.MFSchemeProjection;
import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.repository.UserSchemeDetailsRepository;
import java.util.Arrays;
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
class UserSchemeDetailServiceImplTest {

    @Mock
    private UserSchemeDetailsRepository userSchemeDetailsRepository;

    @Mock
    private MfSchemeService mfSchemeService;

    @InjectMocks
    private UserSchemeDetailServiceImpl userSchemeDetailService;

    private UserSchemeDetails userSchemeDetails;
    private MFSchemeProjection mfSchemeProjection;
    private FundDetailProjection fundDetailProjection;

    @BeforeEach
    void setUp() {
        userSchemeDetails = new UserSchemeDetails();
        userSchemeDetails.setId(1L);
        userSchemeDetails.setRtaCode("ABC123");
        userSchemeDetails.setScheme("Test Scheme");
        userSchemeDetails.setIsin("INE123456789");

        mfSchemeProjection = new MFSchemeProjection() {
            @Override
            public Long getAmfiCode() {
                return 123L;
            }

            @Override
            public String getIsin() {
                return "INE123456789";
            }
        };

        fundDetailProjection = new FundDetailProjection() {
            @Override
            public Long getAmfiCode() {
                return 456L;
            }

            @Override
            public String getAmcName() {
                return "TestAmcName";
            }

            @Override
            public String getSchemeName() {
                return "Test Scheme IDCW";
            }
        };
    }

    @Test
    void setUserSchemeAMFIIfNull_WithEmptyList_ShouldNotProcessAnySchemes() {
        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Collections.emptyList());

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService, never()).fetchSchemesByRtaCode(anyString());
        verify(userSchemeDetailsRepository, never()).updateAmfiAndIsinById(anyLong(), anyString(), anyLong());
    }

    @Test
    void setUserSchemeAMFIIfNull_WithShortRtaCode_ShouldLogWarning() {
        UserSchemeDetails shortRtaCodeScheme = new UserSchemeDetails();
        shortRtaCodeScheme.setId(2L);
        shortRtaCodeScheme.setRtaCode("A");

        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Arrays.asList(shortRtaCodeScheme));

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService, never()).fetchSchemesByRtaCode(anyString());
        verify(userSchemeDetailsRepository, never()).updateAmfiAndIsinById(anyLong(), anyString(), anyLong());
    }

    @Test
    void setUserSchemeAMFIIfNull_WithNullRtaCode_ShouldLogWarning() {
        UserSchemeDetails nullRtaCodeScheme = new UserSchemeDetails();
        nullRtaCodeScheme.setId(3L);
        nullRtaCodeScheme.setRtaCode(null);

        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Arrays.asList(nullRtaCodeScheme));

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService, never()).fetchSchemesByRtaCode(anyString());
        verify(userSchemeDetailsRepository, never()).updateAmfiAndIsinById(anyLong(), anyString(), anyLong());
    }

    @Test
    void setUserSchemeAMFIIfNull_WithValidRtaCodeAndMatchingSchemes_ShouldUpdateSchemeDetails() {
        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Arrays.asList(userSchemeDetails));
        when(mfSchemeService.fetchSchemesByRtaCode("ABC12")).thenReturn(Arrays.asList(mfSchemeProjection));

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService).fetchSchemesByRtaCode("ABC12");
        verify(userSchemeDetailsRepository).updateAmfiAndIsinById(123L, "INE123456789", 1L);
    }

    @Test
    void setUserSchemeAMFIIfNull_WithValidRtaCodeAndNoMatchingIsin_ShouldUseFirstScheme() {
        UserSchemeDetails differentIsinScheme = new UserSchemeDetails();
        differentIsinScheme.setId(4L);
        differentIsinScheme.setRtaCode("XYZ456");
        differentIsinScheme.setIsin("INE987654321");

        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Arrays.asList(differentIsinScheme));
        when(mfSchemeService.fetchSchemesByRtaCode("XYZ45")).thenReturn(Arrays.asList(mfSchemeProjection));

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService).fetchSchemesByRtaCode("XYZ45");
        verify(userSchemeDetailsRepository).updateAmfiAndIsinById(123L, "INE123456789", 4L);
    }

    @Test
    void setUserSchemeAMFIIfNull_WithEmptyMfSchemeListAndSchemeWithIsin_ShouldExtractIsin() {
        UserSchemeDetails schemeWithIsin = new UserSchemeDetails();
        schemeWithIsin.setId(5L);
        schemeWithIsin.setRtaCode("DEF789");
        schemeWithIsin.setScheme("Test Scheme ISIN:INE111222333");

        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Arrays.asList(schemeWithIsin));
        when(mfSchemeService.fetchSchemesByRtaCode("DEF78")).thenReturn(Collections.emptyList());
        when(mfSchemeService.findByPayOut("INE111222333")).thenReturn(Optional.of(mfSchemeProjection));

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService).fetchSchemesByRtaCode("DEF78");
        verify(mfSchemeService).findByPayOut("INE111222333");
        verify(userSchemeDetailsRepository).updateAmfiAndIsinById(123L, "INE111222333", 5L);
    }

    @Test
    void setUserSchemeAMFIIfNull_WithEmptyMfSchemeListAndSchemeWithoutIsin_ShouldFetchSchemes() {
        UserSchemeDetails schemeWithoutIsin = new UserSchemeDetails();
        schemeWithoutIsin.setId(6L);
        schemeWithoutIsin.setRtaCode("GHI012");
        schemeWithoutIsin.setScheme("Test Income Scheme");

        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Arrays.asList(schemeWithoutIsin));
        when(mfSchemeService.fetchSchemesByRtaCode("GHI01")).thenReturn(Collections.emptyList());
        when(mfSchemeService.fetchSchemes("Test Income Scheme")).thenReturn(Arrays.asList(fundDetailProjection));

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService).fetchSchemesByRtaCode("GHI01");
        verify(mfSchemeService).fetchSchemes("Test Income Scheme");
        verify(userSchemeDetailsRepository).updateAmfiAndIsinById(456L, null, 6L);
    }

    @Test
    void setUserSchemeAMFIIfNull_WithEmptyMfSchemeListAndNullScheme_ShouldLogWarning() {
        UserSchemeDetails nullScheme = new UserSchemeDetails();
        nullScheme.setId(7L);
        nullScheme.setRtaCode("JKL345");
        nullScheme.setScheme(null);

        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Arrays.asList(nullScheme));
        when(mfSchemeService.fetchSchemesByRtaCode("JKL34")).thenReturn(Collections.emptyList());

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService).fetchSchemesByRtaCode("JKL34");
        verify(mfSchemeService, never()).findByPayOut(anyString());
        verify(mfSchemeService, never()).fetchSchemes(anyString());
        verify(userSchemeDetailsRepository, never()).updateAmfiAndIsinById(anyLong(), anyString(), anyLong());
    }

    @Test
    void setUserSchemeAMFIIfNull_WithEmptyMfSchemeListAndEmptyFundDetails_ShouldNotUpdate() {
        UserSchemeDetails schemeWithoutIsin = new UserSchemeDetails();
        schemeWithoutIsin.setId(8L);
        schemeWithoutIsin.setRtaCode("MNO678");
        schemeWithoutIsin.setScheme("Test Scheme Without ISIN");

        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Arrays.asList(schemeWithoutIsin));
        when(mfSchemeService.fetchSchemesByRtaCode("MNO67")).thenReturn(Collections.emptyList());
        when(mfSchemeService.fetchSchemes("Test Scheme Without ISIN")).thenReturn(Collections.emptyList());

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService).fetchSchemesByRtaCode("MNO67");
        verify(mfSchemeService).fetchSchemes("Test Scheme Without ISIN");
        verify(userSchemeDetailsRepository, never()).updateAmfiAndIsinById(anyLong(), anyString(), anyLong());
    }

    @Test
    void setUserSchemeAMFIIfNull_WithEmptyMfSchemeListAndNonMatchingFundDetails_ShouldNotUpdate() {
        UserSchemeDetails nonMatchingScheme = new UserSchemeDetails();
        nonMatchingScheme.setId(9L);
        nonMatchingScheme.setRtaCode("PQR901");
        nonMatchingScheme.setScheme("Test Growth Scheme");

        FundDetailProjection nonMatchingFund = new FundDetailProjection() {
            @Override
            public Long getAmfiCode() {
                return 789L;
            }

            @Override
            public String getAmcName() {
                return "TestAmcName";
            }

            @Override
            public String getSchemeName() {
                return "Test IDCW Scheme";
            }
        };

        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Arrays.asList(nonMatchingScheme));
        when(mfSchemeService.fetchSchemesByRtaCode("PQR90")).thenReturn(Collections.emptyList());
        when(mfSchemeService.fetchSchemes("Test Growth Scheme")).thenReturn(Arrays.asList(nonMatchingFund));

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService).fetchSchemesByRtaCode("PQR90");
        verify(mfSchemeService).fetchSchemes("Test Growth Scheme");
        verify(userSchemeDetailsRepository, never()).updateAmfiAndIsinById(anyLong(), anyString(), anyLong());
    }

    @Test
    void setUserSchemeAMFIIfNull_WithEmptyMfSchemeListAndIsinButNotFound_ShouldNotUpdate() {
        UserSchemeDetails schemeWithIsinNotFound = new UserSchemeDetails();
        schemeWithIsinNotFound.setId(10L);
        schemeWithIsinNotFound.setRtaCode("STU234");
        schemeWithIsinNotFound.setScheme("Test Scheme ISIN:INE999888777");

        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Arrays.asList(schemeWithIsinNotFound));
        when(mfSchemeService.fetchSchemesByRtaCode("STU23")).thenReturn(Collections.emptyList());
        when(mfSchemeService.findByPayOut("INE999888777")).thenReturn(Optional.empty());

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService).fetchSchemesByRtaCode("STU23");
        verify(mfSchemeService).findByPayOut("INE999888777");
        verify(userSchemeDetailsRepository, never()).updateAmfiAndIsinById(anyLong(), anyString(), anyLong());
    }

    @Test
    void setUserSchemeAMFIIfNull_WithEmptyMfSchemeListAndEmptyIsinAfterExtraction_ShouldLogWarning() {
        UserSchemeDetails schemeWithEmptyIsin = new UserSchemeDetails();
        schemeWithEmptyIsin.setId(11L);
        schemeWithEmptyIsin.setRtaCode("VWX567");
        schemeWithEmptyIsin.setScheme("Test Scheme ISIN:");

        when(userSchemeDetailsRepository.findByAmfiIsNull()).thenReturn(Arrays.asList(schemeWithEmptyIsin));
        when(mfSchemeService.fetchSchemesByRtaCode("VWX56")).thenReturn(Collections.emptyList());

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailsRepository).findByAmfiIsNull();
        verify(mfSchemeService).fetchSchemesByRtaCode("VWX56");
        verify(mfSchemeService, never()).findByPayOut(anyString());
        verify(userSchemeDetailsRepository, never()).updateAmfiAndIsinById(anyLong(), anyString(), anyLong());
    }

    @Test
    void findBySchemesIn_WithValidList_ShouldReturnResults() {
        List<UserSchemeDetails> inputList = Arrays.asList(userSchemeDetails);
        List<UserSchemeDetails> expectedResult = Arrays.asList(userSchemeDetails);

        when(userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(inputList))
                .thenReturn(expectedResult);

        List<UserSchemeDetails> result = userSchemeDetailService.findBySchemesIn(inputList);

        assertThat(result).isEqualTo(expectedResult);
        verify(userSchemeDetailsRepository).findByUserFolioDetails_SchemesIn(inputList);
    }

    @Test
    void findBySchemesIn_WithEmptyList_ShouldReturnEmptyList() {
        List<UserSchemeDetails> inputList = Collections.emptyList();
        List<UserSchemeDetails> expectedResult = Collections.emptyList();

        when(userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(inputList))
                .thenReturn(expectedResult);

        List<UserSchemeDetails> result = userSchemeDetailService.findBySchemesIn(inputList);

        assertThat(result).isEqualTo(expectedResult);
        verify(userSchemeDetailsRepository).findByUserFolioDetails_SchemesIn(inputList);
    }
}
