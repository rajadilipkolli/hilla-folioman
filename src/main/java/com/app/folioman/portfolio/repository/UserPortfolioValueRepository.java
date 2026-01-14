package com.app.folioman.portfolio.repository;

import com.app.folioman.portfolio.entities.UserPortfolioValue;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPortfolioValueRepository extends JpaRepository<UserPortfolioValue, Long> {

    List<UserPortfolioValue> findByUserCasDetails_IdAndDateBetween(Long id, LocalDate firstDate, LocalDate lastDate);
}
