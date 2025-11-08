package com.app.folioman.mfschemes.repository;

import com.app.folioman.mfschemes.MFSchemeNavProjection;
import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.models.projection.NavDateValueProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MFSchemeNavRepository extends JpaRepository<MFSchemeNav, Long> {

    @Query(
            """
            SELECT DISTINCT
              ms1.amfiCode
            FROM
              MFSchemeNav mn1
              JOIN mn1.mfScheme ms1
            WHERE
              NOT EXISTS (
                SELECT
                  1
                FROM
                  MFSchemeNav mn2
                WHERE
                  mn2.mfScheme.id = mn1.mfScheme.id
                  AND mn2.navDate >=:asOfDate
              )
            """)
    List<Long> findMFSchemeNavsByNavNotLoaded(@Param("asOfDate") LocalDate asOfDate);

    @Query(
            """
            select new com.app.folioman.mfschemes.MFSchemeNavProjection(m.nav, m.navDate, m.mfScheme.amfiCode)
            from MFSchemeNav m
            where m.mfScheme.amfiCode in :amfiCodes and m.navDate >= :startNavDate and m.navDate <= :endNavDate
            order by m.mfScheme.amfiCode , m.navDate
            """)
    List<MFSchemeNavProjection> findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
            @Param("amfiCodes") Set<Long> amfiCodes,
            @Param("startNavDate") LocalDate startNavDate,
            @Param("endNavDate") LocalDate endNavDate);

    /**
     * Find all NAVs for a scheme with their date and value
     * Used for batch processing to avoid individual existence checks
     */
    @Query(
            "SELECT new com.app.folioman.mfschemes.models.projection.NavDateValueProjection(n.nav, n.navDate) FROM MFSchemeNav n WHERE n.mfScheme.id = :schemeId")
    List<NavDateValueProjection> findAllNavDateValuesBySchemeId(@Param("schemeId") Long schemeId);
}
