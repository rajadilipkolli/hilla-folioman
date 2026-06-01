package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.domain.models.projection.PortfolioValueDateProjection;
import com.app.folioman.portfolio.domain.models.projection.UserPortfolioValueProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface UserPortfolioValueRepository extends JpaRepository<UserPortfolioValueEntity, Long> {

    List<UserPortfolioValueEntity> findByUserCasDetailsEntity_IdAndDateBetween(
            Long id, LocalDate firstDate, LocalDate lastDate);

    @NativeQuery("""
            SELECT upv.xirr, upv.live_xirr as liveXirr, upv.invested, upv.value, upv.date
            FROM portfolio.user_portfolio_value upv
            JOIN portfolio.user_cas_details ucd ON upv.user_cas_details_id = ucd.id
            JOIN portfolio.user_folio_details ufd ON ufd.user_cas_details_id = ucd.id
            WHERE ufd.pan = :pan
            ORDER BY upv.date DESC, upv.id DESC
            LIMIT 1
            """)
    Optional<UserPortfolioValueProjection> getLatestPortfolioValueByPan(@Param("pan") String pan);

    @NativeQuery("""
            SELECT upv.value as value, upv.date as date, upv.xirr as xirr
            FROM portfolio.user_portfolio_value upv
            WHERE upv.user_cas_details_id = :casId
            ORDER BY upv.date DESC, upv.id DESC
            LIMIT 1
            """)
    Optional<PortfolioValueDateProjection> getLatestPortfolioValueByCasId(@Param("casId") Long casId);
}
