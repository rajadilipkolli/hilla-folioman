package com.app.folioman.mfschemes.repository;

import com.app.folioman.mfschemes.entities.MFSchemeNav;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MFSchemeNavRepository extends JpaRepository<MFSchemeNav, Long> {

    @Query(
            """
      -- Select distinct AMFI codes for schemes without recent NAV entries
      SELECT DISTINCT
        ms1.amfiCode
      FROM
        MFSchemeNav mn1
        JOIN mn1.mfScheme ms1
      WHERE
        -- Subquery to check if there are no NAV entries on or after the specified date
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
}
