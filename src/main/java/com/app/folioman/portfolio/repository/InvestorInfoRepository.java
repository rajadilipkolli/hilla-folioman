package com.app.folioman.portfolio.repository;

import com.app.folioman.portfolio.entities.InvestorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestorInfoRepository extends JpaRepository<InvestorInfo, Long> {

    boolean existsByEmailAndName(String email, String name);
}
