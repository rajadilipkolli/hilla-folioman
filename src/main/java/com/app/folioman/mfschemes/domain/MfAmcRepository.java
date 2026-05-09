package com.app.folioman.mfschemes.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
interface MfAmcRepository extends JpaRepository<MfAmcEntity, Long> {

    Optional<MfAmcEntity> findByCode(String amcCode);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("update MfAmcEntity m set m.name = :name where m.code = :code")
    void updateMfAmcBy(@Param("name") String name, @Param("code") String code);

    Optional<MfAmcEntity> findByNameIgnoreCase(String amcName);

    /**
     * Find AMCs using PostgreSQL text search capabilities
     * This method uses the to_tsquery function to search in the name_vector field
     *
     * @param searchTerms Search terms to look for in the AMC name
     * @return List of matching AMCs
     */
    @NativeQuery("SELECT a.* FROM mfschemes.mf_amc a "
            + "WHERE a.name_vector @@ to_tsquery('english', :searchTerms) "
            + "ORDER BY ts_rank(a.name_vector, to_tsquery('english', :searchTerms)) DESC")
    List<MfAmcEntity> findByTextSearch(@Param("searchTerms") String searchTerms);
}
