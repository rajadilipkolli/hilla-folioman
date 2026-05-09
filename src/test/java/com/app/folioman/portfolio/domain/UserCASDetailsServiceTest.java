package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.portfolio.domain.models.projection.PortfolioDetailsProjection;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCASDetailsServiceTest {

    @Mock
    private UserCASDetailsRepository UserCasDetailsRepository;

    @InjectMocks
    private UserCASDetailsService userCASDetailsService;

    private UserCasDetailsEntity userCasDetailsEntity;
    private PortfolioDetailsProjection portfolioDetailsProjection;

    @BeforeEach
    void setUp() {
        userCasDetailsEntity = new UserCasDetailsEntity();
        // ensure required enums and investor info for any potential use
        userCasDetailsEntity.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCasDetailsEntity.setFileTypeEnum(FileTypeEnum.CAMS);
        InvestorInfoEntity ii = new InvestorInfoEntity();
        ii.setEmail("");
        ii.setName("");
        userCasDetailsEntity.setInvestorInfoEntity(ii);
        portfolioDetailsProjection = new PortfolioDetailsProjection() {
            @Override
            public @NonNull String getSchemeName() {
                return "";
            }

            @Override
            public @NonNull String getFolioNumber() {
                return "";
            }

            @Override
            public @NonNull Double getBalanceUnits() {
                return 0.0;
            }

            @Override
            public @NonNull Long getSchemeId() {
                return 0L;
            }

            @Override
            public @NonNull Long getSchemeDetailId() {
                return 0L;
            }
        };
    }

    @Test
    void saveEntity_ShouldReturnSavedEntity() {
        when(UserCasDetailsRepository.save(any(UserCasDetailsEntity.class))).thenReturn(userCasDetailsEntity);

        UserCasDetailsEntity result = userCASDetailsService.saveEntity(userCasDetailsEntity);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(userCasDetailsEntity);
        verify(UserCasDetailsRepository).save(userCasDetailsEntity);
    }

    @Test
    void findByInvestorEmailAndName_ShouldReturnUserCASDetails() {
        String email = "test@example.com";
        String name = "Test User";
        when(UserCasDetailsRepository.findByInvestorEmailAndName(email, name))
                .thenReturn(Optional.ofNullable(userCasDetailsEntity));

        Optional<UserCasDetailsEntity> result = userCASDetailsService.findByInvestorEmailAndName(email, name);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(userCasDetailsEntity);
        verify(UserCasDetailsRepository).findByInvestorEmailAndName(email, name);
    }

    @Test
    void findByInvestorEmailAndName_WithEmptyStrings_ShouldReturnEmptyOptional() {
        when(UserCasDetailsRepository.findByInvestorEmailAndName("", "")).thenReturn(Optional.empty());

        Optional<UserCasDetailsEntity> result = userCASDetailsService.findByInvestorEmailAndName("", "");

        assertThat(result).isEmpty();
        verify(UserCasDetailsRepository).findByInvestorEmailAndName("", "");
    }

    @Test
    void getPortfolioDetailsByPanAndAsOfDate_ShouldReturnPortfolioDetails() {
        String panNumber = "ABCDE1234F";
        LocalDate asOfDate = LocalDate.now();
        List<PortfolioDetailsProjection> expectedList = Collections.singletonList(portfolioDetailsProjection);
        when(UserCasDetailsRepository.getPortfolioDetails(panNumber, asOfDate)).thenReturn(expectedList);

        List<PortfolioDetailsProjection> result =
                userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(expectedList);
        verify(UserCasDetailsRepository).getPortfolioDetails(panNumber, asOfDate);
    }

    @Test
    void getPortfolioDetailsByPanAndAsOfDate_WithNullPanNumber_ShouldReturnEmptyList() {
        LocalDate asOfDate = LocalDate.now();
        when(UserCasDetailsRepository.getPortfolioDetails(null, asOfDate)).thenReturn(Collections.emptyList());

        List<PortfolioDetailsProjection> result =
                userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(null, asOfDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
        verify(UserCasDetailsRepository).getPortfolioDetails(null, asOfDate);
    }

    @Test
    void getPortfolioDetailsByPanAndAsOfDate_WithNullAsOfDate_ShouldReturnEmptyList() {
        String panNumber = "ABCDE1234F";
        when(UserCasDetailsRepository.getPortfolioDetails(panNumber, null)).thenReturn(Collections.emptyList());

        List<PortfolioDetailsProjection> result =
                userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, null);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
        verify(UserCasDetailsRepository).getPortfolioDetails(panNumber, null);
    }

    @Test
    void getPortfolioDetailsByPanAndAsOfDate_WithEmptyPanNumber_ShouldReturnEmptyList() {
        LocalDate asOfDate = LocalDate.now();
        when(UserCasDetailsRepository.getPortfolioDetails("", asOfDate)).thenReturn(Collections.emptyList());

        List<PortfolioDetailsProjection> result =
                userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate("", asOfDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
        verify(UserCasDetailsRepository).getPortfolioDetails("", asOfDate);
    }
}
