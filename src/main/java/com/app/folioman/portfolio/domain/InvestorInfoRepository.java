package com.app.folioman.portfolio.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface InvestorInfoRepository extends JpaRepository<InvestorInfoEntity, Long> {

    boolean existsByEmailIgnoreCaseAndName(String email, String name);
}
