package com.app.folioman.portfolio.domain;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
interface UserSchemeDetailsRepository extends JpaRepository<UserSchemeDetailsEntity, Long> {

    @Query("""
            select u from UserSchemeDetailsEntity u left join u.userFolioDetails.schemes schemes join fetch u.transactions where schemes in :schemes
            """)
    List<UserSchemeDetailsEntity> findByUserFolioDetails_SchemesIn(
            @Param("schemes") List<UserSchemeDetailsEntity> schemes);

    List<UserSchemeDetailsEntity> findByAmfiIsNull();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("update UserSchemeDetailsEntity u set u.amfi = :amfi, u.isin = :isin where u.id = :id")
    void updateAmfiAndIsinById(
            @Nullable @Param("amfi") Long schemeId, @Nullable @Param("isin") String isin, @Param("id") Long id);
}
