package com.app.folioman.portfolio.repository;

import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.models.projection.MonthlyInvestmentResponse;
import com.app.folioman.portfolio.models.projection.YearlyInvestmentResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTransactionDetailsRepository extends JpaRepository<UserTransactionDetails, Long> {

    @Query(
            """
            select count (u.id) from UserTransactionDetails u
            where upper(u.userSchemeDetails.userFolioDetails.userCasDetails.investorInfo.email) = upper(:email) and u.userSchemeDetails.userFolioDetails.userCasDetails.investorInfo.name = :name and u.transactionDate >= :fromTransactionDate and u.transactionDate <= :toTransactionDate
            """)
    Long findAllTransactionByEmailAndNameAndInRange(
            @Param("email") String email,
            @Param("name") String name,
            @Param("fromTransactionDate") LocalDate fromTransactionDate,
            @Param("toTransactionDate") LocalDate toTransactionDate);

    List<UserTransactionDetails> findByUserSchemeDetails_IdAndTransactionDateBefore(Long id, LocalDate schemeFromDate);

    List<UserTransactionDetails> findByUserSchemeDetails_IdAndTransactionDateGreaterThanEqual(
            Long id, LocalDate schemeFromDate);

    @Query(
            nativeQuery = true,
            value =
                    """
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

    @Query(
            nativeQuery = true,
            value =
                    """
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
