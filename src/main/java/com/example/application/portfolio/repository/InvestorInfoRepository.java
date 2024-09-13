package com.example.application.portfolio.repository;

import com.example.application.portfolio.entities.InvestorInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorInfoRepository extends JpaRepository<InvestorInfo, Long> {

    boolean existsByEmailAndName(String email, String name);
}
