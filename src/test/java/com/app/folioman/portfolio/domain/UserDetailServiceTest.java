package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.portfolio.UserSchemeDetailService;
import com.app.folioman.portfolio.rest.dtos.CasDTO;
import com.app.folioman.portfolio.rest.dtos.InvestorInfoDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioDetailsDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioResponse;
import com.app.folioman.portfolio.rest.dtos.StatementPeriodDTO;
import com.app.folioman.portfolio.rest.dtos.UploadFileResponse;
import com.app.folioman.portfolio.rest.dtos.UserFolioDTO;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserDetailServiceTest {

    @Mock
    private PortfolioServiceHelper portfolioServiceHelper;

    @Mock
    private CasDetailsMapper casDetailsMapper;

    @Mock
    private UserCASDetailsService userCASDetailsService;

    @Mock
    private InvestorInfoService investorInfoService;

    @Mock
    private UserTransactionDetailsService userTransactionDetailsService;

    @Mock
    private UserFolioDetailService userFolioDetailService;

    @Mock
    private UserSchemeDetailService userSchemeDetailService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PortfolioValueUpdateService portfolioValueUpdateService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private UserDetailService userDetailService;

    private CasDTO mockCasDTO;
    private InvestorInfoDTO mockInvestorInfoDTO;
    private UserFolioDTO mockFolioDTO;
    private UserCasDetailsEntity mockEntity;

    @BeforeEach
    void setUp() {
        mockInvestorInfoDTO = new InvestorInfoDTO("test@example.com", "Test User", "1234567890", "Address");
        mockFolioDTO = new UserFolioDTO("FOLIO123", "AMFI", "KYC", "PAN", "KRA", Collections.emptyList());
        mockCasDTO = new CasDTO(
                new StatementPeriodDTO("01-Jan-2023", "31-Dec-2023"),
                "FileType",
                "CasType",
                mockInvestorInfoDTO,
                List.of(mockFolioDTO));
        mockEntity = new UserCasDetailsEntity();
        mockEntity.setId(1L);
        mockEntity.setFolios(new ArrayList<>());
    }

    @Test
    void upload_invalidInvestorEmail_throwsException() throws IOException {
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        CasDTO invalidCas = new CasDTO(
                new StatementPeriodDTO("01-Jan-2023", "31-Dec-2023"),
                "FileType",
                "CasType",
                new InvestorInfoDTO(null, "Test User", "1234567890", "Address"),
                List.of(mockFolioDTO));
        when(portfolioServiceHelper.readValue(any(), eq(CasDTO.class))).thenReturn(invalidCas);

        assertThatThrownBy(() -> userDetailService.upload(multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email or Name invalid!");
    }

    @Test
    void upload_invalidInvestorName_throwsException() throws IOException {
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        CasDTO invalidCas = new CasDTO(
                new StatementPeriodDTO("01-Jan-2023", "31-Dec-2023"),
                "FileType",
                "CasType",
                new InvestorInfoDTO("test@example.com", "", "1234567890", "Address"),
                List.of(mockFolioDTO));
        when(portfolioServiceHelper.readValue(any(), eq(CasDTO.class))).thenReturn(invalidCas);

        assertThatThrownBy(() -> userDetailService.upload(multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email or Name invalid!");
    }

    @Test
    void upload_noFolios_throwsException() throws IOException {
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        CasDTO invalidCas = new CasDTO(
                new StatementPeriodDTO("01-Jan-2023", "31-Dec-2023"),
                "FileType",
                "CasType",
                mockInvestorInfoDTO,
                Collections.emptyList());
        when(portfolioServiceHelper.readValue(any(), eq(CasDTO.class))).thenReturn(invalidCas);

        assertThatThrownBy(() -> userDetailService.upload(multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No folios found!");
    }

    @Test
    void upload_newUser_processesAndReturnsResponse() throws IOException {
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        when(portfolioServiceHelper.readValue(any(), eq(CasDTO.class))).thenReturn(mockCasDTO);
        when(investorInfoService.existsByEmailAndName("test@example.com", "Test User"))
                .thenReturn(false);
        when(casDetailsMapper.convert(
                        eq(mockCasDTO), any(AtomicInteger.class), any(AtomicInteger.class), any(AtomicInteger.class)))
                .thenReturn(mockEntity);
        when(userCASDetailsService.saveEntity(mockEntity)).thenReturn(mockEntity);

        UploadFileResponse response = userDetailService.upload(multipartFile);

        assertThat(response).isNotNull();
        assertThat(response.userCASDetailsId()).isEqualTo(1L);
        org.mockito.Mockito.verify(portfolioValueUpdateService, org.mockito.Mockito.timeout(1000))
                .updatePortfolioValue(mockEntity.getId());
        verify(userFolioDetailService).setPANIfNotSet(1L);
    }

    @Test
    void getPortfolioByPAN_validPan_returnsPortfolioResponse() {
        String pan = "ABCDE1234F";
        LocalDate date = LocalDate.of(2023, 10, 10);
        PortfolioDetailsDTO portfolioDetailsDTO =
                new PortfolioDetailsDTO(new BigDecimal("1000"), "Scheme1", "Folio1", "2023-10-10", 10.0);
        when(portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(eq(pan), any(LocalDate.class)))
                .thenReturn(List.of(portfolioDetailsDTO));

        PortfolioResponse response = userDetailService.getPortfolioByPAN(pan, date);

        assertThat(response).isNotNull();
        assertThat(response.portfolioDetailsDTOS()).hasSize(1);
        assertThat(response.totalPortfolioValue()).isEqualByComparingTo("1000.0000");
    }

    @Test
    void uploadFromDto_existingUser_processesExisting() {
        when(investorInfoService.existsByEmailAndName("test@example.com", "Test User"))
                .thenReturn(true);
        when(userCASDetailsService.findByInvestorEmailAndName("test@example.com", "Test User"))
                .thenReturn(Optional.of(mockEntity));
        when(portfolioServiceHelper.countTransactionsByUserFolioDTOList(anyList()))
                .thenReturn(0L);
        when(userTransactionDetailsService.findAllTransactionsByEmailNameAndPeriod(
                        anyString(), anyString(), any(), any()))
                .thenReturn(0L);

        UploadFileResponse response = userDetailService.uploadFromDto(mockCasDTO);

        assertThat(response).isNotNull();
        assertThat(response.userCASDetailsId()).isEqualTo(1L);
    }

    @Test
    void uploadFromDto_existingUser_withNewTransactions() {
        when(investorInfoService.existsByEmailAndName("test@example.com", "Test User"))
                .thenReturn(true);
        when(userCASDetailsService.findByInvestorEmailAndName("test@example.com", "Test User"))
                .thenReturn(Optional.of(mockEntity));

        // Req count is 1, DB count is 0 -> importNewTransaction will be called
        when(portfolioServiceHelper.countTransactionsByUserFolioDTOList(anyList()))
                .thenReturn(1L);
        when(userTransactionDetailsService.findAllTransactionsByEmailNameAndPeriod(
                        anyString(), anyString(), any(), any()))
                .thenReturn(0L);

        // Mocking casDetailsMapper for new folios
        UserFolioDetailsEntity newFolioEntity = new UserFolioDetailsEntity();
        newFolioEntity.setFolio("FOLIO123");
        newFolioEntity.setSchemes(new ArrayList<>());
        when(casDetailsMapper.mapUserFolioDTOToUserFolioDetails(any(), any(), any()))
                .thenReturn(newFolioEntity);

        UploadFileResponse response = userDetailService.uploadFromDto(mockCasDTO);

        assertThat(response).isNotNull();
        assertThat(response.userCASDetailsId()).isEqualTo(1L);
        // It should have found 1 new folio because the db mockEntity has no folios
        assertThat(response.newFolios()).isEqualTo(1);
    }
}
