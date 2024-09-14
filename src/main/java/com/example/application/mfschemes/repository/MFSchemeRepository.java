package com.example.application.mfschemes.repository;

import com.example.application.mfschemes.entities.MFScheme;
import com.example.application.shared.FundDetailProjection;
import com.example.application.shared.MFSchemeProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MFSchemeRepository extends JpaRepository<MFScheme, Long> {

    @Query("select o.schemeId from MFScheme o")
    List<Long> findAllSchemeIds();

    @Query(
            """
            select new com.example.application.shared.FundDetailProjection(m.schemeId, m.schemeName, m.fundHouse) from MFScheme m
             where m.schemeNameAlias like :schemeName order by m.schemeId
            """)
    List<FundDetailProjection> findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(@Param("schemeName") String schemeName);

    @Query(
            """
            select m from MFScheme m inner join fetch m.mfSchemeNavs mfSchemeNavs
            where m.schemeId = :schemeCode and mfSchemeNavs.navDate = :date
            """)
    @EntityGraph(attributePaths = {"mfSchemeType"})
    Optional<MFScheme> findBySchemeIdAndMfSchemeNavs_NavDate(
            @Param("schemeCode") Long schemeCode, @Param("date") LocalDate navDate);

    @EntityGraph(attributePaths = {"mfSchemeType", "mfSchemeNavs"})
    Optional<MFScheme> findBySchemeId(@Param("schemeId") Long schemeId);

    Optional<MFSchemeProjection> findByPayOut(String payOut);
}
