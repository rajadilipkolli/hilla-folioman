package com.app.folioman.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.models.projection.PortfolioDetailsProjection;
import com.app.folioman.portfolio.repository.UserCASDetailsRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCASDetailsServiceTest {

    @Mock
    private UserCASDetailsRepository userCASDetailsRepository;

    @InjectMocks
    private UserCASDetailsService userCASDetailsService;

    private UserCASDetails userCASDetails;
    private PortfolioDetailsProjection portfolioDetailsProjection;

    @BeforeEach
    void setUp() {
        userCASDetails = new UserCASDetails();
        // ensure required enums and investor info for any potential use
        userCASDetails.setCasTypeEnum(com.app.folioman.portfolio.entities.CasTypeEnum.DETAILED);
        userCASDetails.setFileTypeEnum(com.app.folioman.portfolio.entities.FileTypeEnum.CAMS);
        com.app.folioman.portfolio.entities.InvestorInfo ii = new com.app.folioman.portfolio.entities.InvestorInfo();
        ii.setEmail("");
        ii.setName("");
        userCASDetails.setInvestorInfo(ii);
        portfolioDetailsProjection = new PortfolioDetailsProjection() {
            @Override
            public String getSchemeName() {
                return "";
            }

            @Override
            public String getFolioNumber() {
                return "";
            }

            @Override
            public Double getBalanceUnits() {
                return 0.0;
            }

            @Override
            public Long getSchemeId() {
                return 0L;
            }

            @Override
            public Long getSchemeDetailId() {
                return 0L;
            }
        };
    }

    @Test
    void saveEntity_ShouldReturnSavedEntity() {
        when(userCASDetailsRepository.save(any(UserCASDetails.class))).thenReturn(userCASDetails);

        UserCASDetails result = userCASDetailsService.saveEntity(userCASDetails);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(userCASDetails);
        verify(userCASDetailsRepository).save(userCASDetails);
    }

    @Test
    void saveEntity_WithNullInput_ShouldHandleGracefully() {
        when(userCASDetailsRepository.save(any())).thenReturn(null);

        UserCASDetails result = userCASDetailsService.saveEntity(null);

        assertThat(result).isNull();
        verify(userCASDetailsRepository).save(null);
    }

    @Test
    void findByInvestorEmailAndName_ShouldReturnUserCASDetails() {
        String email = "test@example.com";
        String name = "Test User";
        when(userCASDetailsRepository.findByInvestorEmailAndName(email, name)).thenReturn(userCASDetails);

        UserCASDetails result = userCASDetailsService.findByInvestorEmailAndName(email, name);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(userCASDetails);
        verify(userCASDetailsRepository).findByInvestorEmailAndName(email, name);
    }

    @Test
    void findByInvestorEmailAndName_WithNullEmail_ShouldReturnNull() {
        String name = "Test User";
        when(userCASDetailsRepository.findByInvestorEmailAndName(null, name)).thenReturn(null);

        UserCASDetails result = userCASDetailsService.findByInvestorEmailAndName(null, name);

        assertThat(result).isNull();
        verify(userCASDetailsRepository).findByInvestorEmailAndName(null, name);
    }

    @Test
    void findByInvestorEmailAndName_WithNullName_ShouldReturnNull() {
        String email = "test@example.com";
        when(userCASDetailsRepository.findByInvestorEmailAndName(email, null)).thenReturn(null);

        UserCASDetails result = userCASDetailsService.findByInvestorEmailAndName(email, null);

        assertThat(result).isNull();
        verify(userCASDetailsRepository).findByInvestorEmailAndName(email, null);
    }

    @Test
    void findByInvestorEmailAndName_WithEmptyStrings_ShouldReturnNull() {
        when(userCASDetailsRepository.findByInvestorEmailAndName("", "")).thenReturn(null);

        UserCASDetails result = userCASDetailsService.findByInvestorEmailAndName("", "");

        assertThat(result).isNull();
        verify(userCASDetailsRepository).findByInvestorEmailAndName("", "");
    }

    @Test
    void getPortfolioDetailsByPanAndAsOfDate_ShouldReturnPortfolioDetails() {
        String panNumber = "ABCDE1234F";
        LocalDate asOfDate = LocalDate.now();
        List<PortfolioDetailsProjection> expectedList = Arrays.asList(portfolioDetailsProjection);
        when(userCASDetailsRepository.getPortfolioDetails(panNumber, asOfDate)).thenReturn(expectedList);

        List<PortfolioDetailsProjection> result =
                userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(expectedList);
        verify(userCASDetailsRepository).getPortfolioDetails(panNumber, asOfDate);
    }

    @Test
    void getPortfolioDetailsByPanAndAsOfDate_WithNullPanNumber_ShouldReturnEmptyList() {
        LocalDate asOfDate = LocalDate.now();
        when(userCASDetailsRepository.getPortfolioDetails(null, asOfDate)).thenReturn(Collections.emptyList());

        List<PortfolioDetailsProjection> result =
                userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(null, asOfDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
        verify(userCASDetailsRepository).getPortfolioDetails(null, asOfDate);
    }

    @Test
    void getPortfolioDetailsByPanAndAsOfDate_WithNullAsOfDate_ShouldReturnEmptyList() {
        String panNumber = "ABCDE1234F";
        when(userCASDetailsRepository.getPortfolioDetails(panNumber, null)).thenReturn(Collections.emptyList());

        List<PortfolioDetailsProjection> result =
                userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, null);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
        verify(userCASDetailsRepository).getPortfolioDetails(panNumber, null);
    }

    @Test
    void getPortfolioDetailsByPanAndAsOfDate_WithEmptyPanNumber_ShouldReturnEmptyList() {
        LocalDate asOfDate = LocalDate.now();
        when(userCASDetailsRepository.getPortfolioDetails("", asOfDate)).thenReturn(Collections.emptyList());

        List<PortfolioDetailsProjection> result =
                userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate("", asOfDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
        verify(userCASDetailsRepository).getPortfolioDetails("", asOfDate);
    }
}
