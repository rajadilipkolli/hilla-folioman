package com.example.application.portfolio.repository;

import com.example.application.portfolio.entities.UserFolioDetails;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserFolioDetailsRepository extends JpaRepository<UserFolioDetails, Long> {

    @Query(
            """
            select u from UserFolioDetails u left join u.userCasDetails.folios folios join fetch u.schemes where folios in :folios
            """)
    List<UserFolioDetails> findByUserCasDetails_FoliosIn(@Param("folios") List<UserFolioDetails> folios);
}
