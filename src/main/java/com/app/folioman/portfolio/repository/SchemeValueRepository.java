package com.app.folioman.portfolio.repository;

import com.app.folioman.portfolio.entities.SchemeValue;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchemeValueRepository extends JpaRepository<SchemeValue, Long> {

    SchemeValue findFirstByUserSchemeDetails_UserFolioDetails_IdOrderByDateDesc(Long id);

    Optional<SchemeValue> findFirstByUserSchemeDetails_IdAndDateBeforeOrderByDateDesc(
            Long id, LocalDate schemeFromDate);
}
