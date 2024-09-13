package com.example.application.portfolio.repository;

import com.example.application.portfolio.entities.UserSchemeDetails;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSchemeDetailsRepository extends JpaRepository<UserSchemeDetails, Long> {

    @Query(
            """
        select u from UserSchemeDetails u left join u.userFolioDetails.schemes schemes join fetch u.transactions where schemes in :schemes
        """)
    List<UserSchemeDetails> findByUserFolioDetails_SchemesIn(@Param("schemes") List<UserSchemeDetails> schemes);
}
