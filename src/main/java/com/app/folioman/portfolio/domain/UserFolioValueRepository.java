package com.app.folioman.portfolio.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserFolioValueRepository extends JpaRepository<UserFolioValueEntity, Long> {

    Optional<UserFolioValueEntity> findFirstByUserFolioDetailsEntity_IdOrderByDateDesc(Long folioId);

    List<UserFolioValueEntity> findByUserFolioDetailsEntity_IdAndDateBetween(
            Long folioId, LocalDate startDate, LocalDate endDate);

    @Modifying
    @Query(
            nativeQuery = true,
            value = "INSERT INTO portfolio.user_folio_value (id, date, invested, value, user_folio_details_id, "
                    + "created_at, updated_at, version) VALUES (nextval('portfolio.user_folio_value_seq'), "
                    + ":date, :invested, :value, :folioId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0) "
                    + "ON CONFLICT (date, user_folio_details_id) DO UPDATE SET "
                    + "invested = EXCLUDED.invested, value = EXCLUDED.value, "
                    + "updated_at = CURRENT_TIMESTAMP, version = portfolio.user_folio_value.version + 1")
    void upsertFolioValue(
            @Param("folioId") Long folioId,
            @Param("date") LocalDate date,
            @Param("invested") BigDecimal invested,
            @Param("value") BigDecimal value);
}
