package com.example.application.mfschemes.repository;

import com.example.application.mfschemes.entities.MFScheme;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MFSchemeRepository extends JpaRepository<MFScheme, Long> {

    @Query("select o.schemeId from MFScheme o")
    List<Long> findAllSchemeIds();
}
