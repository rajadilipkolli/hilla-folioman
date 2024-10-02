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

    @Query("select o.amfiCode from MfFundScheme o")
    List<Long> findAllSchemeIds();

    @Query(
            value =
                    """
            SELECT m.name as schemeName, m.amfi_code as amfiCode, a.name as amcName
            FROM mfschemes.mf_fund_scheme m
            JOIN mfschemes.mf_amc a ON m.mf_amc_id = a.id
            WHERE m.name_tsv @@ plainto_tsquery('english', :query)
            order by m.amfi_code
            """,
            nativeQuery = true)
    List<FundDetailProjection> searchByFullText(@Param("query") String query);

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
