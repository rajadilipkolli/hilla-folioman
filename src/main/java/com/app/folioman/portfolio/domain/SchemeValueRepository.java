package com.app.folioman.portfolio.domain;

import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
interface SchemeValueRepository extends JpaRepository<@NonNull SchemeValueEntity, @NonNull Long> {

    Optional<SchemeValueEntity> findFirstByUserSchemeDetailsEntity_UserFolioDetails_IdOrderByDateDesc(Long id);

    Optional<SchemeValueEntity> findFirstByUserSchemeDetailsEntity_IdAndDateBeforeOrderByDateDesc(
            Long id, LocalDate schemeFromDate);

    @Query("""
            SELECT sv FROM SchemeValueEntity sv
            WHERE sv.userSchemeDetailsEntity.id IN :schemeIds
              AND sv.date = (SELECT MAX(sv2.date) FROM SchemeValueEntity sv2 WHERE sv2.userSchemeDetailsEntity.id = sv.userSchemeDetailsEntity.id)
            """)
    List<SchemeValueEntity> findLatestValuesBySchemeDetailsIds(@Param("schemeIds") Collection<Long> schemeIds);
}
