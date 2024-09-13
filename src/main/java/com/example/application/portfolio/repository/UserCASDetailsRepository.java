package com.example.application.portfolio.repository;

import com.example.application.portfolio.entities.UserCASDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCASDetailsRepository extends JpaRepository<UserCASDetails, Long> {

    @Query(
            """
              select u from UserCASDetails u join fetch u.folios join fetch u.investorInfo as i
              where i.email = :email and i.name = :name
              """)
    UserCASDetails findByInvestorEmailAndName(@Param("email") String email, @Param("name") String name);
}
