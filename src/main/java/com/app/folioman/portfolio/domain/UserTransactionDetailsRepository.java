package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.domain.models.projection.MonthlyInvestmentResponse;
import com.app.folioman.portfolio.domain.models.projection.YearlyInvestmentResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface UserTransactionDetailsRepository extends JpaRepository<UserTransactionDetailsEntity, Long> {

    @Query(
            "select min(u.transactionDate) from UserTransactionDetailsEntity u where u.userSchemeDetails.userFolioDetails.pan = :pan")
    Optional<LocalDate> findMinTransactionDateByPan(@Param("pan") String pan);

    @Query("""
            select count (u.id) from UserTransactionDetailsEntity u
            where upper(u.userSchemeDetails.userFolioDetails.userCasDetailsEntity.investorInfoEntity.email) = upper(:email)
                        and u.userSchemeDetails.userFolioDetails.userCasDetailsEntity.investorInfoEntity.name = :name
                                    and u.transactionDate >= :fromTransactionDate and u.transactionDate <= :toTransactionDate
            """)
    Long findAllTransactionByEmailAndNameAndInRange(
            @Param("email") String email,
            @Param("name") String name,
            @Param("fromTransactionDate") LocalDate fromTransactionDate,
            @Param("toTransactionDate") LocalDate toTransactionDate);

    List<UserTransactionDetailsEntity> findByUserSchemeDetails_IdAndTransactionDateBeforeOrderByTransactionDateAscIdAsc(
            Long id, LocalDate schemeFromDate);

    List<UserTransactionDetailsEntity> findByUserSchemeDetails_IdOrderByTransactionDateAscIdAsc(Long id);

    List<UserTransactionDetailsEntity> findByUserSchemeDetails_IdInOrderByTransactionDateAscIdAsc(List<Long> ids);

    List<UserTransactionDetailsEntity>
            findByUserSchemeDetails_IdAndTransactionDateGreaterThanEqualOrderByTransactionDateAscIdAsc(
                    Long id, LocalDate schemeFromDate);

    @Query(
            "select u from UserTransactionDetailsEntity u where u.userSchemeDetails.userFolioDetails.userCasDetailsEntity.id = :casId order by u.transactionDate asc, u.id asc")
    List<UserTransactionDetailsEntity> findByCasIdOrderByTransactionDateAscIdAsc(@Param("casId") Long casId);

    @NativeQuery("""
                    WITH monthly_totals AS (
                        SELECT DATE_TRUNC('month', transaction_date) AS month,
                               EXTRACT(YEAR FROM transaction_date) AS year,
                               EXTRACT(MONTH FROM transaction_date) AS month_number,
                               SUM(amount) AS total_invested
                        FROM portfolio.user_transaction_details utd
                        JOIN portfolio.user_scheme_details usd ON utd.user_scheme_detail_id = usd.id
                        JOIN portfolio.user_folio_details ufd ON ufd.id = usd.user_folio_id
                        WHERE ufd.pan = ?1
                        GROUP BY DATE_TRUNC('month', transaction_date),
                                 EXTRACT(YEAR FROM transaction_date),
                                 EXTRACT(MONTH FROM transaction_date)
                    )
                    SELECT year,
                           month_number,
                           total_invested AS investmentPerMonth,
                           SUM(total_invested) OVER (ORDER BY month) AS cumulativeInvestment
                    FROM monthly_totals
                    ORDER BY year, month_number
                    """)
    List<MonthlyInvestmentResponse> findMonthlyInvestmentsByPan(String pan);

    @NativeQuery("""
                    SELECT EXTRACT(YEAR FROM transaction_date) AS year,
                           SUM(amount) AS yearlyInvestment
                    FROM portfolio.user_transaction_details utd
                    JOIN portfolio.user_scheme_details usd ON utd.user_scheme_detail_id = usd.id
                    JOIN portfolio.user_folio_details ufd ON ufd.id = usd.user_folio_id
                    WHERE ufd.pan = ?1
                    GROUP BY EXTRACT(YEAR FROM transaction_date)
                    ORDER BY year
                    """)
    List<YearlyInvestmentResponse> findYearlyInvestmentsByPan(String pan);
}
