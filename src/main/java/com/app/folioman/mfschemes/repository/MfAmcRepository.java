package com.app.folioman.mfschemes.repository;

import com.app.folioman.mfschemes.entities.MfAmc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface MfAmcRepository extends JpaRepository<MfAmc, Long> {

    MfAmc findByCode(String amcCode);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("update MfAmc m set m.name = :name where m.code = :code")
    void updateMfAmcBy(@Param("name") String name, @Param("code") String code);

    MfAmc findByNameIgnoreCase(String amcName);
}
