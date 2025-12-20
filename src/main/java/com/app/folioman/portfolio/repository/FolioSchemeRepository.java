package com.app.folioman.portfolio.repository;

import com.app.folioman.portfolio.entities.FolioScheme;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolioSchemeRepository extends JpaRepository<FolioScheme, Long> {

    /**
     * Find a FolioScheme by the user scheme detail ID
     *
     * @param userSchemeDetailId the ID of the user scheme detail
     * @return the FolioScheme if found, null otherwise
     */
    FolioScheme findByUserSchemeDetails_Id(Long userSchemeDetailId);

    List<FolioScheme> findByUserFolioDetails_Id(Long id);
}
