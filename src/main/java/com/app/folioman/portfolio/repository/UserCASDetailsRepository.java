package com.app.folioman.portfolio.repository;

import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.models.projection.PortfolioDetailsProjection;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCASDetailsRepository extends JpaRepository<UserCASDetails, Long> {

    @Query(
            """
              select u from UserCASDetails u join fetch u.folios join fetch u.investorInfo as i
              where i.email = :email and i.name = :name
              """)
    UserCASDetails findByInvestorEmailAndName(@Param("email") String email, @Param("name") String name);

    @NativeQuery(
            """
            WITH tempView AS (
                SELECT utd.balance AS balance,
                       usd.scheme AS schemeName,
                       usd.amfi AS schemeId,
                       ufd.folio AS folioNumber,
                       utd.user_scheme_detail_id AS schemeDetailId,
                       ROW_NUMBER() OVER (
                           PARTITION BY utd.user_scheme_detail_id
                           ORDER BY utd.transaction_date DESC,
                           CASE
                               WHEN utd.type IN ('REDEMPTION', 'SWITCH_OUT')
                                   THEN balance
                               ELSE balance * -1
                           END ASC
                       ) AS row_number
                FROM portfolio.user_transaction_details utd
                JOIN portfolio.user_scheme_details usd
                    ON utd.user_scheme_detail_id = usd.id
                JOIN portfolio.user_folio_details ufd
                    ON usd.user_folio_id = ufd.id
                WHERE utd.type NOT IN ('STAMP_DUTY_TAX', '*** Stamp Duty ***', 'STT_TAX')
                  AND ufd.pan = :pan
                  AND utd.transaction_date <= :asOfDate
            )
            SELECT SUM(balance) AS balanceUnits,
                   schemeName,
                   schemeId,
                   folioNumber,
                   schemeDetailId
            FROM tempView
            WHERE row_number = 1
              AND balance <> 0
            GROUP BY schemeName, schemeId, folioNumber, schemeDetailId
            """)
    List<PortfolioDetailsProjection> getPortfolioDetails(
            @Param("pan") String panNumber, @Param("asOfDate") LocalDate asOfDate);
}
