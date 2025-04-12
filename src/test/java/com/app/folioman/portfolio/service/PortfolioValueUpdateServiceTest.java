package com.app.folioman.portfolio.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MFSchemeNavProjection;
import com.app.folioman.portfolio.TestData;
import com.app.folioman.portfolio.entities.CasTypeEnum;
import com.app.folioman.portfolio.entities.FileTypeEnum;
import com.app.folioman.portfolio.entities.FolioScheme;
import com.app.folioman.portfolio.entities.InvestorInfo;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.entities.UserPortfolioValue;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.models.request.CasDTO;
import com.app.folioman.portfolio.models.request.TransactionType;
import com.app.folioman.portfolio.repository.FolioSchemeRepository;
import com.app.folioman.portfolio.repository.UserPortfolioValueRepository;
import com.app.folioman.portfolio.util.XirrCalculator;
import com.app.folioman.shared.LocalDateUtility;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PortfolioValueUpdateServiceTest {

    @Mock
    private UserPortfolioValueRepository userPortfolioValueRepository;

    @Mock
    private MFNavService mfNavService;

    @Mock
    private FolioSchemeRepository folioSchemeRepository;

    @InjectMocks
    private PortfolioValueUpdateService portfolioValueUpdateService;

    @Captor
    private ArgumentCaptor<List<UserPortfolioValue>> portfolioValueCaptor;

    @Captor
    private ArgumentCaptor<FolioScheme> folioSchemeCaptor;

    private UserCASDetails userCASDetails;
    private MFSchemeNavProjection mfSchemeNavProjection;

    @BeforeEach
    void setUp() {
        // Create a UserCASDetails entity from TestData's CasDTO
        userCASDetails = createUserCASDetailsFromTestData();

        // Mock MFSchemeNavProjection - only initialize it, don't stub behavior here
        mfSchemeNavProjection = mock(MFSchemeNavProjection.class);
    }

    @Test
    @DisplayName("Test updatePortfolioValue with CAS data")
    @Disabled
    void testUpdatePortfolioValueWithCasData() {
        // Given
        when(userPortfolioValueRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(mfSchemeNavProjection.nav()).thenReturn(BigDecimal.valueOf(12.50));
        when(mfNavService.getNavsForSchemesAndDates(anySet(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(createMockNavData());
        when(folioSchemeRepository.findByUserSchemeDetails_Id(anyLong())).thenReturn(null);

        // Mock the XirrCalculator.xirr method
        try (MockedStatic<XirrCalculator> xirrCalculator = Mockito.mockStatic(XirrCalculator.class)) {
            xirrCalculator.when(() -> XirrCalculator.xirr(anyMap())).thenReturn(BigDecimal.valueOf(15.5));

            // Mock LocalDateUtility.getYesterday() and getAdjustedDate()
            try (MockedStatic<LocalDateUtility> localDateUtility = Mockito.mockStatic(LocalDateUtility.class)) {
                LocalDate yesterday = LocalDate.now().minusDays(1);
                localDateUtility.when(LocalDateUtility::getYesterday).thenReturn(yesterday);
                localDateUtility
                        .when(() -> LocalDateUtility.getAdjustedDate(any(LocalDate.class)))
                        .thenAnswer(invocation -> invocation.getArgument(0));

                // When
                portfolioValueUpdateService.updatePortfolioValue(userCASDetails);

                // Then
                verify(userPortfolioValueRepository).saveAll(portfolioValueCaptor.capture());
                verify(folioSchemeRepository, atLeastOnce()).save(folioSchemeCaptor.capture());

                List<UserPortfolioValue> savedValues = portfolioValueCaptor.getValue();
                assertFalse(savedValues.isEmpty());

                // Verify portfolio value data for the last date
                UserPortfolioValue lastValue = savedValues.get(savedValues.size() - 1);
                assertEquals(yesterday, lastValue.getDate());
                assertNotNull(lastValue.getValue());
                assertNotNull(lastValue.getInvested());
                assertEquals(userCASDetails, lastValue.getUserCasDetails());

                // Verify XIRR was set on most recent value
                assertNotNull(lastValue.getXirr());
                assertEquals(BigDecimal.valueOf(15.5), lastValue.getXirr());
            }
        }
    }

    @Test
    @DisplayName("Test addFinalValuationCashFlows uses merge instead of put")
    @Disabled
    void testAddFinalValuationCashFlows() throws Exception {
        // Given
        Method addFinalValuationMethod = PortfolioValueUpdateService.class.getDeclaredMethod(
                "addFinalValuationCashFlows",
                LocalDate.class,
                PortfolioValueUpdateService.PortfolioDataContainer.class,
                Map.class,
                BigDecimal.class);
        addFinalValuationMethod.setAccessible(true);

        // Create a data container using reflection
        PortfolioValueUpdateService.PortfolioDataContainer dataContainer = createDataContainer();

        // Add existing data to test merge functionality
        Map<Long, Double> cumulativeUnitsByScheme = dataContainer.cumulativeUnitsByScheme();
        Long schemeCode = 120503L; // Using AMFI code from TestData
        cumulativeUnitsByScheme.put(schemeCode, 100.0);

        Map<LocalDate, BigDecimal> allCashFlows = dataContainer.allCashFlows();
        LocalDate today = LocalDate.now();
        // Add existing cash flow for today to test the merge function
        allCashFlows.put(today, BigDecimal.valueOf(1000));

        Map<Long, Map<LocalDate, BigDecimal>> cashFlowsByScheme = dataContainer.cashFlowsByScheme();
        Map<LocalDate, BigDecimal> schemeCashFlows = new HashMap<>();
        schemeCashFlows.put(today, BigDecimal.valueOf(500));
        cashFlowsByScheme.put(schemeCode, schemeCashFlows);

        // Create mock NAV data with properly mocked nav values
        Map<Long, Map<LocalDate, MFSchemeNavProjection>> navData = new HashMap<>();
        MFSchemeNavProjection mockNavProjection = mock(MFSchemeNavProjection.class);
        when(mockNavProjection.nav()).thenReturn(BigDecimal.valueOf(12.50));

        Map<LocalDate, MFSchemeNavProjection> schemeNavs = new HashMap<>();
        schemeNavs.put(today, mockNavProjection);
        navData.put(schemeCode, schemeNavs);

        BigDecimal portfolioValue = BigDecimal.valueOf(2000);

        // When
        addFinalValuationMethod.invoke(portfolioValueUpdateService, today, dataContainer, navData, portfolioValue);

        // Then - verify allCashFlows was merged correctly
        assertEquals(
                BigDecimal.valueOf(3000).doubleValue(), allCashFlows.get(today).doubleValue(), 0.001); // 1000 + 2000

        // Verify scheme cash flows were merged correctly
        Map<LocalDate, BigDecimal> updatedSchemeCashFlows = cashFlowsByScheme.get(schemeCode);
        assertEquals(
                BigDecimal.valueOf(1750).doubleValue(), // 500 + (100 units * 12.50 NAV)
                updatedSchemeCashFlows.get(today).doubleValue(),
                0.001);
    }

    private PortfolioValueUpdateService.PortfolioDataContainer createDataContainer() {
        return new PortfolioValueUpdateService.PortfolioDataContainer(
                new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    @Test
    @DisplayName("Test processTransaction uses computeIfAbsent")
    void testProcessTransaction() throws Exception {
        // Given
        Method processTransactionMethod = PortfolioValueUpdateService.class.getDeclaredMethod(
                "processTransaction",
                UserTransactionDetails.class,
                PortfolioValueUpdateService.PortfolioDataContainer.class);
        processTransactionMethod.setAccessible(true);

        // Create a data container
        PortfolioValueUpdateService.PortfolioDataContainer dataContainer = createDataContainer();

        // Get a transaction from the CAS data
        UserTransactionDetails transaction = userCASDetails
                .getFolios()
                .getFirst()
                .getSchemes()
                .getFirst()
                .getTransactions()
                .getFirst();

        // When
        processTransactionMethod.invoke(portfolioValueUpdateService, transaction, dataContainer);

        // Then
        Map<Long, Map<LocalDate, BigDecimal>> cashFlowsByScheme = dataContainer.cashFlowsByScheme();

        // Verify cashFlowsByScheme was initialized using computeIfAbsent
        assertNotNull(cashFlowsByScheme);
        Long amfiCode = transaction.getUserSchemeDetails().getAmfi();
        assertTrue(cashFlowsByScheme.containsKey(amfiCode));

        // Verify recordCashFlows added the transaction correctly
        Map<LocalDate, BigDecimal> schemeCashFlows = cashFlowsByScheme.get(amfiCode);
        assertNotNull(schemeCashFlows);
        assertTrue(schemeCashFlows.containsKey(transaction.getTransactionDate()));
    }

    @Test
    @DisplayName("Test generateOperationId uses UUID")
    void testGenerateOperationId() throws Exception {
        // Given
        Method generateOperationIdMethod =
                PortfolioValueUpdateService.class.getDeclaredMethod("generateOperationId", Long.class);
        generateOperationIdMethod.setAccessible(true);

        // When
        String operationId1 = (String) generateOperationIdMethod.invoke(portfolioValueUpdateService, 123L);
        String operationId2 = (String) generateOperationIdMethod.invoke(portfolioValueUpdateService, 123L);

        // Then
        assertTrue(operationId1.startsWith("folioScheme-123-"));
        assertTrue(operationId2.startsWith("folioScheme-123-"));

        // Verify UUIDs are unique
        assertNotEquals(operationId1, operationId2);

        // Verify UUIDs are valid
        String uuid1 = operationId1.substring("folioScheme-123-".length());
        String uuid2 = operationId2.substring("folioScheme-123-".length());
        assertDoesNotThrow(() -> UUID.fromString(uuid1));
        assertDoesNotThrow(() -> UUID.fromString(uuid2));
    }

    private UserCASDetails createUserCASDetailsFromTestData() {
        // Get CasDTO from TestData
        CasDTO casDTO = TestData.getCasDTO();

        // Create UserCASDetails entity
        UserCASDetails userCASDetails = new UserCASDetails();
        userCASDetails.setId(1L);
        userCASDetails.setCasTypeEnum(Enum.valueOf(CasTypeEnum.class, casDTO.casType()));
        userCASDetails.setFileTypeEnum(Enum.valueOf(FileTypeEnum.class, casDTO.fileType()));

        // Create InvestorInfo
        InvestorInfo investorInfo = new InvestorInfo();
        investorInfo.setEmail(casDTO.investorInfo().email());
        investorInfo.setName(casDTO.investorInfo().name());
        investorInfo.setMobile(casDTO.investorInfo().mobile());
        investorInfo.setAddress(casDTO.investorInfo().address());
        userCASDetails.setInvestorInfo(investorInfo);

        // Convert folios
        List<UserFolioDetails> folios = new ArrayList<>();
        casDTO.folios().forEach(folioDTO -> {
            UserFolioDetails folio = new UserFolioDetails();
            folio.setFolio(folioDTO.folio());
            folio.setAmc(folioDTO.amc());
            folio.setPan(folioDTO.pan());
            folio.setKyc(folioDTO.kyc());
            folio.setPanKyc(folioDTO.panKyc());
            folio.setUserCasDetails(userCASDetails);

            List<UserSchemeDetails> schemes = new ArrayList<>();
            folioDTO.schemes().forEach(schemeDTO -> {
                UserSchemeDetails scheme = new UserSchemeDetails();
                scheme.setScheme(schemeDTO.scheme());
                scheme.setIsin(schemeDTO.isin());
                scheme.setAmfi(schemeDTO.amfi());
                scheme.setAdvisor(schemeDTO.advisor());
                scheme.setRtaCode(schemeDTO.rtaCode());
                scheme.setRta(schemeDTO.rta());
                scheme.setType(schemeDTO.type());
                scheme.setUserFolioDetails(folio);

                List<UserTransactionDetails> transactions = new ArrayList<>();
                schemeDTO.transactions().forEach(transactionDTO -> {
                    if (transactionDTO.type() != null
                            && !TransactionType.STAMP_DUTY_TAX.equals(transactionDTO.type())
                            && !TransactionType.TDS_TAX.equals(transactionDTO.type())
                            && !TransactionType.STT_TAX.equals(transactionDTO.type())
                            && !TransactionType.MISC.equals(transactionDTO.type())) {
                        UserTransactionDetails transaction = new UserTransactionDetails();
                        transaction.setTransactionDate(transactionDTO.date());
                        transaction.setDescription(transactionDTO.description());
                        if (transactionDTO.amount() != null) {
                            transaction.setAmount(BigDecimal.valueOf(transactionDTO.amount()));
                        }
                        transaction.setUnits(transactionDTO.units());
                        transaction.setNav(transactionDTO.nav());
                        transaction.setBalance(transactionDTO.balance());
                        transaction.setType(transactionDTO.type());
                        transaction.setDividendRate(transactionDTO.dividendRate());
                        transaction.setUserSchemeDetails(scheme);
                        transactions.add(transaction);
                    }
                });

                scheme.setTransactions(transactions);
                schemes.add(scheme);
            });

            folio.setSchemes(schemes);
            folios.add(folio);
        });

        userCASDetails.setFolios(folios);
        return userCASDetails;
    }

    private Map<Long, Map<LocalDate, MFSchemeNavProjection>> createMockNavData() {
        Map<Long, Map<LocalDate, MFSchemeNavProjection>> navData = new HashMap<>();

        // Get all scheme codes from the userCASDetails
        Set<Long> schemeCodes = new HashSet<>();
        userCASDetails.getFolios().forEach(folio -> folio.getSchemes()
                .forEach(scheme -> schemeCodes.add(scheme.getAmfi())));

        // Create NAV data for each scheme
        schemeCodes.forEach(schemeCode -> {
            if (schemeCode != null) {
                Map<LocalDate, MFSchemeNavProjection> schemeNavs = new HashMap<>();

                // Add NAVs for all dates in last month
                LocalDate today = LocalDate.now();
                LocalDate startDate = today.minusMonths(1);
                LocalDate endDate = today;

                startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
                    schemeNavs.put(date, mfSchemeNavProjection);
                });

                navData.put(schemeCode, schemeNavs);
            }
        });

        return navData;
    }
}
