package com.example.application.portfolio.repository;

import com.example.application.portfolio.entities.UserFolioDetails;
import com.example.application.portfolio.models.projection.UserFolioDetailsPanProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface UserFolioDetailsRepository extends JpaRepository<UserFolioDetails, Long> {

    @Query(
            """
            select u from UserFolioDetails u left join u.userCasDetails.folios folios join fetch u.schemes where folios in :folios
            """)
    List<UserFolioDetails> findByUserCasDetails_FoliosIn(@Param("folios") List<UserFolioDetails> folios);

    UserFolioDetailsPanProjection findFirstByUserCasDetails_IdAndPanKyc(Long userCasID, String kycStatus);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("update UserFolioDetails set pan = :pan where panKyc = 'NOT OK' and userCasDetails.id = :casId")
    int updatePanByCasId(@Param("pan") String pan, @Param("casId") Long casId);
}
