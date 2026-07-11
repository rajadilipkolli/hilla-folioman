package com.app.folioman.mfschemes.domain;

import com.app.folioman.mfschemes.domain.models.projection.NavDateValueProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeNavProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface MfSchemeNavRepository extends JpaRepository<MFSchemeNavEntity, Long> {

    @Query("""
            SELECT DISTINCT
              ms1.amfiCode
            FROM
              MFSchemeNavEntity mn1
              JOIN mn1.mfFundSchemeEntity ms1
            WHERE
              NOT EXISTS (
                SELECT
                  1
                FROM
                  MFSchemeNavEntity mn2
                WHERE
                  mn2.mfFundSchemeEntity.id = mn1.mfFundSchemeEntity.id
                  AND mn2.navDate >=:asOfDate
              )
            """)
    List<Long> findMFSchemeNavsByNavNotLoaded(@Param("asOfDate") LocalDate asOfDate);

    @Query("""
            select new com.app.folioman.mfschemes.rest.dtos.MFSchemeNavProjection(m.nav, m.navDate, m.mfFundSchemeEntity.amfiCode)
            from MFSchemeNavEntity m
            where m.mfFundSchemeEntity.amfiCode in :amfiCodes and m.navDate >= :startNavDate and m.navDate <= :endNavDate
            order by m.mfFundSchemeEntity.amfiCode , m.navDate
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
            "SELECT new com.app.folioman.mfschemes.domain.models.projection.NavDateValueProjection(n.nav, n.navDate) FROM MFSchemeNavEntity n WHERE n.mfFundSchemeEntity.id = :schemeId")
    List<NavDateValueProjection> findAllNavDateValuesBySchemeId(@Param("schemeId") Long schemeId);

    @Query(value = """
            SELECT nav, nav_date as navDate, amfi_code as amfiCode FROM (
                SELECT msn.nav, msn.nav_date, mfs.amfi_code,
                       ROW_NUMBER() OVER (PARTITION BY mfs.amfi_code ORDER BY msn.nav_date DESC, msn.id DESC) as rn
                FROM mfschemes.mf_scheme_nav msn
                JOIN mfschemes.mf_fund_scheme mfs ON msn.mf_scheme_id = mfs.id
                WHERE mfs.amfi_code IN :amfiCodes
            ) sub
            WHERE sub.rn <= 2
            """, nativeQuery = true)
    List<AmfiNavProjection> findLatest2NavsByAmfiCodes(@Param("amfiCodes") Set<Long> amfiCodes);

    interface AmfiNavProjection {
        java.math.BigDecimal getNav();

        LocalDate getNavDate();

        Long getAmfiCode();
    }
}
