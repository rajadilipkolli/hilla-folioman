package com.app.folioman.portfolio.domain;

import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SchemeValueRepository extends JpaRepository<@NonNull SchemeValueEntity, @NonNull Long> {

    Optional<SchemeValueEntity> findFirstByUserSchemeDetailsEntity_UserFolioDetails_IdOrderByDateDesc(Long id);

    Optional<SchemeValueEntity> findFirstByUserSchemeDetailsEntity_IdAndDateBeforeOrderByDateDesc(
            Long id, LocalDate schemeFromDate);
}
