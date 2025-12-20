package com.app.folioman.portfolio.repository;

import com.app.folioman.portfolio.entities.UserSchemeDetails;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserSchemeDetailsRepository extends JpaRepository<UserSchemeDetails, Long> {

    @Query("""
            select u from UserSchemeDetails u left join u.userFolioDetails.schemes schemes join fetch u.transactions where schemes in :schemes
            """)
    List<UserSchemeDetails> findByUserFolioDetails_SchemesIn(@Param("schemes") List<UserSchemeDetails> schemes);

    List<UserSchemeDetails> findByAmfiIsNull();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("update UserSchemeDetails u set u.amfi = :amfi, u.isin = :isin where u.id = :id")
    void updateAmfiAndIsinById(@Param("amfi") Long schemeId, @Param("isin") String isin, @Param("id") Long id);
}
