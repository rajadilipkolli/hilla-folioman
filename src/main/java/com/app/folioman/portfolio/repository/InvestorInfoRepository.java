package com.app.folioman.portfolio.repository;

import com.app.folioman.portfolio.entities.InvestorInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorInfoRepository extends JpaRepository<InvestorInfo, Long> {

    boolean existsByEmailAndName(String email, String name);
}
