package com.app.folioman.portfolio.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.app.folioman.portfolio.UserSchemeDetailService;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.mapper.CasDetailsMapper;
import com.app.folioman.portfolio.models.request.CasDTO;
import com.app.folioman.portfolio.models.request.InvestorInfoDTO;
import com.app.folioman.portfolio.models.request.StatementPeriodDTO;
import com.app.folioman.portfolio.models.request.UserFolioDTO;
import com.app.folioman.portfolio.models.response.PortfolioDetailsDTO;
import com.app.folioman.portfolio.models.response.PortfolioResponse;
import com.app.folioman.portfolio.models.response.UploadFileResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    private CasDTO casDTO;
    private UserCASDetails userCASDetails;
    private UploadFileResponse uploadFileResponse;

    @BeforeEach
    void setUp() {
        InvestorInfoDTO investorInfo =
                new InvestorInfoDTO("test@example.com", "Test User", "9848022338", "Test Address");
        StatementPeriodDTO statementPeriod = new StatementPeriodDTO("01-Jan-2023", "31-Dec-2023");
        List<UserFolioDTO> folios = new ArrayList<>();
        // Ensure at least one folio is present so validateCasDTO passes in tests
        folios.add(new UserFolioDTO(
                "FOLIO123", "AMC_TEST", "ABCDE1234F", "KYC_STATUS", "PAN_KYC_STATUS", new ArrayList<>()));

        casDTO = new CasDTO(statementPeriod, "NEW", "DETAILED", investorInfo, folios);

        userCASDetails = new UserCASDetails();
        userCASDetails.setId(1L);
        userCASDetails.setFolios(new ArrayList<>());
        // ensure required enums and investor info are set to match DB constraints when used by services
        userCASDetails.setCasTypeEnum(com.app.folioman.portfolio.entities.CasTypeEnum.DETAILED);
        userCASDetails.setFileTypeEnum(com.app.folioman.portfolio.entities.FileTypeEnum.CAMS);
        com.app.folioman.portfolio.entities.InvestorInfo ii = new com.app.folioman.portfolio.entities.InvestorInfo();
        ii.setEmail("test@example.com");
        ii.setName("Test User");
        ii.setMobile("9848022338");
        ii.setAddress("Test Address");
        userCASDetails.setInvestorInfo(ii);

        uploadFileResponse = new UploadFileResponse(1, 2, 3, 1L);
    }

    @Test
    void upload_NewUser_ShouldReturnUploadFileResponse() throws IOException {
        byte[] fileBytes = "test content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(portfolioServiceHelper.readValue(fileBytes, CasDTO.class)).thenReturn(casDTO);
        when(investorInfoService.existsByEmailAndName("test@example.com", "Test User"))
                .thenReturn(false);
        when(casDetailsMapper.convert(
                        eq(casDTO), any(AtomicInteger.class), any(AtomicInteger.class), any(AtomicInteger.class)))
                .thenReturn(userCASDetails);
        when(userCASDetailsService.saveEntity(userCASDetails)).thenReturn(userCASDetails);

        UploadFileResponse result = userDetailService.upload(multipartFile);

        assertNotNull(result);
        verify(portfolioServiceHelper).readValue(fileBytes, CasDTO.class);
        verify(investorInfoService).existsByEmailAndName("test@example.com", "Test User");
        verify(casDetailsMapper)
                .convert(eq(casDTO), any(AtomicInteger.class), any(AtomicInteger.class), any(AtomicInteger.class));
        verify(userCASDetailsService).saveEntity(userCASDetails);
        verify(portfolioValueUpdateService).updatePortfolioValue(userCASDetails);
    }

    @Test
    void upload_ExistingUser_ShouldReturnUploadFileResponse() throws IOException {
        byte[] fileBytes = "test content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(portfolioServiceHelper.readValue(fileBytes, CasDTO.class)).thenReturn(casDTO);
        when(investorInfoService.existsByEmailAndName("test@example.com", "Test User"))
                .thenReturn(true);
        when(portfolioServiceHelper.countTransactionsByUserFolioDTOList(casDTO.folios()))
                .thenReturn(5L);
        when(userTransactionDetailsService.findAllTransactionsByEmailNameAndPeriod(
                        eq("Test User"), eq("test@example.com"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(5L);

        UploadFileResponse result = userDetailService.upload(multipartFile);

        assertNotNull(result);
        assertEquals(0, result.newFolios());
        assertEquals(0, result.newSchemes());
        assertEquals(0, result.newTransactions());
        assertEquals(0L, result.userCASDetailsId());
        verify(portfolioServiceHelper).readValue(fileBytes, CasDTO.class);
        verify(investorInfoService).existsByEmailAndName("test@example.com", "Test User");
    }

    @Test
    void upload_IOExceptionThrown_ShouldPropagateException() throws IOException {
        when(multipartFile.getBytes()).thenThrow(new IOException("File read error"));

        assertThrows(IOException.class, () -> userDetailService.upload(multipartFile));

        verify(multipartFile).getBytes();
    }

    @Test
    void upload_InvalidCasDTO_ShouldThrowIllegalArgumentException() throws IOException {
        CasDTO invalidCasDTO = new CasDTO(null, null, null, null, null);
        byte[] fileBytes = "test content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(portfolioServiceHelper.readValue(fileBytes, CasDTO.class)).thenReturn(invalidCasDTO);

        assertThrows(IllegalArgumentException.class, () -> userDetailService.upload(multipartFile));

        verify(portfolioServiceHelper).readValue(fileBytes, CasDTO.class);
    }

    @Test
    void uploadFromDto_NewUser_ShouldReturnUploadFileResponse() {
        when(investorInfoService.existsByEmailAndName("test@example.com", "Test User"))
                .thenReturn(false);
        when(casDetailsMapper.convert(
                        eq(casDTO), any(AtomicInteger.class), any(AtomicInteger.class), any(AtomicInteger.class)))
                .thenReturn(userCASDetails);
        when(userCASDetailsService.saveEntity(userCASDetails)).thenReturn(userCASDetails);

        UploadFileResponse result = userDetailService.uploadFromDto(casDTO);

        assertNotNull(result);
        verify(investorInfoService).existsByEmailAndName("test@example.com", "Test User");
        verify(casDetailsMapper)
                .convert(eq(casDTO), any(AtomicInteger.class), any(AtomicInteger.class), any(AtomicInteger.class));
        verify(userCASDetailsService).saveEntity(userCASDetails);
        verify(portfolioValueUpdateService).updatePortfolioValue(userCASDetails);
    }

    @Test
    void uploadFromDto_ExistingUser_ShouldReturnUploadFileResponse() {
        when(investorInfoService.existsByEmailAndName("test@example.com", "Test User"))
                .thenReturn(true);
        when(portfolioServiceHelper.countTransactionsByUserFolioDTOList(casDTO.folios()))
                .thenReturn(3L);
        when(userTransactionDetailsService.findAllTransactionsByEmailNameAndPeriod(
                        eq("Test User"), eq("test@example.com"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(3L);

        UploadFileResponse result = userDetailService.uploadFromDto(casDTO);

        assertNotNull(result);
        assertEquals(0, result.newFolios());
        assertEquals(0, result.newSchemes());
        assertEquals(0, result.newTransactions());
        assertEquals(0L, result.userCASDetailsId());
        verify(investorInfoService).existsByEmailAndName("test@example.com", "Test User");
    }

    @Test
    void uploadFromDto_InvalidCasDTO_ShouldThrowIllegalArgumentException() {
        CasDTO invalidCasDTO = new CasDTO(null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> userDetailService.uploadFromDto(invalidCasDTO));
    }

    @Test
    void getPortfolioByPAN_WithData_ShouldReturnPortfolioResponse() {
        String panNumber = "ABCDE1234F";
        LocalDate evaluationDate = LocalDate.now();

        List<PortfolioDetailsDTO> portfolioDetailsList = List.of(
                new PortfolioDetailsDTO(BigDecimal.valueOf(10000), "Test Scheme 1", "98670", "2025-11-01", 19.0),
                new PortfolioDetailsDTO(BigDecimal.valueOf(5000), "Test Scheme 2", "98670", "2025-11-01", 9.0));

        when(portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(eq(panNumber), any(LocalDate.class)))
                .thenReturn(portfolioDetailsList);

        PortfolioResponse result = userDetailService.getPortfolioByPAN(panNumber, evaluationDate);

        assertNotNull(result);
        // The service sums the provided totalValue fields. Expect 10000 + 5000 = 15000
        assertEquals(BigDecimal.valueOf(15000.0000).setScale(4), result.totalPortfolioValue());
        assertEquals(2, result.portfolioDetailsDTOS().size());
        verify(portfolioServiceHelper).getPortfolioDetailsByPANAndAsOfDate(eq(panNumber), any(LocalDate.class));
    }

    @Test
    void getPortfolioByPAN_EmptyData_ShouldReturnEmptyPortfolioResponse() {
        String panNumber = "ABCDE1234F";
        LocalDate evaluationDate = LocalDate.now();

        List<PortfolioDetailsDTO> emptyPortfolioDetailsList = new ArrayList<>();

        when(portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(eq(panNumber), any(LocalDate.class)))
                .thenReturn(emptyPortfolioDetailsList);

        PortfolioResponse result = userDetailService.getPortfolioByPAN(panNumber, evaluationDate);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.setScale(4), result.totalPortfolioValue());
        assertTrue(result.portfolioDetailsDTOS().isEmpty());
        verify(portfolioServiceHelper).getPortfolioDetailsByPANAndAsOfDate(eq(panNumber), any(LocalDate.class));
    }
}
