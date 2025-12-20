package com.app.folioman.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.portfolio.models.projection.PortfolioDetailsProjection;
import com.app.folioman.portfolio.models.request.UserFolioDTO;
import com.app.folioman.portfolio.models.request.UserSchemeDTO;
import com.app.folioman.portfolio.models.request.UserTransactionDTO;
import com.app.folioman.portfolio.models.response.PortfolioDetailsDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceHelperTest {

    @Mock
    private JsonMapper mapper;

    @Mock
    private UserCASDetailsService userCASDetailsService;

    @Mock
    private MFNavService mfNavService;

    @Mock
    private PortfolioDetailsProjection portfolioDetailsProjection;

    private PortfolioServiceHelper portfolioServiceHelper;

    @BeforeEach
    void setUp() {
        portfolioServiceHelper = new PortfolioServiceHelper(mapper, userCASDetailsService, mfNavService);
    }

    @Test
    void readValue_ShouldReturnParsedObject_WhenValidBytesProvided() {
        byte[] bytes = "{\"test\":\"value\"}".getBytes();
        TestClass expected = new TestClass("value");
        when(mapper.readValue(bytes, TestClass.class)).thenReturn(expected);

        TestClass result = portfolioServiceHelper.readValue(bytes, TestClass.class);

        assertThat(result).isEqualTo(expected);
        verify(mapper).readValue(bytes, TestClass.class);
    }

    @Test
    void readValue_ShouldThrowJacksonException_WhenMapperThrowsException() {
        byte[] bytes = "invalid json".getBytes();
        when(mapper.readValue(bytes, TestClass.class)).thenThrow(new JacksonException("Parsing error") {});

        assertThatExceptionOfType(JacksonException.class)
                .isThrownBy(() -> portfolioServiceHelper.readValue(bytes, TestClass.class));
    }

    @Test
    void countTransactionsByUserFolioDTOList_ShouldReturnZero_WhenEmptyList() {
        List<UserFolioDTO> emptyFolios = Collections.emptyList();

        long result = portfolioServiceHelper.countTransactionsByUserFolioDTOList(emptyFolios);

        assertThat(result).isZero();
    }

    @Test
    void countTransactionsByUserFolioDTOList_ShouldReturnZero_WhenFoliosHaveNoTransactions() {
        UserSchemeDTO scheme1 = new UserSchemeDTO(
                "scheme1", null, 0L, null, null, null, null, null, null, null, null, Collections.emptyList());
        UserSchemeDTO scheme2 = new UserSchemeDTO(
                "scheme2", null, 0L, null, null, null, null, null, null, null, null, Collections.emptyList());
        UserFolioDTO folio1 = new UserFolioDTO("folio1", null, null, null, null, List.of(scheme1));
        UserFolioDTO folio2 = new UserFolioDTO("folio2", null, null, null, null, List.of(scheme2));
        List<UserFolioDTO> folios = Arrays.asList(folio1, folio2);

        long result = portfolioServiceHelper.countTransactionsByUserFolioDTOList(folios);

        assertThat(result).isZero();
    }

    @Test
    void countTransactionsByUserFolioDTOList_ShouldReturnCorrectCount_WhenFoliosHaveTransactions() {
        UserTransactionDTO transaction1 = mock(UserTransactionDTO.class);
        UserTransactionDTO transaction2 = mock(UserTransactionDTO.class);
        UserTransactionDTO transaction3 = mock(UserTransactionDTO.class);

        UserSchemeDTO scheme1 = new UserSchemeDTO(
                "scheme1",
                null,
                0L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Arrays.asList(transaction1, transaction2));
        UserSchemeDTO scheme2 = new UserSchemeDTO(
                "scheme2",
                null,
                0L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.singletonList(transaction3));
        UserFolioDTO folio1 = new UserFolioDTO("folio1", null, null, null, null, List.of(scheme1));
        UserFolioDTO folio2 = new UserFolioDTO("folio2", null, null, null, null, List.of(scheme2));
        List<UserFolioDTO> folios = Arrays.asList(folio1, folio2);

        long result = portfolioServiceHelper.countTransactionsByUserFolioDTOList(folios);

        assertThat(result).isEqualTo(3);
    }

    @Test
    void joinFutures_ShouldReturnEmptyList_WhenEmptyFuturesList() {
        List<CompletableFuture<String>> emptyFutures = Collections.emptyList();

        List<String> result = portfolioServiceHelper.joinFutures(emptyFutures);

        assertThat(result).isEmpty();
    }

    @Test
    void joinFutures_ShouldReturnJoinedValues_WhenFuturesProvided() {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("value1");
        CompletableFuture<String> future2 = CompletableFuture.completedFuture("value2");
        List<CompletableFuture<String>> futures = Arrays.asList(future1, future2);

        List<String> result = portfolioServiceHelper.joinFutures(futures);

        assertThat(result).containsExactlyElementsOf(Arrays.asList("value1", "value2"));
    }

    @Test
    void getPortfolioDetailsByPANAndAsOfDate_ShouldReturnEmptyList_WhenNoPortfolioDetails() {
        String panNumber = "PAN123";
        LocalDate asOfDate = LocalDate.of(2023, 12, 1);
        when(userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate))
                .thenReturn(Collections.emptyList());

        List<PortfolioDetailsDTO> result =
                portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(panNumber, asOfDate);

        assertThat(result).isEmpty();
        verify(userCASDetailsService).getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate);
    }

    @Test
    void getPortfolioDetailsByPANAndAsOfDate_ShouldReturnPortfolioDetails_WhenSuccessfulNavLookup() {
        String panNumber = "PAN123";
        LocalDate asOfDate = LocalDate.of(2023, 12, 1);

        when(portfolioDetailsProjection.getSchemeId()).thenReturn(123L);
        when(portfolioDetailsProjection.getBalanceUnits()).thenReturn(100.0);
        when(portfolioDetailsProjection.getSchemeName()).thenReturn("Test Scheme");
        when(portfolioDetailsProjection.getFolioNumber()).thenReturn("FOLIO123");

        MFSchemeDTO mfSchemeDTO = new MFSchemeDTO(null, 0L, "SCHEME123", "Test Scheme", "15.5", "2023-12-01", null);

        when(userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate))
                .thenReturn(Collections.singletonList(portfolioDetailsProjection));
        when(mfNavService.getNavByDateWithRetry(123L, asOfDate)).thenReturn(mfSchemeDTO);

        List<PortfolioDetailsDTO> result =
                portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(panNumber, asOfDate);

        assertThat(result).hasSize(1);
        PortfolioDetailsDTO dto = result.getFirst();
        assertThat(dto.totalValue()).isEqualTo(BigDecimal.valueOf(1550.0).setScale(4, RoundingMode.HALF_UP));
        assertThat(dto.schemeName()).isEqualTo("Test Scheme");
        assertThat(dto.folioNumber()).isEqualTo("FOLIO123");
        assertThat(dto.date()).isEqualTo("2023-12-01");
        assertThat(dto.xirr()).isZero();

        verify(mfNavService).getNavByDateWithRetry(123L, asOfDate);
    }

    @Test
    void getPortfolioDetailsByPANAndAsOfDate_ShouldHandleNavNotFoundException_WhenNavNotFound() {
        String panNumber = "PAN123";
        LocalDate asOfDate = LocalDate.of(2023, 12, 1);

        when(portfolioDetailsProjection.getSchemeId()).thenReturn(123L);
        when(portfolioDetailsProjection.getBalanceUnits()).thenReturn(100.0);
        when(portfolioDetailsProjection.getSchemeName()).thenReturn("Test Scheme");
        when(portfolioDetailsProjection.getFolioNumber()).thenReturn("FOLIO123");

        when(userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate))
                .thenReturn(List.of(portfolioDetailsProjection));
        when(mfNavService.getNavByDateWithRetry(123L, asOfDate))
                .thenThrow(new NavNotFoundException("Nav not found", asOfDate));

        List<PortfolioDetailsDTO> result =
                portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(panNumber, asOfDate);

        assertThat(result).hasSize(1);
        PortfolioDetailsDTO dto = result.getFirst();
        assertThat(dto.totalValue()).isEqualTo(BigDecimal.valueOf(1000.0));
        assertThat(dto.schemeName()).isEqualTo("Test Scheme");
        assertThat(dto.folioNumber()).isEqualTo("FOLIO123");
        assertThat(dto.date()).isEqualTo("2023-12-01");
        assertThat(dto.xirr()).isZero();

        verify(mfNavService).getNavByDateWithRetry(123L, asOfDate);
    }

    private record TestClass(String test) {
        // Test class for readValue method testing
    }
}
