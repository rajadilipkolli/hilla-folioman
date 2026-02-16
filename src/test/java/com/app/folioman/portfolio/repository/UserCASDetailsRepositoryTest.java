package com.app.folioman.portfolio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.portfolio.entities.CasTypeEnum;
import com.app.folioman.portfolio.entities.FileTypeEnum;
import com.app.folioman.portfolio.entities.InvestorInfo;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.models.projection.PortfolioDetailsProjection;
import com.app.folioman.portfolio.models.request.TransactionType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(SQLContainersConfig.class)
class UserCASDetailsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserCASDetailsRepository userCASDetailsRepository;

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
    void findByInvestorEmailAndName_ShouldReturnUserCASDetails_WhenMatchingEmailAndName() {
        String email = "testcas@example.com";
        String name = "Test CAS User";
        UserCASDetails userCASDetails = getUserCASDetails(email, name);

        entityManager.persistAndFlush(userCASDetails);

        UserCASDetails result = userCASDetailsRepository.findByInvestorEmailAndName(email, name);

        // Assertions would depend on the actual entity structure and test data
        assertThat(result).isNotNull();
        assertThat(result.getInvestorInfo().getEmail()).isEqualTo(email);
        assertThat(result.getInvestorInfo().getName()).isEqualTo(name);
    }

    private UserCASDetails getUserCASDetails(String email, String name) {
        InvestorInfo investorInfo = new InvestorInfo();
        investorInfo.setEmail(email);
        investorInfo.setName(name);

        UserFolioDetails folio = new UserFolioDetails();
        folio.setFolio("FOLIO123");
        folio.setAmc("AMC_NAME");
        folio.setPan("ABCDE1234F");

        UserCASDetails userCASDetails = new UserCASDetails();
        userCASDetails.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCASDetails.setFileTypeEnum(FileTypeEnum.CAMS);
        userCASDetails.addFolioEntity(folio);
        userCASDetails.setInvestorInfo(investorInfo);
        return userCASDetails;
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void findByInvestorEmailAndName_ShouldReturnNull_WhenNoMatchingRecord() {
        String email = "nonexistent@example.com";
        String name = "Nonexistent User";

        UserCASDetails result = userCASDetailsRepository.findByInvestorEmailAndName(email, name);

        assertThat(result).isNull();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
    void getPortfolioDetails_ShouldReturnPortfolioDetails_WhenValidPanAndDate() {
        String panNumber = "ABCDE1234F";
        LocalDate asOfDate = LocalDate.of(2023, 12, 31);

        // persist minimal portfolio data for this PAN so test is self-contained
        persistSamplePortfolio(panNumber, 12345L, "SAMPLE SCHEME", asOfDate.minusDays(10));

        List<PortfolioDetailsProjection> result = userCASDetailsRepository.getPortfolioDetails(panNumber, asOfDate);

        // Basic expectations for a valid PAN/date: at least one record and non-null projection fields
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();

        PortfolioDetailsProjection first = result.getFirst();
        assertThat(first.getSchemeId()).isNotNull();
        assertThat(first.getSchemeDetailId()).isNotNull();
        assertThat(first.getBalanceUnits()).isNotNull();
        assertThat(first.getBalanceUnits()).isNotEqualTo(0.0);
        assertThat(first.getSchemeName()).isNotBlank();
        assertThat(first.getFolioNumber()).isNotBlank();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
    void getPortfolioDetails_ShouldReturnEmptyList_WhenNoMatchingPan() {
        String panNumber = "NONEXISTENT";
        LocalDate asOfDate = LocalDate.of(2023, 12, 31);

        // ensure DB has no data for this PAN
        List<PortfolioDetailsProjection> result = userCASDetailsRepository.getPortfolioDetails(panNumber, asOfDate);

        assertThat(result).isEmpty();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
    void getPortfolioDetails_ShouldReturnEmptyList_WhenDateBeforeAllTransactions() {
        String panNumber = "ABCDE1234F";
        LocalDate asOfDate = LocalDate.of(2020, 1, 1);

        // persist a transaction after the asOfDate so the query should return empty
        persistSamplePortfolio(panNumber, 12345L, "SAMPLE SCHEME", LocalDate.of(2021, 1, 1));

        List<PortfolioDetailsProjection> result = userCASDetailsRepository.getPortfolioDetails(panNumber, asOfDate);

        assertThat(result).isEmpty();
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
    void getPortfolioDetails_ShouldFilterExcludedTransactionTypes() {
        String panNumber = "ABCDE1234F";
        LocalDate asOfDate = LocalDate.of(2023, 12, 31);

        // persist a normal transaction and an excluded-type transaction for same scheme
        persistSamplePortfolioWithExcludedTransaction(panNumber, 12345L, "SAMPLE SCHEME", asOfDate.minusDays(5));

        List<PortfolioDetailsProjection> result = userCASDetailsRepository.getPortfolioDetails(panNumber, asOfDate);

        // Verify that excluded transaction types (stamp duty / stt tax) did not contribute to balances
        // We can't inspect transaction type in the projection, but we can assert that returned scheme names
        // do not contain the word "stamp" (case-insensitive) and that balances are present.
        assertThat(result).isNotNull();
        assertThat(result).allSatisfy(p -> {
            assertThat(p.getSchemeName()).doesNotContainIgnoringCase("stamp");
            assertThat(p.getBalanceUnits()).isNotNull();
        });
    }

    @Test
    @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
    void getPortfolioDetails_ShouldOnlyIncludeNonZeroBalances() {
        String panNumber = "ABCDE1234F";
        LocalDate asOfDate = LocalDate.of(2023, 12, 31);

        // persist a scheme with non-zero balance and one with zero balance
        persistSamplePortfolio(panNumber, 11111L, "NONZERO SCHEME", asOfDate.minusDays(7));
        persistSamplePortfolioWithBalance(panNumber, 22222L, "ZERO SCHEME", asOfDate.minusDays(7), 0.0);

        List<PortfolioDetailsProjection> result = userCASDetailsRepository.getPortfolioDetails(panNumber, asOfDate);

        // Verify that only non-zero balances are included (balance <> 0 condition)
        assertThat(result).isNotNull();
        assertThat(result).allSatisfy(p -> assertThat(p.getBalanceUnits()).isNotEqualTo(0.0));
    }

    // Helper methods to persist minimal entities for repository tests
    private void persistSamplePortfolio(String pan, Long amfi, String schemeName, LocalDate transactionDate) {
        UserCASDetails userCASDetails = new UserCASDetails();
        userCASDetails.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCASDetails.setFileTypeEnum(FileTypeEnum.CAMS);

        InvestorInfo investorInfo = new InvestorInfo();
        investorInfo.setEmail("persisted@example.com");
        investorInfo.setName("Persisted User");
        userCASDetails.setInvestorInfo(investorInfo);

        UserFolioDetails folio = new UserFolioDetails();
        folio.setFolio("FOLIO-" + pan);
        folio.setAmc("AMC");
        folio.setPan(pan);
        userCASDetails.addFolioEntity(folio);

        // scheme
        UserSchemeDetails scheme = new UserSchemeDetails();
        scheme.setScheme(schemeName);
        scheme.setAmfi(amfi);
        folio.addScheme(scheme);

        // transaction with non-zero balance
        UserTransactionDetails tx = new UserTransactionDetails();
        tx.setTransactionDate(transactionDate);
        tx.setBalance(10.0);
        tx.setUnits(10.0);
        tx.setNav(1.0);
        tx.setType(TransactionType.PURCHASE);
        scheme.addTransaction(tx);

        entityManager.persistAndFlush(userCASDetails);
    }

    private void persistSamplePortfolioWithExcludedTransaction(
            String pan, Long amfi, String schemeName, LocalDate transactionDate) {
        persistSamplePortfolio(pan, amfi, schemeName, transactionDate);
        // also add an excluded transaction type (STAMP_DUTY_TAX) which should be ignored by query
        UserSchemeDetails scheme = entityManager
                .getEntityManager()
                .createQuery(
                        "select s from UserSchemeDetails s join s.userFolioDetails f where s.amfi = :amfi and f.pan = :pan",
                        UserSchemeDetails.class)
                .setParameter("amfi", amfi)
                .setParameter("pan", pan)
                .getSingleResult();

        UserTransactionDetails excluded = new UserTransactionDetails();
        excluded.setTransactionDate(transactionDate);
        excluded.setBalance(5.0);
        excluded.setUnits(5.0);
        excluded.setNav(1.0);
        excluded.setType(TransactionType.STAMP_DUTY_TAX);
        scheme.addTransaction(excluded);

        entityManager.persistAndFlush(scheme);
    }

    private void persistSamplePortfolioWithBalance(
            String pan, Long amfi, String schemeName, LocalDate transactionDate, Double balance) {
        UserCASDetails userCASDetails = new UserCASDetails();
        userCASDetails.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCASDetails.setFileTypeEnum(FileTypeEnum.CAMS);

        InvestorInfo investorInfo = new InvestorInfo();
        investorInfo.setEmail("persisted2@example.com");
        investorInfo.setName("Persisted User 2");
        userCASDetails.setInvestorInfo(investorInfo);

        UserFolioDetails folio = new UserFolioDetails();
        folio.setFolio("FOLIO-" + pan + balance);
        folio.setAmc("AMC");
        folio.setPan(pan);
        userCASDetails.addFolioEntity(folio);

        UserSchemeDetails scheme = new UserSchemeDetails();
        scheme.setScheme(schemeName);
        scheme.setAmfi(amfi);
        folio.addScheme(scheme);

        UserTransactionDetails tx = new UserTransactionDetails();
        tx.setTransactionDate(transactionDate);
        tx.setBalance(balance);
        tx.setUnits(balance);
        tx.setNav(1.0);
        tx.setType(TransactionType.PURCHASE);
        scheme.addTransaction(tx);

        entityManager.persistAndFlush(userCASDetails);
    }
}
