package com.app.folioman.portfolio.domain;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserFolioValueRepository extends JpaRepository<UserFolioValueEntity, Long> {

    List<UserFolioValueEntity> findByUserFolioDetailsEntity_IdInAndDateBetween(
            Collection<Long> folioIds, LocalDate startDate, LocalDate endDate);
}
