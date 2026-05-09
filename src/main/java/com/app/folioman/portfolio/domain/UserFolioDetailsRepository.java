package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.domain.models.projection.UserFolioDetailsPanProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
interface UserFolioDetailsRepository extends JpaRepository<UserFolioDetailsEntity, Long> {

    @Query("""
            select u from UserFolioDetailsEntity u left join u.userCasDetailsEntity.folios folios join fetch u.schemes where folios in :folios
            """)
    List<UserFolioDetailsEntity> findByUserCasDetails_FoliosIn(@Param("folios") List<UserFolioDetailsEntity> folios);

    Optional<UserFolioDetailsPanProjection> findFirstByUserCasDetailsEntity_IdAndPanKyc(
            Long userCasID, String kycStatus);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("update UserFolioDetailsEntity set pan = :pan where panKyc = 'NOT OK' and userCasDetailsEntity.id = :casId")
    int updatePanByCasId(@Param("pan") String pan, @Param("casId") Long casId);
}
