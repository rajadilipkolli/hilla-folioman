package com.app.folioman.mfschemes.repository;

import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.shared.FundDetailProjection;
import com.app.folioman.shared.MFSchemeProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MfFundSchemeRepository extends JpaRepository<MfFundScheme, Long> {

    @Query("select distinct m.isin from MfFundScheme m")
    List<String> findDistinctIsin();

    @Query("select o.amfiCode from MfFundScheme o")
    List<Long> findAllSchemeIds();

    @Query(
            """
            select new com.app.folioman.shared.FundDetailProjection(m.amfiCode, m.name, m.amc.name) from MfFundScheme m
             where m.name like :schemeName order by m.amfiCode
            """)
    List<FundDetailProjection> findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(@Param("schemeName") String schemeName);

    @Query(
            """
            select m from MfFundScheme m inner join fetch m.mfSchemeNavs mfSchemeNavs
            where m.amfiCode = :schemeCode and mfSchemeNavs.navDate = :date
            """)
    @EntityGraph(attributePaths = {"mfSchemeType"})
    Optional<MfFundScheme> findBySchemeIdAndMfSchemeNavs_NavDate(
            @Param("schemeCode") Long schemeCode, @Param("date") LocalDate navDate);

    @EntityGraph(attributePaths = {"mfSchemeType", "mfSchemeNavs"})
    Optional<MfFundScheme> findByAmfiCode(Long amfiCode);

    Optional<MFSchemeProjection> findByIsin(String isin);

    @Query("select distinct m.amfiCode from MfFundScheme m")
    List<String> findDistinctAmfiCode();

    Optional<MFSchemeProjection> findByRtaCodeStartsWith(String rtaCode);
}
