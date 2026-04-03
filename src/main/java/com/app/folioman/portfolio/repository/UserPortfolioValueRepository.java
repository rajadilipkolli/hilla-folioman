package com.app.folioman.portfolio.repository;

import com.app.folioman.portfolio.entities.UserPortfolioValue;
import com.app.folioman.portfolio.models.projection.UserPortfolioValueProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPortfolioValueRepository extends JpaRepository<UserPortfolioValue, Long> {

    List<UserPortfolioValue> findByUserCasDetails_IdAndDateBetween(Long id, LocalDate firstDate, LocalDate lastDate);

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
}
