package com.app.folioman.mfschemes.repository;

import com.app.folioman.mfschemes.FundDetailProjection;
import com.app.folioman.mfschemes.MFSchemeProjection;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
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
            value =
                    """
            SELECT m.name as schemeName, m.amfi_code as amfiCode, a.name as amcName
            FROM mfschemes.mf_fund_scheme m
            JOIN mfschemes.mf_amc a ON m.mf_amc_id = a.id
            WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))
            order by m.amfi_code
            """,
            nativeQuery = true)
    List<FundDetailProjection> searchByAmc(@Param("query") String query);

    /**
     * Search schemes by AMC name using PostgreSQL text search
     *
     * @param searchTerms the search terms in PostgreSQL ts_query format (term1 & term2 & ...)
     * @return List of fund details matching the AMC search
     */
    @Query(
            value =
                    """
            SELECT m.name as schemeName, m.amfi_code as amfiCode, a.name as amcName
            FROM mfschemes.mf_fund_scheme m
            JOIN mfschemes.mf_amc a ON m.mf_amc_id = a.id
            WHERE a.name_vector @@ to_tsquery('english', :searchTerms)
            ORDER BY ts_rank(a.name_vector, to_tsquery('english', :searchTerms)) DESC, m.amfi_code
            """,
            nativeQuery = true)
    List<FundDetailProjection> searchByAmcTextSearch(@Param("searchTerms") String searchTerms);

    @Query(
            """
            select m from MfFundScheme m inner join fetch m.mfSchemeNavs mfSchemeNavs
            where m.amfiCode = :schemeCode and mfSchemeNavs.navDate = :date
            """)
    @EntityGraph(attributePaths = {"amc", "mfSchemeType"})
    Optional<MfFundScheme> findBySchemeIdAndMfSchemeNavs_NavDate(
            @Param("schemeCode") Long schemeCode, @Param("date") LocalDate navDate);

    @EntityGraph(attributePaths = {"amc", "mfSchemeType", "mfSchemeNavs"})
    MfFundScheme findByAmfiCode(@Param("amfiCode") Long amfiCode);

    boolean existsByAmfiCode(Long amfiCode);

    Optional<MFSchemeProjection> findByIsin(String isin);

    @Query("select distinct m.amfiCode from MfFundScheme m")
    List<String> findDistinctAmfiCode();

    List<MFSchemeProjection> findByRtaCodeStartsWith(String rtaCode);

    MfFundScheme getReferenceByAmfiCode(Long amfiCode);
}
