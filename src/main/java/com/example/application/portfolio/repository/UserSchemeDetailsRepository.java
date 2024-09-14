package com.example.application.portfolio.repository;

import com.example.application.portfolio.entities.UserSchemeDetails;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface UserSchemeDetailsRepository extends JpaRepository<UserSchemeDetails, Long> {

    @Query(
            """
        select u from UserSchemeDetails u left join u.userFolioDetails.schemes schemes join fetch u.transactions where schemes in :schemes
        """)
    List<UserSchemeDetails> findByUserFolioDetails_SchemesIn(@Param("schemes") List<UserSchemeDetails> schemes);

    List<UserSchemeDetails> findByAmfiIsNull();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("update UserSchemeDetails u set u.amfi = :amfi, u.isin = :isin where u.id = :id")
    void updateAmfiAndIsinById(@Param("amfi") Long schemeId, @Param("isin") String isin, @Param("id") Long id);

    @Query(
            value =
                    """
                    select mf_scheme_id, count(msn.id) from portfolio.user_scheme_details usd join mfschemes.mf_scheme_nav msn
                    on usd.amfi = msn.mf_scheme_id
                    group by mf_scheme_id having count(msn.id) < 3
                    """,
            nativeQuery = true)
    List<Long> getHistoricalDataNotLoadedSchemeIdList();
}
