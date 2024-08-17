package com.example.application.mfschemes.repository;

import com.example.application.mfschemes.entities.MFScheme;
import com.example.application.mfschemes.models.projection.FundDetailProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MFSchemeRepository extends JpaRepository<MFScheme, Long> {

    @Query("select o.schemeId from MFScheme o")
    List<Long> findAllSchemeIds();

    @Query(
            """
            select new com.example.application.mfschemes.models.projection.FundDetailProjection(m.schemeId, m.schemeName, m.fundHouse) from MFScheme m
             where UPPER(REPLACE(m.schemeName, '- ', '')) like upper(:schemeName) order by m.schemeId
            """)
    List<FundDetailProjection> findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(@Param("schemeName") String schemeName);
}
